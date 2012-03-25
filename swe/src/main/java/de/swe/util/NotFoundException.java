package de.swe.util;

import static de.swe.util.JpaConstants.UID;

public class NotFoundException extends Exception {
	private static final long serialVersionUID = UID;
	
	public NotFoundException(String msg) {
		super(msg);
	}
}
