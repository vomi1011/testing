package de.swe.util;

public final class Constants {
	//JPA
	public static final Long KEINE_ID = null;
	public static final long UID = 1L;
	public static final int ERSTE_VERSION = 0;
	public static final int INT_ANZ_ZIFFERN = 11;
	public static final int LONG_ANZ_ZIFFERN = 20;
	
	public static final long KUNDEN_ID = 1001;
	public static final long ADRESS_ID = 2001;
	public static final long BESTELLUNG_ID = 5001;
	public static final long BESTELLPOSITION_ID = 9001;
	public static final long FAHRZEUG_ID = 6001;
	public static final long AUTOHERSTELLER_ID = 7001;

	// JAAS
	public static final String SECURITY_DOMAIN = "swe";
	public static final String ROLLE_MITARBEITER = "mitarbeiter";
	public static final String ROLLE_ADMIN = "admin";
	public static final String ROLLE_KUNDE = "kunde";
	public static final String ROLLE_TABELLE = "shop_role";
	
	// REST
	public static final String ARTIKELVERWALTUNG_NS = "urn:swe:artikelverwaltung";
	public static final String BESTELLVERWALTUNG_NS = "urn:swe:bestellverwaltung";
	public static final String KUNDENVERWALTUNG_NS = "urn:swe:kundenverwaltung";
	
	private Constants() {
	}
}
