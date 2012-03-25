package de.swe.bestellverwaltung.rest;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.CONFLICT;

import java.lang.invoke.MethodHandles;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.logging.Logger;

import de.swe.bestellverwaltung.service.AbstractBestellverwaltungException;


@Provider
public class BestellverwaltungExceptionMapper implements ExceptionMapper<AbstractBestellverwaltungException> {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	public Response toResponse(AbstractBestellverwaltungException e) {
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
