package de.swe.kundenverwaltung.domain;

import static de.swe.util.Constants.UID;

import javax.persistence.Cacheable;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Cacheable
@Inheritance
@DiscriminatorValue(AbstractKunde.FIRMENKUNDE)
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Firmenkunde extends AbstractKunde {
	private static final long serialVersionUID = UID;
	
	private String firma;

	public Firmenkunde() {
		super();
	}
	
	public Firmenkunde(String nachname, String email, Adresse adresse) {
		super(nachname, email, adresse);
	}
	
	public Firmenkunde(String nachname, String email, Adresse adresse, String password) {
		super(nachname, email, adresse, password);
	}

	public String getFirma() {
		return firma;
	}

	public void setFirma(String firma) {
		this.firma = firma;
	}
	
	@Override
	public void setValues(AbstractKunde kunde) {
		super.setValues(kunde);
		this.firma = ((Firmenkunde) kunde).getFirma();
	}
	
	@Override
	public String getArt() {
		return FIRMENKUNDE;
	}

	@Override
	public String toString() {
		return "Firmenkunde [id=" + getId() + ", firma=" + firma
				+ ", nachname=" + getNachname() + ", vorname="
				+ getVorname() + ", email=" + getEmail()
				+ ", telefon=" + getTelefon() + ", geschlecht="
				+ getGeschlecht() + ", erstellt=" + getErstellt()
				+ ", aktualisiert=" + getAktualisiert() + "]";
	}
	
}
