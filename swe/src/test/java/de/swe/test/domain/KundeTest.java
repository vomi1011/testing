package de.swe.test.domain;

import static de.swe.util.Constants.ADRESS_ID;
import static de.swe.util.Constants.KUNDEN_ID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.swe.kundenverwaltung.domain.AbstractKunde;
import de.swe.kundenverwaltung.domain.Adresse;
import de.swe.kundenverwaltung.domain.Firmenkunde;
import de.swe.kundenverwaltung.domain.Privatkunde;
import de.swe.test.util.AbstractTest;

@RunWith(Arquillian.class)
public class KundeTest extends AbstractTest {
	private static final Long ID_VORHANDEN = Long.valueOf(1001);
	private static final Long ID_NICHT_VORHANDEN = Long.valueOf(1000);
	private static final String NACHNAME_VORHANDEN = "Abc";
	private static final String NACHNAME_NICHT_VORHANDEN = "Nicht vorhanden";
	private static final String PLZ_VORHANDEN = "76133";
	private static final String PLZ_NICHT_VORHANDEN = "76139";
	private static final String EMAIL_VORHANDEN = "abc@def.de";
	private static final String EMAIL_NICHT_VORHANDEN = "abc@ghi.de";
	private static final String ERSTELLT_VORHANDEN = "2011-10-11";
	private static final String ERSTELLT_NICHT_VORHANDEN = "2011-05-31";
	
	private static final String PRIVATKUNDE_NACHNAME_NEU = "Null";
	private static final String PRIVATKUNDE_VORNAME_NEU = "Dave";
	private static final String PRIVATKUNDE_EMAIL_NEU = "dave@null.com";
	private static final String FIRMENKUNDE_NACHNAME_NEU = "Bcd";
	private static final String FIRMENKUNDE_VORNAME_NEU = "Bob";
	private static final String FIRMENKUNDE_EMAIL_NEU = "bob@firmab.com";
	private static final String FIRMENKUNDE_FIRMA_NEU = "Firma B";
	private static final String PLZ_NEU = "12345";
	private static final String ORT_NEU = "Abc Ort";
	private static final String STRASSE_NEU = "Abc Weg";
	private static final String HAUSNR_NEU = "1";

	@Test
	public void findKundenByIdVorhanden() {
		final Long id = ID_VORHANDEN;
		
		final AbstractKunde kunde = em.find(AbstractKunde.class, id);
		assertThat(kunde.getId(), is(id));
	}
	
	@Test
	public void findKundenByIdNichtVorhanden() {
		final Long id = ID_NICHT_VORHANDEN;
		
		final AbstractKunde kunde = em.find(AbstractKunde.class, id);
		
		assertThat(kunde, is(nullValue()));
	}

	@Test
	public void findKundenByNachnameVorhanden() {
		final String nachname = NACHNAME_VORHANDEN;
		
		final List<AbstractKunde> kunden = em.createNamedQuery(AbstractKunde.FIND_KUNDEN_BY_NACHNAME, AbstractKunde.class)
				.setParameter(AbstractKunde.PARAM_KUNDE_NACHNAME, nachname)
				.getResultList();
		
		assertThat(kunden.isEmpty(), is(false));
		for (AbstractKunde k : kunden) {
			assertThat(k.getNachname(), is(nachname));
		}
	}
	
	@Test
	public void findKundenByNachnameNichtVorhanden() {
		String nachname = NACHNAME_NICHT_VORHANDEN;
		
		final List<AbstractKunde> kunden = em.createNamedQuery(AbstractKunde.FIND_KUNDEN_BY_NACHNAME, AbstractKunde.class)
				.setParameter(AbstractKunde.PARAM_KUNDE_NACHNAME, nachname)
				.getResultList();
		
		assertThat(kunden.isEmpty(), is(true));
	}
	
	@Test
	public void findKundenByPlzVorhanden() {
		final String plz = PLZ_VORHANDEN;
		
		final List<AbstractKunde> kunden = em.createNamedQuery(AbstractKunde.FIND_KUNDEN_BY_PLZ, AbstractKunde.class)
        		.setParameter(AbstractKunde.PARAM_KUNDE_ADRESSE_PLZ, plz)
        		.getResultList();
		
		assertThat(kunden.isEmpty(), is(false));
		for (AbstractKunde k : kunden) {
			assertThat(k.getAdresse().getPlz(), is(plz));
		}
	}
	
	@Test
	public void findKundenByPlzNichtVorhanden() {
		final String plz = PLZ_NICHT_VORHANDEN;
		
		final List<AbstractKunde> kunden = em.createNamedQuery(AbstractKunde.FIND_KUNDEN_BY_PLZ, AbstractKunde.class)
				.setParameter(AbstractKunde.PARAM_KUNDE_ADRESSE_PLZ, plz)
				.getResultList();
		
		assertThat(kunden.isEmpty(), is(true));
	}

	@Test
	public void findKundenByEmailVorhanden() {
		final String email = EMAIL_VORHANDEN;
		
		final AbstractKunde kunde = em.createNamedQuery(AbstractKunde.FIND_KUNDEN_BY_EMAIL, AbstractKunde.class)
				.setParameter(AbstractKunde.PARAM_KUNDE_EMAIL, email)
				.getSingleResult();
		
		assertThat(kunde.getEmail(), is(email));
	}

	@Test
	public void findKundenByEmailNichtVorhanden() {
		final String email = EMAIL_NICHT_VORHANDEN;
		
		final TypedQuery<AbstractKunde> query = em.createNamedQuery(AbstractKunde.FIND_KUNDEN_BY_EMAIL, AbstractKunde.class)
				.setParameter(AbstractKunde.PARAM_KUNDE_EMAIL, email);
		
		thrown.expect(NoResultException.class);
		query.getSingleResult();
	}
	
