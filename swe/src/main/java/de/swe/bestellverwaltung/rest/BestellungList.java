package de.swe.bestellverwaltung.rest;

import static javax.xml.bind.annotation.XmlAccessType.FIELD;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import de.swe.bestellverwaltung.domain.Bestellung;

@XmlRootElement(name = "bestellungen")
@XmlAccessorType(FIELD)
public class BestellungList {
	@XmlElement(name = "bestellung")
	private List<Bestellung> bestellungen;
		
	public BestellungList() {
		super();
	}
	
	public BestellungList(List<Bestellung> bestellungen) {
		this.bestellungen = bestellungen;
	}

	public void setBestellungen(List<Bestellung> bestellungen) {
		this.bestellungen = bestellungen;
	}

	public List<Bestellung> getBestellungen() {
		return bestellungen;
	}
	
	@Override
	public String toString() {
		return "BestellungListe [bestellungen=" + bestellungen + "]";
	}
}
