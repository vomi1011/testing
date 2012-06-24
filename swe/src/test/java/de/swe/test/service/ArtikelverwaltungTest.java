package de.swe.test.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import javax.inject.Inject;
import javax.security.auth.login.LoginException;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.swe.artikelverwaltung.domain.Autohersteller;
import de.swe.artikelverwaltung.domain.Fahrzeug;
import de.swe.artikelverwaltung.service.ArtikelValidationException;
import de.swe.artikelverwaltung.service.ArtikelValidationExceptionAH;
import de.swe.artikelverwaltung.service.Artikelverwaltung;
import de.swe.artikelverwaltung.service.ArtikelverwaltungDao.Order;
import de.swe.test.util.AbstractTest;

// TODO Tests schreiben

@RunWith(Arquillian.class)
public class ArtikelverwaltungTest extends AbstractTest {
	private static final long FID_VORHANDEN = 6002;
	private static final long AID_VORHANDEN = 7001;
	private static final long FID_NICHT_VORHANDEN = 1;
	private static final long AID_NICHT_VORHANDEN = 1;
	private static final String AUTOHERSTELLER_NAME = "Audi";
	private static final String AUTOHERSTELLER_NAME2 = "Eclipse";
	private static final String NAME_NICHT_VORHANDEN = "1";
	private static final String MODELL = "Modell 3";
	private static final String MODELL2 = "Modell 2";

	@Inject
	private Artikelverwaltung av;
	
	@Ignore
	@Test
	public void findFahrzeugByIdVorhanden() {
		
		final Long fid = FID_VORHANDEN;
		
		Fahrzeug fahrzeug = av.findFahrzeugById(fid);
		assertThat(fahrzeug.getId(), is(fid));
	}
	@Ignore
	@Test
	public void findAutoherstellerByIdVorhanden() {
		
		final Long aId = AID_VORHANDEN;
		
		Autohersteller autohersteller = av.findAutoherstellerById(aId);
		assertThat(autohersteller.getId(), is(aId));
	}
	
	@Test
	public void findFahrzeugByIdNichtVorhanden() {
		
		final Long fid = FID_NICHT_VORHANDEN;
		
		Fahrzeug fahrzeug = av.findFahrzeugById(fid);
		assertThat(fahrzeug, is(nullValue()));
	} 
	
	@Test
	public void findAutoherstellerByIdNichtVorhanden() {
		
		final Long aId = AID_NICHT_VORHANDEN;
		
		Autohersteller autohersteller = av.findAutoherstellerById(aId);
		assertThat(autohersteller, is(nullValue()));
	}
	
	@Test
	public void findAutoherstellerByNameVorhanden() {
		
		final String name = AUTOHERSTELLER_NAME;
		
		Autohersteller autohersteller = av.findAutoherstellerByName(name);
		assertThat(autohersteller.getName(), is(name));
	}

	@Test
	public void findAutoherstellerByNameNichtVorhanden() {
		
		final String name = NAME_NICHT_VORHANDEN;
		
		Autohersteller autohersteller = av.findAutoherstellerByName(name);
		assertThat(autohersteller, is(nullValue()));
	}
	
	@Test
	public void findFahrzeugByIdHerstellerVorhanden() {
		
		final Long herstellerfk = AID_VORHANDEN;
		
		List<Fahrzeug> fahrzeuge = av.findFahrzeugByHerstellerId(herstellerfk);
		assertThat(fahrzeuge.isEmpty(), is(false));
		
		for (Fahrzeug fahrzeug : fahrzeuge) {
			assertThat(fahrzeug.getHersteller().getId(), is(herstellerfk));
		}
	}
	
	@Test
	public void findFahrzeugByIdHerstellerNichtVorhanden() {
		
		final Long herstellerfk = AID_NICHT_VORHANDEN;
		
		List<Fahrzeug> hersteller = av.findFahrzeugByHerstellerId(herstellerfk);	
		assertThat(hersteller.isEmpty(), is(true));
	}
	
