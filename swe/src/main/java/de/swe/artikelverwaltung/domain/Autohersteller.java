package de.swe.artikelverwaltung.domain;

import static javax.persistence.CascadeType.PERSIST;
import static javax.persistence.CascadeType.REMOVE;
import static de.swe.util.JpaConstants.UID;
import static de.swe.util.JpaConstants.AUTOHERSTELLER_ID;
import static de.swe.util.JpaConstants.KEINE_ID;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import de.swe.util.IdGroup;


//TODO Kommentare entfernen

@Entity

//XML Annotation
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)

@Table(name = "Autohersteller")
@Inheritance
@NamedQueries({
	@NamedQuery(name = Autohersteller.FIND_AUTOHERSTELLER,
		query = "FROM Autohersteller"),
	@NamedQuery(name = Autohersteller.FIND_AUTOHERSTELLER_ORDER_BY_ID,
		query = "FROM Autohersteller a order by a.aId"),
	@NamedQuery(name = Autohersteller.FIND_AUTOHERSTELLER_BY_ID,
	query = "FROM Autohersteller a WHERE a.aId = :" + Autohersteller.PARAM_ID),
	@NamedQuery(name = Autohersteller.FIND_AUTOHERSTELLER_BY_NAME,
	query =  "FROM Autohersteller a WHERE a.name = :" + Autohersteller.PARAM_NAME)
	
})
public class Autohersteller implements Serializable {

	private static final String NAME_PATTERN = "[A-Z\u00C4\u00D6\u00DC][a-z\u00E4\u00F6\u00FC\u00DF]+";
	
	public static final String HERSTELLERNAME_PATTERN = NAME_PATTERN + "(-" + NAME_PATTERN + ")?";
	private static final String PREFIX = "Autohersteller.";
	public static final String FIND_AUTOHERSTELLER = PREFIX + "findAutohersteller";
	public static final String FIND_AUTOHERSTELLER_ORDER_BY_ID = PREFIX + "findAutoherstellerOrderById";
	public static final String FIND_AUTOHERSTELLER_BY_ID = PREFIX + "findAutoherstellerById";
	public static final String FIND_AUTOHERSTELLER_BY_NAME = PREFIX + "findAutoherstellerByHersteller";
	public static final String PARAM_ID = "id";
	public static final String PARAM_NAME = "name";
		
	private static final long serialVersionUID = UID;

	@Id
	@XmlAttribute(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "a_id")
	@Min(value = AUTOHERSTELLER_ID, message = "{artikelverwaltung.autohersteller.id.min}", groups = IdGroup.class)
	private Long aId = KEINE_ID;
	
	@XmlElement(required = true)
	@NotNull(message = "{artikelverwaltung.autohersteller.name.notNull}")
	@Pattern(regexp = HERSTELLERNAME_PATTERN, message = "{artikelverwaltung.autohersteller.name.pattern}")
	private String name;
	
	//bi-directional one-to-many association to Fahrzeug
	@OneToMany(mappedBy = "hersteller", cascade = { PERSIST, REMOVE })
//	@NotNull(message = "{artikelverwaltung.autohersteller.fahrzeug.notNull}")
	@XmlTransient
	private List<Fahrzeug> fahrzeuge;

	public Long getAId() {
		return aId;
	}

	public void setAId(Long aId) {
		this.aId = aId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	
	public List<Fahrzeug> getFahrzeuge() {
		return fahrzeuge;
	}

	public void setFahrzeuge(List<Fahrzeug> fahrzeuge) {
		this.fahrzeuge = fahrzeuge;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((aId == null) ? 0 : aId.hashCode());
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
		Autohersteller other = (Autohersteller) obj;
		if (aId == null) {
			if (other.aId != null) {
				return false;
			}
		}
		else if (!aId.equals(other.aId)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Autohersteller [aId=" + aId + ", name=" + name + "]";
	}

	public void addFahrzeug(Fahrzeug fahrzeug) {
		if (fahrzeuge == null) {
			fahrzeuge = new ArrayList<Fahrzeug>();
		}
		
		if (fahrzeug != null) {
			fahrzeuge.add(fahrzeug);
		}	
	}

	public void setValues(Autohersteller autohersteller) {
		name = autohersteller.getName();
		
	}
	
}
