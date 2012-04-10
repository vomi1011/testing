package de.swe.bestellverwaltung.service;

import de.swe.util.AbstractSweException;
import static de.swe.util.Constants.UID;

public class AbstractBestellverwaltungException extends 
	AbstractSweException {
	private static final long serialVersionUID = UID;

	public AbstractBestellverwaltungException(String msg) {
		super(msg);
	}
}
