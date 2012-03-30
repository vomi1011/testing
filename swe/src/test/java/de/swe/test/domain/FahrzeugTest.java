package de.swe.test.domain;

import static de.swe.util.JpaConstants.KEINE_ID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.swe.artikelverwaltung.domain.Autohersteller;
import de.swe.artikelverwaltung.domain.Fahrzeug;
import de.swe.test.util.AbstractTest;

@RunWith(Arquillian.class)
public class FahrzeugTest extends AbstractTest {
//	TODO Liste aufräumen
//	TODO Testdaten für Validierung ändern
	private static final Long FID_VORHANDEN = Long.valueOf(6001);
	private static final Long FID_NICHT_VORHANDEN = Long.valueOf(1);
	private static final Long HERSTELLER_ID_VORHANDEN = Long.valueOf(7001);
	private static final Long HERSTELLER_ID_NICHT_VORHANDEN = Long.valueOf(1000);
	private static final String MODELL = "Modell";
	private static final short BAUJAHR = 1900;
	private static final String BESCHREIBUNG = "silber";
	private static final Boolean LIEFERBAR = true;
	private static final int PREIS = 30025;
	private static final String NAME = "Name";
	
	@Test
	public void findFahrzeugByIdVorhanden() {
		
		final Long fId = FID_VORHANDEN;
		
		Fahrzeug fahrzeug = em.find(Fahrzeug.class, fId);
		assertThat(fahrzeug.getFId(), is(fId));
	}
	
	@Test
	public void findFahrzeugByIdNichtVorhanden() {
		
		final Long fId = FID_NICHT_VORHANDEN;
		
		Fahrzeug fahrzeug = em.find(Fahrzeug.class, fId);
		assertThat(fahrzeug, is(nullValue()));
	}
	
	@Test
	public void findFahrzeugByIdHerstellerVorhanden() {
		
		final Long herstellerfk = HERSTELLER_ID_VORHANDEN;
		
		List<Fahrzeug> fahrzeug = em.createNamedQuery(Fahrzeug.FIND_FAHRZEUG_BY_HERSTELLER, Fahrzeug.class)
				.setParameter("hersteller_fk", herstellerfk)
				.getResultList();
		
		assertThat(fahrzeug.isEmpty(), is(false));
		for (Fahrzeug f : fahrzeug) {
			assertThat(f.getHersteller().getAId(), is(herstellerfk));
		}
	}
	
	@Test
	public void findFahrzeugByIdHerstellerNichtVorhanden() {
		
		final Long herstellerfk = HERSTELLER_ID_NICHT_VORHANDEN;
		
		List<Fahrzeug> hersteller = em.createNamedQuery(Fahrzeug.FIND_FAHRZEUG_BY_HERSTELLER, Fahrzeug.class)
				.setParameter("hersteller_fk", herstellerfk)
				.getResultList();
		
		assertThat(hersteller.isEmpty(), is(true));
	}
	
	@Test
	public void checkFahrzeug() {
		Fahrzeug f1 = new Fahrzeug();
		f1.setFId(KEINE_ID);
		f1.setModell(MODELL);
		f1.setBaujahr(BAUJAHR);
		f1.setBeschreibung(BESCHREIBUNG);
		f1.setPreis(PREIS);
		f1.setLieferbar(LIEFERBAR);
		
		final Autohersteller h1 = new Autohersteller();
		h1.setName(NAME);
		h1.addFahrzeug(f1);
		f1.setHersteller(h1);
		
		try {
			em.persist(h1);
			em.persist(f1);
		}
		catch (ConstraintViolationException e) {
			final Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
			for (ConstraintViolation<?> v : violations) {
				System.out.println("!!! FEHLERMELDUNG>>> " + v.getMessage());
				System.out.println("!!! ATTRIBUT>>> " + v.getPropertyPath());
				System.out.println("!!! ATTRIBUTWERT>>> " + v.getInvalidValue());
			}
			
			throw new RuntimeException(e);
		}
		
		final Autohersteller hersteller = em.createNamedQuery(Autohersteller.FIND_AUTOHERSTELLER_BY_NAME, Autohersteller.class)
				.setParameter("name", NAME)
				.getSingleResult();
		
		assertThat(hersteller.getFahrzeuge().isEmpty(), is(false));
		for (Fahrzeug f : hersteller.getFahrzeuge()) {
			assertThat(f.getModell(), is(MODELL));
		}
	}	
} 