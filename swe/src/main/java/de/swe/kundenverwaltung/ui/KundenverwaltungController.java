package de.swe.kundenverwaltung.ui;

import static de.swe.util.Constants.UID;
import static de.swe.util.Constants.KUNDENVERWALTUNG;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jboss.logging.Logger;
import org.jboss.seam.international.status.Messages;
import org.jboss.seam.international.status.builder.BundleKey;
import org.jboss.solder.core.Client;
import org.richfaces.component.SortOrder;

import de.swe.kundenverwaltung.dao.KundenverwaltungDao.Fetch;
import de.swe.kundenverwaltung.domain.AbstractKunde;
import de.swe.kundenverwaltung.domain.Privatkunde;
import de.swe.kundenverwaltung.service.Kundenverwaltung;
import de.swe.util.Log;

@Named("kv")
@SessionScoped
@Log
public class KundenverwaltungController implements Serializable {
	private static final long serialVersionUID = UID;

	private static final int MAX_AUTOCOMPLETE = 10;

	private static final String JSF_KUNDENVERWALTUNG = "/kundenverwaltung/";
	private static final String JSF_VIEW_KUNDE = JSF_KUNDENVERWALTUNG + "viewKunde";
	private static final String JSF_LIST_KUNDEN = JSF_KUNDENVERWALTUNG + "/kundenverwaltung/listKunden";
	private static final String JSF_UPDATE_PRIVATKUNDE = JSF_KUNDENVERWALTUNG + "updatePrivatkunde";
	private static final String JSF_UPDATE_FIRMENKUNDE = JSF_KUNDENVERWALTUNG + "updateFirmenkunde";
	private static final String JSF_DELETE_OK = JSF_KUNDENVERWALTUNG + "okDelete";

	private static final String CLIENT_ID_KUNDEID = "form:kundeId";
	private static final String MSG_KEY_KUNDE_NOT_FOUND_BY_ID = "viewKunde.notFound";
	
	private Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());;
	
	@Inject
	private Kundenverwaltung kv;
	
	@PersistenceContext
	@SuppressWarnings("unused")
	private transient EntityManager em;
	
	@Inject
	private transient ExternalContext ext;
	
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
	
	private boolean geandertKunde; // fuer ValueChangeListener
	private Privatkunde neuerPrivatKunde;
	
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
	
	public Privatkunde getNeuerPrivatKunde() {
		return neuerPrivatKunde;
	}
	
	public void setNeuerPrivatKunde(Privatkunde neuerPrivatKunde) {
		this.neuerPrivatKunde = neuerPrivatKunde;
	}
	
	public Date getAktuellesDatum() {
		Date datum = new Date();
		return datum;
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
	
	private String findKundeByIdErrorMsg(String id) {
		messages.error(new BundleKey(KUNDENVERWALTUNG, MSG_KEY_KUNDE_NOT_FOUND_BY_ID), id)
                .targets(CLIENT_ID_KUNDEID);
		return null;
	}
	
}
