package de.swe.kundenverwaltung.service;

import static de.swe.util.Constants.UID;
import de.swe.util.AbstractSweException;

public abstract class AbstractKundenverwaltungException extends AbstractSweException {
	private static final long serialVersionUID = UID;

	public AbstractKundenverwaltungException(String msg) {
		super(msg);
	}
}
