package de.swe.test.service;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.lang.invoke.MethodHandles;
import java.util.GregorianCalendar;
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

import de.swe.artikelverwaltung.domain.Fahrzeug;
import de.swe.artikelverwaltung.service.Artikelverwaltung;
import de.swe.bestellverwaltung.domain.Bestellposition;
import de.swe.bestellverwaltung.domain.Bestellung;
import de.swe.bestellverwaltung.domain.Bestellung.Status;
import de.swe.bestellverwaltung.service.BestellungValidationException;
import de.swe.bestellverwaltung.service.Bestellverwaltung;
import de.swe.kundenverwaltung.dao.KundenverwaltungDao.Fetch;
import de.swe.kundenverwaltung.domain.AbstractKunde;
import de.swe.kundenverwaltung.service.Kundenverwaltung;
import de.swe.test.util.AbstractConcurrencyHelper;
import de.swe.test.util.AbstractConcurrencyHelper.Cmd;
import de.swe.test.util.AbstractTest;
import de.swe.util.ConcurrentDeletedException;
import de.swe.util.ConcurrentUpdatedException;

@RunWith(Arquillian.class)
public class BestellverwaltungConcurrencyTest extends AbstractTest {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());

	private static final Long KUNDE_ID_VORHANDEN = Long.valueOf(1001);
	private static final Long KUNDE_ID_VORHANDEN2 = Long.valueOf(1002);
	private static final Long FAHRZEUGID_VORHANDEN = Long.valueOf(6004);
	private static final Short PRODUKT_X_ANZAHL = 1;
	private static final Status STATUS = Bestellung.Status.NEU;
	private static final GregorianCalendar DATUM_NEU = new GregorianCalendar(2011, 10, 01);
	
	@Inject
	private Bestellverwaltung bv;
	
	@Inject
	private Artikelverwaltung av;
	
	@Inject
	private Kundenverwaltung kv;
	
	@Test
	public void updateUpdateBestellung() throws 
	NotSupportedException, SystemException, LoginException, 
	RollbackException, HeuristicMixedException, 
	HeuristicRollbackException, InterruptedException, ExecutionException {
		LOGGER.debug("BEGINN updateUpdateBestellung");
		
		
		final Long kundeId = KUNDE_ID_VORHANDEN;
		final Long fahrzeugId1 = FAHRZEUGID_VORHANDEN;
		final short artikel1Anzahl = PRODUKT_X_ANZAHL;
		final Status status = STATUS;

		securityClient.logout();
		securityClient.setSimple(USERNAME_ADMIN, PASSWORD_ADMIN);
		securityClient.login();
		
		Bestellung bestellung = new Bestellung();
		bestellung.setBestelldatum(DATUM_NEU.getTime());
		bestellung.setStatus(status);
		
		Fahrzeug fahrzeug1 = av.findFahrzeugById(fahrzeugId1);
		Bestellposition bpos1 = new Bestellposition(fahrzeug1, artikel1Anzahl);
		bestellung.addBestellposition(bpos1);
		
		AbstractKunde kunde = kv.findKundeById(kundeId, Fetch.MIT_BESTELLUNG);
		
		bestellung = bv.createBestellung(bestellung, kunde, LOCALE);
		assertThat(bestellung.getBestellpositionen().size(), is(1));
		trans.commit();
			
		BestellverwaltungConcurrencyHelper concurrentUpdate = 
				new BestellverwaltungConcurrencyHelper(Cmd.UPDATE, bestellung.getId());
		final ExecutorService executorService = Executors.newSingleThreadExecutor();
		final Future<Void> future = executorService.submit(concurrentUpdate);
		future.get();
		
		trans.begin();
		bestellung.setStatus(Status.BEARBEITET);
		
		try {
			bv.updateBestellung(bestellung, LOCALE);
			fail("ConcurrentUpdateException wurde nicht geworfen!");
		}
		catch (ConcurrentUpdatedException e) {
			trans.rollback();
			
			securityClient.logout();
			securityClient.setSimple(USERNAME_ADMIN, PASSWORD_ADMIN);
			securityClient.login();
			
			trans.begin();
			bv.deleteBestellung(bestellung);
		}
		
		LOGGER.debug("ENDE updateUpdateBestellung");
	}
	
	@Test
	public void deleteUpdateBestellung() throws 
	NotSupportedException, SystemException, LoginException, 
	RollbackException, HeuristicMixedException, 
	HeuristicRollbackException, InterruptedException, ExecutionException {
		LOGGER.debug("BEGINN deleteUpdateBestellung");
		
		
		final Long kundeId = KUNDE_ID_VORHANDEN2;
		final Long fahrzeugId1 = FAHRZEUGID_VORHANDEN;
		final short artikel1Anzahl = PRODUKT_X_ANZAHL;
		final Status status = STATUS;

		securityClient.logout();
		securityClient.setSimple(USERNAME_ADMIN, PASSWORD_ADMIN);
		securityClient.login();
		
		Bestellung bestellung = new Bestellung();
		bestellung.setBestelldatum(DATUM_NEU.getTime());
		bestellung.setStatus(status);
		
		Fahrzeug fahrzeug1 = av.findFahrzeugById(fahrzeugId1);
		Bestellposition bpos1 = new Bestellposition(fahrzeug1, artikel1Anzahl);
		bestellung.addBestellposition(bpos1);
		
		AbstractKunde kunde = kv.findKundeById(kundeId, Fetch.MIT_BESTELLUNG);
		
		bestellung = bv.createBestellung(bestellung, kunde, LOCALE);
		assertThat(bestellung.getBestellpositionen().size(), is(1));
		trans.commit();
			
		BestellverwaltungConcurrencyHelper concurrentDelete = 
				new BestellverwaltungConcurrencyHelper(Cmd.DELETE, bestellung.getId());
		final ExecutorService executorService = Executors.newSingleThreadExecutor();
		final Future<Void> future = executorService.submit(concurrentDelete);
		future.get();
		
		trans.begin();
		bestellung.setStatus(Status.BEARBEITET);
		
		thrown.expect(ConcurrentDeletedException.class);
		bv.updateBestellung(bestellung, LOCALE);
		
		LOGGER.debug("BEGINN deleteUpdateBestellung");
	}
	
	
	private class BestellverwaltungConcurrencyHelper extends
	AbstractConcurrencyHelper {
		private Long bestellId;

		protected BestellverwaltungConcurrencyHelper(Cmd cmd, Long id) {
			super(cmd);
			bestellId = id;
		}

		@Override
		protected void update() {
			LOGGER.debug("BEGINN update");
			
			try {
				final Bestellung bestellung = bv.findBestellungById(bestellId);
				bestellung.setStatus(Status.BEARBEITET);
				bv.updateBestellung(bestellung, LOCALE);
			}
			catch (ConcurrentUpdatedException |
				   ConcurrentDeletedException | 
				   BestellungValidationException e) {
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
				final Bestellung bestellung = bv.findBestellungById(bestellId);
				bv.deleteBestellung(bestellung);
			}
			catch (BestellungValidationException e) {
				throw new IllegalStateException(e);
			}
			
			LOGGER.debug("ENDE delete");
			
		}
		
		
	}
}
