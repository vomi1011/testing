package de.swe.bestellverwaltung.domain;

import static de.swe.util.Constants.BESTELLPOSITION_ID;
import static de.swe.util.Constants.ERSTE_VERSION;

import java.io.Serializable;
import java.net.URI;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import de.swe.artikelverwaltung.domain.Fahrzeug;
import de.swe.util.IdGroup;


/**
 * The persistent class for the bestellposition database table.
 * 
 */

@Entity
@Table(name = "Bestellposition")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Bestellposition implements Serializable {
	private static final long serialVersionUID = 1L;

	private static final String PREFIX = "Bestellposition.";
	public static final String FIND_BESTELLPOSITION = PREFIX + "findBestellPosition";
	public static final String FIND_VERKAUFT_FAHRZEUG_IN_BESTELLPOSITION = PREFIX + "findBestellPosition";
	public static final String PARAM_FAHRZEUG_ID = "id";
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "bp_id")
	@Min(value = BESTELLPOSITION_ID, message = "{bestellverwaltung.bestellposition.id.min}", groups = IdGroup.class)
	@XmlAttribute(name = "id")
	private Long id;

	@NotNull(message = "{bestellverwaltung.bestellposition.anzahl.notNull}")
	@XmlElement(required = true)
	//@Min(value = 1, message = "{bestellverwaltung.bestellposition.anzahl.min}")
	private short anzahl;
	
    @Transient
    @XmlElement(name = "zwischenErgebnis")
    public long total;
	
    public long getTotal() {
		return total;
	}

	public void setTotal() {
		this.total = this.fahrzeug.getPreis() * this.anzahl;
	}


    @Version
	@XmlTransient
	private int version = ERSTE_VERSION;
	
	//one-directional one-to-one association to Fahrzeug
	@OneToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "fahrzeug_fk", nullable = false)
	@NotNull(message = "{bestellverwaltung.bestellposition.fahrzeug.notNull}")
	@Valid
    @XmlTransient
	private Fahrzeug fahrzeug;

	@Transient
	@XmlElement(name = "fahrzeug")
	private URI fahrzeugUri;
	
	//notwendig für Rest
	public Bestellposition() {
	}
	
	public Bestellposition(Fahrzeug fahrzeug) {
		this.fahrzeug = fahrzeug;
		this.anzahl = 1;
	}
	
	public Bestellposition(Fahrzeug fahrzeug, short anzahl) {
		this.fahrzeug = fahrzeug;
		this.anzahl = anzahl;
	}
	
	public Fahrzeug getFahrzeug() {
		return fahrzeug;
	}

	public void setFahrzeug(Fahrzeug fahrzeug) {
		this.fahrzeug = fahrzeug;
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	//TODO Methode entfernen, wenn alle Abhaengigkeiten beseitig sind
	@Deprecated
	public Long getBpId() {
		return this.id;
	}

	//TODO Methode entfernen, wenn alle Abhaengigkeiten beseitig sind
	@Deprecated
	public void setBpId(Long bpId) {
		this.id = bpId;
	}

	public short getAnzahl() {
		return this.anzahl;
	}

	public void setAnzahl(short anzahl) {
		this.anzahl = anzahl;
	}
	
	public URI getFahrzeugUri() {
		return this.fahrzeugUri;
	}
	
	public void setFahrzeugUri(URI fahrzeugUri) {
		this.fahrzeugUri = fahrzeugUri;
	}

	@Override
	public String toString() {
		return "Bestellposition [bpId=" + id + ", anzahl=" + anzahl + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Bestellposition other = (Bestellposition) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} 
		else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

}