	@Test
	public void findKundenByErstelltVorhanden() {
		final String erstellt = ERSTELLT_VORHANDEN;
		Date datum = null;
		
		try {
			datum = new SimpleDateFormat("yyyy-MM-dd", LOCALE)
				.parse(erstellt);
		}
		catch (ParseException e) {
			throw new RuntimeException("Datum konnte nicht erstellt werden: " + e);
		}
		
		final List<AbstractKunde> kunden = em.createNamedQuery(AbstractKunde.FIND_KUNDEN_BY_ERSTELLT, AbstractKunde.class)
				.setParameter(AbstractKunde.PARAM_KUNDE_ERSTELLT, erstellt)		
				.getResultList();
		
		assertThat(kunden.isEmpty(), is(false));
		for (AbstractKunde k : kunden) {
			assertThat(k.getErstellt(), is(datum));
		}
	}
	
	@Test
	public void findKundenByErstelltNichtVorhanden() {
		final String erstellt = ERSTELLT_NICHT_VORHANDEN;

		final TypedQuery<AbstractKunde> query = em.createNamedQuery(AbstractKunde.FIND_KUNDEN_BY_ERSTELLT, AbstractKunde.class)
				.setParameter(AbstractKunde.PARAM_KUNDE_ERSTELLT, erstellt);

		thrown.expect(NoResultException.class);
		query.getSingleResult();
	}
	
	@Test
	public void createPrivatkunde() {
		Privatkunde kunde = new Privatkunde();
		kunde.setNachname(PRIVATKUNDE_NACHNAME_NEU);
		kunde.setVorname(PRIVATKUNDE_VORNAME_NEU);
		kunde.setEmail(PRIVATKUNDE_EMAIL_NEU);
		
		final Adresse adresse = new Adresse();
		adresse.setPlz(PLZ_NEU);
		adresse.setOrt(ORT_NEU);
		adresse.setStrasse(STRASSE_NEU);
		adresse.setHausnr(HAUSNR_NEU);
		adresse.setKunde(kunde);
		kunde.setAdresse(adresse);
		
		try {
			em.persist(kunde);
			em.persist(adresse);
		}
		catch (ConstraintViolationException e) {
			final Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
			for (ConstraintViolation<?> v : violations) {
				System.err.println("!!! FEHLERMELDUNG>>> " + v.getMessage());
				System.err.println("!!! ATTRIBUT>>> " + v.getPropertyPath());
				System.err.println("!!! ATTRIBUTWERT>>> " + v.getInvalidValue());
			}
			
			throw new RuntimeException(e);
		}
		
		final List<AbstractKunde> kunden = em.createNamedQuery(AbstractKunde.FIND_KUNDEN_BY_EMAIL, AbstractKunde.class)
				.setParameter(AbstractKunde.PARAM_KUNDE_EMAIL, PRIVATKUNDE_EMAIL_NEU)
				.getResultList();
		
		assertThat(kunden.size(), is(1));
		kunde = (Privatkunde) kunden.get(0);
		assertThat(kunde.getId().longValue() > KUNDEN_ID, is(true));
		assertThat(kunde.getNachname(), is(PRIVATKUNDE_NACHNAME_NEU));
		assertThat(kunde.getAdresse(), is(adresse));
		assertThat(adresse.getId().longValue() > ADRESS_ID, is(true));
	}
	
	@Test
	public void createFirmenkunde() {
		Firmenkunde kunde = new Firmenkunde();
		kunde.setNachname(FIRMENKUNDE_NACHNAME_NEU);
		kunde.setVorname(FIRMENKUNDE_VORNAME_NEU);
		kunde.setFirma(FIRMENKUNDE_FIRMA_NEU);
		kunde.setEmail(FIRMENKUNDE_EMAIL_NEU);
		
		final Adresse adresse = new Adresse();
		adresse.setPlz(PLZ_NEU);
		adresse.setOrt(ORT_NEU);
		adresse.setStrasse(STRASSE_NEU);
		adresse.setHausnr(HAUSNR_NEU);
		adresse.setKunde(kunde);
		kunde.setAdresse(adresse);
		
		try {
			em.persist(kunde);
			em.persist(adresse);
		}
		catch (ConstraintViolationException e) {
			final Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
			for (ConstraintViolation<?> v : violations) {
				System.err.println("!!! FEHLERMELDUNG>>> " + v.getMessage());
				System.err.println("!!! ATTRIBUT>>> " + v.getPropertyPath());
				System.err.println("!!! ATTRIBUTWERT>>> " + v.getInvalidValue());
			}
			
			throw new RuntimeException(e);
		}
		
		final List<AbstractKunde> kunden = em.createNamedQuery(AbstractKunde.FIND_KUNDEN_BY_EMAIL, AbstractKunde.class)
        		.setParameter(AbstractKunde.PARAM_KUNDE_EMAIL, FIRMENKUNDE_EMAIL_NEU)
        		.getResultList();
		
		assertThat(kunden.size(), is(1));
		kunde = (Firmenkunde) kunden.get(0);
		assertThat(kunde.getId().longValue() > KUNDEN_ID, is(true));
		assertThat(kunde.getNachname(), is(FIRMENKUNDE_NACHNAME_NEU));
		assertThat(kunde.getFirma(), is(FIRMENKUNDE_FIRMA_NEU));
		assertThat(kunde.getAdresse(), is(adresse));
		assertThat(adresse.getId().longValue() > ADRESS_ID, is(true));
		assertThat(kunde.getErstellt(), is(nullValue()));
	}
}
