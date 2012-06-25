package de.swe.test.domain;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.swe.artikelverwaltung.domain.Autohersteller;
import de.swe.test.util.AbstractTest;


@RunWith(Arquillian.class)
public class AutoherstellerTest extends AbstractTest {
	private static final Long AID_VORHANDEN = Long.valueOf(7002);
	private static final Long AID_NICHT_VORHANDEN = Long.valueOf(234);
	private static final String NAME = "Audi";
	private static final String NAME_NICHT_VORHANDEN = "1";
	
	@Test
	public void findAutoherstellerByIdVorhanden() {
		
		final Long aId = AID_VORHANDEN;
		
		Autohersteller autohersteller = em.find(Autohersteller.class, aId);
		assertThat(autohersteller.getId(), is(aId));
	}
	
	@Test
	public void findAutoherstellerByIdNichtVorhanden() {
		
		final Long aId = AID_NICHT_VORHANDEN;
		
		Autohersteller autohersteller = em.find(Autohersteller.class, aId);
		assertThat(autohersteller, is(nullValue()));
	}
	
	@Test
	public void findAutoherstellerByNameVorhanden() {
		
		final String name = NAME;
		
		List<Autohersteller> autohersteller = em.createNamedQuery(Autohersteller.FIND_AUTOHERSTELLER_BY_NAME, Autohersteller.class)
				.setParameter("name", name)
				.getResultList();
		
		for (Autohersteller a : autohersteller) {
			assertThat(a.getName(), is(name));
		}
	}
	
	@Test
	public void findAutoherstellerByNameNichtVorhanden() {
		
		final String name = NAME_NICHT_VORHANDEN;
		
		List<Autohersteller> autohersteller = em.createNamedQuery(Autohersteller.FIND_AUTOHERSTELLER_BY_NAME, Autohersteller.class)
				.setParameter("name", name)
				.getResultList();
		
		assertThat(autohersteller.isEmpty(), is(true));
	}
} 