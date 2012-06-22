package de.swe.kundenverwaltung.ui;

import static de.swe.util.Constants.JSF_DEFAULT_ERROR;
import static de.swe.util.Constants.JSF_INDEX;
import static de.swe.util.Constants.JSF_REDIRECT_SUFFIX;
import static de.swe.util.Constants.KUNDENVERWALTUNG;
import static de.swe.util.Constants.UID;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJBTransactionRolledbackException;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Event;
import javax.faces.context.ExternalContext;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.ConstraintViolation;

import org.jboss.logging.Logger;
import org.jboss.seam.international.status.Messages;
import org.jboss.seam.international.status.builder.BundleKey;
import org.jboss.solder.core.Client;
import org.richfaces.cdi.push.Push;
import org.richfaces.component.SortOrder;

import de.swe.kundenverwaltung.dao.KundenverwaltungDao.Fetch;
import de.swe.kundenverwaltung.dao.KundenverwaltungDao.Order;
import de.swe.kundenverwaltung.domain.AbstractKunde;
import de.swe.kundenverwaltung.domain.Adresse;
import de.swe.kundenverwaltung.domain.PasswordGroup;
import de.swe.kundenverwaltung.domain.Privatkunde;
import de.swe.kundenverwaltung.service.EmailExistsException;
import de.swe.kundenverwaltung.service.KundeDeleteBestellungException;
import de.swe.kundenverwaltung.service.KundeValidationException;
import de.swe.kundenverwaltung.service.Kundenverwaltung;
import de.swe.util.AbstractSweException;
import de.swe.util.ConcurrentDeletedException;
import de.swe.util.ConcurrentUpdatedException;
import de.swe.util.Log;

@Named("kv")
@SessionScoped
@Log
public class KundenverwaltungController implements Serializable {
	private static final long serialVersionUID = UID;

	private static final int MAX_AUTOCOMPLETE = 10;

	private static final String JSF_KUNDENVERWALTUNG = "/kundenverwaltung/";
	private static final String JSF_VIEW_KUNDE = JSF_KUNDENVERWALTUNG + "viewKunde";
	private static final String JSF_LIST_KUNDEN = JSF_KUNDENVERWALTUNG + "listKunden";
	private static final String JSF_UPDATE_PRIVATKUNDE = JSF_KUNDENVERWALTUNG + "updatePrivatkunde";
	private static final String JSF_UPDATE_FIRMENKUNDE = JSF_KUNDENVERWALTUNG + "updateFirmenkunde";
	private static final String JSF_DELETE_OK = JSF_KUNDENVERWALTUNG + "okDelete";

	private static final String REQUEST_KUNDE_ID = "kundeId";

	private static final String CLIENT_ID_KUNDEID = "form:kundeId";
	private static final String CLIENT_ID_UPDATE_PASSWORD = "updateKundeForm:password";
	private static final String CLIENT_ID_UPDATE_EMAIL = "updateKundeForm:email";

	private static final String CLIENT_ID_CREATE_EMAIL = "createKundeForm:email";
	private static final String MSG_KEY_CREATE_PRIVATKUNDE_EMAIL_EXISTS = "createPrivatkunde.emailExists";

	private static final Class<?>[] PASSWORD_GROUP = { PasswordGroup.class };
	
	private static final String CLIENT_ID_KUNDEN_NACHNAME = "form:nachname";
	private static final String MSG_KEY_KUNDEN_NOT_FOUND_BY_NACHNAME = "listKunden.notFound";
	
	private static final String MSG_KEY_UPDATE_PRIVATKUNDE_DUPLIKAT = "updatePrivatkunde.duplikat";
	private static final String MSG_KEY_UPDATE_FIRMENKUNDE_DUPLIKAT = "updateFirmenkunde.duplikat";
	private static final String MSG_KEY_UPDATE_PRIVATKUNDE_CONCURRENT_UPDATE = "updatePrivatkunde.concurrentUpdate";
	private static final String MSG_KEY_UPDATE_FIRMENKUNDE_CONCURRENT_UPDATE = "updateFirmenkunde.concurrentUpdate";
	private static final String MSG_KEY_UPDATE_PRIVATKUNDE_CONCURRENT_DELETE = "updatePrivatkunde.concurrentDelete";
	private static final String MSG_KEY_UPDATE_FIRMENKUNDE_CONCURRENT_DELETE = "updateFirmenkunde.concurrentDelete";
	private static final String MSG_KEY_KUNDE_NOT_FOUND_BY_ID = "viewKunde.notFound";

	private static final String CLIENT_ID_DELETE_BUTTON = "form:deleteButton";
	private static final String MSG_KEY_DELETE_KUNDE_BESTELLUNG = "viewKunde.deleteKundeBestellung";

