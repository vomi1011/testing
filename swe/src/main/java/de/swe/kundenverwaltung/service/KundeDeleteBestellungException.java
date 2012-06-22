package de.swe.kundenverwaltung.service;

import static de.swe.util.Constants.UID;

import javax.ejb.ApplicationException;

import de.swe.kundenverwaltung.domain.AbstractKunde;

@ApplicationException(rollback = true)
public class KundeDeleteBestellungException extends AbstractKundenverwaltungException {
	private static final long serialVersionUID = UID;
	private Long kundeId;
	private int anzahlBestellungen;
	
	public KundeDeleteBestellungException(AbstractKunde kunde) {
		super("Kunde mit ID=" + kunde.getId() + " kann nicht geloescht werden: "
			  + kunde.getBestellungen().size() + " Bestellung(en)");
		this.kundeId = kunde.getId();
		this.anzahlBestellungen = kunde.getBestellungen().size();
	}

	public Long getKundeId() {
		return kundeId;
	}

	public int getAnzahlBestellungen() {
		return anzahlBestellungen;
	}
}
