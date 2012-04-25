package de.swe.bestellverwaltung.service;
import static de.swe.util.Constants.UID;

import java.util.Collection;

import javax.validation.ConstraintViolation;

import de.swe.bestellverwaltung.domain.Bestellung;

public class BestellungValidationException extends
		AbstractBestellverwaltungException {
	private static final long serialVersionUID = UID;
	private final Bestellung bestellung;
	private final Collection<ConstraintViolation<Bestellung>> violations;
	
	public BestellungValidationException(Bestellung b, 
			Collection<ConstraintViolation<Bestellung>> violations) {
		super("Ungueltige Bestelluung: " + b + "Violations: " + violations);
		this.bestellung = b;
		this.bestellung.setStatus(b.getStatus());
		this.violations = violations;
	}
	
	public Bestellung getBestellung() {
		return bestellung;
	}
	
	public Collection<ConstraintViolation<Bestellung>> getViolations() {
		return violations;
	}
}
