package de.swe.bestellverwaltung.service;

import static javax.ejb.TransactionAttributeType.MANDATORY;
import static de.swe.util.Constants.UID;
import static de.swe.util.Dao.QueryParameter.with;

import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;

import de.swe.bestellverwaltung.domain.Bestellung;
import de.swe.bestellverwaltung.domain.Bestellung.Status;
import de.swe.kundenverwaltung.domain.AbstractKunde;
import de.swe.util.Dao;

@Stateless
@TransactionAttribute(MANDATORY)
public class BestellverwaltungDao extends Dao {
	private static final long serialVersionUID = UID;
	
	public enum Order {
		KEINE,
		ID
	}
	
	public List<Bestellung> findBestellungenByKundenId(Long id) {
		List<Bestellung> best = find(Bestellung.class, Bestellung.FIND_BESTELLUNG_BY_KUNDEN_ID, 
						with(Bestellung.PARAM_KUNDE_ID, id).build());
		return best;
	}
	
	public Bestellung findBestellungById(Long id) {
		Bestellung best = findSingle(Bestellung.class, Bestellung.FIND_BESTELLUNG_BY_ID,
						with(Bestellung.PARAM_BESTELL_ID, id).build());
		return best;
	}
	
	//TODO 
	//Methode testen
	public List<Bestellung> findAllBestellungen()  {
		List<Bestellung> bestellungen = find(Bestellung.class, Bestellung.FIND_BESTELLUNGEN);
		
		return bestellungen;
	}

	public List<Bestellung> findBestellungenByStatus(Status st) {
		List<Bestellung> bestellungen = find(Bestellung.class, Bestellung.FIND_BESTELLUNGEN_BY_STATUS, 
				with(Bestellung.PARAM_STATUS_DEFAULT, st).build());
		
		return bestellungen;
	}

	public AbstractKunde findKundeByBestellid(Long id) {
		AbstractKunde kunde = findBestellungById(id).getKunde();
		return kunde;
	}
}
