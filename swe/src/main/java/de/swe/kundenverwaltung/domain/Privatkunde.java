package de.swe.kundenverwaltung.domain;

import static de.swe.util.JpaConstants.UID;

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
@DiscriminatorValue(AbstractKunde.PRIVATKUNDE)
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Privatkunde extends AbstractKunde {
	private static final long serialVersionUID = UID;
	
	
	public Privatkunde() {
		super();
	}
	
	public Privatkunde(String nachname, String email, Adresse adresse) {
		super(nachname, email, adresse);
	}
	
	public Privatkunde(String nachname, String email, Adresse adresse, String password) {
		super(nachname, email, adresse, password);
	}

	@Override
	public String getArt() {
		return PRIVATKUNDE;
	}
	
	@Override
	public String toString() {
		return "Privatkunde [id=" + getId() + ", nachname="
				+ getNachname() + ", vorname=" + getVorname()
				+ ", email=" + getEmail() + ", telefon="
				+ getTelefon() + ", geschlecht=" + getGeschlecht()
				+ ", erstellt=" + getErstellt() + ", aktualisiert="
				+ getAktualisiert() + "]";
	}
}