	private static final String MSG_KEY_SELECT_DELETE_KUNDE_BESTELLUNG = "listKunden.deleteKundeBestellung";
	
	private Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());
	
	@Inject
	private Kundenverwaltung kv;
	
	@PersistenceContext
	@SuppressWarnings("unused")
	private transient EntityManager em;
	
	@Inject
	private transient ExternalContext externalCtx;
	
	@Inject
	@Client // Sprache des Clients
	private Locale locale;
	
	@Inject
	private Messages messages;
	
	private Long kundeId;
	private AbstractKunde kunde;
	
	private String nachname;
	
	private List<AbstractKunde> kunden = Collections.emptyList();
	
	private SortOrder vornameSortOrder = SortOrder.unsorted;
	private String vornameFilter = "";
	
	private boolean geaendertKunde; // fuer ValueChangeListener
	private Privatkunde neuerPrivatkunde;

	@Inject
	@Push(topic = "marketing")
	private transient Event<String> neuerKundeEvent;
	
	@Inject
	@Push(topic = "updateKunde")
	private transient Event<String> updateKundeEvent;

	@SuppressWarnings("unused")
	@PostConstruct
	private void postConstruct() {
		createEmptyPrivatkunde();

		LOGGER.debug("KundenverwaltungController wurde erzeugt");
	}

	@SuppressWarnings("unused")
	@PreDestroy
	private void preDestroy() {
		LOGGER.debug("KundenverwaltungController wird geloescht");
	}
	
	public Long getKundeId() {
		return kundeId;
	}
	
	public void setKundeId(Long kundeId) {
		this.kundeId = kundeId;
	}
	
	public AbstractKunde getKunde() {
		return kunde;
	}
	
	public void setKunde(AbstractKunde kunde) {
		this.kunde = kunde;
	}
	
	public String getNachname() {
		return nachname;
	}
	
	public void setNachname(String nachname) {
		this.nachname = nachname;
	}
	
	public List<AbstractKunde> getKunden() {
		return kunden;
	}
	
	public void setKunden(List<AbstractKunde> kunden) {
		this.kunden = kunden;
	}
	
	public SortOrder getVornameSortOrder() {
		return vornameSortOrder;
	}
	
	public void setVornameSortOrder(SortOrder vornameSortOrder) {
		this.vornameSortOrder = vornameSortOrder;
	}
	
	public String getVornameFilter() {
		return vornameFilter;
	}
	
	public void setVornameFilter(String vornameFilter) {
		this.vornameFilter = vornameFilter;
	}
	
	public Privatkunde getNeuerPrivatkunde() {
		return neuerPrivatkunde;
	}
	
	public void setNeuerPrivatkunde(Privatkunde neuerPrivatkunde) {
		this.neuerPrivatkunde = neuerPrivatkunde;
	}
	
	public Date getAktuellesDatum() {
		Date datum = new Date();
		return datum;
	}
	
	public Class<?>[] getPasswordGroup() {
		return PASSWORD_GROUP.clone();
	}
	
	public void sortByVorname() {
		vornameSortOrder = vornameSortOrder.equals(SortOrder.ascending)
						   ? SortOrder.descending
						   : SortOrder.ascending;
	}
	
	public List<AbstractKunde> findKundenByIdPrefix(String idPrefix) {
		List<AbstractKunde> kundenPrefix = null;
		Long id = null;
		
		try {
			id = Long.valueOf(idPrefix);
		}
		catch (NumberFormatException e) {
			findKundeByIdErrorMsg(idPrefix);
			return null;
		}
		
		kundenPrefix = kv.findKundenbyIdPrefix(id);
		
		if (kundenPrefix == null || kundenPrefix.isEmpty()) {
			findKundeByIdErrorMsg(idPrefix);
			return null;
		}
		
		if (kundenPrefix.size() > MAX_AUTOCOMPLETE) {
			return kundenPrefix.subList(0, MAX_AUTOCOMPLETE);
		}
		
		return kundenPrefix;
	}
	
	public String findKundeById() {
		kunde = kv.findKundeById(kundeId, Fetch.MIT_BESTELLUNG);
		
		if (kunde == null) {
			return findKundeByIdErrorMsg(kundeId.toString());
		}
		
		return JSF_VIEW_KUNDE;
	}
	
	/**
	 * Action Methode, um einen Kunden zu gegebener ID zu suchen
	 * @return URL fuer Anzeige des gefundenen Kunden; sonst null
	 */
	public String findKundenByNachname() {
		if (nachname == null || nachname.isEmpty()) {
			kunden = kv.findAllKunden(Fetch.MIT_BESTELLUNG, Order.KEINE);
			return JSF_LIST_KUNDEN;
		}

		kunden = kv.findKundenByNachname(nachname, Fetch.MIT_BESTELLUNG);
		return JSF_LIST_KUNDEN;
	}
	
	/**
	 * F&uuml;r rich:autocomplete
	 * @return Liste der potenziellen Nachnamen
	 */
	public List<String> findNachnamenByPrefix(String nachnamePrefix) {
		final List<String> nachnamen = kv.findNachnamenByPrefix(nachnamePrefix);
		if (nachnamen.isEmpty()) {
			messages.error(new BundleKey(KUNDENVERWALTUNG, MSG_KEY_KUNDEN_NOT_FOUND_BY_NACHNAME), kundeId)
                    .targets(CLIENT_ID_KUNDEN_NACHNAME);
			return nachnamen;
		}

		if (nachnamen.size() > MAX_AUTOCOMPLETE) {
			return nachnamen.subList(0, MAX_AUTOCOMPLETE);
		}

		return nachnamen;
	}
	
	public void loadKundeById() {
		// Request-Parameter "kundeId" fuer ID des gesuchten Kunden
		final String idStr = externalCtx.getRequestParameterMap().get("kundeId");
		Long id;
		try {
			id = Long.valueOf(idStr);
		}
		catch (NumberFormatException e) {
			return;
		}
		
		// Suche durch den Anwendungskern
		kunde = kv.findKundeById(id, Fetch.NUR_KUNDE);
		if (kunde == null) {
			return;
		}
	}
	
	public String details(AbstractKunde ausgewaehlterKunde) {
		if (ausgewaehlterKunde == null) {
			return null;
		}
		
		// Bestellungen nachladen
		this.kunde = kv.findKundeById(ausgewaehlterKunde.getId(), Fetch.MIT_BESTELLUNG);
		this.kundeId = this.kunde.getId();
		
		return JSF_VIEW_KUNDE;
	}
	
	/**
	 * Action Methode, um einen zuvor gesuchten Kunden zu l&ouml;schen
	 * @return URL fuer Startseite im Erfolgsfall, sonst wieder die gleiche Seite
	 */
	public String deleteAngezeigtenKunden() {
		if (kunde == null) {
			return null;
		}
		
		LOGGER.tracef("kunde = %s", kunde);
		try {
			kv.deleteKunde(kunde);
//			kv.deleteKundeById(getKundeId());
		}
		catch (KundeDeleteBestellungException e) {
//			catch(EJBTransactionRolledbackException e) {
			messages.error(new BundleKey(KUNDENVERWALTUNG, MSG_KEY_DELETE_KUNDE_BESTELLUNG),
					       e.getKundeId(),
                           e.getAnzahlBestellungen())
                    .targets(CLIENT_ID_DELETE_BUTTON);
			return null;
		}
		
		// Aufbereitung fuer ok.xhtml
		final Map<String, Object> requestParams = externalCtx.getRequestMap();
		requestParams.put(REQUEST_KUNDE_ID, kunde.getId());
		
		// Zuruecksetzen
		kunde = null;
		kundeId = null;

		return JSF_DELETE_OK;
	}
	
	public String selectForUpdate(AbstractKunde ausgewaehlterKunde) {
		this.kunde = ausgewaehlterKunde;
		
		if (AbstractKunde.PRIVATKUNDE.equals(ausgewaehlterKunde.getArt())) {
			return JSF_UPDATE_PRIVATKUNDE;
		}
		else {
			return JSF_UPDATE_FIRMENKUNDE;
		}
	}
	
	public String delete(AbstractKunde ausgewaehlterKunde) {
		try {
			kv.deleteKunde(ausgewaehlterKunde);
		}
		catch (KundeDeleteBestellungException e) {
			messages.error(new BundleKey(KUNDENVERWALTUNG, MSG_KEY_SELECT_DELETE_KUNDE_BESTELLUNG),
				           e.getKundeId(),
                           e.getAnzahlBestellungen())
                    .targets(null);
			return null;
		}

		kunden.remove(ausgewaehlterKunde);
		return null;
	}
	
	public String update() {
		if (!geaendertKunde || kunde == null) {
			return JSF_INDEX;
		}
		
		LOGGER.tracef("kunde = %s", kunde);
		try {
			kunde = kv.updateKunde(kunde, locale);
		}
		catch (EmailExistsException | KundeValidationException
			  | ConcurrentUpdatedException | ConcurrentDeletedException e) {
			final String outcome = updateErrorMsg(e, kunde.getClass());
			return outcome;
		}
		
		if (kunde == null) {
			return JSF_DEFAULT_ERROR;
		}
		
		// Push-Event fuer Webbrowser
		updateKundeEvent.fire("" + kunde.getId());
		
		// ValueChangeListener zuruecksetzen
		geaendertKunde = false;
		
		// Aufbereitung fuer viewKunde.xhtml
		kundeId = kunde.getId();
		
		return JSF_VIEW_KUNDE + JSF_REDIRECT_SUFFIX;
	}
	
	public String createPrivatkunde() {
		try {
			neuerPrivatkunde = (Privatkunde) kv.createKunde(neuerPrivatkunde, locale);
		}
		catch (KundeValidationException | EmailExistsException e) {
			final String outcome = createPrivatkundeErrorMsg(e);
			return outcome;
		}
		
		// Push-Event fuer Webbrowser
		neuerKundeEvent.fire("" + neuerPrivatkunde.getId());
		
		// Aufbereitung fuer viewKunde.xhtml
		kundeId = neuerPrivatkunde.getId();
		kunde = neuerPrivatkunde;
		createEmptyPrivatkunde();
		
		return JSF_VIEW_KUNDE + JSF_REDIRECT_SUFFIX;
	}
	
	private String createPrivatkundeErrorMsg(AbstractSweException e) {
		if (e.getClass().equals(EmailExistsException.class)) {
			messages.error(new BundleKey(KUNDENVERWALTUNG, MSG_KEY_CREATE_PRIVATKUNDE_EMAIL_EXISTS))
			        .targets(CLIENT_ID_CREATE_EMAIL);
		}
		
		return null;
	}
	
	private void createEmptyPrivatkunde() {
		neuerPrivatkunde = new Privatkunde();
		final Adresse adresse = new Adresse();
		adresse.setKunde(neuerPrivatkunde);
		neuerPrivatkunde.setAdresse(adresse);
	}
	
	/**
	 * Verwendung als ValueChangeListener bei updatePrivatkunde.xhtml und updateFirmenkunde.xhtml
	 */
	public void geaendert(ValueChangeEvent e) {
		if (geaendertKunde) {
			return;
		}
		
		if (e.getOldValue() == null) {
			if (e.getNewValue() != null) {
				geaendertKunde = true;
			}
			return;
		}

		if (!e.getOldValue().equals(e.getNewValue())) {
			geaendertKunde = true;				
		}
	}
	
	private String findKundeByIdErrorMsg(String id) {
		messages.error(new BundleKey(KUNDENVERWALTUNG, MSG_KEY_KUNDE_NOT_FOUND_BY_ID), id)
                .targets(CLIENT_ID_KUNDEID);
		return null;
	}
	
	private String updateErrorMsg(AbstractSweException e, Class<? extends AbstractKunde> kundeClass) {
		if (e.getClass().equals(KundeValidationException.class)) {
			// Ungueltiges Password: Attribute wurden bereits von JSF validiert
			final KundeValidationException orig = (KundeValidationException) e;
			final Collection<ConstraintViolation<AbstractKunde>> violations = orig.getViolations();
			for (ConstraintViolation<AbstractKunde> v : violations) {
				messages.error(v.getMessage()).targets(CLIENT_ID_UPDATE_PASSWORD);
			}
		}
		else if (e.getClass().equals(EmailExistsException.class)) {
			if (kundeClass.equals(Privatkunde.class)) {
				messages.error(new BundleKey(KUNDENVERWALTUNG, MSG_KEY_UPDATE_PRIVATKUNDE_DUPLIKAT))
	                    .targets(CLIENT_ID_UPDATE_EMAIL);
			}
			else {
				messages.error(new BundleKey(KUNDENVERWALTUNG, MSG_KEY_UPDATE_FIRMENKUNDE_DUPLIKAT))
                        .targets(CLIENT_ID_UPDATE_EMAIL);
			}
		}
		else if (e.getClass().equals(ConcurrentUpdatedException.class)) {
			if (kundeClass.equals(Privatkunde.class)) {
				messages.error(new BundleKey(KUNDENVERWALTUNG, MSG_KEY_UPDATE_PRIVATKUNDE_CONCURRENT_UPDATE))
	                    .targets(null);
			}
			else {
				messages.error(new BundleKey(KUNDENVERWALTUNG, MSG_KEY_UPDATE_FIRMENKUNDE_CONCURRENT_UPDATE))
                        .targets(null);
			}
		}
		else if (e.getClass().equals(ConcurrentDeletedException.class)) {
			if (kundeClass.equals(Privatkunde.class)) {
				messages.error(new BundleKey(KUNDENVERWALTUNG, MSG_KEY_UPDATE_PRIVATKUNDE_CONCURRENT_DELETE))
	                    .targets(null);
			}
			else {
				messages.error(new BundleKey(KUNDENVERWALTUNG, MSG_KEY_UPDATE_FIRMENKUNDE_CONCURRENT_DELETE))
                        .targets(null);
			}
		}
		return null;
	}
	
}
