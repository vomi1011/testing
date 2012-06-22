package de.swe.artikelverwaltung.ui;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.Flash;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import de.swe.artikelverwaltung.service.ArtikelverwaltungDao.Order;

import org.jboss.logging.Logger;

import de.swe.artikelverwaltung.domain.Fahrzeug;
import de.swe.artikelverwaltung.service.Artikelverwaltung;
import de.swe.util.Log;


/**
 * Dialogsteuerung fuer die Artikelverwaltung
 */
@Named("av")
@RequestScoped
@Log
public class ArtikelverwaltungController implements Serializable {
	private static final long serialVersionUID = 1564024850446471639L;
	
	private static final String JSF_LIST_ARTIKEL = "/artikelverwaltung/listArtikel";
	private static final String FLASH_ARTIKEL = "artikel";
	private static final String JSF_SELECT_ARTIKEL = "/artikelverwaltung/selectArtikel";
	private static final String SESSION_VERFUEGBARE_ARTIKEL = "verfuegbareArtikel";

	private String beschreibung;

	private Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private Artikelverwaltung av;
	
	@PersistenceContext
	@SuppressWarnings("unused")
	private transient EntityManager entityManager;
	
	@Inject
	private transient Flash flash;
	
	@Inject
	private transient ExternalContext externalCtx;
	
	@SuppressWarnings("unused")
	@PostConstruct
	private void postConstruct() {
		LOGGER.debug("ArtikelverwaltungController wurde erzeugt");
	}

	@SuppressWarnings("unused")
	@PreDestroy
	private void preDestroy() {
		LOGGER.debug("ArtikelverwaltungController wird geloescht");
	}
	
	@Override
	public String toString() {
		return "ArtikelverwaltungController [bezeichnung=" + beschreibung + "]";
	}

	public String getBeschreibung() {
		return beschreibung;
	}

	public void setBeschreibung(String beschreibung) {
		this.beschreibung = beschreibung;
	}

	/**
	 */
	public String findFahrzeugeByBeschreibung() {
		final List<Fahrzeug> artikel = av.findFahrzeugeByBeschreibung(beschreibung);
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
}
