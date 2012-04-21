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

import de.swe.artikelverwaltung.domain.Autohersteller;
import de.swe.artikelverwaltung.domain.Fahrzeug;
import de.swe.artikelverwaltung.service.ArtikelValidationExeption;
import de.swe.artikelverwaltung.service.Artikelverwaltung;
import de.swe.test.util.AbstractConcurrencyHelper;
import de.swe.test.util.AbstractConcurrencyHelper.Cmd;
import de.swe.test.util.AbstractTest;
import de.swe.util.ConcurrentDeleteException;
import de.swe.util.ConcurrentUpdateException;

@RunWith(Arquillian.class)
public class ArtikelverwaltungConcurrencyTest extends AbstractTest {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());

	private static final String AUTOHERSTELLER_NAME = "Eclipse";
	private static final String MODELL = "Modell 9";
	
	@Inject
	Artikelverwaltung av;
	
//Test update Fahrzeug	währen update
	@Test
	public void updateUpdateFahrzeug()
			throws InterruptedException, LoginException, ExecutionException, SecurityException,
				   RollbackException, HeuristicMixedException, HeuristicRollbackException, 
				   SystemException, NotSupportedException {
		LOGGER.debug("BEGINN updateUpdateFahrzeug");
		
		final String modell = MODELL;
		final String modellUpdated = "updated";
		
		Fahrzeug fahrzeug = new Fahrzeug();
		fahrzeug = av.createFahrzeug(fahrzeug, LOCALE);
		assertThat(fahrzeug.getId().longValue() > 0, is(true));
		trans.commit();
		
		ArtikelverwaltungConcurrencyHelper concurrentUpdate = 
				new ArtikelverwaltungConcurrencyHelper(Cmd.UPDATE, fahrzeug.getId());
		final ExecutorService executorService = Executors.newSingleThreadExecutor();
		final Future<Void> future = executorService.submit(concurrentUpdate);
		future.get();
		
		trans.begin();
		fahrzeug.setModell(modell + modellUpdated);
		
		try {
			av.updateFahrzeug(fahrzeug, LOCALE);
			fail("ConcurrentUpdateException wurde nicht geworfen!");
		}
		catch (ConcurrentUpdateException e) {
			trans.rollback();
			
			securityClient.logout();
			securityClient.setSimple(USERNAME_ADMIN, PASSWORD_ADMIN);
			securityClient.login();
			
			trans.begin();
			av.deleteFahrzeug(fahrzeug);
		}
		
		LOGGER.debug("ENDE updateUpdateFahrzeug");
	}

//Test update Autohersteller währen update
	@Test
	public void updateUpdateAutohersteller()
			throws InterruptedException, LoginException, ExecutionException, SecurityException,
				   RollbackException, HeuristicMixedException, HeuristicRollbackException, 
				   SystemException, NotSupportedException {
		LOGGER.debug("BEGINN updateUpdateAutohersteller");
		
		final String autohersteller_name = AUTOHERSTELLER_NAME;
		final String nameUpdated = "updated";
		
		Autohersteller autohersteller = new Autohersteller();
		autohersteller = av.createAutohersteller(autohersteller, LOCALE);
		assertThat(autohersteller.getId().longValue() > 0, is(true));
		trans.commit();
		
		ArtikelverwaltungConcurrencyHelper concurrentUpdate = 
				new ArtikelverwaltungConcurrencyHelper(Cmd.UPDATE, autohersteller.getId());
		final ExecutorService executorService = Executors.newSingleThreadExecutor();
		final Future<Void> future = executorService.submit(concurrentUpdate);
		future.get();
		
		trans.begin();
		autohersteller.setName(autohersteller_name + nameUpdated);
		
		try {
			av.updateAutohersteller(autohersteller, LOCALE);
			fail("ConcurrentUpdateException wurde nicht geworfen!");
		}
		catch (ConcurrentUpdateException e) {
			trans.rollback();
			
			securityClient.logout();
			securityClient.setSimple(USERNAME_ADMIN, PASSWORD_ADMIN);
			securityClient.login();
			
			trans.begin();
			av.deleteAutohersteller(autohersteller);
		}
		
		LOGGER.debug("ENDE updateUpdateAutohersteller");
	}

//Test update Fahrzeug, der nicht existiert	
	@Test
	public void updateDeleteFahrzeug()
			throws SecurityException, IllegalStateException, RollbackException, HeuristicMixedException, 
				   HeuristicRollbackException, SystemException, InterruptedException, ExecutionException, 
				   NotSupportedException {
		LOGGER.debug("BEGINN updateDeleteFahrzeug");	
		
		final String modell = MODELL;
		final String modellUpdate = "updated";
		
		Fahrzeug fahrzeug = new Fahrzeug();
		fahrzeug = av.createFahrzeug(fahrzeug, LOCALE);
		assertThat(fahrzeug.getId().longValue() > 0, is(true));
		trans.commit();
		
		ArtikelverwaltungConcurrencyHelper concurrentDelete = new ArtikelverwaltungConcurrencyHelper(Cmd.DELETE, fahrzeug.getId());
		final ExecutorService executorService = Executors.newSingleThreadExecutor();
		final Future<Void> future = executorService.submit(concurrentDelete);
		future.get();
		
		trans.begin();
		fahrzeug.setModell(modell + modellUpdate);
		
		thrown.expect(ConcurrentDeleteException.class);
		av.updateFahrzeug(fahrzeug, LOCALE);
		
		LOGGER.debug("ENDE updateDeleteFahrzeug");

	}
	
