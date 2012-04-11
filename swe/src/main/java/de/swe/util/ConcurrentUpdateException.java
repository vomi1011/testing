package de.swe.util;

import static de.swe.util.Constants.UID;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class ConcurrentUpdateException extends AbstractSweException {
	private static final long serialVersionUID = UID;
	private final Object id;
	
	public ConcurrentUpdateException(Object id, Throwable t) {
		super("Das Object mit der ID " + id + " wurde konkurrierend modifiziert", t);
		this.id = id;
	}

	public Object getId() {
		return id;
	}
}
