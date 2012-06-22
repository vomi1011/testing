package de.swe.bestellverwaltung.ui;

import static de.swe.util.Constants.JSF_DEFAULT_ERROR;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.enterprise.context.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.Flash;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jboss.logging.Logger;
import org.jboss.seam.security.annotations.LoggedIn;
import org.jboss.solder.core.Client;

//import de.swe.auth.ui.AuthController;
//import de.swe.auth.ui.LoggedIn;
//import de.swe.bestellverwaltung.ui.Warenkorb;
import de.swe.bestellverwaltung.domain.Bestellposition;
import de.swe.bestellverwaltung.domain.Bestellung;
import de.swe.bestellverwaltung.service.BestellungValidationException;
import de.swe.bestellverwaltung.service.Bestellverwaltung;
//import de.swe.kundenverwaltung.dao.KundenverwaltungDao.FetchType;
import de.swe.kundenverwaltung.dao.KundenverwaltungDao.Fetch;
import de.swe.kundenverwaltung.domain.AbstractKunde;
import de.swe.kundenverwaltung.service.Kundenverwaltung;
import de.swe.util.Log;

@Named("bv")
@RequestScoped
@Log
public class BestellverwaltungController implements Serializable {
	private static final long serialVersionUID = -1790295502719370565L;

//	@Inject
//	private Logger logger;
	private Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());
	
	private static final String JSF_VIEW_BESTELLUNG = "/bestellverwaltung/viewBestellung";
	
	@Inject
	private Warenkorb warenkorb;
	
	@Inject
	private Bestellverwaltung bv;
	
	@Inject
	private Kundenverwaltung kv;
	
//	@Inject
//	private AuthController auth;
	
//	@Inject
//	@LoggedIn
//	private AbstractKunde user;
	
	@PersistenceContext
	@SuppressWarnings("unused")
	private transient EntityManager entityManager;
	
	@Inject
	private Flash flash;

	@Inject
	@Client                  // Attributwert bezieht sich auf den Client
	private Locale locale;   // #{userLocale} in JSF-Seiten, vor allem Template-Seiten
	
	@Inject
	private transient ExternalContext ctx;   // fuer Redirecct
	
	// TODO https://issues.jboss.org/browse/SOLDER-311
//	@Inject
//	private HttpSession session;             // fuer Redirect


	public String bestellen() {
//		if (!auth.isLoggedIn()) {
//			// Java Reflection mit Klasse Method nicht moeglich, weil Method nicht serialisierbar ist
//			ctx.getSessionMap().put("methodOrigin", "bestellen");
//			return "/auth/login";
//		}
		
		if (warenkorb == null || warenkorb.getPositionen() == null || warenkorb.getPositionen().isEmpty()) {
			// Darf nicht passieren, wenn der Button zum Bestellen verfuegbar ist
			return JSF_DEFAULT_ERROR;
		}
		//TODO Attribut "kunde" verwenden, wenn Autentifizierung funktioniert
		// Den eingeloggten Kunden mit seinen Bestellungen ermitteln
		final AbstractKunde kunde = kv.findKundeById(Long.valueOf(1001), Fetch.MIT_BESTELLUNG);
		
		// Aus dem Warenkorb nur Positionen mit Anzahl > 0
		final List<Bestellposition> positionen = warenkorb.getPositionen();
		final List<Bestellposition> neuePositionen = new ArrayList<>(positionen.size());
		for (Bestellposition bp : positionen) {
			if (bp.getAnzahl() > 0) {
				neuePositionen.add(bp);
			}
		}
		
		// Warenkorb zuruecksetzen
		warenkorb.endConversation();
		
		// Neue Bestellung mit neuen Bestellpositionen erstellen
		Bestellung bestellung = new Bestellung();
		bestellung.setBestellpositionen(neuePositionen);
		LOGGER.tracef("Neue Bestellung: %s\nBestellpositionen: %s", bestellung,
				      bestellung.getBestellpositionen());
		
		// Bestellung mit VORHANDENEM Kunden verknuepfen:
		// dessen Bestellungen muessen geladen sein, weil es eine bidirektionale Beziehung ist
		try {
			bestellung = bv.createBestellung(bestellung, kunde, locale);
		}
		catch (BestellungValidationException e) {
			// Validierungsfehler KOENNEN NICHT AUFTRETEN, da Attribute durch JSF validiert wurden
			// und in der Klasse Bestellung keine Validierungs-Methoden vorhanden sind
			throw new IllegalStateException(e);
		}
		
		// Bestellung im Flash speichern wegen anschliessendem Redirect
		flash.put("bestellung", bestellung);
		
		return JSF_VIEW_BESTELLUNG;
	}
}

