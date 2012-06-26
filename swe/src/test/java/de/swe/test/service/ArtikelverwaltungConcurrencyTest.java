package de.swe.test.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
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
import de.swe.artikelverwaltung.service.ArtikelValidationException;
import de.swe.artikelverwaltung.service.Artikelverwaltung;
import de.swe.test.util.AbstractConcurrencyHelper;
import de.swe.test.util.AbstractConcurrencyHelper.Cmd;
import de.swe.test.util.AbstractTest;
import de.swe.util.ConcurrentDeletedException;
import de.swe.util.ConcurrentUpdatedException;

@RunWith(Arquillian.class)
public class ArtikelverwaltungConcurrencyTest extends AbstractTest {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());

	private static final String AUTOHERSTELLER_NAME = "Eclipse";
	private static final String MODELL_NAME_NEU = "Modell 9";
	
	@Inject
	private Artikelverwaltung av;
	
//Test update Fahrzeug	währen update
	@Test
	public void updateUpdateFahrzeug()
			throws InterruptedException, LoginException, ExecutionException,
				   RollbackException, HeuristicMixedException, HeuristicRollbackException, 
				   SystemException, NotSupportedException {
		LOGGER.debug("BEGINN updateUpdateFahrzeug");
		
		final Long id = Long.valueOf(7001);
		final String modell = MODELL_NAME_NEU;
		final String modellUpdated = "updated";
		final Autohersteller autohersteller = av.findAutoherstellerById(id);
		assertThat(autohersteller, is(notNullValue()));
		
		securityClient.logout();
		securityClient.setSimple(USERNAME_ARTIKELVERWALTER, PASSWORD_ARTIKELVERWALTER);
		securityClient.login();
		
		Fahrzeug fahrzeug = new Fahrzeug();
		fahrzeug.setModell(modell);
		fahrzeug.setHersteller(autohersteller);
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
		catch (ConcurrentUpdatedException e) {
			trans.rollback();
			
			securityClient.logout();
			securityClient.setSimple(USERNAME_ADMIN, PASSWORD_ADMIN);
			securityClient.login();
			
			trans.begin();
			av.deleteFahrzeug(fahrzeug);
		}
		
		LOGGER.debug("ENDE updateUpdateFahrzeug");
	}

