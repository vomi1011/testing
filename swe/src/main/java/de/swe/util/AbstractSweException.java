package de.swe.util;

public abstract class AbstractSweException extends Exception {
	private static final long serialVersionUID = -1030863258479949134L;

	public AbstractSweException(String msg) {
		super(msg);
	}

	public AbstractSweException(String msg, Throwable t) {
		super(msg, t);
	}
}
