package de.swe.artikelverwaltung.rest;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.CONFLICT;

import java.lang.invoke.MethodHandles;
import java.util.Collection;

import javax.validation.ConstraintViolation;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.logging.Logger;

import de.swe.artikelverwaltung.domain.Fahrzeug;
import de.swe.artikelverwaltung.service.ArtikelValidationExeption;


@Provider
public class ArtikelValidationExceptionMapper implements ExceptionMapper<ArtikelValidationExeption> {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	public Response toResponse(ArtikelValidationExeption e) {
		LOGGER.debugf("BEGINN toResponse: %s", e.getViolations());
		
		final Collection<ConstraintViolation<Fahrzeug>> violations = e.getViolations();
		final StringBuilder sb = new StringBuilder();
		for (ConstraintViolation<Fahrzeug> v : violations) {
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
