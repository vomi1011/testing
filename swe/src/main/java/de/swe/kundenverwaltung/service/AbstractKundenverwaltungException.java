package de.swe.kundenverwaltung.service;

import de.swe.util.AbstractSweException;

public abstract class AbstractKundenverwaltungException extends AbstractSweException {
	private static final long serialVersionUID = -2849585609393128387L;

	public AbstractKundenverwaltungException(String msg) {
		super(msg);
	}
}
