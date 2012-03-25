package de.swe.kundenverwaltung.rest;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import de.swe.kundenverwaltung.domain.AbstractKunde;

@XmlRootElement(name = "kunden")
@XmlAccessorType(XmlAccessType.FIELD)
public class KundeList {
	@XmlElementRef
	private List<AbstractKunde> kunden;
	
	public KundeList() {
		super();
	}
	
	public KundeList(List<AbstractKunde> kunden) {
		this.kunden = kunden;
	}

	public List<AbstractKunde> getKunden() {
		return kunden;
	}

	public void setKunden(List<AbstractKunde> kunden) {
		this.kunden = kunden;
	}

	@Override
	public String toString() {
		return "KundeList [kunden=" + kunden + "]";
	}
}
