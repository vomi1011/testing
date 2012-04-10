package de.swe.util;

public enum RolleType {
	ADMIN("admin"),
	MITARBEITER("mitarbeiter"),
	KUNDE("kunde");
	
	private String value;
	
	RolleType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
