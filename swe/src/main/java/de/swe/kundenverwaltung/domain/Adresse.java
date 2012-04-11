package de.swe.kundenverwaltung.domain;

import static de.swe.util.Constants.ADRESS_ID;
import static de.swe.util.Constants.ERSTE_VERSION;
import static de.swe.util.Constants.KEINE_ID;
import static de.swe.util.Constants.LONG_ANZ_ZIFFERN;
import static de.swe.util.Constants.UID;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import org.jboss.logging.Logger;

import de.swe.util.IdGroup;


/**
 * The persistent class for the adresse database table.
 * 
 */
@Entity
@XmlAccessorType(XmlAccessType.FIELD)
public class Adresse implements Serializable {
	private static final long serialVersionUID = UID;
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());

	public static final int PLZ_LENGTH_MAX = 5;
	public static final int ORT_LENGTH_MIN = 2;
	public static final int ORT_LENGTH_MAX = 50;
	public static final int STRASSE_LENGTH_MIN = 2;
	public static final int STRASSE_LENGTH_MAX = 100;
	public static final int HAUSNR_LENGTH_MAX = 4;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "a_id", unique = true, nullable = false, updatable = false, precision = LONG_ANZ_ZIFFERN)
	@Min(value = ADRESS_ID, message = "{kundenverwaltung.adresse.id.min}", groups = IdGroup.class)
	@XmlAttribute(name = "id")
	private Long id = KEINE_ID;
	
	@Version
	@XmlTransient
	private int version = ERSTE_VERSION;

	@Column(length = STRASSE_LENGTH_MAX, nullable = false)
	@NotNull(message = "{kundenverwaltung.adresse.strasse.notNull}")
	@Size(min = STRASSE_LENGTH_MIN, max = STRASSE_LENGTH_MAX, message = "{kundenverwaltung.adresse.strasse.size}")
	@XmlElement(required = true)
	private String strasse;

	@Column(length = HAUSNR_LENGTH_MAX)
	@Size(max = HAUSNR_LENGTH_MAX, message = "{kundenverwaltung.adresse.hausnr.size}")
	private String hausnr;

	@Column(length = PLZ_LENGTH_MAX, nullable = false)
	@NotNull(message = "{kundenverwaltung.adresse.plz.notNull}")
	@Pattern(regexp = "\\d{5}", message = "{kundenverwaltung.adresse.plz.pattern}")
	@XmlElement(required = true)
	private String plz;

	@Column(length = ORT_LENGTH_MAX, nullable = false)
	@NotNull(message = "{kundenverwaltung.adresse.ort.notNull}")
	@Size(min = ORT_LENGTH_MIN, max = ORT_LENGTH_MAX, message = "{kundenverwaltung.adresse.ort.size}")
	@XmlElement(required = true)
	private String ort;

	@OneToOne
	@JoinColumn(name = "kunde_fk", nullable = false)
	@NotNull(message = "{kundenverwaltung.adresse.kunde.notNull}")
	@XmlTransient
	private AbstractKunde kunde;

	@Temporal(TemporalType.DATE)
	@XmlTransient
	private Date erstellt;

	@Temporal(TemporalType.DATE)
	@XmlTransient
	private Date aktualisiert;
	
	@PostPersist
	@SuppressWarnings("unused")
	private void postPersist() {
		LOGGER.tracef("Neue Adresse mit ID=%d", id);
	}
	
	@PostUpdate
	@SuppressWarnings("unused")
	private void postUpdate() {
		LOGGER.tracef("Adresse mit ID=%d aktualisiert: version=%d", id, version);
	}
	
	public Adresse() {
		super();
	}

	public Adresse(String strasse, String hausnr, String plz, String ort) {
		this.strasse = strasse;
		this.hausnr = hausnr;
		this.plz = plz;
		this.ort = ort;
	}

	public Adresse(String strasse, String hausnr, String plz, String ort, AbstractKunde kunde) {
		this.strasse = strasse;
		this.hausnr = hausnr;
		this.plz = plz;
		this.ort = ort;
		this.kunde = kunde;
		kunde.setAdresse(this);
	}

	public Long getId() {
		return id;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getHausnr() {
		return this.hausnr;
	}

	public void setHausnr(String hausnr) {
		this.hausnr = hausnr;
	}

	public Date getErstellt() {
		return (erstellt == null) ? null : (Date) erstellt.clone();
	}

	public void setErstellt(Date erstellt) {
		this.erstellt = (erstellt == null) ? null : (Date) erstellt.clone();
	}

	public Date getAktualisiert() {
		return (aktualisiert == null) ? null : (Date) aktualisiert.clone();
	}

	public void setAktualisiert(Date aktualisiert) {
		this.aktualisiert = (aktualisiert == null) ? null : (Date) aktualisiert.clone();
	}

	public String getPlz() {
		return this.plz;
	}

	public void setPlz(String plz) {
		this.plz = plz;
	}

	public String getOrt() {
		return this.ort;
	}

	public void setOrt(String ort) {
		this.ort = ort;
	}

	public String getStrasse() {
		return this.strasse;
	}

	public void setStrasse(String strasse) {
		this.strasse = strasse;
	}

	public AbstractKunde getKunde() {
		return kunde;
	}

	public void setKunde(AbstractKunde kunde) {
		this.kunde = kunde;
	}
	
	public void setValues(Adresse adresse) {
		this.strasse = adresse.getStrasse();
		this.hausnr = adresse.getHausnr();
		this.plz = adresse.getPlz();
		this.ort = adresse.getOrt();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((hausnr == null) ? 0 : hausnr.hashCode());
		result = prime * result + ((ort == null) ? 0 : ort.hashCode());
		result = prime * result + ((plz == null) ? 0 : plz.hashCode());
		result = prime * result + ((strasse == null) ? 0 : strasse.hashCode());
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
		Adresse other = (Adresse) obj;
		if (hausnr == null) {
			if (other.hausnr != null) {
				return false;
			}
		}
		else if (!hausnr.equals(other.hausnr)) {
			return false;
		}
		if (ort == null) {
			if (other.ort != null) {
				return false;
			}
		}
		else if (!ort.equals(other.ort)) {
			return false;
		}
		if (plz == null) {
			if (other.plz != null) {
				return false;
			}
		}
		else if (!plz.equals(other.plz)) {
			return false;
		}
		if (strasse == null) {
			if (other.strasse != null) {
				return false;
			}
		}
		else if (!strasse.equals(other.strasse)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Adresse [id=" + id + ", strasse=" + strasse + ", hausnr="
				+ hausnr + ", plz=" + plz + ", ort=" + ort + ", erstellt="
				+ erstellt + ", aktualisiert=" + aktualisiert + "]";
	}
}