//Test update Autohersteller, der nicht existiert	
	@Test
	public void updateDeleteAutohersteller()
			throws SecurityException, IllegalStateException, RollbackException, HeuristicMixedException, 
				   HeuristicRollbackException, SystemException, InterruptedException, ExecutionException, 
				   NotSupportedException {
		LOGGER.debug("BEGINN updateDeleteAutohersteller");		
		
		final String autohersteller_name = AUTOHERSTELLER_NAME;
		final String nameUpdated = "updated";
		
		Autohersteller autohersteller = new Autohersteller();
		autohersteller = av.createAutohersteller(autohersteller, LOCALE);
		assertThat(autohersteller.getId().longValue() > 0, is(true));
		trans.commit();
		
		ArtikelverwaltungConcurrencyHelper concurrentDelete = new ArtikelverwaltungConcurrencyHelper(Cmd.DELETE, autohersteller.getId());
		final ExecutorService executorService = Executors.newSingleThreadExecutor();
		final Future<Void> future = executorService.submit(concurrentDelete);
		future.get();
		
		trans.begin();
		autohersteller.setName(autohersteller_name + nameUpdated);
		
		thrown.expect(ConcurrentDeleteException.class);
		av.updateAutohersteller(autohersteller, LOCALE);
		
		LOGGER.debug("ENDE updateDeleteAutohersteller");

	}

//Test delete Fahrzeug, der upgedated wird	
	@Test
	public void deleteUpdateFahrzeug()
			throws SecurityException, IllegalStateException, RollbackException, HeuristicMixedException,
				   HeuristicRollbackException, SystemException, InterruptedException, ExecutionException,
				   LoginException, NotSupportedException {
		LOGGER.debug("BEGINN deleteUpdateFahrzeug");

		Fahrzeug fahrzeug = new Fahrzeug();
		fahrzeug = av.createFahrzeug(fahrzeug, LOCALE);
		assertThat(fahrzeug.getId().longValue() > 0, is(true));
		trans.commit();
		
		ArtikelverwaltungConcurrencyHelper concurrentUpdate = 
				new ArtikelverwaltungConcurrencyHelper(Cmd.UPDATE, fahrzeug.getId());
		final ExecutorService executorService = Executors.newSingleThreadExecutor();
		final Future<Void> future = executorService.submit(concurrentUpdate);
		future.get();

		securityClient.logout();
		securityClient.setSimple(USERNAME_ADMIN, PASSWORD_ADMIN);
		securityClient.login();
		
		trans.begin();
		av.deleteFahrzeug(fahrzeug);
		
		final Fahrzeug tmp = av.findFahrzeugById(fahrzeug.getId());
		assertThat(tmp, is(nullValue()));
		
		LOGGER.debug("ENDE deleteUpdateFahrzeug");
		

	}

//Test delete Autohersteller, der upgedated wird	
	@Test
	public void deleteUpdateAutohersteller()
			throws SecurityException, IllegalStateException, RollbackException, HeuristicMixedException,
				   HeuristicRollbackException, SystemException, InterruptedException, ExecutionException,
				   LoginException, NotSupportedException {
		LOGGER.debug("BEGINN deleteUpdateAutohersteller");

		Autohersteller autohersteller = new Autohersteller();
		autohersteller = av.createAutohersteller(autohersteller, LOCALE);
		assertThat(autohersteller.getId().longValue() > 0, is(true));
		trans.commit();
		
		ArtikelverwaltungConcurrencyHelper concurrentUpdate = 
				new ArtikelverwaltungConcurrencyHelper(Cmd.UPDATE, autohersteller.getId());
		final ExecutorService executorService = Executors.newSingleThreadExecutor();
		final Future<Void> future = executorService.submit(concurrentUpdate);
		future.get();

		securityClient.logout();
		securityClient.setSimple(USERNAME_ADMIN, PASSWORD_ADMIN);
		securityClient.login();
		
		trans.begin();
		av.deleteAutohersteller(autohersteller);
		
		final Autohersteller tmp = av.findAutoherstellerById(autohersteller.getId());
		assertThat(tmp, is(nullValue()));
		
		LOGGER.debug("ENDE deleteUpdateAutohersteller");

		}

	private class ArtikelverwaltungConcurrencyHelper extends
			AbstractConcurrencyHelper {
		private Long fahrzeugId;
		private Long autoherstellerId;
	
		protected ArtikelverwaltungConcurrencyHelper(Cmd cmd, Long id) {
			super(cmd);
			fahrzeugId = id;
			autoherstellerId = id;
		}
	
		@Override
		protected void update() {
			LOGGER.debug("BEGINN update");
			
			try {
				final Fahrzeug fahrzeug = av.findFahrzeugById(fahrzeugId);
				final Autohersteller autohersteller = av.findAutoherstellerById(autoherstellerId);
				fahrzeug.setModell(fahrzeug.getModell() + "concurrent");
				autohersteller.setName(autohersteller.getName() + "concurrent");
				av.updateFahrzeug(fahrzeug, LOCALE);
				av.updateAutohersteller(autohersteller, LOCALE);
			}
			catch (ConcurrentUpdateException |
				   ConcurrentDeleteException | 
				   ArtikelValidationExeption e) {
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
			} catch (LoginException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			
			try {
				av.deleteFahrzeug(av.findFahrzeugById(fahrzeugId));
				av.deleteAutohersteller(av.findAutoherstellerById(autoherstellerId));
			}

			catch (ArtikelValidationExeption e) {
				throw new IllegalStateException(e);
			}
			
			LOGGER.debug("ENDE delete");
		}
	}
}
