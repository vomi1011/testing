package de.swe.artikelverwaltung.rest;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.List;
import java.util.Locale;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.jboss.logging.Logger;

import de.swe.artikelverwaltung.domain.Autohersteller;
import de.swe.artikelverwaltung.domain.Fahrzeug;
import de.swe.artikelverwaltung.service.ArtikelValidationExeption;
import de.swe.artikelverwaltung.service.ArtikelValidationExeptionAH;
import de.swe.artikelverwaltung.service.Artikelverwaltung;
import de.swe.util.NotFoundException;

@Stateless
public class ArtikelverwaltungResourceImpl implements ArtikelverwaltungResource {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().getClass());
	
	@EJB
	Artikelverwaltung av;
	
	@Override
	public Fahrzeug findFahrzeug(Long id, UriInfo uriInfo)
			throws NotFoundException {
		Fahrzeug fahrzeug = av.findFahrzeugById(id);
		if (fahrzeug == null) {
			String msg = "Kein Fahrzeug gefunden mit ID " + id;
			throw new NotFoundException(msg);
		}
		
		return fahrzeug;
	}
	
	@Override
	public Autohersteller findAutohersteller(Long id, UriInfo uriInfo)
			throws NotFoundException {
		Autohersteller autohersteller = av.findAutoherstellerById(id);
		
		if (autohersteller == null) {
			String msg = "Kein Autohersteller gefunden mit ID " + id;
			throw new NotFoundException(msg);			
		}
		return autohersteller;
	}
	
	@Override
	public Response createFahrzeug(Fahrzeug fahrzeug, UriInfo uriInfo, HttpHeaders headers)
			throws ArtikelValidationExeption {
		Autohersteller autohersteller = fahrzeug.getHersteller();
		
		if (autohersteller != null) {
			autohersteller.addFahrzeug(fahrzeug);
		}
		
		List<Locale> locales = headers.getAcceptableLanguages();
		Locale locale = locales.isEmpty() ? Locale.getDefault() : locales.get(0);
		av.createFahrzeug(fahrzeug, locale);
		LOGGER.tracef("%s", fahrzeug);
		
		final URI fahrzeugUri = getUriFahrzeug(fahrzeug, uriInfo);
		final Response response =Response.created(fahrzeugUri).build();
		
		return response;
	}
	
	@Override
	public Response createAutohersteller(Autohersteller autohersteller,
										 UriInfo uriInfo,
										 HttpHeaders headers)
			throws ArtikelValidationExeptionAH {
		List<Locale> locales = headers.getAcceptableLanguages();
		Locale locale = locales.isEmpty() ? Locale.getDefault() : locales.get(0);
		av.createAutohersteller(autohersteller, locale);
		LOGGER.tracef("%s", autohersteller);
		
		final URI autoherstellerUri = getUriAutohersteller(autohersteller, uriInfo);
		final Response response =Response.created(autoherstellerUri).build();
		
		return response;
	}

	@Override
	public Response updateFahrzeug(Fahrzeug fahrzeug, UriInfo uriInfo)
			throws NotFoundException, ArtikelValidationExeption {
		
		final Fahrzeug fahrzeugAlt = av.findFahrzeugById(fahrzeug.getFId());
		
		if (fahrzeugAlt == null) {
			String msg = "Kein Fahrzeug gefunden mit ID " + fahrzeug.getFId();
			throw new NotFoundException(msg);
		}
		
		LOGGER.tracef("%s", fahrzeugAlt);
		
		fahrzeugAlt.setValues(fahrzeug);
		LOGGER.tracef("%s", fahrzeugAlt);
		
		fahrzeug = av.updateFahrzeug(fahrzeug, Locale.getDefault());
		
		if (fahrzeug == null) {
			String msg = "Kein Fahrzeug gefunden mit ID " + fahrzeugAlt.getFId();
			throw new NotFoundException(msg);
		}
		
		return Response.noContent().build();
	}
	
	@Override
	public Response updateAutohersteller(Autohersteller autohersteller,
			UriInfo uriInfo) throws NotFoundException,
			ArtikelValidationExeptionAH {
		final Autohersteller autoherstellerAlt = av.findAutoherstellerById(autohersteller.getAId());
		
		if (autoherstellerAlt == null) {
			String msg = "Kein Autohersteller gefunden mit ID " + autohersteller.getAId();
			throw new NotFoundException(msg);
		}
		
		LOGGER.tracef("%s", autoherstellerAlt);
		autoherstellerAlt.setValues(autohersteller);
		LOGGER.tracef("%s", autoherstellerAlt);
		autohersteller = av.updateAutohersteller(autohersteller, Locale.getDefault());
		
		if (autohersteller == null) {
			String msg = "Kein Autohersteller gefunden mit ID " + autoherstellerAlt.getAId();
			throw new NotFoundException(msg);
		}
		
		return Response.noContent().build();
	}

	@Override
	public Response deleteFahrzeug(Long id, UriInfo uriInfo)
			throws NotFoundException {
		final Fahrzeug fahrzeug = av.findFahrzeugById(id);
		
		if (fahrzeug == null) {
			String msg = "Kein Fahrzeug gefunden mit ID " + id;
			throw new NotFoundException(msg);
		}
		
		av.deleteFahrzeug(fahrzeug);
		
		return Response.noContent().build();
	}
	
	@Override
	public Response deleteAutohersteller(Long id, UriInfo uriInfo)
			throws NotFoundException {
		final Autohersteller autohersteller = av.findAutoherstellerById(id);
		
		if (autohersteller == null) {
			String msg = "Kein Autohersteller gefunden mit ID " + id;
			throw new NotFoundException(msg);
		}
		
		av.deleteAutohersteller(autohersteller);
		return Response.noContent().build();
	}
	
	@Override
	public URI getUriFahrzeug(Fahrzeug fahrzeug, UriInfo uriInfo) {
		final UriBuilder ub = uriInfo.getBaseUriBuilder()
		                             .path(ArtikelverwaltungResource.class)
		                             .path(ArtikelverwaltungResource.class, "findFahrzeug");
		final URI fahrzeugUri = ub.build(fahrzeug.getFId());
		return fahrzeugUri;
	}

	@Override
	public void updateUriFahrzeug(Fahrzeug fahrzeug, UriInfo uriInfo) {
		// TODO Auto-generated method stub
		final UriBuilder ub = uriInfo.getBaseUriBuilder()
                .path(ArtikelverwaltungResource.class)
                .path(ArtikelverwaltungResource.class, "findFahrzeugbyAutoherstellerId");
		final URI autoherstellerUri = ub.build(fahrzeug.getFId());
		fahrzeug.setHersteller(autoherstellerUri);
	}

	@Override
	public URI getUriAutohersteller(Autohersteller autohersteller, UriInfo uriInfo) {
		final UriBuilder ub = uriInfo.getBaseUriBuilder()
                .path(ArtikelverwaltungResource.class)
                .path(ArtikelverwaltungResource.class, "findAutohersteller");
		final URI autoherstellerUri = ub.build(autohersteller.getAId());
		return autoherstellerUri;
	}

	@Override
	public Fahrzeug findFahrzeuge(UriInfo uriInfo) throws NotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Fahrzeug findFahrzeuge(String name, UriInfo uriInfo)
			throws NotFoundException {
		// TODO Auto-generated method stub
		return null;
	}
}
