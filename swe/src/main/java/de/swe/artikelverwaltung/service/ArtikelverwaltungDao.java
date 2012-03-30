package de.swe.artikelverwaltung.service;

import static de.swe.util.Dao.QueryParameter.with;
import static de.swe.util.JpaConstants.UID;

import java.util.List;

import javax.inject.Named;

import de.swe.artikelverwaltung.domain.Autohersteller;
import de.swe.artikelverwaltung.domain.Fahrzeug;
import de.swe.util.Dao;
import de.swe.util.Log;

//TODO JCache benutzen

@Named
@Log

public class ArtikelverwaltungDao extends Dao {
	private static final long serialVersionUID = UID;
	
	public enum Order {
		KEINE,
		ID
	}
	
	public List<Fahrzeug> findAllFahrzeuge(Order order) {
		List<Fahrzeug> fahrzeuge = null;
		
		if (order.equals(Order.ID)) {
			fahrzeuge = find(Fahrzeug.class, Fahrzeug.FIND_FAHRZEUG_ORDER_BY_ID);
		}
		
		else {
			fahrzeuge = find(Fahrzeug.class, Fahrzeug.FIND_FAHRZEUG);
		}
		
		return fahrzeuge;
	}
	
	public Fahrzeug findFahrzeugById(Long id) {
			Fahrzeug fahrzeug = findSingle(Fahrzeug.class, Fahrzeug.FIND_FAHRZEUG_BY_ID,
										   with(Fahrzeug.PARAM_FAHRZEUG_ID, id).build());
			
			return fahrzeug;
	}
	
	public List<Fahrzeug> findFahrzeugByHerstellerId(Long id) {
		List<Fahrzeug> fahrzeuge = find(Fahrzeug.class, Fahrzeug.FIND_FAHRZEUG_BY_HERSTELLER, 
										with(Fahrzeug.PARAM_FAHRZEUG_HERSTELLER, id).build());
		
		return fahrzeuge;
	}
	
	public List<Autohersteller> findAllAutohersteller(Order order) {
		List<Autohersteller> autohersteller = null;
		
		if (order.equals(Order.ID)) {
			autohersteller = find(Autohersteller.class, Autohersteller.FIND_AUTOHERSTELLER_ORDER_BY_ID);
		}
		
		else {
			autohersteller = find(Autohersteller.class, Autohersteller.FIND_AUTOHERSTELLER);
		}
		
		return autohersteller;
	}
	
	public Autohersteller findAutoherstellerById(Long id) {
		Autohersteller autohersteller = findSingle(Autohersteller.class, Autohersteller.FIND_AUTOHERSTELLER_BY_ID,
									               with(Autohersteller.PARAM_ID, id).build());
		
		return autohersteller;
	}
	
	public Autohersteller findAutoherstellerByName(String name) {
		Autohersteller autohersteller = findSingle(Autohersteller.class, Autohersteller.FIND_AUTOHERSTELLER_BY_NAME,
									               with(Autohersteller.PARAM_NAME, name).build());
		
		return autohersteller;
	}
}
