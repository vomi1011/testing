package de.swe.artikelverwaltung.rest;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.CONFLICT;

import java.lang.invoke.MethodHandles;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.logging.Logger;

import de.swe.artikelverwaltung.service.AbstractArtikelverwaltungExeption;


@Provider
public class ArtikelverwaltungExceptionMapper implements ExceptionMapper<AbstractArtikelverwaltungExeption> {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	public Response toResponse(AbstractArtikelverwaltungExeption e) {
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
