package de.swe.kundenverwaltung.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_XML;

import java.net.URI;
import java.text.ParseException;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.jboss.resteasy.annotations.Form;
import org.jboss.resteasy.annotations.providers.jaxb.Formatted;
import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;

import de.swe.bestellverwaltung.rest.BestellungList;
import de.swe.kundenverwaltung.domain.AbstractKunde;
import de.swe.kundenverwaltung.service.EmailExistsException;
import de.swe.kundenverwaltung.service.KundeDeleteBestellungException;
import de.swe.kundenverwaltung.service.KundeValidationException;
import de.swe.util.NotFoundException;

@Path("/kundenverwaltung")
@Produces({ APPLICATION_XML, TEXT_XML })
public interface KundenverwaltungResource {
	@GET
	@Path("/kunden/{id:[1-9][0-9]+}")
	@Formatted
	AbstractKunde findKunde(@PathParam("id") Long id, @Context UriInfo uriInfo)
			throws NotFoundException;
	
	@GET
	@Path("/kunden")
	@Wrapped(element = "kunden")
	KundeList findKunden(@QueryParam("nachname") @DefaultValue("") String nachname,
						 @Context UriInfo uriInfo)
			throws NotFoundException;
	
	@GET
	@Path("/kunden/{id:[0-9]+}/bestellungen")
	BestellungList findBestellungenByKundeId(@PathParam("id") Long id,
			                                 @Context UriInfo uriInfo)
			       throws NotFoundException;
	
	@POST
	@Path("/kunden")
	@Consumes({ APPLICATION_XML, TEXT_XML })
	Response createKunde(AbstractKunde kunde,
						 @Context UriInfo uriInfo,
						 @Context HttpHeaders headers)
			throws EmailExistsException, KundeValidationException;
	
	@POST
	@Path("/kunden/form")
	@Consumes(APPLICATION_FORM_URLENCODED)
	@Produces
	Response createPrivatkunde(@Form KundeForm kunde,
							  @Context UriInfo uriInfo,
							  @Context HttpHeaders headers)
			throws EmailExistsException, ParseException, KundeValidationException;
	
	@PUT
	@Path("/kunden")
	@Consumes({ APPLICATION_XML, TEXT_XML })
	@Produces
	Response updateKunde(AbstractKunde kunde,
						 @Context UriInfo uriInfo,
						 @Context HttpHeaders headers)
			throws NotFoundException, EmailExistsException, KundeValidationException;
	
	@DELETE
	@Path("/kunden/{id:[1-9][0-9]+}")
	@Produces
	Response deleteKunde(@PathParam("id") Long id, @Context UriInfo uriInfo)
			throws NotFoundException, KundeDeleteBestellungException;
	
	void updateUriKunde(AbstractKunde kunde, UriInfo uriInfo);
	
	URI getUriKunde(AbstractKunde kunde, UriInfo uriInfo);
}