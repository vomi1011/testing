package de.swe.kundenverwaltung.service;

import static de.swe.util.Constants.KEINE_ID;
import static de.swe.util.Constants.ROLLE_ADMIN;
import static de.swe.util.Constants.ROLLE_MITARBEITER;
import static de.swe.util.Constants.SECURITY_DOMAIN;
import static de.swe.util.Constants.UID;
import static de.swe.util.Dao.QueryParameter.with;
import static javax.ejb.TransactionAttributeType.MANDATORY;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.security.Principal;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.groups.Default;

import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.logging.Logger;
import org.jboss.security.SimpleGroup;

import de.swe.kundenverwaltung.dao.KundenverwaltungDao;
import de.swe.kundenverwaltung.dao.KundenverwaltungDao.Fetch;
import de.swe.kundenverwaltung.dao.KundenverwaltungDao.Order;
import de.swe.kundenverwaltung.domain.AbstractKunde;
import de.swe.kundenverwaltung.domain.PasswordGroup;
import de.swe.util.IdGroup;
import de.swe.util.RolleType;
import de.swe.util.ValidationService;
import de.swe.util.jboss.PasswordService;
import de.swe.util.jboss.SecurityCache;

@Stateless
@TransactionAttribute(MANDATORY)
@SecurityDomain(SECURITY_DOMAIN) //TODO entfernen wenn nachher Schutz fuer alle EJBs eingestellt wird
public class Kundenverwaltung implements Serializable {
	private static final long serialVersionUID = UID;
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());
	
	@SuppressWarnings("unused")
	@PersistenceContext
	private transient EntityManager em;
	
	@Inject
	private KundenverwaltungDao dao;
	
	@Inject
	private ValidationService validationService;
	
	@Inject
	private PasswordService passwordService;
	
	@Inject
	private SecurityCache securityCache;
	
	@Resource
	private SessionContext ctx;
	
	public List<AbstractKunde> findAllKunden(Fetch fetch, Order order) {
		final List<AbstractKunde> kunden = dao.findAllKunden(fetch, order);
		
		return kunden;
	}
	
	public AbstractKunde findKundeById(Long id, Fetch fetch) {
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
	
	private void validateKunde(AbstractKunde kunde, Locale locale, Class<?>... groups) {
		final Validator validator = validationService.getValidator(locale);
		final Set<ConstraintViolation<AbstractKunde>> violations = validator.validate(kunde, groups);
		
		if (!violations.isEmpty()) {
			throw new KundeValidationException(kunde, violations);
		}
	}
	
	public AbstractKunde createKunde(AbstractKunde kunde, Locale locale) {
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
		
		passwordVerschluesseln(kunde);
		
		kunde.setId(KEINE_ID);
		kunde = dao.create(kunde);
		
		return kunde;
	}
	
	public AbstractKunde updateKunde(AbstractKunde kunde, Locale locale) {
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
		else if (kunde.getPassword() != null && !kunde.getPassword().equals(vorhandenerKunde.getPassword())) {
			passwordVerschluesseln(kunde);
		}
		
		kunde = dao.update(kunde, kunde.getId());
		kunde.setPasswordWdh(kunde.getPassword());
		
		return kunde;
	}
	
	@RolesAllowed(ROLLE_ADMIN)
	public void deleteKunde(AbstractKunde kunde) {
		if (kunde == null) {
			return;
		}
		
		deleteKundeById(kunde.getId());
	}
	
	@RolesAllowed(ROLLE_ADMIN)
	public void deleteKundeById(Long id) {
		AbstractKunde kunde = findKundeById(id, Fetch.MIT_BESTELLUNG);
		
		if (kunde == null) {
			return;
		}
		
		if (kunde.getBestellungen() != null && !kunde.getBestellungen().isEmpty()) {
			throw new KundeDeleteBestellungException(kunde);
		}
		
		dao.delete(kunde);
	}
	
	@RolesAllowed({ ROLLE_ADMIN, ROLLE_MITARBEITER })
	public Principal whoAmI() {
		final Principal principal = ctx.getCallerPrincipal();
		return principal;
	}

	@RolesAllowed(ROLLE_ADMIN)
	public void addRollen(Long kundeId, RolleType... rollen) {
		final boolean ok = dao.addRollen(kundeId, rollen);
		
		if (!ok) {
			ctx.setRollbackOnly();
			
			return;
		}
		
		securityCache.remove(kundeId.toString());
	}

	@RolesAllowed(ROLLE_ADMIN)
	public void removeRollen(Long kundeId, RolleType... rollen) {
		dao.removeRollen(kundeId, rollen);
		securityCache.remove(kundeId.toString());
	}
	
	public List<RolleType> getEigeneRollen() {
		List<RolleType> rollen = new LinkedList<>();
		
		Subject subject = null;
		try {
			subject = (Subject) PolicyContext.getContext("javax.security.auth.Subject.container");
		}
		catch (PolicyContextException e) {
			final InternalError error = new InternalError(e.getMessage());
			LOGGER.error(error.getMessage(), error);
			throw error;
		}
		if (subject == null) {
			return null;
		}
		
		final Set<Principal> principals = subject.getPrincipals(Principal.class);
		for (Principal p : principals) {
			if (!(p instanceof SimpleGroup)) {
				continue;
			}

			final SimpleGroup sg = (SimpleGroup) p;
			if (!"Roles".equals(sg.getName())) {
				continue;
			}
			
			final Enumeration<Principal> members = sg.members();
			while (members.hasMoreElements()) {
				final String rolle = members.nextElement().toString();
				if (rolle != null) {
					rollen.add(RolleType.valueOf(rolle.toUpperCase()));
				}
			}
		}
		
		return rollen;
	}
	
	private void passwordVerschluesseln(AbstractKunde kunde) {
		LOGGER.debugf("BEGINN passwordVerschluesseln: kunde=%s", kunde);
		
		final String unverschluesselt = kunde.getPassword();
		final String verschluesselt = passwordService.verschluesseln(unverschluesselt);
		
		kunde.setPassword(verschluesselt);
		kunde.setPasswordWdh(verschluesselt);

		LOGGER.debugf("ENDE passwordVerschluesseln: kunde=%s", verschluesselt);
	}
}
