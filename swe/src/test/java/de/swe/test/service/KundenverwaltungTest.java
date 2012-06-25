package de.swe.test.service;

import static de.swe.kundenverwaltung.domain.AbstractKunde.FIRMENKUNDE;
import static de.swe.kundenverwaltung.domain.AbstractKunde.PRIVATKUNDE;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.either;

import java.lang.invoke.MethodHandles;
import java.util.List;

import javax.inject.Inject;
import javax.security.auth.login.LoginException;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.logging.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.swe.bestellverwaltung.domain.Bestellung;
import de.swe.kundenverwaltung.dao.KundenverwaltungDao.Fetch;
import de.swe.kundenverwaltung.dao.KundenverwaltungDao.Order;
import de.swe.kundenverwaltung.domain.AbstractKunde;
import de.swe.kundenverwaltung.domain.Adresse;
import de.swe.kundenverwaltung.domain.Privatkunde;
import de.swe.kundenverwaltung.service.EmailExistsException;
import de.swe.kundenverwaltung.service.KundeValidationException;
import de.swe.kundenverwaltung.service.Kundenverwaltung;
import de.swe.test.util.AbstractTest;

@RunWith(Arquillian.class)
public class KundenverwaltungTest extends AbstractTest {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());
	
	private static final Long KUNDE_ID_VORHANDEN = Long.valueOf(1007);
	private static final Long KUNDE_ID_NICHT_VORHANDEN = Long.valueOf(0);
	private static final String EMAIL_VORHANDEN = "abc@def.de";
	private static final String EMAIL_NICHT_VORHANDEN = "abc@def.com";
	private static final String PLZ_VORHANDEN = "76137";
	private static final String PLZ_NICHT_VORHANDEN = "99999";
	private static final String NACHNAME_VORHANDEN = "Abc";
	private static final String NACHNAME_NICHT_VORHANDEN = "Abcd";
	private static final String NACHNAME_NEU = "Null";
	private static final String EMAIL_NEU = "dave@null.de";
	private static final String NACHNAME2_NEU = "Test";
	private static final String EMAIL2_NEU = "test@neu.de";
	private static final Long KUNDE_ID_OHNE_BESTELLUNG = Long.valueOf(1006);
	private static final String STRASSE_NEU = "Kaiserstraße";
	private static final String HAUSNR_NEU = "20";
	private static final String PLZ_NEU = "76133";
	private static final String ORT_NEU = "Karlsruhe";
	
	@Inject
	private Kundenverwaltung kv;
	
	@Test
	public void findKundeByIdVorhanden() {
		LOGGER.debug("BEGINN findKundeByIdVorhanden");
		
		final Long id = KUNDE_ID_VORHANDEN;
		final AbstractKunde kunde = kv.findKundeById(id, Fetch.NUR_KUNDE);
		
		assertThat(kunde.getId(), is(id));

		LOGGER.debug("ENDE findKundeByIdVorhanden");
	}
	
	@Test
	public void findKundeByIdNichtVorhanden() {
		final Long id = KUNDE_ID_NICHT_VORHANDEN;
		final AbstractKunde kunde = kv.findKundeById(id, Fetch.NUR_KUNDE);
		
		assertThat(kunde, is(nullValue()));
	}
	
	@Test
	public void findKundeByEmailVorhanden() {
		final String email = EMAIL_VORHANDEN;
		final AbstractKunde kunde = kv.findKundeByEmail(email, Fetch.NUR_KUNDE);
		
		assertThat(kunde.getEmail(), is(email));
	}
	
	@Test
	public void findKundeByEmailNichtVorhanden() {
		final String email = EMAIL_NICHT_VORHANDEN;
		final AbstractKunde kunde = kv.findKundeByEmail(email, Fetch.NUR_KUNDE);
		
		assertThat(kunde, is(nullValue()));
	}
	
	@Test
	public void findKundenByPLZVorhanden() {
		final String plz = PLZ_VORHANDEN;
		final List<AbstractKunde> kunden = kv.findKundenByPLZ(plz);
		
		for (AbstractKunde k : kunden) {
			assertThat(k.getAdresse().getPlz(), is(plz));
		}
	}
	
	@Test
	public void findKundenByPLZNichtVorhanden() {
		final String plz = PLZ_NICHT_VORHANDEN;
		final List<AbstractKunde> kunden = kv.findKundenByPLZ(plz);
		
		assertThat(kunden.isEmpty(), is(true));
	}
	
	@Test
	public void findKundenByNachnameVorhanden() {
		final String nachname = NACHNAME_VORHANDEN;
		final List<AbstractKunde> kunden = kv.findKundenByNachname(nachname, Fetch.NUR_KUNDE);
		
		for (AbstractKunde k : kunden) {
			assertThat(k.getNachname(), is(nachname));
			assertThat(k.getArt(), either(is(PRIVATKUNDE)).or(is(FIRMENKUNDE)));
		}
		
		final List<AbstractKunde> kundenMitBestellungen =
				kv.findKundenByNachname(nachname, Fetch.MIT_BESTELLUNG);
		
		for (AbstractKunde k : kundenMitBestellungen) {
			assertThat(k.getNachname(), is(nachname));
			
			final List<Bestellung> bestellungen = k.getBestellungen();
			
			if (bestellungen == null || bestellungen.isEmpty()) {
				continue;
			}
			
			assertThat(bestellungen.isEmpty(), is(false));
		}
	}
	
	@Test
	public void findKundenBynachnameNichtVorhanden() {
		final String nachname = NACHNAME_NICHT_VORHANDEN;
		final List<AbstractKunde> kunden = kv.findKundenByNachname(nachname, Fetch.NUR_KUNDE);
		
		assertThat(kunden.isEmpty(), is(true));
	}
	
	@Test
	public void createPrivatkunde() throws LoginException {
		final String nachname = NACHNAME_NEU;
		final String email = EMAIL_NEU;
		final String strasse = STRASSE_NEU;
		final String hausnr = HAUSNR_NEU;
		final String plz = PLZ_NEU;
		final String ort = ORT_NEU;
		final Adresse adresse = new Adresse(strasse, hausnr, plz, ort);
		final Privatkunde kunde = new Privatkunde(nachname, email, adresse);
		kunde.setAgbAkzeptiert(true);
		
		final List<AbstractKunde> kundenVorher = kv.findAllKunden(Fetch.NUR_KUNDE, Order.ID);
		AbstractKunde neuerKunde = kv.createKunde(kunde, LOCALE);
		final List<AbstractKunde> kundenNachher = kv.findAllKunden(Fetch.NUR_KUNDE, Order.ID);
		
		assertThat(kundenVorher.size() + 1, is(kundenNachher.size()));
		
		neuerKunde = kv.findKundeById(neuerKunde.getId(), Fetch.NUR_KUNDE);
		
		assertThat(neuerKunde.getNachname(), is(NACHNAME_NEU));
		assertThat(neuerKunde.getEmail(), is(EMAIL_NEU));
		assertThat(neuerKunde.getArt(), is(PRIVATKUNDE));
		assertThat(neuerKunde.getAdresse().getStrasse(), is(strasse));
		assertThat(neuerKunde.getAdresse().getHausnr(), is(hausnr));
		assertThat(neuerKunde.getAdresse().getPlz(), is(plz));
		assertThat(neuerKunde.getAdresse().getOrt(), is(ort));
	}
	
	@Test
	public void createPrivatkundeOhneAdresse()
			throws LoginException {
		final String nachname = NACHNAME2_NEU;
		final String email = EMAIL2_NEU;
		final Privatkunde kunde = new Privatkunde();
		kunde.setNachname(nachname);
		kunde.setEmail(email);
		kunde.setAgbAkzeptiert(true);

		thrown.expect(KundeValidationException.class);
		kv.createKunde(kunde, LOCALE);
	}
	
	@Test
	public void createDuplikatPrivatkunde()
			throws LoginException {
		final Long id = KUNDE_ID_VORHANDEN;
		
		final AbstractKunde vorhandenerKunde = kv.findKundeById(id, Fetch.NUR_KUNDE);
		assertThat(vorhandenerKunde, is(notNullValue()));
		assertThat(vorhandenerKunde, is(instanceOf(Privatkunde.class)));
		
		final AbstractKunde neuerKunde = new Privatkunde();
		neuerKunde.setNachname(vorhandenerKunde.getNachname());
		neuerKunde.setEmail(vorhandenerKunde.getEmail());
		neuerKunde.setAdresse(vorhandenerKunde.getAdresse());
		neuerKunde.setAgbAkzeptiert(true);
		
		thrown.expect(EmailExistsException.class);
		kv.createKunde(neuerKunde, LOCALE);
	}
	
	@Test
	public void createPrivatkundeFalschesPassword()
			throws LoginException {
		final String nachname = NACHNAME_NICHT_VORHANDEN;
		final String email = EMAIL_NICHT_VORHANDEN;
		
		final Adresse adresse = new Adresse("Kaiserstraße", "20", "76133", "Karlsruhe");
		final Privatkunde neuerPrivatkunde = new Privatkunde(nachname, email, adresse);
		neuerPrivatkunde.setPassword("12345");
		neuerPrivatkunde.setPasswordWdh("112345");

		thrown.expect(KundeValidationException.class);
		thrown.expectMessage("kundenverwaltung.kunde.password.notEqual");
		kv.createKunde(neuerPrivatkunde, LOCALE);
	}
	
	@Test
	public void deleteKunde() throws LoginException {
		final Long id = KUNDE_ID_OHNE_BESTELLUNG;

		securityClient.logout();
		securityClient.setSimple(USERNAME_ADMIN, PASSWORD_ADMIN);
		securityClient.login();
		
		final List<AbstractKunde> kundenVorher = kv.findAllKunden(Fetch.NUR_KUNDE, Order.ID);
		
		final AbstractKunde kunde = kv.findKundeById(id, Fetch.MIT_BESTELLUNG);
		assertThat(kunde, is(notNullValue()));
		assertThat(kunde.getBestellungen().isEmpty(), is(true));
		
		kv.deleteKunde(kunde);
		
		final List<AbstractKunde> kundenNachher = kv.findAllKunden(Fetch.NUR_KUNDE, Order.ID);
		assertThat(kundenVorher.size() - 1, is(kundenNachher.size()));
	}
	
	@Test
	public void neuerNameFuerKunde() throws LoginException {
		final Long id = KUNDE_ID_VORHANDEN;
		
		AbstractKunde kunde = kv.findKundeById(id, Fetch.NUR_KUNDE);
		
		final String alterNachname = kunde.getNachname();
		final String neuerName = alterNachname + alterNachname.charAt(alterNachname.length() - 1);
		kunde.setNachname(neuerName);
		
		kunde = kv.updateKunde(kunde, LOCALE);
		assertThat(kunde.getNachname(), is(neuerName));
		
		kunde = kv.findKundeById(id, Fetch.NUR_KUNDE);
		assertThat(kunde.getNachname(), is(neuerName));
	}

//	@Test
//	public void neueEmailFuerKunde() throws LoginException {
//		final String email = EMAIL_VORHANDEN;
//		
//		AbstractKunde kunde = kv.findKundeByEmail(email, Fetch.NUR_KUNDE);
//		
//		final String alteEmail = kunde.getEmail();
//		final String neueEmail = alteEmail + alteEmail.charAt(alteEmail.length() - 1);
//		kunde.setNachname(neueEmail);
//		
//		kunde = kv.updateKunde(kunde, LOCALE);
//		assertThat(kunde.getEmail(), is(neueEmail));
//		
//		kunde = kv.findKundeByEmail(email, Fetch.NUR_KUNDE);;
//		assertThat(kunde.getEmail(), is(neueEmail));
//	}
}
