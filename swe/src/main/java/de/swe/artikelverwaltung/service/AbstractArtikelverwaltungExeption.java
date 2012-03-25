package de.swe.artikelverwaltung.service;

import de.swe.util.AbstractSweException;
import static de.swe.util.JpaConstants.UID;

public abstract class AbstractArtikelverwaltungExeption extends
		AbstractSweException {
	private static final long serialVersionUID = UID;
	
	public AbstractArtikelverwaltungExeption(String msg) {
		super(msg);
	}

}
