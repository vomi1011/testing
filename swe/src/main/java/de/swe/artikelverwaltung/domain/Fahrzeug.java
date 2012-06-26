package de.swe.artikelverwaltung.domain;
import static de.swe.util.Constants.ERSTE_VERSION;
import static de.swe.util.Constants.FAHRZEUG_ID;

import java.io.Serializable;
import java.net.URI;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.hibernate.validator.constraints.NotBlank;

import de.swe.util.IdGroup;
import de.swe.util.XmlDateAdapter;

@Entity
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Table(name = "Fahrzeug")
@Inheritance
@NamedQueries({
	@NamedQuery(name = Fahrzeug.FIND_FAHRZEUG,
		query = "FROM Fahrzeug"),
	@NamedQuery(name = Fahrzeug.FIND_FAHRZEUG_ORDER_BY_ID,
		query = "FROM Fahrzeug f order by f.id"),
	@NamedQuery(name = Fahrzeug.FIND_FAHRZEUG_BY_ID,
		query = "FROM Fahrzeug f WHERE f.id = :" + Fahrzeug.PARAM_FAHRZEUG_ID),
	@NamedQuery(name = Fahrzeug.FIND_FAHRZEUG_BY_HERSTELLER,
		query =  "FROM Fahrzeug f WHERE f.hersteller.id = :" + Fahrzeug.PARAM_FAHRZEUG_HERSTELLER),
	@NamedQuery(name = Fahrzeug.FIND_FAHRZEUG_BY_MODELL,
		query =  "FROM Fahrzeug f WHERE f.modell LIKE CONCAT(:" + Fahrzeug.PARAM_FAHRZEUG_MODELL + ", '%')"),
	@NamedQuery(name = Fahrzeug.FIND_FAHRZEUG_BY_BAUJAHR,
		query = "FROM Fahrzeug f WHERE f.baujahr = :" + Fahrzeug.PARAM_FAHRZEUG_BAUJAHR),
	@NamedQuery(name = Fahrzeug.FIND_FAHRZEUG_BY_BESCHREIBUNG,
		query = "FROM Fahrzeug f "
				+ "WHERE f.beschreibung LIKE CONCAT(:" + Fahrzeug.PARAM_FAHRZEUG_BESCHREIBUNG + ", '%')"),
	@NamedQuery(name = Fahrzeug.FIND_FAHRZEUG_BY_ERSTELLT,
		query =  "FROM Fahrzeug f WHERE f.erstellt = :" + Fahrzeug.PARAM_FAHRZEUG_ERSTELLT)
	})
public class Fahrzeug implements Serializable {
	
	private static final String PREFIX = "Fahrzeug.";
	public static final String FIND_FAHRZEUG = PREFIX + "findFahrzeug";
	public static final String FIND_FAHRZEUG_ORDER_BY_ID = PREFIX + "findFahrzeugOrderById";
	public static final String FIND_FAHRZEUG_BY_ID = PREFIX + "findFahrzeugById";
	public static final String FIND_FAHRZEUG_BY_HERSTELLER = PREFIX + "findFahrzeugByHersteller";
	public static final String FIND_FAHRZEUG_BY_MODELL = PREFIX + "findFahrzeugByModell";
	public static final String FIND_FAHRZEUG_BY_BAUJAHR = PREFIX + "findFahrzeugByBaujahr";
	public static final String FIND_FAHRZEUG_BY_BESCHREIBUNG = PREFIX + "findFahrzeugByBeschreibung";
	public static final String FIND_FAHRZEUG_BY_ERSTELLT = PREFIX + "findFahrzeugByErstellt";
	public static final String FIND_FAHRZEUG_BY_MODELL_PREFIX = PREFIX + "findFahrzeugByModellPrefix";
	public static final String PARAM_FAHRZEUG_ID = "id";
	public static final String PARAM_FAHRZEUG_HERSTELLER = "hersteller_fk";
	public static final String PARAM_FAHRZEUG_MODELL = "modell";
	public static final String PARAM_FAHRZEUG_BAUJAHR = "baujahr";
	public static final String PARAM_FAHRZEUG_BESCHREIBUNG = "beschreibung";
	public static final String PARAM_FAHRZEUG_ERSTELLT = "erstellt";

