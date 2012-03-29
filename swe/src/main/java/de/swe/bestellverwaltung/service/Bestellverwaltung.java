package de.swe.bestellverwaltung.service;

import static de.swe.util.JpaConstants.KEINE_ID;
import static de.swe.util.JpaConstants.UID;
import static javax.ejb.TransactionAttributeType.MANDATORY;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.groups.Default;

import de.swe.bestellverwaltung.domain.Bestellposition;
import de.swe.bestellverwaltung.domain.Bestellung;
import de.swe.bestellverwaltung.domain.Bestellung.Status;
import de.swe.kundenverwaltung.domain.AbstractKunde;
import de.swe.kundenverwaltung.service.Kundenverwaltung;
import de.swe.kundenverwaltung.service.KundenverwaltungDao.Fetch;
import de.swe.util.ValidationService;

@Stateless
@TransactionAttribute(MANDATORY)
public class Bestellverwaltung implements Serializable {
	private static final long serialVersionUID = UID;
	
	@EJB
	private BestellverwaltungDao dao;
	
	@EJB
	private Kundenverwaltung kv;
	
	@EJB
	private ValidationService validationService;
	
	public List<Bestellung> findAllBestellungen() {
		final List<Bestellung> bestellungen = dao.findAllBestellungen();
		
		return bestellungen;
	}
	
	public List<Bestellung> findBestellungenByStatus(Status st) {
		final List<Bestellung> bestellungen = dao.findBestellungenByStatus(st);
		return bestellungen;
	}
	
	public List<Bestellung> findBestellungenByKundenId(Long id) {
		final List<Bestellung> bestellungen = dao.findBestellungenByKundenId(id);
		return bestellungen;
	}
	
	public Bestellung findBestellungById(Long id) {
		final Bestellung bestellung = dao.findBestellungById(id);
		return bestellung;		
	}
	

	public AbstractKunde findKundeByBestellid(Long id) {
		final AbstractKunde kunde = dao.findKundeByBestellid(id);
		return kunde;
	}
	
	private void validateBestellung(Bestellung bestellung, Locale locale, 
			Class<?>... groups) throws BestellungValidationException {
		Validator validator = (Validator) validationService.getValidator(locale);
		
		Set<ConstraintViolation<Bestellung>> violations = 
				validator.validate(bestellung, groups);				
		
		if (!violations.isEmpty()) {
			throw new BestellungValidationException(bestellung, violations);
		}
	}

	public Bestellung createBestellung(Bestellung bestellung,
			AbstractKunde kunde, Locale locale) throws BestellungValidationException {
		if (bestellung == null) {
			return bestellung;
		}
		
		kunde = kv.findKundeById(kunde.getId(), Fetch.MIT_BESTELLUNG);
		
		kunde.addBestellung(bestellung);
		bestellung.setKunde(kunde);
		
		validateBestellung(bestellung, locale, Default.class);
		
		bestellung.setBId(KEINE_ID);
		for (Bestellposition bp : bestellung.getBestellpositionen()) {
			bp.setBpId(KEINE_ID);
		}
		
		bestellung = dao.create(bestellung);
		return bestellung;
	}
	
	public Bestellung updateBestellung(Bestellung bestellung, 
			Locale locale) throws BestellungValidationException {
		if (bestellung == null) {
			return bestellung;
		}
		
		validateBestellung(bestellung, locale, Default.class);
		
		bestellung = dao.update(bestellung);
		return bestellung;
	}
	
	public Bestellung stornierenBestellung(Bestellung bestellung, Status status,
			Locale locale) throws BestellungValidationException {
		if (bestellung == null) {
			return bestellung;
		}
		
		validateBestellung(bestellung, locale, Default.class);
		
		if(!bestellung.status.toString().equals("ABGEHOLT"))
				{
					bestellung.setStatus(Status.STORNIERT);
					bestellung = dao.update(bestellung);
				}
		return bestellung;
		
	}
	
	
	public void deleteBestellung(Bestellung bestellung, Locale locale) 
			throws BestellungValidationException {
		if (bestellung == null) {
			return;
		}
		
		bestellung = findBestellungById(bestellung.getBId());
		
		if (bestellung == null) {
			return;
		}
		
		dao.delete(bestellung);
	}
	
}
