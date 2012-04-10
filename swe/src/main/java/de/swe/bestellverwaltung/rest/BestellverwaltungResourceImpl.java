package de.swe.bestellverwaltung.rest;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.jboss.logging.Logger;

import de.swe.artikelverwaltung.domain.Fahrzeug;
import de.swe.artikelverwaltung.rest.ArtikelverwaltungResource;
import de.swe.artikelverwaltung.service.Artikelverwaltung;
import de.swe.bestellverwaltung.domain.Bestellposition;
import de.swe.bestellverwaltung.domain.Bestellung;
import de.swe.bestellverwaltung.service.BestellungValidationException;
import de.swe.bestellverwaltung.service.Bestellverwaltung;
import de.swe.kundenverwaltung.domain.AbstractKunde;
import de.swe.kundenverwaltung.rest.KundenverwaltungResource;
import de.swe.kundenverwaltung.service.Kundenverwaltung;
import de.swe.kundenverwaltung.service.KundenverwaltungDao.Fetch;
import de.swe.util.NotFoundException;


@Stateless
public class BestellverwaltungResourceImpl implements BestellverwaltungResource {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());
	
	@SuppressWarnings("unused")
	@PersistenceContext
	private EntityManager em;
	
	@Inject
	private Bestellverwaltung bv;
	
	@Inject
	private Kundenverwaltung kv;
	
	@Inject
	private Artikelverwaltung av;
	
	@EJB
	private KundenverwaltungResource kvResource;
	
	@Inject
	private ArtikelverwaltungResource avResource;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Bestellung findBestellung(Long id, UriInfo uriInfo)
	                  throws NotFoundException {
		final Bestellung bestellung = bv.findBestellungById(id);
		if (bestellung == null) {
			final String msg = "Keine Bestellung gefunden mit der ID " + id;
			throw new NotFoundException(msg);
		}
		
		updateUriBestellung(bestellung, uriInfo);
		

		return bestellung;
	}
	
	@Override
	public BestellungList findBestellungen(UriInfo uriInfo)
	                  throws NotFoundException {
		List<Bestellung> bestellungen = bv.findAllBestellungen();
		if (bestellungen.isEmpty()) {
			final String msg = "Keine Bestellungen gefunden.";
			throw new NotFoundException(msg);
		}

		final BestellungList bestelliste = new BestellungList(bestellungen);
		
		for (Bestellung bs : bestellungen) {
			updateUriBestellung(bs, uriInfo);
		}

		return bestelliste;
	}
	
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public AbstractKunde findKundeByBid(Long id, UriInfo uriInfo)
	                     throws NotFoundException {
		AbstractKunde kunde = bv.findKundeByBestellid(id);
		if (kunde == null) {
			final String msg = "Keine Kunden zur Bestellnr. " + id + " gefunden.";
			throw new NotFoundException(msg);
		}

		//URLs innerhalb der gefundenen Bestellung anpassen
		kvResource.getUriKunde(kunde, uriInfo);

		return kunde;
	}
	


	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Response createBestellung(Bestellung bestellung, UriInfo uriInfo, HttpHeaders headers)
	                throws NotFoundException, BestellungValidationException {
		// Schluessel des Kunden extrahieren - weil beim XML kein Kunde vorliegt
		final String kundeUriStr = bestellung.getKundeUri().toString();
		int startPos = kundeUriStr.lastIndexOf('/') + 1;
		final String kundeIdStr = kundeUriStr.substring(startPos);
		Long kundeId = null;
		try {
			kundeId = Long.valueOf(kundeIdStr);
		}
		catch (NumberFormatException e) {
			throw new WebApplicationException(e, NOT_FOUND);
		}
		
		// Kunde mit den vorhandenen ("alten") Bestellungen ermitteln
		final AbstractKunde kunde = kv.findKundeById(kundeId, Fetch.MIT_BESTELLUNG);
		// Implizites Nachladen innerhalb der Transaktion wuerde auch funktionieren
		// final AbstractKunde kunde = kv.findKundeById(kundeId);
		if (kunde == null) {
			final String msg = "Kein Kunde gefunden mit der ID " + kundeId;
			throw new NotFoundException(msg);
		}
		
		// persistente Artikel ermitteln
		List<Bestellposition> bestellpositionen = bestellung.getBestellpositionen();
		List<Long> fahrzeugIds = new ArrayList<Long>(bestellpositionen.size());
		for (Bestellposition bp : bestellpositionen) {
			final String fahrzeugUriStr = bp.getFahrzeugUri().toString();
			startPos = fahrzeugUriStr.lastIndexOf('/') + 1;
			final String fahrzeugIdStr = fahrzeugUriStr.substring(startPos);
			Long fahrzeugId = null;
			try {
				fahrzeugId = Long.valueOf(fahrzeugIdStr);
			}
			catch (NumberFormatException e) {
				throw new WebApplicationException(e, NOT_FOUND);
			}
			
			fahrzeugIds.add(fahrzeugId);
		}
		
		List<Fahrzeug> fahrzeuge = new ArrayList<Fahrzeug>();
		for (Long id : fahrzeugIds) {
			fahrzeuge.add(av.findFahrzeugById(id));
		}
		if (fahrzeuge.isEmpty()) {
			final String msg = "Keine Artikel gefunden mit den IDs " + fahrzeugIds;
			throw new NotFoundException(msg);
		}
		
		// Bestellpositionen haben URLs fuer persistente Artikel.
		// Diese persistenten Artikel wurden in einem DB-Zugriff ermittelt (s.o.)
		// Fuer jede Bestellposition wird der Artikel passend zur Artikel-URL bzw. Artikel-ID gesetzt.
		// Bestellpositionen mit nicht-gefundene Artikel werden eliminiert.
		int i = 0;
		final List<Bestellposition> neueBestellpositionen = 
				new ArrayList<Bestellposition>(bestellpositionen.size());
		for (Bestellposition bp : bestellpositionen) {
			// Artikel-ID der aktuellen Bestellposition (s.o.):
			// artikelIds haben gleiche Reihenfolge wie bestellpositionen
			final long fId = fahrzeugIds.get(i++);
			
			// Wurde der Artikel beim DB-Zugriff gefunden?
			for (Fahrzeug fahrzeug : fahrzeuge) {
				if (fahrzeug.getFId().longValue() == fId) {
					// Der Artikel wurde gefunden
					bp.setFahrzeug(fahrzeug);
					neueBestellpositionen.add(bp);
					break;					
				}
			}
		}
		bestellung.setBestellpositionen(neueBestellpositionen);
		
		// Die neue Bestellung mit den aktualisierten persistenten Artikeln abspeichern.
		// Die Bestellung darf dem Kunden noch nicht hinzugefuegt werden, weil dieser
		// sonst in einer Transaktion modifiziert werden wuerde.
		// Beim naechsten DB-Zugriff (auch lesend!) wuerde der EntityManager sonst
		// erstmal versuchen den Kunden-Datensatz in der DB zu modifizieren.
		// Dann wuerde aber der Kunde mit einer *transienten* Bestellung modifiziert werden,
		// was zwangslaeufig zu einer Inkonsistenz fuehrt!
		// Das ist die Konsequenz einer Transaktion (im Gegensatz zu den Action-Methoden von JSF!).
		final List<Locale> locales = headers.getAcceptableLanguages();
		final Locale locale = locales.isEmpty() ? Locale.getDefault() : locales.get(0);
		bestellung = bv.createBestellung(bestellung, kunde, locale);

		final URI bestellungUri = getUriBestellung(bestellung, uriInfo);
		final Response response = Response.created(bestellungUri).build();
		LOGGER.trace(bestellungUri);
		
		return response;
	}
		
	@Override
	public void updateUriBestellung(Bestellung bestellung, UriInfo uriInfo) throws NotFoundException {
	
		// URL fuer Kunde setzen
		if (bestellung == null) {
			final String msg = "Kein Bestellung vorhanden";
			throw new NotFoundException(msg);
		}
			
		final URI kundeUri = kvResource.getUriKunde(bestellung.getKunde(), uriInfo);
		bestellung.setKundeUri(kundeUri);
		
		final List<Bestellposition> bestellpositionen = bestellung.getBestellpositionen();
		
		if (bestellpositionen != null && !bestellpositionen.isEmpty()) {
			for (Bestellposition bp : bestellpositionen) {			
				if (bp != null) {
					final URI fahrzeugUri = avResource.getUriFahrzeug(bp.getFahrzeug(), uriInfo);
					bp.setFahrzeugUri(fahrzeugUri);
				}
			}
		}	
	}
	
	
	@Override
	public URI getUriBestellung(Bestellung bestellung, UriInfo uriInfo) throws NotFoundException {
		if (bestellung == null) {
			final String msg = "Kein Bestellung vorhanden";
			throw new NotFoundException(msg);
		}
		final UriBuilder ub = uriInfo.getBaseUriBuilder()
		                             .path(BestellverwaltungResource.class)
		                             .path(BestellverwaltungResource.class, "findBestellung");
		final URI uri = ub.build(bestellung.getBId());
		
		return uri;
	}

}
