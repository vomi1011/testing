package de.swe.kundenverwaltung.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
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

import org.jboss.resteasy.annotations.Form;
import org.jboss.resteasy.annotations.providers.jaxb.Formatted;
import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;

import de.swe.bestellverwaltung.rest.BestellungList;
import de.swe.kundenverwaltung.domain.AbstractKunde;
import de.swe.util.NotFoundException;

@Path("/kunden")
@Produces({ APPLICATION_XML, TEXT_XML })
public interface KundenverwaltungResource {
	/**
	 * Mit der URL /kunden/{id} einen Kunden ermitteln
	 * @param id ID des Kunden
	 * @return Objekt mit Kundendaten, falls die ID vorhanden ist
	 */
	@GET
	@Path("{id:[1-9][0-9]+}")
	@Formatted
	AbstractKunde findKunde(@PathParam("id") Long id, @Context UriInfo uriInfo);
	
	/**
	 * Mit der URL /kunden werden alle Kunden ermittelt oder
	 * mit kundenverwaltung/kunden?nachname=... diejenigen mit einem bestimmten Nachnamen.
	 * @return Liste mit den gefundenen Kundendaten
	 */
	@GET
	@Wrapped(element = "kunden")
	KundeList findKunden(@QueryParam("nachname") @DefaultValue("") String nachname,
						 @Context UriInfo uriInfo);
	
	/**
	 * Mit der URL kunden/{id}/bestellungen die Bestellungen zu eine Kunden ermitteln
	 * @param id ID des Kunden
	 * @return Objekt mit Bestellungsdaten, falls die ID vorhanden ist
	 */
	@GET
	@Path("{id:[0-9]+}/bestellungen")
	BestellungList findBestellungenByKundeId(@PathParam("id") Long id,
			                                 @Context UriInfo uriInfo);
	
	/**
	 * Mit der URL /kunden einen Privatkunden per POST anlegen.
	 * @param kunde neuer Kunde
	 * @return Response-Objekt mit URL des neuen Privatkunden
	 */
	@POST
	@Consumes({ APPLICATION_XML, TEXT_XML })
	Response createKunde(AbstractKunde kunde,
						 @Context UriInfo uriInfo,
						 @Context HttpHeaders headers);
	
	/**
	 * Mit der URL /kunden/form einen Privatkunden per POST anlegen wie in einem HTML-Formular.
	 * @param kundeForm Form-Objekt mit den neuen Daten
	 * @param headers
	 * @param uriInfo
	 * @return Response-Objekt mit URL des neuen Privatkunden
	 */
	@POST
	@Path("form")
	@Consumes(APPLICATION_FORM_URLENCODED)
	@Produces
	Response createPrivatkunde(@Form KundeForm kunde,
							  @Context UriInfo uriInfo,
							  @Context HttpHeaders headers);
	
	/**
	 * Mit der URL /kunden einen Kunden per PUT aktualisieren
	 * @param kunde zu aktualisierende Daten des Kunden
	 */
	@PUT
	@Consumes({ APPLICATION_XML, TEXT_XML })
	@Produces
	Response updateKunde(AbstractKunde kunde,
						 @Context UriInfo uriInfo,
						 @Context HttpHeaders headers)
			throws NotFoundException;
	
	/**
	 * Mit der URL /kunden{id} einen Kunden per DELETE l&ouml;schen
	 * @param kundeId des zu l&ouml;schenden Kunden
	 *         gel&ouml;scht wurde, weil es zur gegebenen id keinen Kunden gibt
	 */
	@DELETE
	@Path("{id:[1-9][0-9]+}")
	@Produces
	Response deleteKunde(@PathParam("id") Long id, @Context UriInfo uriInfo);
	
	void updateUriKunde(AbstractKunde kunde, UriInfo uriInfo);
	
	URI getUriKunde(AbstractKunde kunde, UriInfo uriInfo);
}