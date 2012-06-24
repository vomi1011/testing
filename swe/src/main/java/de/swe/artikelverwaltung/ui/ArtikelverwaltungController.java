package de.swe.artikelverwaltung.ui;

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
import org.jboss.solder.core.Client;
import org.richfaces.cdi.push.Push;

import de.swe.artikelverwaltung.domain.Fahrzeug;
import de.swe.artikelverwaltung.service.ArtikelValidationExeption;
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
	private static final String FLASH_FAHRZEUG = "fahrzeug";

	private String modell;

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
	@Push(topic = "marketingArtikel")
	private transient Event<String> neuerArtikelEvent;
	
	@Inject
	@Push(topic = "updateArtikel")
	private transient Event<String> updateArtikelEvent;

	private Fahrzeug fahrzeug;
	
	@SuppressWarnings("unused")
	@PostConstruct
	private void postConstruct() {
		LOGGER.debug("ArtikelverwaltungController wurde erzeugt");
		neuerArtikel = new Fahrzeug();
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
		
		LOGGER.tracef("artikel = %s", fahrzeug);
		try {
			fahrzeug = av.updateFahrzeug(fahrzeug, locale);
		}
		catch (ArtikelValidationExeption | ConcurrentUpdatedException
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
