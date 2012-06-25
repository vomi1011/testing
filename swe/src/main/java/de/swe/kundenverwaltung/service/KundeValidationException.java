package de.swe.kundenverwaltung.service;



import static de.swe.util.Constants.UID;

import java.util.Collection;

import javax.ejb.ApplicationException;
import javax.validation.ConstraintViolation;

import de.swe.kundenverwaltung.domain.AbstractKunde;


/**
 * Exception, die ausgel&ouml;st wird, wenn die Attributwerte eines Kunden nicht korrekt sind
 */
@ApplicationException(rollback = true)
public class KundeValidationException extends AbstractKundenverwaltungException {
	private static final long serialVersionUID = UID;
	private final AbstractKunde kunde;
	private final Collection<ConstraintViolation<AbstractKunde>> violations;

	public KundeValidationException(AbstractKunde kunde,
			                        Collection<ConstraintViolation<AbstractKunde>> violations) {
		super("Ungueltiger Kunde: " + kunde + ", Violations: " + violations);
		this.kunde = kunde;
		this.violations = violations;
	}

	public AbstractKunde getKunde() {
		return kunde;
	}

	public Collection<ConstraintViolation<AbstractKunde>> getViolations() {
		return violations;
	}
}
