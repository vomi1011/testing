package de.swe.util;

import java.io.Serializable;
import java.util.Locale;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.seam.international.Alter;
import org.jboss.solder.core.Client;


@Named("sprache")
@SessionScoped
@Log
public class SpracheController implements Serializable {
	private static final long serialVersionUID = 1986565724093259408L;
	
	@Inject
	@Client                  // Attributwert bezieht sich auf den Client
	private Locale locale;   // #{userLocale} in JSF-Seiten, vor allem Template-Seiten
	
	@Inject
	@Alter                   // Event loest eine Aenderung aus
	@Client                  // Attributwert bezieht sich auf den Client
	private transient Event<Locale> localeEvent;
	
	/**
	 */
	@Override
	public String toString() {
		return "SpracheController [locale=" + locale + "]";
	}

	/**
	 */
	public void change(String localeStr) {
		final Locale newLocale = new Locale(localeStr);
		if (newLocale.equals(locale)) {
			return;
		}
		locale = newLocale;
		localeEvent.fire(locale);
	}
}
