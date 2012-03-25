package de.swe.bestellverwaltung.rest;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.CONFLICT;

import java.lang.invoke.MethodHandles;
import java.util.Collection;

import javax.validation.ConstraintViolation;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.logging.Logger;

import de.swe.bestellverwaltung.domain.Bestellung;
import de.swe.bestellverwaltung.service.BestellungValidationException;


@Provider
public class BestellungValidationExceptionMapper implements ExceptionMapper<BestellungValidationException> {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	public Response toResponse(BestellungValidationException e) {
		LOGGER.debugf("BEGINN toResponse: %s", e.getViolations());
		
		final Collection<ConstraintViolation<Bestellung>> violations = e.getViolations();
		final StringBuilder sb = new StringBuilder();
		for (ConstraintViolation<Bestellung> v : violations) {
			sb.append(v.getMessage());
			sb.append(" ");
		}
		
		final String responseStr = sb.toString();
		final Response response = Response.status(CONFLICT)
		                                  .type(TEXT_PLAIN)
		                                  .entity(responseStr)
		                                  .build();
		
		LOGGER.debugf("ENDE toResponse: %s", responseStr);
		return response;
	}

}
