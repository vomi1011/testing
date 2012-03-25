	package de.swe.kundenverwaltung.service;

import static de.swe.util.Dao.QueryParameter.with;
import static de.swe.util.JpaConstants.UID;
import static javax.ejb.TransactionAttributeType.MANDATORY;

import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;

import de.swe.kundenverwaltung.domain.AbstractKunde;
import de.swe.util.Dao;

//TODO Zu CDI wechseln, @Log und JCache benutzen
@Stateless
@TransactionAttribute(MANDATORY)
public class KundenverwaltungDao extends Dao {
	private static final long serialVersionUID = UID;
	
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
}
