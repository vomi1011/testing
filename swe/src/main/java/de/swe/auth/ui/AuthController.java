package de.swe.auth.ui;

import static de.swe.util.Constants.JSF_INDEX;
import static de.swe.util.Constants.JSF_REDIRECT_SUFFIX;
import static de.swe.util.Constants.SHOP;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.jboss.logging.Logger;
import org.jboss.seam.international.status.Messages;
import org.jboss.seam.international.status.builder.BundleKey;

import de.swe.bestellverwaltung.ui.BestellverwaltungController;
import de.swe.kundenverwaltung.dao.KundenverwaltungDao.Fetch;
import de.swe.kundenverwaltung.domain.AbstractKunde;
import de.swe.kundenverwaltung.service.Kundenverwaltung;
import de.swe.util.InternalError;
import de.swe.util.Log;
import de.swe.util.RolleType;


/**
 * Ein Managed Bean zur Abwicklung von Authentifizierung (Login und Logout) und erweiterbar f&uuml;r
 * Authorisierung (rollenbasierte Berechtigungen).
 */
@Named(AuthController.NAME)
@SessionScoped
@Log
public class AuthController implements Serializable {
	private static final long serialVersionUID = -8604525347843804815L;
	
	public static final String NAME = "auth";

	private static final String MSG_KEY_LOGIN_ERROR = "login.error";
	private static final String CLIENT_ID_USERNAME = "loginFormHeader:username";
	private static final String CLIENT_ID_USERNAME_REDIRECT = "loginForm:username";
	
	private String username;
	private String password;
	
	private Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());
	
	@Produces
	@SessionScoped
	@LoggedIn
	private AbstractKunde user;
	
	@Inject
	private Kundenverwaltung kv;
	
	@Inject
	private BestellverwaltungController bv;             // Redirect
	
	@PersistenceContext
	@SuppressWarnings("unused")
	private transient EntityManager em;
	
	// TODO https://issues.jboss.org/browse/SOLDER-311
//	@Inject
//	private HttpServletRequest request;
//	private Instance<HttpServletRequest> request;
	
	@Inject
	private transient ExternalContext externalCtx;
	
	// TODO https://issues.jboss.org/browse/SOLDER-311
