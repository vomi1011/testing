package de.swe.artikelverwaltung.service;

import static de.swe.util.Constants.UID;

import java.util.Collection;

import javax.validation.ConstraintViolation;

import de.swe.artikelverwaltung.domain.Autohersteller;

/**
 * Exception, die ausgel&ouml;st wird, wenn die Attributwerte eines Kunden nicht korrekt sind
 */
//@ApplicationException(rollback = true)
public class ArtikelValidationExeptionAH extends AbstractArtikelverwaltungExeption {
	private static final long serialVersionUID = UID;
	private final Autohersteller autohersteller;
	private final Collection<ConstraintViolation<Autohersteller>> violations;
	
	public ArtikelValidationExeptionAH(Autohersteller autohersteller, 
			Collection<ConstraintViolation<Autohersteller>> violations) {
		super("Ungueltiger Autohersteller: " + autohersteller + "Violations: " + violations);
		this.autohersteller = autohersteller;
		this.violations = violations;
	}
	
	public Autohersteller getAutohersteller() {
		return autohersteller;
	}
	
	public Collection<ConstraintViolation<Autohersteller>> getViolations() {
		return violations;
	}
}
