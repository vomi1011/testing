package de.swe.bestellverwaltung.ui;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.logging.Logger;

import de.swe.artikelverwaltung.domain.Fahrzeug;
import de.swe.artikelverwaltung.service.Artikelverwaltung;
import de.swe.bestellverwaltung.domain.Bestellposition;

@Named("wk")
@ConversationScoped
//@Log
public class Warenkorb implements Serializable {
	private static final long serialVersionUID = -1981070683990640854L;

	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());;
	
	private static final String JSF_VIEW_WARENKORB = "/bestellverwaltung/viewWarenkorb?init=true";
	private static final int TIMEOUT = 5;
	
	private final List<Bestellposition> positionen = new ArrayList<Bestellposition>();;
	private Long artikelId;
	
	@Inject
	private transient Conversation conversation;
	
	@Inject
	private Artikelverwaltung av;

	@SuppressWarnings("unused")
	@PostConstruct
	private void postConstruct() {
		LOGGER.debug("Warenkorb wird erzeugt");
	}
	
	@SuppressWarnings("unused")
	@PreDestroy
	private void preDestroy() {
		LOGGER.debug("Warenkorb wird geloescht");
	}
	
	public List<Bestellposition> getPositionen() {
		return positionen;
	}
		
	public void setArtikelId(Long artikelId) {
		this.artikelId = artikelId;
	}

	public Long getArtikelId() {
		return artikelId;
	}

	@Override
	public String toString() {
		return "Warenkorb [" + positionen.toString() + "]";
	}
	
	/**
	 */
	public String add(Fahrzeug fahrzeug) {
		beginConversation();
		
		for (Bestellposition bp : positionen) {
			if (bp.getFahrzeug().equals(fahrzeug)) {
				// bereits im Warenkorb
				short vorhandeneAnzahl = bp.getAnzahl();
				bp.setAnzahl((short) (vorhandeneAnzahl + 1));
				return JSF_VIEW_WARENKORB;
			}
		}
		
		Bestellposition neu = new Bestellposition(fahrzeug);
		positionen.add(neu);
		return JSF_VIEW_WARENKORB;
	}
	
	/**
	 */
	public String add() {
		final Fahrzeug artikel = av.findFahrzeugById(artikelId);
		if (artikel == null) {
			return null;
		}
		
		final String outcome = add(artikel);
		artikelId = null;
		return outcome;
	}
	
	/**
	 */
	public void beginConversation() {
		if (!conversation.isTransient()) {
			return;
		}
		conversation.begin();
		conversation.setTimeout(TimeUnit.MINUTES.toMillis(TIMEOUT));
		LOGGER.trace("Conversation beginnt");
	}
	
	/**
	 */
	public void endConversation() {
		conversation.end();
		LOGGER.trace("Conversation beendet");
	}
	
	/**
	 */
	public void remove(Bestellposition bestellposition) {
		positionen.remove(bestellposition);
		if (positionen.isEmpty()) {
			endConversation();
		}
	}
}