//Test update Autohersteller waehrend update
	@Test
	public void updateUpdateAutohersteller()
			throws InterruptedException, LoginException, ExecutionException,
				   RollbackException, HeuristicMixedException, HeuristicRollbackException, 
				   SystemException, NotSupportedException {
		LOGGER.debug("BEGINN updateUpdateAutohersteller");
		
		final String name = AUTOHERSTELLER_NAME;
		final String nameUpdated = "updated";
		
		securityClient.logout();
		securityClient.setSimple(USERNAME_ADMIN, PASSWORD_ADMIN);
		securityClient.login();
		
		Autohersteller autohersteller = new Autohersteller();
		autohersteller.setName(name);
		autohersteller = av.createAutohersteller(autohersteller, LOCALE);
		assertThat(autohersteller.getId().longValue() > 0, is(true));
		trans.commit();
		
		ArtikelverwaltungConcurrencyHelper concurrentUpdate = 
				new ArtikelverwaltungConcurrencyHelper(Cmd.UPDATE, autohersteller.getId());
		final ExecutorService executorService = Executors.newSingleThreadExecutor();
		final Future<Void> future = executorService.submit(concurrentUpdate);
		future.get();
		
		trans.begin();
		autohersteller.setName(name + nameUpdated);
		
		try {
			av.updateAutohersteller(autohersteller, LOCALE);
			fail("ConcurrentUpdateException wurde nicht geworfen!");
		}
		catch (ConcurrentUpdatedException e) {
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
			throws RollbackException, HeuristicMixedException, 
				   HeuristicRollbackException, SystemException, InterruptedException,
				   ExecutionException, NotSupportedException, LoginException {
		LOGGER.debug("BEGINN updateDeleteFahrzeug");	
		
		final Long id = Long.valueOf(7001);
		final Autohersteller autohersteller = av.findAutoherstellerById(id);
		final String modell = MODELL_NAME_NEU;
		final String modellUpdate = "updated";
		
		securityClient.logout();
		securityClient.setSimple(USERNAME_ADMIN, PASSWORD_ADMIN);
		securityClient.login();
		
		Fahrzeug fahrzeug = new Fahrzeug();
		fahrzeug.setHersteller(autohersteller);
		fahrzeug.setModell(modell);
		fahrzeug = av.createFahrzeug(fahrzeug, LOCALE);
		assertThat(fahrzeug.getId().longValue() > 0, is(true));
		trans.commit();
		
		ArtikelverwaltungConcurrencyHelper concurrentDelete = 
				new ArtikelverwaltungConcurrencyHelper(Cmd.DELETE, fahrzeug.getId());
		final ExecutorService executorService = Executors.newSingleThreadExecutor();
		final Future<Void> future = executorService.submit(concurrentDelete);
		future.get();
		
		trans.begin();
		fahrzeug.setModell(modell + modellUpdate);
		
		thrown.expect(ConcurrentDeletedException.class);
		av.updateFahrzeug(fahrzeug, LOCALE);
		
		LOGGER.debug("ENDE updateDeleteFahrzeug");

	}
	
//Test update Autohersteller, der nicht existiert	
	@Test
	public void updateDeleteAutohersteller()
			throws RollbackException, HeuristicMixedException, 
				   HeuristicRollbackException, SystemException,
				   InterruptedException, ExecutionException, 
				   NotSupportedException, LoginException {
		LOGGER.debug("BEGINN updateDeleteAutohersteller");		
		
		final String name = AUTOHERSTELLER_NAME;
		final String nameUpdated = "updated";
		
		securityClient.logout();
		securityClient.setSimple(USERNAME_ADMIN, PASSWORD_ADMIN);
		securityClient.login();
		
		Autohersteller autohersteller = new Autohersteller();
		autohersteller.setName(name);
		autohersteller = av.createAutohersteller(autohersteller, LOCALE);
		assertThat(autohersteller.getId().longValue() > 0, is(true));
		trans.commit();
		
		ArtikelverwaltungConcurrencyHelper concurrentDelete = 
				new ArtikelverwaltungConcurrencyHelper(Cmd.DELETE, autohersteller.getId());
		final ExecutorService executorService = Executors.newSingleThreadExecutor();
		final Future<Void> future = executorService.submit(concurrentDelete);
		future.get();
		
		trans.begin();
		autohersteller.setName(name + nameUpdated);
		
		thrown.expect(ConcurrentDeletedException.class);
		av.updateAutohersteller(autohersteller, LOCALE);
		
		LOGGER.debug("ENDE updateDeleteAutohersteller");

	}

//Test delete Fahrzeug, der upgedated wird	
	@Test
	public void deleteUpdateFahrzeug()
			throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
				   SystemException, InterruptedException, ExecutionException,
				   LoginException, NotSupportedException {
		LOGGER.debug("BEGINN deleteUpdateFahrzeug");

		final Long id = Long.valueOf(7002);
		final Autohersteller autohersteller = av.findAutoherstellerById(id);
		final String modell = MODELL_NAME_NEU;
		
		securityClient.logout();
		securityClient.setSimple(USERNAME_ADMIN, PASSWORD_ADMIN);
		securityClient.login();
		
		Fahrzeug fahrzeug = new Fahrzeug();
		fahrzeug.setHersteller(autohersteller);
		fahrzeug.setModell(modell);
		fahrzeug = av.createFahrzeug(fahrzeug, LOCALE);
		assertThat(fahrzeug.getId().longValue() > 0, is(true));
		trans.commit();
		
		ArtikelverwaltungConcurrencyHelper concurrentUpdate = 
				new ArtikelverwaltungConcurrencyHelper(Cmd.UPDATE, fahrzeug.getId());
		final ExecutorService executorService = Executors.newSingleThreadExecutor();
		final Future<Void> future = executorService.submit(concurrentUpdate);
		future.get();

		trans.begin();
		av.deleteFahrzeug(fahrzeug);
		
		final Fahrzeug tmp = av.findFahrzeugById(fahrzeug.getId());
		assertThat(tmp, is(nullValue()));
		
		LOGGER.debug("ENDE deleteUpdateFahrzeug");
		

	}

//Test delete Autohersteller, der upgedated wird	
	@Test
	public void deleteUpdateAutohersteller()
			throws RollbackException, HeuristicMixedException,
				   HeuristicRollbackException, SystemException,
				   InterruptedException, ExecutionException,
				   LoginException, NotSupportedException {
		LOGGER.debug("BEGINN deleteUpdateAutohersteller");
		final String name = AUTOHERSTELLER_NAME;

		securityClient.logout();
		securityClient.setSimple(USERNAME_ADMIN, PASSWORD_ADMIN);
		securityClient.login();
		
		Autohersteller autohersteller = new Autohersteller();
		autohersteller.setName(name);
		autohersteller = av.createAutohersteller(autohersteller, LOCALE);
		assertThat(autohersteller.getId().longValue() > 0, is(true));
		trans.commit();
		
		ArtikelverwaltungConcurrencyHelper concurrentUpdate = 
				new ArtikelverwaltungConcurrencyHelper(Cmd.UPDATE, autohersteller.getId());
		final ExecutorService executorService = Executors.newSingleThreadExecutor();
		final Future<Void> future = executorService.submit(concurrentUpdate);
		future.get();
		
		trans.begin();
		av.deleteAutohersteller(autohersteller);
		
		final Autohersteller tmp = av.findAutoherstellerById(autohersteller.getId());
		assertThat(tmp, is(nullValue()));
		
		LOGGER.debug("ENDE deleteUpdateAutohersteller");

		}

	private class ArtikelverwaltungConcurrencyHelper extends
			AbstractConcurrencyHelper {
		private final Long artikelId;
		private Long fahrzeugId;
		private Long autoherstellerId;
		private final Long min = Long.valueOf(6000);
		private final Long max = Long.valueOf(7000);
	
		protected ArtikelverwaltungConcurrencyHelper(Cmd cmd, Long id) {
			super(cmd);
			artikelId = id;
		}
	
		@Override
		protected void update() {
			LOGGER.debug("BEGINN update");
	
			try {
				
				if (artikelId > min && artikelId < max) {
					fahrzeugId = artikelId;
					final Fahrzeug fahrzeug = av.findFahrzeugById(fahrzeugId);
					fahrzeug.setModell(fahrzeug.getModell() + "concurrent");
					av.updateFahrzeug(fahrzeug, LOCALE);
				}
				else {
					autoherstellerId = artikelId;
					final Autohersteller autohersteller = 
							av.findAutoherstellerById(autoherstellerId);
					autohersteller.setName(autohersteller.getName() + "concurrent");
					av.updateAutohersteller(autohersteller, LOCALE);
				}
			}
			catch (ConcurrentUpdatedException |
				   ConcurrentDeletedException | 
				   ArtikelValidationException e) {
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
				if (artikelId > min && artikelId < max) {
					fahrzeugId = artikelId;
				av.deleteFahrzeug(av.findFahrzeugById(fahrzeugId));
				}
				else {
					autoherstellerId = artikelId;
				av.deleteAutohersteller(av.findAutoherstellerById(autoherstellerId));
				}
			}

			catch (ArtikelValidationException e) {
				throw new IllegalStateException(e);
			}
			
			LOGGER.debug("ENDE delete");
		}
	}
}
