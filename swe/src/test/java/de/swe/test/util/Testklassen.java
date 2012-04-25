package de.swe.test.util;

import java.util.Arrays;
import java.util.List;

import de.swe.test.service.KundenverwaltungTest;

public enum Testklassen {
	INSTANCE;
	
	// Testklassen aus verschiedenen Packages auflisten,
	// so dass alle darin enthaltenen Klassen ins Web-Archiv mitverpackt werden
	private final List<Class<? extends AbstractTest>> klassen = Arrays.asList(AbstractTest.class,
			                                                                  KundenverwaltungTest.class);
	
	public List<Class<? extends AbstractTest>> getTestklassen() {
		return klassen;
	}
}
