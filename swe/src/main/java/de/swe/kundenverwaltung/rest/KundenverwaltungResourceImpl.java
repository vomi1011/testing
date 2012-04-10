package de.swe.kundenverwaltung.rest;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.jboss.logging.Logger;

import de.swe.bestellverwaltung.domain.Bestellung;
import de.swe.bestellverwaltung.rest.BestellungList;
import de.swe.bestellverwaltung.rest.BestellverwaltungResource;
import de.swe.bestellverwaltung.service.Bestellverwaltung;
import de.swe.kundenverwaltung.domain.AbstractKunde;
import de.swe.kundenverwaltung.domain.Adresse;
import de.swe.kundenverwaltung.domain.Privatkunde;
import de.swe.kundenverwaltung.service.EmailExistsException;
import de.swe.kundenverwaltung.service.KundeDeleteBestellungException;
import de.swe.kundenverwaltung.service.KundeValidationException;
import de.swe.kundenverwaltung.service.Kundenverwaltung;
import de.swe.kundenverwaltung.service.KundenverwaltungDao.Fetch;
import de.swe.kundenverwaltung.service.KundenverwaltungDao.Order;
import de.swe.util.NotFoundException;

@Stateless
public class KundenverwaltungResourceImpl implements KundenverwaltungResource {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().getClass());
	
	@SuppressWarnings("unused")
	@PersistenceContext
	private EntityManager em;
	
	@Inject
	private Kundenverwaltung kv;
	
	@Inject
	private Bestellverwaltung bv;

	@EJB
	private BestellverwaltungResource bvResource;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public AbstractKunde findKunde(Long id, UriInfo uriInfo)
			throws NotFoundException {
		AbstractKunde kunde = kv.findKundeById(id, Fetch.NUR_KUNDE);
		
		if (kunde == null) {
			String msg = "Kein Kunde gefunden mit ID " + id;
			throw new NotFoundException(msg);
		}
		
		updateUriKunde(kunde, uriInfo);
		
		return kunde;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public KundeList findKunden(String nachname, UriInfo uriInfo)
			throws NotFoundException {
		List<AbstractKunde> kunden = null;
		
		if ("".equals(nachname)) {
			kunden = kv.findAllKunden(Fetch.NUR_KUNDE, Order.ID);

			if (kunden.isEmpty()) {
				String msg = "Keine Kunden gefunden";
				throw new NotFoundException(msg);
			}
		}
		else {
			kunden = kv.findKundenByNachname(nachname, Fetch.NUR_KUNDE);
			
			if (kunden.isEmpty()) {
				String msg = "Keine Kunden gefunden mit Nachnamen " + nachname;
				throw new NotFoundException(msg);
			}
		}
		
		for (AbstractKunde kunde : kunden) {
			updateUriKunde(kunde, uriInfo);
		}
		
		final KundeList kundeList = new KundeList(kunden);
		
		return kundeList;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BestellungList findBestellungenByKundeId(Long id, UriInfo uriInfo)
			throws NotFoundException {
		List<Bestellung> bestellungen = bv.findBestellungenByKundenId(id);
		
		if (bestellungen.isEmpty()) {
			String msg = "Keine Bestellungen gefunden fuer Kunde mit ID " + id;
			throw new NotFoundException(msg);
		}
		
		for (Bestellung b : bestellungen) {
			bvResource.updateUriBestellung(b, uriInfo);
		}
		
		BestellungList bestellungList = new BestellungList(bestellungen);
		
		return bestellungList;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Response createKunde(AbstractKunde kunde, UriInfo uriInfo,
			HttpHeaders headers) throws EmailExistsException,
			KundeValidationException {
		final Adresse adresse = kunde.getAdresse();
		
		if (adresse != null) {
			adresse.setKunde(kunde);
		}
		
		final List<Locale> locales = headers.getAcceptableLanguages();
		final Locale locale = locales.isEmpty() ? Locale.getDefault() : locales.get(0);
		kunde = kv.createKunde(kunde, locale);
		LOGGER.tracef("%s", kunde);
		
		final URI kundeUri = getUriKunde(kunde, uriInfo);
		final Response response = Response.created(kundeUri).build();
		
		return response;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Response createPrivatkunde(KundeForm kundeForm, UriInfo uriInfo,
									  HttpHeaders headers)
			throws EmailExistsException, ParseException, KundeValidationException {
		AbstractKunde kunde = new Privatkunde();
		kunde.setNachname(kundeForm.getNachname());
		kunde.setVorname(kundeForm.getVorname());
		kunde.setEmail(kundeForm.getEmail());
		
		final Adresse adresse = new Adresse();
		adresse.setStrasse(kundeForm.getStrasse());
		adresse.setHausnr(kundeForm.getHausnr());
		adresse.setPlz(kundeForm.getPlz());
		adresse.setOrt(kundeForm.getOrt());
		adresse.setKunde(kunde);
		kunde.setAdresse(adresse);
		
		final List<Locale> locales = headers.getAcceptableLanguages();
		final Locale locale = locales.isEmpty() ? Locale.getDefault() : locales.get(0);
		kunde = kv.createKunde(kunde, locale);
		LOGGER.tracef("%s",	kunde);
		LOGGER.tracef("%s", adresse);
		
		final URI kundeUri = getUriKunde(kunde, uriInfo);
		final Response respose = Response.created(kundeUri).build();
		
		return respose;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Response updateKunde(AbstractKunde kunde, UriInfo uriInfo,
								HttpHeaders headers)
			throws NotFoundException, EmailExistsException, KundeValidationException {
		AbstractKunde kundeVorher = kv.findKundeById(kunde.getId(), Fetch.NUR_KUNDE);
		
		if (kundeVorher == null) {
			String msg = "Kein Kunde gefunden mit ID " + kunde.getId();
			throw new NotFoundException(msg);
		}
		
		LOGGER.tracef("%s", kundeVorher);
		
		kundeVorher.setValues(kunde);
		LOGGER.tracef("%s", kundeVorher);
		
		final List<Locale> locales = headers.getAcceptableLanguages();
		final Locale locale = locales.isEmpty() ? Locale.getDefault() : locales.get(0);
		kunde = kv.updateKunde(kundeVorher, locale);
		
		if (kunde == null) {
			String msg = "Kein Kunde gefunden mit ID " + kundeVorher.getId();
			throw new NotFoundException(msg);
		}
		
		return Response.noContent().build();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Response deleteKunde(Long id, UriInfo uriInfo)
			throws KundeDeleteBestellungException {
		kv.deleteKundeById(id);
		
		return Response.noContent().build();
	}

	/**
	 */
	@Override
	public void updateUriKunde(AbstractKunde kunde, UriInfo uriInfo) {
		final UriBuilder ub = uriInfo.getBaseUriBuilder()
									 .path(KundenverwaltungResource.class)
									 .path(KundenverwaltungResource.class, "findBestellungenByKundeId");
		final URI bestellungenUri = ub.build(kunde.getId());
		kunde.setBestellungenUri(bestellungenUri);
	}

	/**
	 */
	@Override
	public URI getUriKunde(AbstractKunde kunde, UriInfo uriInfo) {
		final UriBuilder ub = uriInfo.getBaseUriBuilder()
		                             .path(KundenverwaltungResource.class)
		                             .path(KundenverwaltungResource.class, "findKunde");
		final URI kundeUri = ub.build(kunde.getId());
		return kundeUri;
	}
}
