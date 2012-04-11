package de.swe.kundenverwaltung.service;

import static de.swe.util.Constants.UID;

import javax.ejb.ApplicationException;


@ApplicationException(rollback = true)
public class EmailExistsException extends AbstractKundenverwaltungException {
	private static final long serialVersionUID = UID;
	private final String email;

	public EmailExistsException(String email) {
		super("Die Email-Adresse " + email + " existiert bereits");
		this.email = email;
	}

	public String getEmail() {
		return email;
	}
}
