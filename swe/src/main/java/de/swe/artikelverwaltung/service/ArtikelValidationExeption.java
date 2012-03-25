package de.swe.artikelverwaltung.service;

import static de.swe.util.JpaConstants.UID;

import java.util.Collection;

import javax.validation.ConstraintViolation;

import de.swe.artikelverwaltung.domain.Fahrzeug;


public class ArtikelValidationExeption extends AbstractArtikelverwaltungExeption {
	private static final long serialVersionUID = UID;
	private final Fahrzeug fahrzeug;
	private final Collection<ConstraintViolation<Fahrzeug>> violations;
	
	public ArtikelValidationExeption(Fahrzeug fahrzeug, Collection<ConstraintViolation<Fahrzeug>> violations) {
		super("Ungueltiges Fahrzeug: " + fahrzeug + "Violations: " + violations);
		this.fahrzeug = fahrzeug;
		this.violations = violations;
	}
	
	public Fahrzeug getFahrzeug() {
		return fahrzeug;
	}
	
	public Collection<ConstraintViolation<Fahrzeug>> getViolations() {
		return violations;
	}
}
