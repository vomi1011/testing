package de.swe.test.service;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.GregorianCalendar;
import java.util.List;

import javax.ejb.EJB;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.swe.artikelverwaltung.domain.Fahrzeug;
import de.swe.artikelverwaltung.service.Artikelverwaltung;
import de.swe.bestellverwaltung.domain.Bestellposition;
import de.swe.bestellverwaltung.domain.Bestellung;
import de.swe.bestellverwaltung.domain.Bestellung.Status;
import de.swe.bestellverwaltung.service.BestellungValidationException;
import de.swe.bestellverwaltung.service.Bestellverwaltung;
import de.swe.kundenverwaltung.domain.AbstractKunde;
import de.swe.kundenverwaltung.service.Kundenverwaltung;
import de.swe.kundenverwaltung.service.KundenverwaltungDao.Fetch;
import de.swe.test.util.AbstractTest;


@RunWith(Arquillian.class)
public class BestellverwaltungTest extends AbstractTest {
	private static final Long BESTELL_ID_VORHANDEN = Long.valueOf(5001);
//	private static final Long BESTELL_ID_VORHANDEN2 = Long.valueOf(5002);
	private static final Long BESTELL_ID_LOESCHEN = Long.valueOf(5005);
//	private static final Long BPOS_ID = Long.valueOf(9002);
	private static final Long BESTELL_ID_NICHT_VORHANDEN = Long.valueOf(5060);
	private static final Long KUNDE_ID_VORHANDEN = Long.valueOf(1001);
	private static final Long KUNDE_ID_NICHT_VORHANDEN = Long.valueOf(1060);
	private static final Long FId1_VORHANDEN = Long.valueOf(6004);
	private static final Short PRODUKT_X_ANZAHL = 1;
	private static final Long FId2_VORHANDEN = Long.valueOf(6002);
	private static final Short PRODUKT_Y_ANZAHL = 3;
	private static final GregorianCalendar DATUM_NEU = new GregorianCalendar(2011, 10, 01);
	private static final Status STATUS = Bestellung.Status.NEU;
	
	
	@EJB
	Bestellverwaltung bv;
	
	@EJB
	Artikelverwaltung av;
	
	@EJB
	Kundenverwaltung kv;
	
	@Test
	public void findBestellungenByKundenIdVorhanden() {
		long id = KUNDE_ID_VORHANDEN;
		List<Bestellung> bestellungen = bv.findBestellungenByKundenId(id);
		assertThat(bestellungen.get(0).getBId(), is(BESTELL_ID_VORHANDEN));
	}
	
	@Test
	public void findBestellungenByKundenIdNichtVorhanden() {
		long id = KUNDE_ID_NICHT_VORHANDEN;
		List<Bestellung> bestellungen = bv.findBestellungenByKundenId(id);
		assertThat(bestellungen.isEmpty(), is(true));
	}
	
	@Test
	public void findKundeByBestellid() {
		long id = BESTELL_ID_VORHANDEN;
		AbstractKunde kunde = bv.findKundeByBestellid(id);
		assertThat(kunde.getId(), is((id)-4000));
	}
	
	@Test
	public void findBestellungByIdVorhanden() {
		long id = BESTELL_ID_VORHANDEN;
		Bestellung bestellung = bv.findBestellungById(id);
		assertThat(bestellung.getBId(), is(id));
	}
	
	@Test
	public void findBestellungByIdNichtVorhanden() {
		long id = BESTELL_ID_NICHT_VORHANDEN;
		Bestellung bestellung = bv.findBestellungById(id);
		assertThat(bestellung, is(nullValue()));
	}
	
	@Test
	public void findAllBestellungen() {
		List<Bestellung> bestellungen = bv.findAllBestellungen();
		assertThat(bestellungen.size(), is(6));
	}
	
	@Test
	public void findBestellpositionenBybIdVorhanden() {
		long id = BESTELL_ID_VORHANDEN;
		Bestellung bestellung = bv.findBestellungById(id);
		List<Bestellposition> bestellpositionen = bestellung.getBestellpositionen();
		assertThat(bestellpositionen.size(), is(3));
	}
	
	@Test
	public void findBestellByStatus() {
		
		final Status status = STATUS;
	
		List<Bestellung> bestellungen = bv.findBestellungenByStatus(status);
			for (Bestellung b : bestellungen) {
				assertThat(b.getStatus(), is(status));
		}
		
	}

