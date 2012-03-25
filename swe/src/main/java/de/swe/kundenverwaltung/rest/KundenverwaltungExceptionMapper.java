package de.swe.kundenverwaltung.rest;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.CONFLICT;

import java.lang.invoke.MethodHandles;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.logging.Logger;

import de.swe.kundenverwaltung.service.AbstractKundenverwaltungException;


@Provider
public class KundenverwaltungExceptionMapper implements ExceptionMapper<AbstractKundenverwaltungException> {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	public Response toResponse(AbstractKundenverwaltungException e) {
		LOGGER.debugf("BEGINN toResponse: %s", e.getMessage());
		
		final String msg = e.getMessage();
		final Response response = Response.status(CONFLICT)
		                                  .type(TEXT_PLAIN)
		                                  .entity(msg)
		                                  .build();
		
		LOGGER.debug("ENDE toResponse");
		return response;
	}

}
