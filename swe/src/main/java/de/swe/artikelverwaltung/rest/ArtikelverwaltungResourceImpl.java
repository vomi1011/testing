package de.swe.artikelverwaltung.rest;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.List;
import java.util.Locale;

import javax.ejb.Stateless;
import javax.inject.Inject;
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
import de.swe.artikelverwaltung.service.ArtikelverwaltungDao.Order;
import de.swe.util.NotFoundException;

@Stateless
public class ArtikelverwaltungResourceImpl implements ArtikelverwaltungResource {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().getClass());
	
	@Inject
	private Artikelverwaltung av;
	
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
		final Response response = Response.created(fahrzeugUri).build();
		
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
		final Response response = Response.created(autoherstellerUri).build();
		
		return response;
	}

	@Override
	public Response updateFahrzeug(Fahrzeug fahrzeug, UriInfo uriInfo)
			throws NotFoundException, ArtikelValidationExeption {
		
		final Fahrzeug fahrzeugAlt = av.findFahrzeugById(fahrzeug.getId());
		
		if (fahrzeugAlt == null) {
			String msg = "Kein Fahrzeug gefunden mit ID " + fahrzeug.getId();
			throw new NotFoundException(msg);
		}
		
		LOGGER.tracef("%s", fahrzeugAlt);
		
		fahrzeugAlt.setValues(fahrzeug);
		LOGGER.tracef("%s", fahrzeugAlt);
		
		fahrzeug = av.updateFahrzeug(fahrzeug, Locale.getDefault());
		
		if (fahrzeug == null) {
			String msg = "Kein Fahrzeug gefunden mit ID " + fahrzeugAlt.getId();
			throw new NotFoundException(msg);
		}
		
		return Response.noContent().build();
	}
	
	@Override
	public Response updateAutohersteller(Autohersteller autohersteller,
			UriInfo uriInfo) throws NotFoundException,
			ArtikelValidationExeptionAH {
		final Autohersteller autoherstellerAlt = av.findAutoherstellerById(autohersteller.getId());
		
		if (autoherstellerAlt == null) {
			String msg = "Kein Autohersteller gefunden mit ID " + autohersteller.getId();
			throw new NotFoundException(msg);
		}
		
		LOGGER.tracef("%s", autoherstellerAlt);
		autoherstellerAlt.setValues(autohersteller);
		LOGGER.tracef("%s", autoherstellerAlt);
		autohersteller = av.updateAutohersteller(autohersteller, Locale.getDefault());
		
		if (autohersteller == null) {
			String msg = "Kein Autohersteller gefunden mit ID " + autoherstellerAlt.getId();
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
		final URI fahrzeugUri = ub.build(fahrzeug.getId());
		return fahrzeugUri;
	}

	@Override
	public void updateUriFahrzeug(Fahrzeug fahrzeug, UriInfo uriInfo) {
		// TODO Auto-generated method stub
		final UriBuilder ub = uriInfo.getBaseUriBuilder()
                .path(ArtikelverwaltungResource.class)
                .path(ArtikelverwaltungResource.class, "findFahrzeugbyAutoherstellerId");
		final URI autoherstellerUri = ub.build(fahrzeug.getId());
		fahrzeug.setHersteller(autoherstellerUri);
	}

	@Override
	public URI getUriAutohersteller(Autohersteller autohersteller, UriInfo uriInfo) {
		final UriBuilder ub = uriInfo.getBaseUriBuilder()
                .path(ArtikelverwaltungResource.class)
                .path(ArtikelverwaltungResource.class, "findAutohersteller");
		final URI autoherstellerUri = ub.build(autohersteller.getId());
		return autoherstellerUri;
	}

	@Override
	public FahrzeugList findFahrzeuge(UriInfo uriInfo) throws NotFoundException {
		List<Fahrzeug> fahrzeuge = av.findAllFahrzeuge(Order.ID);
		final FahrzeugList fahrzeugliste = new FahrzeugList(fahrzeuge);
		return fahrzeugliste;
	}


	@Override
	public AutoherstellerList findAutohersteller(String name, UriInfo uriInfo)
			throws NotFoundException {
		// TODO Auto-generated method stub
		List<Autohersteller> autohersteller = null;
		
		if (name.equals("")) {
			autohersteller = av.findAllAutohersteller(Order.ID);
			
			if (autohersteller.isEmpty()) {
				throw new NotFoundException("Kein Autohersteller gefunden.");
			}
		}
		else {
			autohersteller = av.findAllAutoherstellerByName(Order.NAME);
			if (autohersteller.isEmpty()) {
				throw new NotFoundException("Kein Autohersteller gefunden.");
			}
		}
		
		AutoherstellerList autoherstellerliste = new AutoherstellerList(autohersteller);
		
		
		return autoherstellerliste;
	}
}
