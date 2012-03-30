package de.swe.test.domain;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.swe.bestellverwaltung.domain.Bestellposition;
import de.swe.test.util.AbstractTest;

@RunWith(Arquillian.class)
public class BestellpositionTest extends AbstractTest {
	private static final Long BPID_VORHANDEN = Long.valueOf(9001);
	private static final Long BPID_NICHT_VORHANDEN = Long.valueOf(400);
	private static final Long FAHRZEUGID_NICHT_VORHANDEN =  Long.valueOf(5000);
	private static final Long FAHRZEUGID_VORHANDEN =  Long.valueOf(6001);
	
	@Test
	public void findBestellPositionByIdVorhanden() {
	
		final Long bpid = BPID_VORHANDEN;
		
		Bestellposition bestellposition = em.find(Bestellposition.class, bpid);
		assertThat(bestellposition.getBpId(), is(bpid));
		
	}
	
	@Test
	public void findBestellPositionByIdNichtVorhanden() {
		
		final Long bpid = BPID_NICHT_VORHANDEN;
		
		Bestellposition bestellposition = em.find(Bestellposition.class, bpid);
		assertThat(bestellposition, is(nullValue()));
	}
	
	@Test
	public void findVerkauftFahrzeugBestellPositionByIdVorhanden() {
		
		final Long fId = FAHRZEUGID_VORHANDEN;
		
		Bestellposition bestellposition = em.find(Bestellposition.class, fId);
		assertThat(bestellposition, is(nullValue()));
	}
	
	@Test
	public void findVerkauftFahrzeugBestellPositionByIdNichtVorhanden() {
		
		final Long fId = FAHRZEUGID_NICHT_VORHANDEN;
		
		Bestellposition bestellposition = em.find(Bestellposition.class, fId);
		assertThat(bestellposition, is(nullValue()));
	}	
	
} 