	//FIXME fahrzeug_fk ist null
	@Ignore
	@Test
	public void createBestellungTest() throws BestellungValidationException {
		final Long kundeId = KUNDE_ID_VORHANDEN;
		final Long fahrzeugId1 = FId1_VORHANDEN;
		final short artikel1Anzahl = PRODUKT_X_ANZAHL;
		final Long fahrzeugId2 = FId2_VORHANDEN;
		final short artikel2Anzahl = PRODUKT_Y_ANZAHL;
		final Status status = STATUS;

		Bestellung bestellung = new Bestellung();
		bestellung.setBestelldatum(DATUM_NEU.getTime());
		bestellung.setStatus(status);
		
		Fahrzeug fahrzeug1 = av.findFahrzeugById(fahrzeugId1);
		Bestellposition bpos1 = new Bestellposition(fahrzeug1, artikel1Anzahl);
		bestellung.addBestellposition(bpos1);
		
		Fahrzeug fahrzeug2 = av.findFahrzeugById(fahrzeugId2);
		Bestellposition bpos2 = new Bestellposition(fahrzeug2, artikel2Anzahl);
		bestellung.addBestellposition(bpos2);
		
		AbstractKunde kunde = kv.findKundeById(kundeId, Fetch.MIT_BESTELLUNG);
		
		bestellung = bv.createBestellung(bestellung, kunde, LOCALE);
		assertThat(bestellung.getBestellpositionen().size(), is(2));
		
		for (Bestellposition bp : bestellung.getBestellpositionen()) {
			assertThat(bp.getFahrzeug().getFId(), anyOf(is(fahrzeugId1), is(fahrzeugId2)));
		}
			
		kunde = bestellung.getKunde();
		assertThat(kunde.getId(), is(kundeId));
	}
	

	@Test
	public void deleteBestellungTest() throws BestellungValidationException {
		final Long id = BESTELL_ID_LOESCHEN;
		
		Bestellung bestellung = bv.findBestellungById(id);
		
		assertThat(bestellung, is(notNullValue()));
		
		bv.deleteBestellung(bestellung, LOCALE);
		
		assertThat(bv.findBestellungById(id), is(nullValue()));
	}
	
	@Test
	public void createBestellpositionTest() throws BestellungValidationException {
		final Long fahrzeugId1 = FId1_VORHANDEN;
		Bestellung bestellung = bv.findBestellungById(BESTELL_ID_VORHANDEN);
		assertThat(bestellung, is(notNullValue()));
		
		int anzahlBestellpositionenVorher = bestellung.getBestellpositionen().size();
		
		Fahrzeug fahrzeug1 = av.findFahrzeugById(fahrzeugId1);
		Bestellposition bpos = new Bestellposition(fahrzeug1);
		assertThat(bpos, is(notNullValue()));
		
		bestellung.addBestellposition(bpos);
		
		bestellung = bv.updateBestellung(bestellung, LOCALE);
		
		assertThat(bestellung.getBestellpositionen().size(), is(anzahlBestellpositionenVorher+1));
	}
	
	
	@Test
	public void deleteBestellpositionTest() throws BestellungValidationException {
		Bestellung bestellung = bv.findBestellungById(BESTELL_ID_VORHANDEN);
		
		int anzahlBestellpositionenVorher = bestellung.getBestellpositionen().size();
		
		bestellung.removeBestellposition(bestellung.getBestellpositionen().get(1));
		
		bestellung = bv.findBestellungById(BESTELL_ID_VORHANDEN);
		
		assertThat(bestellung.getBestellpositionen().size(), is(anzahlBestellpositionenVorher-1));
	}
	
	@Test
	public void stornierenBestellungTest() throws BestellungValidationException {
		final Long id = BESTELL_ID_VORHANDEN;
		Bestellung bestellung = bv.findBestellungById(id);
		final Status status = Status.STORNIERT;
		
		bestellung.setStatus(status);
		bestellung = bv.updateBestellung(bestellung, LOCALE);
		assertThat(bestellung.getStatus(), is(status));
	}	

	
//	@Test
//	public void updateBestellpositionTest() throws BestellungValidationException {
//		Bestellung bestellung = bv.findBestellungById(BESTELL_ID_VORHANDEN);
//		
//		bestellung.removeBestellposition(bestellung.getBestellpositionen().get(1));
//		
//		bestellung = bv.findBestellungById(BESTELL_ID_VORHANDEN);
//		
//		assertThat(bestellung.getBestellpositionen().size(), is(anzahlBestellpositionenVorher-1));
//	}
}