//	@Inject
//	private HttpSession session;

	@Inject
	private transient FacesContext facesCtx;
	
	@Inject
	private Messages messages;
	
	private List<RolleType> ausgewaehlteRollen;
	private List<RolleType> verfuegbareRollen;

	@SuppressWarnings("unused")
	@PostConstruct
	private void postConstruct() {
		LOGGER.debug("AuthController wurde erzeugt");
	}

	@SuppressWarnings("unused")
	@PreDestroy
	private void preDestroy() {
		LOGGER.debug("AuthController wird geloescht");
	}

	@Override
	public String toString() {
		return "AuthController [username=" + username + ", password=" + password + "]";
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public List<RolleType> getAusgewaehlteRollen() {
		return ausgewaehlteRollen;
	}

	public void setAusgewaehlteRollen(List<RolleType> ausgewaehlteRollen) {
		this.ausgewaehlteRollen = ausgewaehlteRollen;
	}

	public List<RolleType> getVerfuegbareRollen() {
		return verfuegbareRollen;
	}

	public void setVerfuegbareRollen(List<RolleType> verfuegbareRollen) {
		this.verfuegbareRollen = verfuegbareRollen;
	}

	/**
	 * Einloggen eines registrierten Kunden mit Benutzername und Password.
	 */
	public String login() {
		if (username == null || username.equals("")) {
			reset();
			messages.error(new BundleKey(SHOP, MSG_KEY_LOGIN_ERROR))
					.targets(CLIENT_ID_USERNAME);
			return null;
		}
		
		try {
			// TODO https://issues.jboss.org/browse/SOLDER-311
			final HttpServletRequest request = (HttpServletRequest) externalCtx.getRequest();
			request.login(username, password);
		}
		catch (ServletException e) {
			reset();
			messages.error(new BundleKey(SHOP, MSG_KEY_LOGIN_ERROR))
					.targets(CLIENT_ID_USERNAME);
			
			return null;   // Gleiche Seite nochmals aufrufen: mit den fehlerhaften Werten
		}
		
		user = kv.findKundeByEmail(username, Fetch.NUR_KUNDE);
		if (user == null) {
			logout();
			throw new InternalError("Kein Kunde mit dem Loginnamen \"" + username + "\" gefunden");
		}
		
		// Gleiche JSF-Seite erneut aufrufen: Re-Render fuer das Navigationsmenue stattfindet
		final String path = facesCtx.getViewRoot().getViewId();
		return path;
	}
	
	/**
	 * Nachtraegliche Einloggen eines registrierten Kunden mit Benutzername und Password.
	 */
	public String loginRedirect() {
		if (username == null || username.equals("")) {
			reset();
			messages.error(new BundleKey(SHOP, MSG_KEY_LOGIN_ERROR))
					.targets(CLIENT_ID_USERNAME_REDIRECT);
			return null;
		}
		
		try {
			// TODO https://issues.jboss.org/browse/SOLDER-311
			final HttpServletRequest request = (HttpServletRequest) externalCtx.getRequest();
			request.login(username, password);
		}
		catch (ServletException e) {
			reset();
			messages.error(new BundleKey(SHOP, MSG_KEY_LOGIN_ERROR))
					.targets(CLIENT_ID_USERNAME_REDIRECT);
			
			return null;   // Gleiche Seite nochmals aufrufen: mit den fehlerhaften Werten
		}
		
		user = kv.findKundeByEmail(username, Fetch.NUR_KUNDE);
		if (user == null) {
			logout();
			throw new InternalError("Kein Kunde mit dem Loginnamen \"" + username + "\" gefunden");
		}
		
		// Redirect auf die urspruengliche Seite
		final Map<String, Object> sessionMap = externalCtx.getSessionMap();
		final String methodOrigin = (String) sessionMap.get("methodOrigin");
		switch (methodOrigin) {
			case "bestellen":
				sessionMap.remove("methodOrigin");
				return bv.bestellen();
			default:
				throw new InternalError("Die Methode " + methodOrigin
						                + " ist nicht fuer Login-Umleitung implementiert");
		}
	}

	/**
	 */
	private void reset() {
		username = null;
		password = null;
		user = null;
	}

	
	/**
	 * Ausloggen und L&ouml;schen der gesamten Session-Daten.
	 */
	public String logout() {
		try {
			// TODO https://issues.jboss.org/browse/SOLDER-311
			final HttpServletRequest request = (HttpServletRequest) externalCtx.getRequest();
			request.logout();  // Der Loginname wird zurueckgesetzt
		}
		catch (ServletException e) {
			return null;
		}
		
		reset();
		// TODO https://issues.jboss.org/browse/SOLDER-311
		//session.invalidate();
		externalCtx.invalidateSession();
		
		// redirect bewirkt neuen Request, der *NACH* der Session ist
		return JSF_INDEX + JSF_REDIRECT_SUFFIX;
	}

	/**
	 * &Uuml;berpr&uuml;fen, ob Login-Informationen vorhanden sind.
	 * @return true, falls man eingeloggt ist.
	 */
	public boolean isLoggedIn() {
		return user != null;
	}
	
	public void loadRollen() {
		verfuegbareRollen = Arrays.asList(RolleType.values());
		ausgewaehlteRollen = kv.getEigeneRollen();
	}
	
	public String updateRollen() {
		// Ist-Zustand: Eigene Rollen (macht eigentlich keinen Sinn, dass man sich selbst Rollen zuordnen kann)
		final List<RolleType> eigeneRollen = kv.getEigeneRollen();
		
		// Zusaetzliche Rollen?
		final List<RolleType> zusaetzlicheRollen = new ArrayList<>();
		for (RolleType rolle : ausgewaehlteRollen) {
			if (!eigeneRollen.contains(rolle)) {
				zusaetzlicheRollen.add(rolle);
			}
		}
		RolleType[] rollenArray = new RolleType[zusaetzlicheRollen.size()];
		zusaetzlicheRollen.toArray(rollenArray);
		kv.addRollen(user.getId(), rollenArray);
		
		// Zu entfernende Rollen?
		final List<RolleType> zuEntfernendeRollen = new ArrayList<>();
		for (RolleType rolle : eigeneRollen) {
			if (!ausgewaehlteRollen.contains(rolle)) {
				zuEntfernendeRollen.add(rolle);
			}
		}
		rollenArray = new RolleType[zuEntfernendeRollen.size()];
		zuEntfernendeRollen.toArray(rollenArray);
		kv.removeRollen(user.getId(), rollenArray);
		
		return JSF_INDEX;
	}
}
