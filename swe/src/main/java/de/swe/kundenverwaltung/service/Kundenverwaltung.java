package de.swe.kundenverwaltung.service;

import static de.swe.util.Dao.QueryParameter.with;
import static de.swe.util.JpaConstants.KEINE_ID;
import static de.swe.util.JpaConstants.UID;
import static javax.ejb.TransactionAttributeType.MANDATORY;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.groups.Default;

import org.jboss.logging.Logger;

import de.swe.kundenverwaltung.domain.AbstractKunde;
import de.swe.kundenverwaltung.domain.PasswordGroup;
import de.swe.kundenverwaltung.service.KundenverwaltungDao.Fetch;
import de.swe.kundenverwaltung.service.KundenverwaltungDao.Order;
import de.swe.util.IdGroup;
import de.swe.util.ValidationService;

@Stateless
@TransactionAttribute(MANDATORY)
public class Kundenverwaltung implements Serializable {
	private static final long serialVersionUID = UID;
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());
	
	@SuppressWarnings("unused")
	@PersistenceContext
	private transient EntityManager em;
	
	@EJB
	private KundenverwaltungDao dao;
	
	@EJB
	private ValidationService validationService;
	
	public List<AbstractKunde> findAllKunden(Fetch fetch, Order order) {
		final List<AbstractKunde> kunden = dao.findAllKunden(fetch, order);
		
		return kunden;
	}
	
	public AbstractKunde findKundeById(long id, Fetch fetch) {
		final AbstractKunde kunde = dao.findKundeById(id, fetch);
		
		return kunde;
	}
	
	public List<AbstractKunde> findKundenByNachname(String nachname, Fetch fetch) {
		final List<AbstractKunde> kunden = dao.findKundenByNachname(nachname, fetch);
		
		return kunden;
	}
	
	public AbstractKunde findKundeByEmail(String email, Fetch fetch) {
		final AbstractKunde kunde = dao.findKundeByEmail(email, fetch);
		
		return kunde;
	}
	
	public List<AbstractKunde> findKundenByPLZ(String plz) {
		final List<AbstractKunde> kunden = dao.find(AbstractKunde.class, AbstractKunde.FIND_KUNDEN_BY_PLZ,
													with(AbstractKunde.PARAM_KUNDE_ADRESSE_PLZ, plz).build());
		
		return kunden;
	}
	
	public List<AbstractKunde> findKundenByErstellt(Date datum) {
		final List<AbstractKunde> kunden = dao.find(AbstractKunde.class, AbstractKunde.FIND_KUNDEN_BY_ERSTELLT,
													with(AbstractKunde.PARAM_KUNDE_ERSTELLT, datum).build());
		
		return kunden;
	}
	
	private void validateKunde(AbstractKunde kunde, Locale locale, Class<?>... groups)
			throws KundeValidationException {
		final Validator validator = validationService.getValidator(locale);
		final Set<ConstraintViolation<AbstractKunde>> violations = validator.validate(kunde, groups);
		
		if (!violations.isEmpty()) {
			throw new KundeValidationException(kunde, violations);
		}
	}
	
	public AbstractKunde createKunde(AbstractKunde kunde, Locale locale)
			throws EmailExistsException, KundeValidationException {
		if (kunde == null) {
			return kunde;
		}
		
		validateKunde(kunde, locale, Default.class, PasswordGroup.class);
		
		final AbstractKunde vorhandenderKunde =
				dao.findSingle(AbstractKunde.class, AbstractKunde.FIND_KUNDEN_BY_EMAIL,
							   with(AbstractKunde.PARAM_KUNDE_EMAIL, kunde.getEmail()).build());
		
		if (vorhandenderKunde != null) {
			throw new EmailExistsException(kunde.getEmail());
		}
		
		LOGGER.trace("E-Mail Adresse existiert noch nicht.");
		
		kunde.setId(KEINE_ID);
		kunde = dao.create(kunde);
		
		return kunde;
	}
	
	public AbstractKunde updateKunde(AbstractKunde kunde, Locale locale)
			throws EmailExistsException, KundeValidationException {
		if (kunde == null) {
			return kunde;
		}
		
		validateKunde(kunde, locale, Default.class, PasswordGroup.class, IdGroup.class);
		
		final AbstractKunde vorhandenerKunde =
				dao.findSingle(AbstractKunde.class, AbstractKunde.FIND_KUNDEN_BY_EMAIL,
							   with(AbstractKunde.PARAM_KUNDE_EMAIL, kunde.getEmail()).build());
		
		if (vorhandenerKunde != null && vorhandenerKunde.getId().longValue() != kunde.getId().longValue()) {
			throw new EmailExistsException(kunde.getEmail());
		}
		
		LOGGER.trace("Email Adresse existiert noch nicht.");
		
		kunde = dao.update(kunde);
		
		return kunde;
	}
	
	public void deleteKunde(AbstractKunde kunde, Locale locale) throws KundeDeleteBestellungException {
		if (kunde == null) {
			return;
		}
		
		kunde = findKundeById(kunde.getId(), Fetch.MIT_BESTELLUNG);
		
		if (kunde == null) {
			return;
		}
		
		if (!kunde.getBestellungen().isEmpty()) {
			throw new KundeDeleteBestellungException(kunde);
		}
		
		dao.delete(kunde);
	}
}
