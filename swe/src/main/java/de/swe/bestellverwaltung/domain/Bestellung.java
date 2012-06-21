package de.swe.bestellverwaltung.domain;

import static de.swe.util.Constants.ERSTE_VERSION;
import static de.swe.util.Constants.BESTELLUNG_ID;
import static javax.persistence.CascadeType.ALL;
import static javax.persistence.EnumType.STRING;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.hibernate.validator.constraints.NotEmpty;

import de.swe.kundenverwaltung.domain.AbstractKunde;
import de.swe.util.IdGroup;
import de.swe.util.XmlDateAdapter;


/**
 * The persistent class for the bestellung database table.
 * 
 */

@Entity
@Table(name = "Bestellung")
@Inheritance
@NamedQueries({
	@NamedQuery(name = Bestellung.FIND_BESTELLUNGEN,
			query = "FROM Bestellung"),
	@NamedQuery(name = Bestellung.FIND_BESTELLUNG_BY_ID,
			query = "FROM Bestellung b where b.id = :" + Bestellung.PARAM_BESTELL_ID),	
	@NamedQuery(name = Bestellung.FIND_BESTELLUNG_BY_KUNDEN_ID,
			query = "FROM Bestellung b where b.kunde.id = :" + Bestellung.PARAM_KUNDE_ID),
	@NamedQuery(name = Bestellung.FIND_BESTELLUNG_BY_DATUM,
			query = "FROM Bestellung b where date_format(b.bestelldatum, '%Y-%m-%d') = :" 
					+ Bestellung.PARAM_DATUM),
	@NamedQuery(name = Bestellung.FIND_BESTELLUNGEN_BY_STATUS,
			query = "FROM Bestellung b where b.status = :" + Bestellung.PARAM_STATUS_DEFAULT),
})

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Bestellung implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@XmlEnum
	public enum Status {
		NEU,
		BEARBEITET,
		ABGEHOLT,
		STORNIERT;
	}
	
	private static final String PREFIX = "Bestellung.";
	public static final String FIND_BESTELLUNGEN = PREFIX + "findBestellungen";
	public static final String FIND_BESTELLUNG_BY_ID = PREFIX + "findBestellungById";
	public static final String FIND_BESTELLUNG_BY_KUNDEN_ID = PREFIX + "findBestellungByKundenId";
	public static final String FIND_BESTELLUNG_BY_DATUM = PREFIX + "findBestellungByDatum";	
	public static final String FIND_BESTELLUNGEN_BY_STATUS = PREFIX + "findBestellungByStatus";	
	public static final String PARAM_KUNDE_ID = "kundenid";
	public static final String PARAM_DATUM = "datum";
	public static final String PARAM_BESTELL_ID = "bestellId";
	public static final String PARAM_STATUS_DEFAULT = "status";
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "b_id")
	@Min(value = BESTELLUNG_ID, message = "{bestellverwaltung.bestellung.id.min}", groups = IdGroup.class)
	@XmlAttribute(name = "id")
	private Long id;
	
	@Version
	@XmlTransient
	private int version = ERSTE_VERSION;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "kunde_fk", nullable = false, insertable = false, updatable = false)
	@NotNull(message = "{bestellverwaltung.bestellung.kunde.notNull}")
    @XmlTransient
	private AbstractKunde kunde;

	//one-directional many-to-one association to Bestellposition
	@OneToMany(fetch = FetchType.EAGER, cascade = ALL)
	@JoinColumn(name = "bestellung_fk", nullable = false)
	@OrderColumn(name = "idx", nullable = false)
	@NotEmpty(message = "{bestellverwaltung.bestellung.bestellpositionen.notEmpty}")
	@XmlElementWrapper(name = "bestellpositionen", required = true)
	@XmlElement(name = "bestellposition", required = true)
	private List<Bestellposition> bestellpositionen;
	
	@Column(name = "status", nullable = false)
	@Enumerated(STRING)
	private Status status;
	
	@Temporal(TemporalType.DATE)
	@XmlJavaTypeAdapter(XmlDateAdapter.class)
	private Date bestelldatum;
	
    @Temporal(TemporalType.DATE)
    @XmlTransient
	private Date aktualisiert;
    
	@Transient
	@XmlElement(name = "kunde")
	private URI kundeUri;
	
	@Transient
	@XmlElement(name = "bestellurl")
	private URI bestellungUri;

	public AbstractKunde getKunde() {
		return kunde;
	}

	public void setKunde(AbstractKunde kunde) {
		this.kunde = kunde;
	}
	
	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((bestelldatum == null) ? 0 : bestelldatum.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
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
		Bestellung other = (Bestellung) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} 
		else if (!id.equals(other.id)) {
			return false;
		}
		if (bestelldatum == null) {
			if (other.bestelldatum != null) {
				return false;
			}
		} 
		else if (!bestelldatum.equals(other.bestelldatum)) {
			return false;
		}
		return true;
	}

	//TODO Methode entfernen, wenn alle Abhaengigkeiten beseitig sind
	@Deprecated
	public void setBId(Long bId) {
		this.id = bId;
	}
	
	//TODO Methode entfernen, wenn alle Abhaengigkeiten beseitig sind
	@Deprecated
	public Long getBId() {
		return this.id;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getAktualisiert() {
		return this.aktualisiert == null ? null : (Date) aktualisiert.clone();
	}

	public void setAktualisiert(Date aktualisiert) {
		this.aktualisiert = aktualisiert == null ? null : (Date) aktualisiert.clone();
	}

	public List<Bestellposition> getBestellpositionen() {
		return Collections.unmodifiableList(this.bestellpositionen);
	}

	public void setBestellpositionen(List<Bestellposition> bestellpositionen) {
		if (this.bestellpositionen == null) {
			this.bestellpositionen = bestellpositionen;
			return;
		}
		this.bestellpositionen.clear();
		if (this.bestellpositionen != null) {
			this.bestellpositionen.addAll(bestellpositionen);
		}
	}
	
	public Bestellung addBestellposition(Bestellposition bestellposition) {
		if (this.bestellpositionen == null)	{
			bestellpositionen = new ArrayList<Bestellposition>();
		}
		
		bestellpositionen.add(bestellposition);
		return this;
	}
	
	public Bestellung removeBestellposition(Bestellposition bestellposition) {
		if (this.bestellpositionen == null) {
			return this;
		}
		
		if (bestellposition == null) {
			return this;
		}
		
		int indexBestellposition = bestellpositionen.indexOf(bestellposition);
		this.bestellpositionen.remove(indexBestellposition);
		
		return this;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Bestellung [Bestellnummer=" + id + ", Status= " + status
				+ ", Bestelldatum=" + bestelldatum + "]";
	}

	public Date getBestelldatum() {
		return bestelldatum == null ? null : (Date) bestelldatum.clone();
	}

	public void setBestelldatum(Date bestelldatum) {
		this.bestelldatum = bestelldatum == null ? null : (Date) bestelldatum.clone();
	}

	public Status getStatus() {
		return this.status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}
	
	@PrePersist
	protected void prePersist() {
		this.aktualisiert = new Date();
	}
	
	@PreUpdate
	protected void preUpdate() {
		this.aktualisiert = new Date();
	}
	
	public URI getKundeUri() {
		return this.kundeUri;
	}
	
	public void setKundeUri(URI kundeUri) {
		this.kundeUri = kundeUri;
	}
	
	public URI getBestellungUri() {
		return this.bestellungUri;
	}
	
	public void setBestellungUri(URI bestellungUri) {
		this.bestellungUri = bestellungUri;
	}
}
	
