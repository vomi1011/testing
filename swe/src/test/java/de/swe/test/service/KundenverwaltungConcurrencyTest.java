package de.swe.test.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.inject.Inject;
import javax.security.auth.login.LoginException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.logging.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.swe.kundenverwaltung.dao.KundenverwaltungDao.Fetch;
import de.swe.kundenverwaltung.domain.AbstractKunde;
import de.swe.kundenverwaltung.domain.Adresse;
import de.swe.kundenverwaltung.domain.Privatkunde;
import de.swe.kundenverwaltung.service.EmailExistsException;
import de.swe.kundenverwaltung.service.KundeDeleteBestellungException;
import de.swe.kundenverwaltung.service.KundeValidationException;
import de.swe.kundenverwaltung.service.Kundenverwaltung;
import de.swe.test.util.AbstractConcurrencyHelper;
import de.swe.test.util.AbstractConcurrencyHelper.Cmd;
import de.swe.test.util.AbstractTest;
import de.swe.util.ConcurrentDeleteException;
import de.swe.util.ConcurrentUpdateException;

@RunWith(Arquillian.class)
public class KundenverwaltungConcurrencyTest extends AbstractTest {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());

	private static final String NACHNAME_NEU = "Kauf";
	private static final String EMAIL_NEU = "kauf@cdf.de";
	private static final String STRASSE_NEU = "Weg";
	private static final String HAUSNR_NEU = "22";
	private static final String PLZ_NEU = "76133";
	private static final String ORT_NEU = "Karlsruhe";
	
	@Inject
	private Kundenverwaltung kv;
	
	@Test
	public void updateUpdateKunde()
			throws InterruptedException, LoginException, ExecutionException,
				   RollbackException, HeuristicMixedException, HeuristicRollbackException, 
				   SystemException, NotSupportedException {
		LOGGER.debug("BEGINN updateUpdateKunde");

		final String nachname = NACHNAME_NEU;
		final String nachnameUpdated = "updated";
		final String email = EMAIL_NEU;
		final String strasse = STRASSE_NEU;
		final String hausnr = HAUSNR_NEU;
		final String plz = PLZ_NEU;
		final String ort = ORT_NEU;
		
		final Adresse adresse = new Adresse(strasse , hausnr , plz , ort);
		AbstractKunde kunde = new Privatkunde(nachname , email , adresse);
		kunde = kv.createKunde(kunde, LOCALE);
		assertThat(kunde.getId().longValue() > 0, is(true));
		trans.commit();
			
		final KundenverwaltungConcurrencyHelper concurrentUpdate = 
				new KundenverwaltungConcurrencyHelper(Cmd.UPDATE, kunde.getId());
		final ExecutorService executorService = Executors.newSingleThreadExecutor();
		final Future<Void> future = executorService.submit(concurrentUpdate);
		future.get();
		
		trans.begin();
		kunde.setNachname(nachname + nachnameUpdated);
		
		try {
			kv.updateKunde(kunde, LOCALE);
			fail("ConcurrentUpdateException wurde nicht geworfen!");
		}
		catch (final ConcurrentUpdateException e) {
			trans.rollback();
			
			securityClient.logout();
			securityClient.setSimple(USERNAME_ADMIN, PASSWORD_ADMIN);
			securityClient.login();
			
			trans.begin();
			kv.deleteKunde(kunde);
		}
		
		LOGGER.debug("ENDE updateUpdateKunde");
	}
	
	@Test
	public void updateDeleteKunde()
			throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
			SystemException, InterruptedException, ExecutionException, NotSupportedException {
		LOGGER.debug("BEGINN updateDeleteKunde");
		
		final String nachname = NACHNAME_NEU;
		final String nachnameUpdate = "updated";
		final String email = EMAIL_NEU;
		final String strasse = STRASSE_NEU;
		final String hausnr = HAUSNR_NEU;
		final String plz = PLZ_NEU;
		final String ort = ORT_NEU;
		
		final Adresse adresse = new Adresse(strasse, hausnr, plz, ort);
		AbstractKunde kunde = new Privatkunde(nachname, email, adresse);
		kunde = kv.createKunde(kunde, LOCALE);
		assertThat(kunde.getId().longValue() > 0, is(true));
		trans.commit();
		
		final KundenverwaltungConcurrencyHelper concurrentDelete = new KundenverwaltungConcurrencyHelper(Cmd.DELETE, kunde.getId());
		final ExecutorService executorService = Executors.newSingleThreadExecutor();
		final Future<Void> future = executorService.submit(concurrentDelete);
		future.get();
		
		trans.begin();
		kunde.setNachname(nachname + nachnameUpdate);
		
		thrown.expect(ConcurrentDeleteException.class);
		kv.updateKunde(kunde, LOCALE);
		
		LOGGER.debug("BEGINN updateDeleteKunde");
	}
	
	@Test
	public void deleteUpdateKunde()
			throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
			SystemException, InterruptedException, ExecutionException,
				   LoginException, NotSupportedException {
		LOGGER.debug("BEGINN deleteUpdateKunde");

		final String nachname = NACHNAME_NEU;
		final String email = EMAIL_NEU;
		final String strasse = STRASSE_NEU;
		final String hausnr = HAUSNR_NEU;
		final String plz = PLZ_NEU;
		final String ort = ORT_NEU;

		final Adresse adresse = new Adresse(strasse, hausnr, plz, ort);
		AbstractKunde kunde = new Privatkunde(nachname, email, adresse);
		kunde = kv.createKunde(kunde, LOCALE);
		assertThat(kunde.getId().longValue() > 0, is(true));
		trans.commit();
		
		final KundenverwaltungConcurrencyHelper concurrentUpdate = 
				new KundenverwaltungConcurrencyHelper(Cmd.UPDATE, kunde.getId());
		final ExecutorService executorService = Executors.newSingleThreadExecutor();
		final Future<Void> future = executorService.submit(concurrentUpdate);
		future.get();

		securityClient.logout();
		securityClient.setSimple(USERNAME_ADMIN, PASSWORD_ADMIN);
		securityClient.login();
		
		trans.begin();
		kv.deleteKunde(kunde);
		
		final AbstractKunde tmp = kv.findKundeById(kunde.getId(), Fetch.NUR_KUNDE);
		assertThat(tmp, is(nullValue()));
		
		LOGGER.debug("BEGINN deleteUpdateKunde");
	}

	private class KundenverwaltungConcurrencyHelper extends
			AbstractConcurrencyHelper {
		final private Long kundeId;
	
		protected KundenverwaltungConcurrencyHelper(Cmd cmd, Long id) {
			super(cmd);
			kundeId = id;
		}
	
		@Override
		protected void update() {
			LOGGER.debug("BEGINN update");
			
			try {
				final AbstractKunde kunde = kv.findKundeById(kundeId, Fetch.NUR_KUNDE);
				kunde.setNachname(kunde.getNachname() + "concurrent");
				kv.updateKunde(kunde, LOCALE);
			}
			catch (ConcurrentUpdateException |
				   ConcurrentDeleteException | 
				   EmailExistsException | 
				   KundeValidationException e) {
				throw new IllegalStateException(e);
			}
	
			LOGGER.debug("ENDE update");
		}
	
		@Override
		protected void delete() {
			LOGGER.debug("BEGINN delete");
			
			securityClient.logout();
			securityClient.setSimple(USERNAME_ADMIN, PASSWORD_ADMIN);
			
			try {
				securityClient.login();
			} catch (final LoginException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			
			try {
				kv.deleteKundeById(kundeId);
			}
			catch (final KundeDeleteBestellungException e) {
				throw new IllegalStateException(e);
			}
			
			LOGGER.debug("ENDE delete");
		}
	}
}
