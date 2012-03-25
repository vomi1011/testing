package de.swe.test.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.lang.invoke.MethodHandles;
import java.util.Locale;
import java.util.Set;

import javax.annotation.Resource;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.logging.Logger;
import org.jboss.security.client.SecurityClient;
import org.jboss.security.client.SecurityClientFactory;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;


public abstract class AbstractTest {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());
	
	protected static final Locale LOCALE = Locale.GERMAN;

	protected static final String USERNAME = "2";
	protected static final String PASSWORD = "2";
	protected static final String USERNAME_ADMIN = "1";
	protected static final String PASSWORD_ADMIN = "1";
	
	@Resource(mappedName = "java:jboss/UserTransaction")
	protected UserTransaction trans;
	
	@PersistenceContext
	@Produces
	protected EntityManager em;
	
	@Inject
	protected DbService dbService;
	
	protected SecurityClient securityClient;
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	/**
	 */
	@Deployment
	public static Archive<?> deployment() {
		return ArchiveService.getInstance().getArchive();
	}
	
	/**
	 */
	@Before
	public void setup() throws Exception {
		dbService.reload();
		
		securityClient = SecurityClientFactory.getSecurityClient();
		securityClient.setSimple(USERNAME, PASSWORD);
		securityClient.login();
		
		assertThat(em, is(notNullValue()));
		
		assertThat(trans.getStatus(), is(Status.STATUS_NO_TRANSACTION));
		trans.begin();
	}
	
	/**
	 */
	@After
	public void teardown() throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
	                              SystemException {
		try {
			if (trans != null) {
				switch (trans.getStatus()) {
					case Status.STATUS_ACTIVE:
						trans.commit();
						break;
					    
					case Status.STATUS_MARKED_ROLLBACK:
						trans.rollback();
						break;
		                
		            default:
		            	fail();
		            	break;
				}
			}
		}
		catch (RollbackException e) {
			// Commit ist fehlgeschlagen
			final Throwable t = e.getCause();
			// "Caused by" ueberpruefen
			if (t instanceof ConstraintViolationException) {
				// Es gibt Verletzungen bzgl. Bean Validation: auf der Console ausgeben
				final ConstraintViolationException cve = (ConstraintViolationException) t;
				final Set<ConstraintViolation<?>> violations = cve.getConstraintViolations();
				for (ConstraintViolation<?> v : violations) {
					LOGGER.error("!!! MESSAGE>>> " + v.getMessage());
					LOGGER.error("!!! INVALID VALUE>>> " + v.getInvalidValue());
					LOGGER.error("!!! ATTRIBUT>>> " + v.getPropertyPath());
				}
			}
	
			throw new RuntimeException(e);
		}

		securityClient.logout();
	}
}