	@Test
	public void createFahrzeug() throws LoginException {
		final String modell = MODELL;
		final Long id = Long.valueOf(7001);
		List<Fahrzeug> vorhandeneFahrzeuge = av.findAllFahrzeuge(Order.ID);

		final Autohersteller hersteller = av.findAutoherstellerById(id);
		assertThat(hersteller, is(notNullValue()));
		
		securityClient.logout();
		securityClient.setSimple(USERNAME_ADMIN, PASSWORD_ADMIN);
		securityClient.login();
		
		Fahrzeug neuesFahrzeug = new Fahrzeug();
		neuesFahrzeug.setModell(modell);
		neuesFahrzeug.setHersteller(hersteller);

		neuesFahrzeug = av.createFahrzeug(neuesFahrzeug, LOCALE);
		List<Fahrzeug> aktuelleFahrzeuge = av.findAllFahrzeuge(Order.ID);
		assertThat(vorhandeneFahrzeuge.size() + 1, is(aktuelleFahrzeuge.size()));
		assertThat(aktuelleFahrzeuge.contains(neuesFahrzeug), is(true));
	}

	@Ignore
	@Test
	public void createFahrzeugOhneHersteller() {
		final String modell = MODELL2;
		
		
		
		final Fahrzeug neuesFahrzeug = new Fahrzeug();
		neuesFahrzeug.setModell(modell);
		
		thrown.expect(ArtikelValidationException.class);
		av.createFahrzeug(neuesFahrzeug, LOCALE);
	}
	
	@Test
	public void updateFahrzeug() throws LoginException {
		final Long id = FID_VORHANDEN;
		final String modell = MODELL;
		
		securityClient.logout();
		securityClient.setSimple(USERNAME_ADMIN, PASSWORD_ADMIN);
		securityClient.login();
		
		Fahrzeug fahrzeug = av.findFahrzeugById(id);
		fahrzeug.setModell(modell);
		fahrzeug = av.updateFahrzeug(fahrzeug, LOCALE);
		assertThat(fahrzeug.getModell(), is(modell));
		
	}
	
	@Test
	public void deleteFahrzeug() throws LoginException {
		final Long id = FID_VORHANDEN;
		
		securityClient.logout();
		securityClient.setSimple(USERNAME_ADMIN, PASSWORD_ADMIN);
		securityClient.login();
		
		Fahrzeug fahrzeug = av.findFahrzeugById(id);
		av.deleteFahrzeug(fahrzeug);
		
		fahrzeug = av.findFahrzeugById(id);
		assertThat(fahrzeug, is(nullValue()));
		
	}
	
	@Test
	public void createAutohersteller() throws LoginException {
		final String name = AUTOHERSTELLER_NAME2;
		List<Autohersteller> vorhandeneAutohersteller = av.findAllAutohersteller(Order.ID);
		
		securityClient.logout();
		securityClient.setSimple(USERNAME_ADMIN, PASSWORD_ADMIN);
		securityClient.login();
		
		Autohersteller neuesAutohersteller = new Autohersteller();
		assertThat(neuesAutohersteller, is(notNullValue()));
		neuesAutohersteller.setName(name);		
		neuesAutohersteller = av.createAutohersteller(neuesAutohersteller, LOCALE);
		List <Autohersteller> aktuelleAutohersteller = av.findAllAutohersteller(Order.ID);
		assertThat(vorhandeneAutohersteller.size() + 1, is(aktuelleAutohersteller.size()));
		assertThat(aktuelleAutohersteller.contains(neuesAutohersteller), is(true));					
	}
	
	@Ignore
	@Test
	public void createAutoherstellerOhneName() {
		Autohersteller neuesAutohersteller = new Autohersteller();
		neuesAutohersteller.setName(null);
		
		thrown.expect(ArtikelValidationExceptionAH.class);
		av.createAutohersteller(neuesAutohersteller, LOCALE);
			
	}
	
	@Test
	public void updateAutohersteller() throws LoginException {
		final Long id = AID_VORHANDEN;
		final String name = AUTOHERSTELLER_NAME2;
		
		securityClient.logout();
		securityClient.setSimple(USERNAME_ADMIN, PASSWORD_ADMIN);
		securityClient.login();
		
		Autohersteller autohersteller = av.findAutoherstellerById(id);
		autohersteller.setName(name);
		autohersteller = av.updateAutohersteller(autohersteller, LOCALE);
		assertThat(autohersteller.getName(), is(name));
		
	}
	
	@Test
	public void deleteAutohersteller() throws LoginException {
		final Long id = AID_VORHANDEN;
		Autohersteller autohersteller = av.findAutoherstellerById(id);
		
		securityClient.logout();
		securityClient.setSimple(USERNAME_ADMIN, PASSWORD_ADMIN);
		securityClient.login();
		
		av.deleteAutohersteller(autohersteller);
		autohersteller = av.findAutoherstellerById(id);
		assertThat(autohersteller, is(nullValue()));
	}	
}
