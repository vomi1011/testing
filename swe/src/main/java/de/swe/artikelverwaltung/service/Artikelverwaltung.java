package de.swe.artikelverwaltung.service;

import static de.swe.util.Constants.ROLLE_ADMIN;
import static de.swe.util.Constants.ROLLE_ARTIKELVERWALTER;
import static de.swe.util.Constants.UID;
import static javax.ejb.TransactionAttributeType.MANDATORY;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.groups.Default;

import de.swe.artikelverwaltung.domain.Autohersteller;
import de.swe.artikelverwaltung.domain.Fahrzeug;
import de.swe.artikelverwaltung.service.ArtikelverwaltungDao.Order;
import de.swe.util.ValidationService;

//TODO Code kommentieren
@Stateless
@TransactionAttribute(MANDATORY)
public class Artikelverwaltung implements Serializable {
	
	private static final long serialVersionUID = UID;	

	@Inject
	private ArtikelverwaltungDao dao;
	
	@Inject
	private ValidationService validationService;
	
	public List<Fahrzeug> findAllFahrzeuge(Order order) {
		final List<Fahrzeug> fahrzeuge = dao.findAllFahrzeuge(order);
		
		return fahrzeuge;
	}
	
	public Fahrzeug findFahrzeugById(Long id) {
		Fahrzeug fahrzeug = dao.findFahrzeugById(id);
		
		return fahrzeug;
	}
	
	public List<Fahrzeug> findFahrzeugeByModell(String modell) {
		List<Fahrzeug> fahrzeuge = dao.findFahrzeugeByModell(modell);
		
		return fahrzeuge;
	}
	
	public List<Fahrzeug> findFahrzeugByHerstellerId(Long id) {
		final List<Fahrzeug> fahrzeuge = dao.findFahrzeugByHerstellerId(id);
		
		return fahrzeuge;
	}
	
	private void validate(Fahrzeug fahrzeug, Locale locale, Class<?>... groups) {
		Validator validator = (Validator) validationService.getValidator(locale);
		Set<ConstraintViolation<Fahrzeug>> violations = validator.validate(fahrzeug, groups);				
		
		if (!violations.isEmpty()) {
			throw new ArtikelValidationException(fahrzeug, violations);
		}
	}
	
	@RolesAllowed({ROLLE_ADMIN, ROLLE_ARTIKELVERWALTER })
	public Fahrzeug createFahrzeug(Fahrzeug fahrzeug, Locale locale) {
		validate(fahrzeug, locale, Default.class);	
		fahrzeug = dao.create(fahrzeug);
		
		return fahrzeug;
	}
	
	@RolesAllowed({ROLLE_ADMIN, ROLLE_ARTIKELVERWALTER })
	public Fahrzeug updateFahrzeug(Fahrzeug fahrzeug, Locale locale) {
		validate(fahrzeug, locale, Default.class);
		fahrzeug = dao.update(fahrzeug, fahrzeug.getId());
		
		return fahrzeug;
	}
	
	@RolesAllowed({ROLLE_ADMIN, ROLLE_ARTIKELVERWALTER })
	public void deleteFahrzeug(Fahrzeug fahrzeug) {
		
		if (fahrzeug == null) {
			return;
		}
		
		deleteFahrzeugById(fahrzeug.getId());
		
	}
	
	@RolesAllowed({ROLLE_ADMIN, ROLLE_ARTIKELVERWALTER })
	public void deleteFahrzeugById(Long id) {
		Fahrzeug fahrzeug = findFahrzeugById(id);
		
		if (fahrzeug == null) {
			return;
		}
		
		dao.delete(fahrzeug);
	}

//	Autohersteller
	
	public List<Autohersteller> findAllAutohersteller(Order order) {
		final List<Autohersteller> autohersteller = dao.findAllAutohersteller(order);
		
		return autohersteller;
	}
	
	public List<Autohersteller> findAllAutoherstellerByName(Order order) {
		final List<Autohersteller> autohersteller = dao.findAllAutohersteller(order);
		
		return autohersteller;
	}
	
	public Autohersteller findAutoherstellerById(Long id) {
		Autohersteller autohersteller = dao.findAutoherstellerById(id);
		
		return autohersteller;
	}
	
	private void validate(Autohersteller autohersteller, Locale locale, Class<?>... groups) {
		Validator validator = (Validator) validationService.getValidator(locale);
		Set<ConstraintViolation<Autohersteller>> violations = validator.validate(autohersteller, groups);				
		
		if (!violations.isEmpty()) {
			throw new ArtikelValidationExceptionAH(autohersteller, violations);
		}
	}
	
	public Autohersteller findAutoherstellerByName(String name) {
		Autohersteller autohersteller = dao.findAutoherstellerByName(name);
		
		return autohersteller;
	}
	
	@RolesAllowed(ROLLE_ADMIN)
	public Autohersteller createAutohersteller(Autohersteller autohersteller, Locale locale) {
		validate(autohersteller, locale, Default.class);
		
		autohersteller = dao.create(autohersteller);
		
		return autohersteller;
	}
	
	@RolesAllowed({ROLLE_ADMIN, ROLLE_ARTIKELVERWALTER })
	public Autohersteller updateAutohersteller(Autohersteller autohersteller, Locale locale) {
		validate(autohersteller, locale, Default.class);
		
		autohersteller = dao.update(autohersteller, autohersteller.getId());
		
		return autohersteller;
	}

	@RolesAllowed(ROLLE_ADMIN)
	public void deleteAutohersteller(Autohersteller autohersteller) {
		if (autohersteller == null) {
			return;
		}
		
		deleteAutoherstellerById(autohersteller.getId());
		
	}
	
	@RolesAllowed(ROLLE_ADMIN)
	public void deleteAutoherstellerById(Long id) {
		Autohersteller autohersteller = findAutoherstellerById(id);
		if (autohersteller == null) {
			return;
		}
		
		dao.delete(autohersteller);
	}

}