	private static final long serialVersionUID = 1L;

	@Id
	@XmlAttribute(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "f_id")
	@Min(value = FAHRZEUG_ID, message = "{artikelverwaltung.fahrzeug.id.min}", groups = IdGroup.class)
	private Long id;
	
	@Version
	@XmlTransient
	private int version = ERSTE_VERSION;

	@ManyToOne(optional = false)
	@JoinColumn(name = "hersteller_fk", nullable = false)
	@NotNull(message = "{artikelverwaltung.fahrzeug.hersteller.notNull}")
	@XmlElement(required = true)
	private Autohersteller hersteller;
	
	@Size(min = 2, max = 32, message = "{artikelverwaltung.fahrzeug.modell.size}")
	@NotBlank(message = "{artikelverwaltung.fahrzeug.modell.notBlank}")
	@XmlElement(required = true)
    private String modell;
	
	private short baujahr;

	private String beschreibung;

    private Boolean lieferbar;

	private int preis;

	@XmlJavaTypeAdapter(XmlDateAdapter.class)
    @Temporal(TemporalType.DATE)
	private Date erstellt;

	@XmlTransient
	@Temporal(TemporalType.DATE)
	private Date aktualisiert;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public Date getAktualisiert() {
		return (aktualisiert == null) ? null : (Date) aktualisiert.clone();
	}

	public void setAktualisiert(Date lastupdate) {
		this.aktualisiert = (lastupdate == null) ? null : (Date) lastupdate.clone();
	}

	public short getBaujahr() {
		return this.baujahr;
	}

	public void setBaujahr(short baujahr) {
		this.baujahr = baujahr;
	}

	public String getBeschreibung() {
		return this.beschreibung;
	}

	public void setBeschreibung(String beschreibung) {
		this.beschreibung = beschreibung;
	}

	public Date getErstellt() {
		return (erstellt == null) ? null : (Date) erstellt.clone();
	}

	public void setErstellt(Date erstellt) {
		this.erstellt = (erstellt == null) ? null : (Date) erstellt.clone();
	}

	public Boolean getLieferbar() {
		return this.lieferbar;
	}

	public void setLieferbar(Boolean lieferbar) {
		this.lieferbar = lieferbar;
	}

	public String getModell() {
		return this.modell;
	}

	public void setModell(String modell) {
		this.modell = modell;
	}

	public int getPreis() {
		return this.preis;
	}

	public void setPreis(int preis) {
		this.preis = preis;
	}

	public Autohersteller getHersteller() {
		return hersteller;
	}

	public void setHersteller(Autohersteller hersteller) {
		this.hersteller = hersteller;
	}
	
	public Fahrzeug() {
		super();
	}
	
	public Fahrzeug(String modell, Autohersteller hersteller) {
		this.modell = modell;
		this.hersteller = hersteller;
	}
	
	@Override
	public String toString() {
		return "Fahrzeug [id=" + id + ", modell=" + modell + ", baujahr="
				+ baujahr + ", beschreibung=" + beschreibung + ", lieferbar="
				+ lieferbar + ", preis=" + preis + ", erstellt=" + erstellt
				+ ", aktualisiert=" + aktualisiert + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((erstellt == null) ? 0 : erstellt.hashCode());
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
		Fahrzeug other = (Fahrzeug) obj;
		if (erstellt == null) {
			if (other.erstellt != null) {
				return false;
			}
		} 
		else if (!erstellt.equals(other.erstellt)) {
			return false;
		}
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
	
	public void setValues(Fahrzeug fahrzeug) {
		id = fahrzeug.getId();
		hersteller = fahrzeug.getHersteller();
		modell = fahrzeug.getModell();
		baujahr = fahrzeug.getBaujahr();
		beschreibung = fahrzeug.getBeschreibung();
		lieferbar = fahrzeug.getLieferbar();
		preis = fahrzeug.getPreis();
		erstellt = fahrzeug.getErstellt();
	}

	public void setHersteller(URI autoherstellerUri) {
		// TODO Auto-generated method stub
		
	}

}