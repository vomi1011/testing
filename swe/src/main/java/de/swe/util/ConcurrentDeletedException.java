package de.swe.util;

import static de.swe.util.Constants.UID;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class ConcurrentDeletedException extends AbstractSweException {
	private static final long serialVersionUID = UID;
	private final Object id;
	
	public ConcurrentDeletedException(Object id) {
		super("Das Object mit der ID " + id + " wurde konkurrierend geloescht");
		this.id = id;
	}

	public Object getId() {
		return id;
	}
}
