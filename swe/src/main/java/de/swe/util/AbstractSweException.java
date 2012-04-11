package de.swe.util;

import static de.swe.util.Constants.UID;

public abstract class AbstractSweException extends RuntimeException {
	private static final long serialVersionUID = UID;

	public AbstractSweException(String msg) {
		super(msg);
	}

	public AbstractSweException(String msg, Throwable t) {
		super(msg, t);
	}
}
