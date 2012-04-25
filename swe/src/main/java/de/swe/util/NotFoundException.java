package de.swe.util;

import static de.swe.util.Constants.UID;

public class NotFoundException extends RuntimeException {
	private static final long serialVersionUID = UID;
	
	public NotFoundException(String msg) {
		super(msg);
	}

	public NotFoundException(String msg, Throwable t) {
		super(msg, t);
	}
}
