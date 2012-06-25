package de.swe.artikelverwaltung.ui;

import static de.swe.util.Constants.BESTELLVERWALTUNG;
import static de.swe.util.Constants.JSF_DEFAULT_ERROR;
import static de.swe.util.Constants.JSF_INDEX;
import static de.swe.util.Constants.JSF_REDIRECT_SUFFIX;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Event;
import javax.faces.context.ExternalContext;
import javax.faces.context.Flash;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jboss.logging.Logger;
import org.jboss.seam.international.status.Messages;
import org.jboss.seam.international.status.builder.BundleKey;
import org.jboss.solder.core.Client;
import org.richfaces.cdi.push.Push;

import de.swe.artikelverwaltung.domain.Autohersteller;
import de.swe.artikelverwaltung.domain.Fahrzeug;
import de.swe.artikelverwaltung.service.ArtikelValidationException;
import de.swe.artikelverwaltung.service.Artikelverwaltung;
import de.swe.artikelverwaltung.service.ArtikelverwaltungDao.Order;
import de.swe.util.ConcurrentDeletedException;
import de.swe.util.ConcurrentUpdatedException;
import de.swe.util.Log;


/**
 * Dialogsteuerung fuer die Artikelverwaltung
 */
@Named("av")
@SessionScoped
@Log
public class ArtikelverwaltungController implements Serializable {
	private static final long serialVersionUID = 1564024850446471639L;

	private static final String JSF_ARTIKELVERWALTUNG = "/artikelverwaltung/";
	private static final String JSF_LIST_ARTIKEL = "/artikelverwaltung/listArtikel";
	private static final String JSF_SELECT_ARTIKEL = "/artikelverwaltung/selectArtikel";
	private static final String SESSION_VERFUEGBARE_ARTIKEL = "verfuegbareArtikel";
	private static final String JSF_UPDATE_ARTIKEL = JSF_ARTIKELVERWALTUNG + "updateArtikel";

	private static final String FLASH_ARTIKEL = "artikel";
	private static final String MSG_KEY_ARTIKEL_NOT_FOUND_BY_MODELL = "selectArtikel.notFound";
	private static final String CLIENT_ID_ARTIKEL_MODELL = "form:modell";
	private static final int MAX_AUTOCOMPLETE = 10;
	

	private Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private Artikelverwaltung av;
	
	@PersistenceContext
	@SuppressWarnings("unused")
	private transient EntityManager em;
	
	@Inject
	@Client // Sprache des Clients
	private Locale locale;
	
	@Inject
	private transient Flash flash;
	
	@Inject
	private transient ExternalContext externalCtx;

	private boolean geaendertArtikel; // fuer ValueChangeListener
	private Fahrzeug neuerArtikel;
	
	@Inject
	private Messages messages;

	private Long fahrzeufId;
	private String modell;
//	
//	private List<Fahrzeug> fahrzeuge = Collections.emptyList();
	
	@Inject
	@Push(topic = "marketingArtikel")
	private transient Event<String> neuerArtikelEvent;
	
	@Inject
	@Push(topic = "updateArtikel")
	private transient Event<String> updateArtikelEvent;

	private Fahrzeug fahrzeug;
	private List<Autohersteller> hersteller;
	private long herstellerId;
	
	/**
	 * F&uuml;r rich:autocomplete
	 * @return Liste der Produkt nach Modelle
	 */
	public List<String> findFahrzeugByModell(String modell) {
		final List<String> modelle = av.findFahrzeugByModell(modell);
		if (modell.isEmpty()) {
			messages.error(new BundleKey(BESTELLVERWALTUNG, MSG_KEY_ARTIKEL_NOT_FOUND_BY_MODELL), fahrzeufId)
                    .targets(CLIENT_ID_ARTIKEL_MODELL);
			return modelle;
		}

		if (modelle.size() > MAX_AUTOCOMPLETE) {
			return modelle.subList(0, MAX_AUTOCOMPLETE);
		}

		return modelle;
	}


	
	@SuppressWarnings("unused")
	@PostConstruct
	private void postConstruct() {
		LOGGER.debug("ArtikelverwaltungController wurde erzeugt");
		neuerArtikel = new Fahrzeug();
		hersteller = av.findAllAutohersteller(Order.NAME);
	}

	@SuppressWarnings("unused")
	@PreDestroy
	private void preDestroy() {
		LOGGER.debug("ArtikelverwaltungController wird geloescht");
	}
	
	public Fahrzeug getFahrzeug() {
		return fahrzeug;
	}

	public void setFahrzeug(Fahrzeug artikel) {
		this.fahrzeug = artikel;
	}

	public long getHerstellerId() {
		return herstellerId;
	}

	public void setHerstellerId(long herstellerId) {
		this.herstellerId = herstellerId;
	}

	public List<Autohersteller> getHersteller() {
		return hersteller;
	}

