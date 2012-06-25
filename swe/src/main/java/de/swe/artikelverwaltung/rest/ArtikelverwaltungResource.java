package de.swe.artikelverwaltung.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_XML;

import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.jboss.resteasy.annotations.providers.jaxb.Formatted;

import de.swe.artikelverwaltung.domain.Autohersteller;
import de.swe.artikelverwaltung.domain.Fahrzeug;
import de.swe.util.NotFoundException;

//@Path("/artikelverwaltung")
@Produces({ APPLICATION_XML, TEXT_XML })
public interface ArtikelverwaltungResource {

	/**
	 * Mit der URL /fahrzeuge werden alle Fahrzeuge ermittelt
	 * @return Liste mit den gefundenen Fahrzeugdaten
	 */
	// TODO Mit findFahrzeug mergen
	@GET
	@Path("/fahrzeuge")
	@Formatted
	FahrzeugList findFahrzeuge(@Context UriInfo uriInfo);
	
	/**
	 * Mit der URL /fahrzeuge/{id} einen Fahrzeug ermittelt
	 * @param id ID des Fahrzeuges
	 * @return Objekt mit Fahrzeugdaten, falls die ID vorhanden ist
	 */
	@GET
	@Path("/fahrzeuge/{id:[6][0-9]+}")
	@Formatted
	Fahrzeug findFahrzeug(@PathParam("id") Long id, @Context UriInfo uriInfo);

	/**
	 * Mit der URL /autohersteller werden alle Autohersteller ermittelt
	 * @return Liste mit den gefundenen Autoherstellern
	 */
	// TODO Mit findAutohersteller mergen
	@GET
	@Path("/autohersteller")
	@Formatted
	AutoherstellerList findAutohersteller(@Context UriInfo uriInfo);	

	/**
	 * Mit der URL /autohersteller/{id} einen Autohersteller ermittelt
	 * @param id ID des Autoherstellers
	 * @return Objekt mit Autoherstellerdaten, falls die ID vorhanden ist
	 */
	@GET
	@Path("/autohersteller/{id:[7][0-9]+}")
	@Formatted
	Autohersteller findAutohersteller(@PathParam("id") Long id, @Context UriInfo uriInfo);
	
	@POST
	@Path("/fahrzeuge")
	@Consumes({ APPLICATION_XML, TEXT_XML })
	@Produces
	Response createFahrzeug(Fahrzeug fahrzeug,
							@Context UriInfo uriInfo,
							@Context HttpHeaders headers);
	
	@POST
	@Path("/autohersteller")
	@Consumes({ APPLICATION_XML, TEXT_XML })
	Response createAutohersteller(Autohersteller autohersteller,
								  @Context UriInfo uriInfo,
								  @Context HttpHeaders headers);
	
	@PUT
	@Path("/fahrzeuge")
	@Consumes({ APPLICATION_XML, TEXT_XML })
	@Produces
	Response updateFahrzeug(Fahrzeug fahrzeug, @Context UriInfo uriInfo);
	
	@PUT
	@Path("/autohersteller")
	@Consumes({ APPLICATION_XML, TEXT_XML })
	@Produces
	Response updateAutohersteller(Autohersteller autohersteller, @Context UriInfo uriInfo);
	
	@DELETE
	@Path("/fahrzeuge/{id:[6][0-9]+}")
	@Produces
	Response deleteFahrzeug(@PathParam("id") Long id, @Context UriInfo uriInfo);
	
	@DELETE
	@Path("/autohersteller/{id:[7][0-9]+}")
	@Produces
	Response deleteAutohersteller(@PathParam("id") Long id, @Context UriInfo uriInfo);
	
	void updateUriFahrzeug(Fahrzeug fahrzeug, UriInfo uriInfo);
	
	URI getUriFahrzeug(Fahrzeug fahrzeug, UriInfo uriInfo);
	
	URI getUriAutohersteller(Autohersteller autohersteller, UriInfo uriInfo);
}
