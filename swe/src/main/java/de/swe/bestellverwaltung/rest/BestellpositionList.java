package de.swe.bestellverwaltung.rest;

import static javax.xml.bind.annotation.XmlAccessType.FIELD;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import de.swe.bestellverwaltung.domain.Bestellposition;

@XmlRootElement(name = "bestellpositionen")
@XmlAccessorType(FIELD)
public class BestellpositionList {
	@XmlElement(name = "bestellposition")
	private List<Bestellposition> bestellpositionen;
		
	public BestellpositionList() {
		super();
	}
	
	public BestellpositionList(List<Bestellposition> bestellpositionen) {
		this.bestellpositionen = bestellpositionen;
	}

	public void setBestellpositionen(List<Bestellposition> bestellpositionen) {
		this.bestellpositionen = bestellpositionen;
	}

	public List<Bestellposition> getBestellpositionen() {
		return bestellpositionen;
	}

	@Override
	public String toString() {
		return "BestellpositionList [bestellpositionen=" + bestellpositionen + "]";
	}
}
