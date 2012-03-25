package de.swe.artikelverwaltung.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_XML;

import java.net.URI;

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

import org.jboss.resteasy.annotations.providers.jaxb.Formatted;

import de.swe.artikelverwaltung.domain.Autohersteller;
import de.swe.artikelverwaltung.domain.Fahrzeug;
import de.swe.artikelverwaltung.service.ArtikelValidationExeption;
import de.swe.artikelverwaltung.service.ArtikelValidationExeptionAH;
import de.swe.util.NotFoundException;

@Path("/artikelverwaltung")
@Produces({ APPLICATION_XML, TEXT_XML })
public interface ArtikelverwaltungResource {
	@GET
	@Path("/fahrzeuge/{id:[6][0-9]+}")
	@Formatted
	Fahrzeug findFahrzeug(@PathParam("id") Long id, @Context UriInfo uriInfo)
			throws NotFoundException;
	
	@GET
	@Path("/fahrzeuge")
	@Formatted
	Fahrzeug findFahrzeuge(@Context UriInfo uriInfo)
			throws NotFoundException;
	
	@GET
	@Path("/autohersteller/{id:[7][0-9]+}")
	@Formatted
	Autohersteller findAutohersteller(@PathParam("id") Long id, @Context UriInfo uriInfo)
			throws NotFoundException;

	@GET
	@Path("/autohersteller")
	@Formatted
	Fahrzeug findFahrzeuge(@QueryParam("name") @DefaultValue("") String name, @Context UriInfo uriInfo)
			throws NotFoundException;
	
	@POST
	@Path("/fahrzeuge")
	@Consumes({ APPLICATION_XML, TEXT_XML })
	@Produces
	Response createFahrzeug(Fahrzeug fahrzeug,
							@Context UriInfo uriInfo,
							@Context HttpHeaders headers)
			throws ArtikelValidationExeption;
	
	@POST
	@Path("/autohersteller")
	@Consumes({ APPLICATION_XML, TEXT_XML })
	Response createAutohersteller(Autohersteller autohersteller,
								  @Context UriInfo uriInfo,
								  @Context HttpHeaders headers)
			throws ArtikelValidationExeptionAH;
	
	@PUT
	@Path("/fahrzeuge")
	@Consumes({ APPLICATION_XML, TEXT_XML })
	@Produces
	Response updateFahrzeug(Fahrzeug fahrzeug, @Context UriInfo uriInfo)
			throws NotFoundException, ArtikelValidationExeption;
	
	@PUT
	@Path("/autohersteller")
	@Consumes({ APPLICATION_XML, TEXT_XML })
	@Produces
	Response updateAutohersteller(Autohersteller autohersteller, @Context UriInfo uriInfo)
			throws NotFoundException, ArtikelValidationExeptionAH;
	
	@DELETE
	@Path("/fahrzeuge/{id:[6][0-9]+}")
	@Produces
	Response deleteFahrzeug(@PathParam("id") Long id, @Context UriInfo uriInfo)
			throws NotFoundException;
	
	@DELETE
	@Path("/autohersteller/{id:[7][0-9]+}")
	@Produces
	Response deleteAutohersteller(@PathParam("id") Long id, @Context UriInfo uriInfo)
			throws NotFoundException;
	
	void updateUriFahrzeug(Fahrzeug fahrzeug, UriInfo uriInfo);
	
	URI getUriFahrzeug(Fahrzeug fahrzeug, UriInfo uriInfo);
	
	URI getUriAutohersteller(Autohersteller autohersteller, UriInfo uriInfo);
}
