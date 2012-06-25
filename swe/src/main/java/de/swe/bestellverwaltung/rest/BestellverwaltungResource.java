package de.swe.bestellverwaltung.rest;

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

import de.swe.bestellverwaltung.domain.Bestellung;
import de.swe.kundenverwaltung.domain.AbstractKunde;
import de.swe.util.NotFoundException;

@Path("/bestellungen")
@Produces({ APPLICATION_XML, TEXT_XML })
@Consumes
public interface BestellverwaltungResource {

	/**
	 * Mit der URL /bestellverwaltung/bestellungen/{id} eine Bestellung ermitteln
	 * @param id ID der Bestellung
	 * @return Objekt mit Bestelldaten, falls die ID vorhanden ist
	 * @throws NotFoundExceptionRest 
	 */
	@GET
	@Path("/{id:[5][0-9]+}")
	Bestellung findBestellung(@PathParam("id") Long id, @Context UriInfo uriInfo);

	
	@GET
	BestellungList findBestellungen(@Context UriInfo uriInfo);
	
	/**
	 * Mit der URL /bestellverwaltung/bestellungen/{id}/kunde den Kunden einer Bestellung ermitteln
	 * @param id ID der Bestellung
	 * @return Objekt mit Kundendaten, falls die ID vorhanden ist
	 * @throws NotFoundExceptionRest 
	 */
	@GET
	@Path("/{id:[5][0-9]+}/kunde")
	AbstractKunde findKundeByBid(@PathParam("id") Long id, @Context UriInfo uriInfo);
	
	/**
	 * Mit der URL /bestellverwaltung/bestellungen eine neue Bestellung anlegen
	 * @param bestellung die neue Bestellung
	 * @return Objekt mit Bestelldaten, falls die ID vorhanden ist
	 * @throws NotFoundException 
	 * @throws BestellungDuplikatException 
	 * @throws BestellungCreateException 
	 * @throws NotFoundExceptionRest
	 */
	@POST
	@Consumes({ APPLICATION_XML, TEXT_XML })
	@Produces
	Response createBestellung(Bestellung bestellung, @Context UriInfo uriInfo, @Context HttpHeaders headers);

	void updateUriBestellung(Bestellung bestellung, UriInfo uriInfo) throws NotFoundException;
	
	URI getUriBestellung(Bestellung bestellung, UriInfo uriInfo) throws NotFoundException;

	@DELETE
	@Path("/bestellungen/{id:[5][0-9]+}")
	@Produces
	Response deleteBestellung(@PathParam("id") Long id, @Context UriInfo uriInfo);
	
	@PUT
	@Path("/bestellungen/{id:[5][0-9]+}")
	@Consumes({ APPLICATION_XML, TEXT_XML })
	@Produces
	Response stornierenBestellung(@PathParam("id") Long id, @Context UriInfo uriInfo);

}
