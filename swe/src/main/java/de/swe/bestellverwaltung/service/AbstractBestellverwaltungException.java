package de.swe.bestellverwaltung.service;

import de.swe.util.AbstractSweException;
import static de.swe.util.JpaConstants.UID;

public class AbstractBestellverwaltungException extends 
	AbstractSweException {
	private static final long serialVersionUID = UID;

	public AbstractBestellverwaltungException(String msg) {
		super(msg);
	}
}
