	package de.swe.kundenverwaltung.dao;

import static de.swe.util.Constants.ROLLE_TABELLE;
import static de.swe.util.Constants.UID;
import static de.swe.util.Dao.QueryParameter.with;

import java.lang.invoke.MethodHandles;
import java.text.MessageFormat;
import java.util.List;

import javax.cache.annotation.CachePut;
import javax.cache.annotation.CacheRemoveEntry;
import javax.cache.annotation.CacheResult;
import javax.cache.annotation.CacheValue;
import javax.inject.Named;
import javax.persistence.EntityExistsException;
import javax.persistence.Query;

import org.jboss.logging.Logger;

import de.swe.kundenverwaltung.domain.AbstractKunde;
import de.swe.util.Dao;
import de.swe.util.Log;
import de.swe.util.RolleType;

@Named
@Log
public class KundenverwaltungDao extends Dao {
	private static final long serialVersionUID = UID;
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());
	
	public enum Fetch {
		NUR_KUNDE,
		MIT_BESTELLUNG
	}
	
	public enum Order {
		KEINE,
		ID
	}
	
	public List<AbstractKunde> findAllKunden(Fetch fetch, Order order) {
		List<AbstractKunde> kunden = null;
		
		if (fetch == null || fetch.equals(Fetch.NUR_KUNDE)) {
			if (order.equals(Order.ID)) {
				kunden = find(AbstractKunde.class, AbstractKunde.FIND_KUNDEN_ORDER_BY_ID);
			}
			else {
				kunden = find(AbstractKunde.class, AbstractKunde.FIND_KUNDEN);
			}
		}
		else if (fetch.equals(Fetch.MIT_BESTELLUNG)) {
			kunden = find(AbstractKunde.class, AbstractKunde.FIND_KUNDEN_FETCH_BESTELLUNGEN);
		}
		
		return kunden;
	}
	
	public List<AbstractKunde> findKundenByNachname(String nachname, Fetch fetch) {
		List<AbstractKunde> kunden = null;
		
		if (fetch == null || fetch.equals(Fetch.NUR_KUNDE)) {
			kunden = find(AbstractKunde.class, AbstractKunde.FIND_KUNDEN_BY_NACHNAME,
						  with(AbstractKunde.PARAM_KUNDE_NACHNAME, nachname).build());
		}
		else if (fetch.equals(Fetch.MIT_BESTELLUNG)) {
			kunden = find(AbstractKunde.class, AbstractKunde.FIND_KUNDEN_BY_NACHNAME_FETCH_BESTELLUNGEN,
						  with(AbstractKunde.PARAM_KUNDE_NACHNAME, nachname).build());
		}
		
		return kunden;
	}
	
	public List<AbstractKunde> findKundenByNachname(String nachname) {
		List<AbstractKunde> kunden = find(AbstractKunde.class, AbstractKunde.FIND_KUNDEN_BY_NACHNAME,
					  					  with(AbstractKunde.PARAM_KUNDE_NACHNAME, nachname).build());
		
		return kunden;
	}
	
//	@CacheResult(cacheName = "kunde-cache")
	public AbstractKunde findKundeById(Long id, Fetch fetch) {
		AbstractKunde kunde = null;
		
		if (fetch == null || fetch.equals(Fetch.NUR_KUNDE)) {
			kunde = findSingle(AbstractKunde.class, AbstractKunde.FIND_KUNDEN_BY_ID,
							   with(AbstractKunde.PARAM_KUNDE_ID, id).build());
		}
		else if (fetch.equals(Fetch.MIT_BESTELLUNG)) {
			kunde = findSingle(AbstractKunde.class, AbstractKunde.FIND_KUNDEN_BY_ID_FETCH_BESTELLUNGEN,
							   with(AbstractKunde.PARAM_KUNDE_ID, id).build());
		}
		
		return kunde;
	}

	@CacheResult(cacheName = "kunde-cache")
	public AbstractKunde findKundeByEmail(String email, Fetch fetch) {
		AbstractKunde kunde = null;
		
		if (fetch == null || fetch.equals(Fetch.NUR_KUNDE)) {
			kunde = findSingle(AbstractKunde.class, AbstractKunde.FIND_KUNDEN_BY_EMAIL,
							   with(AbstractKunde.PARAM_KUNDE_EMAIL, email).build());
		}
		else if (fetch.equals(Fetch.MIT_BESTELLUNG)) {
			kunde = findSingle(AbstractKunde.class, AbstractKunde.FIND_KUNDEN_BY_EMAIL_FETCH_BESTELLUNGEN,
							   with(AbstractKunde.PARAM_KUNDE_EMAIL, email).build());
		}
		
		return kunde;
	}
	
	@CachePut(cacheName = "kunde-cache")
	public void createKunde(Long id, @CacheValue AbstractKunde kunde) {
		create(kunde);
	}

	@CacheRemoveEntry(cacheName = "kunde-cache")
	public void removeKunde(Long id) {
		delete(AbstractKunde.class, id);
	}

	public boolean addRollen(Long kundeId, RolleType... rollen) {
		if (rollen == null || rollen.length == 0) {
			return true;
		}
		
		findKundeById(kundeId, Fetch.NUR_KUNDE);
		
		final String insertTemplate = "INSERT INTO " + ROLLE_TABELLE + " VALUES (" + kundeId + ", ''{0}'')";
		for (RolleType rolle : rollen) {
			final String rolleStr = rolle.getValue();
			final String insertStr = MessageFormat.format(insertTemplate, rolleStr);
			
			LOGGER.tracef("INSERT string=%s", insertStr);
			
			final Query query = em.createNativeQuery(insertStr);
			try {
				query.executeUpdate();
			}
			catch(EntityExistsException e) {
				LOGGER.warnf("Der Kunde mit der ID %s hat bereits die Rolle %s", kundeId, rolleStr);
				
				return false;
			}
		}
		
		return true;
	}
	
	public void removeRollen(Long kundeId, RolleType... rollen) {
		if (rollen == null || rollen.length == 0) {
			return;
		}

		findKundeById(kundeId, Fetch.NUR_KUNDE);

		final String deleteTemplate = "DELETE FROM " + ROLLE_TABELLE + " WHERE kunde_fk = " + kundeId
                                      + " AND role = ''{0}''";
		for (RolleType rolle: rollen) {
			final String rolleStr = rolle.getValue();
			final String deleteStr = MessageFormat.format(deleteTemplate, rolleStr);
			LOGGER.tracef("DELETE string = %s", deleteStr);
			final Query query = em.createNativeQuery(deleteStr);
			query.executeUpdate();
		}
	}
}
