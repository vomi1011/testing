package de.swe.util;


import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import javax.validation.MessageInterpolator;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.jboss.logging.Logger;


/*
 * Bei Verwendung von JSF werden die Attribute bereits in der
 * Praesentationsschicht validiert.
 */
@Singleton
public class ValidationService implements Serializable {
	private static final long serialVersionUID = 7886864531128694923L;
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());

	private String[] locales;   // META-INF\seam-beans.xml
	
	private Locale defaultLocale;
	private transient HashMap<Locale, Validator> validators;
	
	@SuppressWarnings("unused")
	@PostConstruct
	private void init() {
		validators = new HashMap<>();

		if (locales == null || locales.length == 0) {
			LOGGER.error("In META-INF/seam-beans.xml sind keine Sprachen eingetragen");
			return;
		}
		
		final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();

		for (String localeStr : locales) {
			if (localeStr == null || localeStr.isEmpty()) {
				continue;
			}
			final Locale locale = new Locale(localeStr);
			
			// Erste Sprache als Default
			if (defaultLocale == null) {
				defaultLocale = locale;
			}
		
			final MessageInterpolator interpolator = validatorFactory.getMessageInterpolator();
			final MessageInterpolator localeInterpolator =
				                      new LocaleSpecificMessageInterpolator(interpolator,
                                                                            locale);
			final Validator validator = validatorFactory.usingContext()
                                                        .messageInterpolator(localeInterpolator)
                                                        .getValidator();
			validators.put(locale, validator);
		}

		if (validators.keySet() == null || validators.keySet().isEmpty()) {
			LOGGER.error("In META-INF/seam-beans.xml sind keine Sprachen eingetragen");
			return;
		}
		LOGGER.infof("Locales fuer die Fehlermeldungen bei Bean Validation: %s", validators.keySet());
	}
	
	/*
	 * JSF liefert Locale durch FacesContext.getCurrentInstance().getViewRoot().getLocale();
	 * JAX-RS liefert List<Locale> durch HttpHeaders.getAcceptableLanguages() mit absteigenden Prioritaeten.
	 */
	public Validator getValidator(Locale locale) {
		Validator validator = validators.get(locale);
		if (validator != null) {
			return validator;
		}
			
		if (!locale.getCountry().isEmpty()) {
			// z.B. de_DE
			locale = new Locale(locale.getLanguage());  // z.B. de_DE -> de
			validator = validators.get(locale);
			if (validator != null) {
				return validator;
			}
		}

		return validators.get(defaultLocale);
	}
	
	/**
	 * http://hibernate.org/~emmanuel/validation
	 * 
	 * ResourceBundle.getBundle(String, Locale, ClassLoader)
	 * ResourceBundleMessageInterpolator.loadBundle(ClassLoader, Locale, String) line: 206	
	 * ResourceBundleMessageInterpolator.getFileBasedResourceBundle(Locale) line: 177	
	 * ResourceBundleMessageInterpolator.findUserResourceBundle(Locale) line: 284	
	 * ResourceBundleMessageInterpolator.interpolateMessage(String, Map<String,Object>, Locale) line: 123	
	 * ResourceBundleMessageInterpolator.interpolate(String, MessageInterpolator$Context, Locale) line: 101	
	 * ValidationUtil$LocaleSpecificMessageInterpolator.interpolate(String, MessageInterpolator$Context) line: 100	
	 */
	public static class LocaleSpecificMessageInterpolator implements MessageInterpolator {
		private final MessageInterpolator interpolator;
		private final Locale locale;

		public LocaleSpecificMessageInterpolator(MessageInterpolator interpolator, Locale locale) {
			this.locale = locale;
			this.interpolator = interpolator;
		}

		@Override
		public String interpolate(String message, Context context) {
			final String resultMessage = interpolator.interpolate(message, context, locale);
			return resultMessage;
		}

		@Override
		public String interpolate(String message, Context context, Locale loc) {
			return interpolator.interpolate(message, context, loc);
		}
	}
}