	public void setHersteller(List<Autohersteller> hersteller) {
		this.hersteller = hersteller;
	}

	public boolean isGeaendertArtikel() {
		return geaendertArtikel;
	}

	public void setGeaendertArtikel(boolean geaendertArtikel) {
		this.geaendertArtikel = geaendertArtikel;
	}

	public Fahrzeug getNeuerArtikel() {
		return neuerArtikel;
	}

	public void setNeuerArtikel(Fahrzeug neuerArtikel) {
		this.neuerArtikel = neuerArtikel;
	}

	public Event<String> getNeuerArtikelEvent() {
		return neuerArtikelEvent;
	}

	public void setNeuerArtikelEvent(Event<String> neuerArtikelEvent) {
		this.neuerArtikelEvent = neuerArtikelEvent;
	}

	public Event<String> getUpdateArtikelEvent() {
		return updateArtikelEvent;
	}

	public void setUpdateArtikelEvent(Event<String> updatArtikelEvent) {
		this.updateArtikelEvent = updatArtikelEvent;
	}

	public String getModell() {
		return modell;
	}

	public void setModell(String modell) {
		this.modell = modell;
	}

	/**
	 */
	public String findFahrzeugeByModell() {
		final List<Fahrzeug> artikel = av.findFahrzeugeByModell(modell);
		flash.put(FLASH_ARTIKEL, artikel);

		return JSF_LIST_ARTIKEL;
	}
	
	public String createArtikel() {
		Autohersteller hrst = av.findAutoherstellerById(herstellerId);
		neuerArtikel.setHersteller(hrst);
		
		try {
			neuerArtikel = (Fahrzeug) av.createFahrzeug(neuerArtikel, locale);
		}
		catch (ArtikelValidationException e) {
			//TODO genauere Fehlermeldung erzeugen 
			return JSF_DEFAULT_ERROR;
		}
		
		// Push-Event fuer Webbrowser
		neuerArtikelEvent.fire("" + neuerArtikel.getId());
		
		fahrzeug = neuerArtikel;
		neuerArtikel = new Fahrzeug();
		
		return JSF_LIST_ARTIKEL + JSF_REDIRECT_SUFFIX;
	}
	
	/**
	 * fuer index.xhtml
	 */
//	public void loadLadenhueter() {
//		final List<Artikel> ladenhueter = av.ladenhueter(ANZAHL_LADENHUETER);
//		final Map<String, Object> requestMap = externalCtx.getRequestMap();
//		requestMap.put(REQUEST_LADENHUETER, ladenhueter);
//	}
	
	public String selectArtikel() {
		final Map<String, Object> sessionMap = externalCtx.getSessionMap();  // oder Flash
		if (sessionMap.containsKey(SESSION_VERFUEGBARE_ARTIKEL)) {
			return JSF_SELECT_ARTIKEL;
		}
		
		final List<Fahrzeug> alleArtikel = av.findAllFahrzeuge(Order.KEINE);
		sessionMap.put(SESSION_VERFUEGBARE_ARTIKEL, alleArtikel);
		return JSF_SELECT_ARTIKEL;
	}
	
	public String selectForUpdate(Fahrzeug fahrzeug) {
		this.fahrzeug = fahrzeug;

		return JSF_UPDATE_ARTIKEL;
	}

	public String update() {
		if (!geaendertArtikel || fahrzeug == null) {
			return JSF_INDEX;
		}

		Autohersteller hrst = av.findAutoherstellerById(herstellerId);
		fahrzeug.setHersteller(hrst);
		
		LOGGER.tracef("artikel = %s", fahrzeug);
		try {
			fahrzeug = av.updateFahrzeug(fahrzeug, locale);
		}
		catch (ArtikelValidationException | ConcurrentUpdatedException
			  | ConcurrentDeletedException e) {
			//TODO genauere Fehlermeldung erzeugen 
			return JSF_DEFAULT_ERROR;
		}
		
		if (fahrzeug == null) {
			return JSF_DEFAULT_ERROR;
		}
		
		// Push-Event fuer Webbrowser
		updateArtikelEvent.fire("" + fahrzeug.getId());
		
		// ValueChangeListener zuruecksetzen
		geaendertArtikel = false;
		
		return JSF_LIST_ARTIKEL + JSF_REDIRECT_SUFFIX;
	}
	
	/**
	 * Verwendung als ValueChangeListener bei updateArtikel.xhtml
	 */
	public void geaendert(ValueChangeEvent e) {
		if (geaendertArtikel) {
			return;
		}
		
		if (e.getOldValue() == null) {
			if (e.getNewValue() != null) {
				geaendertArtikel = true;
			}
			return;
		}

		if (!e.getOldValue().equals(e.getNewValue())) {
			geaendertArtikel = true;				
		}
	}
}
