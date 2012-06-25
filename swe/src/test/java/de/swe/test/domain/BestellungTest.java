package de.swe.test.domain;

import static de.swe.util.Constants.KEINE_ID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.swe.artikelverwaltung.domain.Fahrzeug;
import de.swe.bestellverwaltung.domain.Bestellposition;
import de.swe.bestellverwaltung.domain.Bestellung;
import de.swe.bestellverwaltung.domain.Bestellung.Status;
import de.swe.kundenverwaltung.domain.AbstractKunde;
import de.swe.test.util.AbstractTest;

@RunWith(Arquillian.class)
public class BestellungTest extends AbstractTest {
	private static final Long BID_VORHANDEN = Long.valueOf(5016);
	private static final Long BID_NICHT_VORHANDEN =  Long.valueOf(100);
	private static final Long KUNDE_VORHANDEN = Long.valueOf(1205);
	private static final Long KUNDE_NICHT_VORHANDEN = Long.valueOf(1);
	private static final String DATUM_NICHT_VORHANDEN = "2000-10-10";
	private static final String DATUM_VORHANDEN = "2011-10-22";
	private static final GregorianCalendar DATUM_NEU = new GregorianCalendar(2011, 10, 23);
//	public static final Long BESTELL_ID = Long.valueOf(5014);
//	public static final Long BESTELLPOSITION_ID = Long.valueOf(9016);
	private static final Long FAHRZEUG_ID = Long.valueOf(6021);
	private static final Status STATUS = Bestellung.Status.NEU;
	
	@Test
	public void findBestellungenByKunde() {
		final Long kundenid = KUNDE_VORHANDEN;
		final List<Bestellung> bestellungen = 
				em.createNamedQuery(Bestellung.FIND_BESTELLUNG_BY_KUNDEN_ID, Bestellung.class)
				.setParameter(Bestellung.PARAM_KUNDE_ID, kundenid).getResultList();
		assertThat(bestellungen.isEmpty(), is(false));
		
		for (Bestellung b : bestellungen) {
			assertThat(b.getKunde().getId(), is(kundenid));	
		}
	}
	
	@Test
	public void findBestellungenByKundeNichtVorhanden() {
		final Long kundenid = KUNDE_NICHT_VORHANDEN;
		final List<Bestellung> bestellungen = 
			em.createNamedQuery(Bestellung.FIND_BESTELLUNG_BY_KUNDEN_ID, Bestellung.class)
			.setParameter(Bestellung.PARAM_KUNDE_ID, kundenid).getResultList();
		
		assertThat(bestellungen.isEmpty(), is(true));
	}
	
	@Test
	public void findBestellungByDatumNichtVorhanden() {
		final String bestelld = DATUM_NICHT_VORHANDEN;
		
		final List<Bestellung> bestellungen = 
			em.createNamedQuery(Bestellung.FIND_BESTELLUNG_BY_DATUM, Bestellung.class)
			.setParameter(Bestellung.PARAM_DATUM, bestelld)
			.getResultList();
		
		assertThat(bestellungen.isEmpty(), is(true));
	}
	
	@Test
	public void findBestellungByDatum() {
		final String bestelld = DATUM_VORHANDEN;
		Date datum = null;
		try {
			datum = new SimpleDateFormat("yyyy-MM-dd").parse(bestelld);
		}
		catch (ParseException e) {
			throw new RuntimeException("Datum konnte nicht erstellt werden: " + e);
		}
	
		
		List<Bestellung> bestellungen = 
			em.createNamedQuery(Bestellung.FIND_BESTELLUNG_BY_DATUM, Bestellung.class)
			.setParameter(Bestellung.PARAM_DATUM, bestelld)
			.getResultList();
		
		assertThat(bestellungen.isEmpty(), is(false));
		
		for (Bestellung b : bestellungen) {
			assertThat(b.getBestelldatum(), is(datum));	
		}
	}
	
	@Test
	public void findBestellungBybIdVorhanden() {
		final Long bid = BID_VORHANDEN;
		
		Bestellung bestellung = em.find(Bestellung.class, bid);
		assertThat(bestellung.getId(), is(bid));
	}
	
	@Test
	public void findBestellungBybIdNichtVorhanden() {
		final Long bid = BID_NICHT_VORHANDEN;
		
		Bestellung bestellung = em.find(Bestellung.class, bid);
		assertThat(bestellung, is(nullValue()));
	}
	
	@Test
	public void findBestellungByStatus() {
		
		final Status status = STATUS;
		
		List<Bestellung> bestellungen = 
				em.createNamedQuery(Bestellung.FIND_BESTELLUNGEN_BY_STATUS, Bestellung.class)
				.setParameter(Bestellung.PARAM_STATUS_DEFAULT, status)
				.getResultList();
		
		for (Bestellung b : bestellungen) {
			assertThat(b.getStatus(), is(status));
		}
	}
	
	@Test
	public void createBestellung() {
		Fahrzeug fahrzeug = em.find(Fahrzeug.class, FAHRZEUG_ID);
		Bestellposition neueBestellposition = new Bestellposition(fahrzeug);

		Bestellung neueBestellung = new Bestellung();
		neueBestellung.setId(KEINE_ID);
		neueBestellung.setStatus(STATUS);
		neueBestellung.setBestelldatum(DATUM_NEU.getTime());
		neueBestellung.addBestellposition(neueBestellposition);
		
		AbstractKunde kunde = em.find(AbstractKunde.class, KUNDE_VORHANDEN);
		kunde.addBestellung(neueBestellung);
		neueBestellung.setKunde(kunde);
		
		try {
			em.persist(neueBestellung);
			em.persist(neueBestellposition);
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
		
		assertThat(kunde.getBestellungen().contains(neueBestellung), is(true));
	}
	
	
}
