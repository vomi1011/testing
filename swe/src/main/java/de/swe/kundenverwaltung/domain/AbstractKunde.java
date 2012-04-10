package de.swe.kundenverwaltung.domain;

import static de.swe.util.Constants.KEINE_ID;
import static de.swe.util.Constants.KUNDEN_ID;
import static de.swe.util.Constants.LONG_ANZ_ZIFFERN;
import static de.swe.util.Constants.UID;
import static javax.persistence.CascadeType.PERSIST;
import static javax.persistence.CascadeType.REMOVE;
import static javax.persistence.CascadeType.MERGE;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.hibernate.validator.constraints.Email;

import de.swe.bestellverwaltung.domain.Bestellung;
import de.swe.util.IdGroup;
import de.swe.util.XmlDateAdapter;

@Entity
@Table(name = "kunde")
@Inheritance
@DiscriminatorColumn(name = "art", length = 1)
@NamedQueries({
	@NamedQuery(name = AbstractKunde.FIND_KUNDEN,
				query = "SELECT k "
						+ "FROM AbstractKunde k"),
	@NamedQuery(name = AbstractKunde.FIND_KUNDEN_FETCH_BESTELLUNGEN,
				query = "SELECT DISTINCT k "
						+ "FROM AbstractKunde k LEFT JOIN FETCH k.bestellungen"),
	@NamedQuery(name = AbstractKunde.FIND_KUNDEN_ORDER_BY_ID,
				query = "SELECT k "
						+ "FROM AbstractKunde k order by k.id"),
	@NamedQuery(name = AbstractKunde.FIND_KUNDEN_BY_ID,
				query = "FROM AbstractKunde k "
						+ "WHERE k.id = :" + AbstractKunde.PARAM_KUNDE_ID),
	@NamedQuery(name = AbstractKunde.FIND_KUNDEN_BY_EMAIL,
				query = "SELECT k "
						+ "FROM AbstractKunde k "
						+ "WHERE k.email = :" + AbstractKunde.PARAM_KUNDE_EMAIL),
	@NamedQuery(name = AbstractKunde.FIND_KUNDEN_BY_NACHNAME,
				query = "SELECT k "
						+ "FROM AbstractKunde k "
						+ "WHERE k.nachname = :" + AbstractKunde.PARAM_KUNDE_NACHNAME),
	@NamedQuery(name = AbstractKunde.FIND_KUNDEN_BY_PLZ,
				query = "SELECT k "
						+ "FROM AbstractKunde k "
						+ "WHERE k.adresse.plz = :" + AbstractKunde.PARAM_KUNDE_ADRESSE_PLZ),
	@NamedQuery(name = AbstractKunde.FIND_KUNDEN_BY_ERSTELLT,
				query = "SELECT k "
						+ "FROM AbstractKunde k "
						+ "WHERE DATE_FORMAT(k.erstellt, '%Y-%m-%d') = :"
						+ AbstractKunde.PARAM_KUNDE_ERSTELLT),
	@NamedQuery(name = AbstractKunde.FIND_ANZ_KUNDEN_ALL,
				query = "SELECT COUNT(k) "
						+ "FROM AbstractKunde k"),
	@NamedQuery(name = AbstractKunde.FIND_ANZ_KUNDEN_BY_ART,
				query = "SELECT COUNT(k) "
						+ "FROM AbstractKunde k "
						+ "WHERE TYPE(k) =  :" + AbstractKunde.PARAM_KUNDE_ART),
	@NamedQuery(name = AbstractKunde.FIND_KUNDEN_BY_ID_FETCH_BESTELLUNGEN,
				query = "SELECT DISTINCT k "
						+ "FROM AbstractKunde k LEFT JOIN FETCH k.bestellungen "
						+ "WHERE k.id = :" + AbstractKunde.PARAM_KUNDE_ID),
	@NamedQuery(name = AbstractKunde.FIND_KUNDEN_BY_NACHNAME_FETCH_BESTELLUNGEN,
				query = "SELECT DISTINCT k "
						+ "FROM AbstractKunde k LEFT JOIN FETCH k.bestellungen "
						+ "WHERE k.nachname = :" + AbstractKunde.PARAM_KUNDE_NACHNAME),
	@NamedQuery(name = AbstractKunde.FIND_KUNDEN_BY_EMAIL_FETCH_BESTELLUNGEN,
				query = "SELECT DISTINCT k "
						+ "FROM AbstractKunde k LEFT JOIN FETCH k.bestellungen "
						+ "WHERE k.email = :" + AbstractKunde.PARAM_KUNDE_EMAIL)
})
//isPasswordEqual verwenden
//@ScriptAssert(lang = "javascript",
//			  script = "(_this.password == null && _this.passwordWdh == null)"
//					   + "|| (_this.password != null) && (_this.password.equals(_this.passwordWdh))",
//			  message = "{kundenverwaltung.kunde.password.notequal}",
//			  groups = PasswordGroup.class)
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({
	Privatkunde.class,
	Firmenkunde.class
})
public abstract class AbstractKunde implements Serializable {
	private static final long serialVersionUID = UID;
	
	private static final String NAME_PATTERN = "[A-Z\u00C4\u00D6\u00DC][a-z\u00E4\u00F6\u00FC\u00DF]+";
	private static final String PREFIX_ADEL = "(o'|von|von der|von und zu|van)?";
	
	public static final String NACHNAME_PATTERN = PREFIX_ADEL + NAME_PATTERN + "(-" + NAME_PATTERN + ")?";
	public static final int NACHNAME_LENGTH_MIN =  2;
	public static final int NACHNAME_LENGTH_MAX =  40;
	public static final int VORNAME_LENGTH_MAX =  40;
	public static final int EMAIL_LENGTH_MAX = 100;
	public static final int TELEFON_LENGTH_MAX = 20;
	public static final int GESCHLECHT_LENGTH_MAX = 1;
	public static final int PASSWORD_LENGTH_MIN = 5;
	public static final int PASSWORD_LENGTH_MAX = 50;
	
	public static final String PRIVATKUNDE = "P";
	public static final String FIRMENKUNDE = "F";
	
	private static final String PREFIX = "AbstractKunde.";
	public static final String FIND_KUNDEN = PREFIX + "findKunden";
	public static final String FIND_KUNDEN_FETCH_BESTELLUNGEN = PREFIX + "findKundenFetchBestellungen";
	public static final String FIND_KUNDEN_ORDER_BY_ID = PREFIX + "findKundenOrderById";
	public static final String FIND_KUNDEN_BY_ID = PREFIX + "findKundenById";
	public static final String FIND_KUNDEN_BY_EMAIL = PREFIX + "findKundenEmail";
	public static final String FIND_KUNDEN_BY_NACHNAME = PREFIX + "findKundenByNachname";
	public static final String FIND_KUNDEN_BY_PLZ = PREFIX + "findKundenByPlz";
	public static final String FIND_KUNDEN_BY_ERSTELLT = PREFIX + "findKundenByErstellt";
	public static final String FIND_ANZ_KUNDEN_ALL = PREFIX + "findAnzKundenAll";
	public static final String FIND_ANZ_KUNDEN_BY_ART = PREFIX + "findAnzKundenByArt";
	public static final String FIND_KUNDEN_BY_ID_FETCH_BESTELLUNGEN =
				   PREFIX + "findKundenByIdFetchBestellungen";
	public static final String FIND_KUNDEN_BY_NACHNAME_FETCH_BESTELLUNGEN =
				   PREFIX + "findKundenByNachnameFetchBestellungen";
	public static final String FIND_KUNDEN_BY_EMAIL_FETCH_BESTELLUNGEN = 
				   PREFIX + "findKundenByEmailFetchBestellungen";
	
	public static final String PARAM_KUNDE_ID = "id";
	public static final String PARAM_KUNDE_EMAIL = "email";
	public static final String PARAM_KUNDE_NACHNAME = "nachname";
	public static final String PARAM_KUNDE_ADRESSE_PLZ = "plz";
	public static final String PARAM_KUNDE_ERSTELLT = "erstellt";
	public static final String PARAM_KUNDE_ART = "art";

	private static final int TELFON_LENGTH_MAX = 0;
 
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "k_id", unique = true, nullable = false, updatable = false, precision = LONG_ANZ_ZIFFERN)
	@Min(value = KUNDEN_ID, message = "{kundenverwaltung.kunde.id.min}", groups = IdGroup.class)
	@XmlAttribute(name = "id")
	private Long id = KEINE_ID;

	@Column(length = NACHNAME_LENGTH_MAX, nullable = false)
	@NotNull(message = "{kundenverwaltung.kunde.nachname.notnull}")
	@Size(min = NACHNAME_LENGTH_MIN, max = NACHNAME_LENGTH_MAX, message = "{kundenverwaltung.kunde.nachname.size}")
	@Pattern(regexp = NACHNAME_PATTERN, message = "{kundenverwaltung.kunde.nachname.pattern}")
	@XmlElement(required = true)
	private String nachname;

	@Column(length = VORNAME_LENGTH_MAX)
	@Size(max = VORNAME_LENGTH_MAX, message = "{kundenverwaltung.kunde.vorname.size}")
	private String vorname;

	@Column(length = EMAIL_LENGTH_MAX, nullable = false, unique = true)
	@Email(message = "{kundenverwaltung.kunde.email.pattern}")
	private String email;

	@Column(length = TELFON_LENGTH_MAX)
	@Pattern(regexp = "\\d*", message = "{kundenverwaltung.kunde.telefon.pattern}")
	private String telefon;

	@Column(length = GESCHLECHT_LENGTH_MAX)
	private String geschlecht;

	@Column(length = PASSWORD_LENGTH_MAX)
	@Size(min = PASSWORD_LENGTH_MIN, max = PASSWORD_LENGTH_MAX, message = "{kundenverwaltung.kunde.password.size}")
	private String password;
	
	@Transient
	private String passwordWdh;
	
	@Column(nullable = false)
	@Temporal(TemporalType.DATE)
	@XmlJavaTypeAdapter(XmlDateAdapter.class)
	private Date erstellt = null;
	
	@Column(nullable = false)
	@Temporal(TemporalType.DATE)
	@XmlTransient
	private Date aktualisiert = null;

	//bi-directional one-to-one association to Adresse
	@OneToOne(mappedBy = "kunde", cascade = { PERSIST, REMOVE, MERGE }) 
	@Valid
	@NotNull(message = "{kundenverwaltung.kunde.adresse.notNull}")
	@XmlElement(required = true)
	private Adresse adresse;

	//bi-directional one-to-many association to Bestellung
	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name = "kunde_fk", nullable = false)
	@OrderColumn(name = "idx", nullable = false)
	@XmlTransient
	private List<Bestellung> bestellungen;
	
	@Transient
	@XmlElement(name = "bestellungen")
	private URI bestellungenUri;
	
	@PostLoad
	protected void postLoad() {
		passwordWdh = password;
	}
	
	@AssertTrue(groups = PasswordGroup.class, message = "{kundenverwaltung.kunde.password.notEqual}")
	public boolean isPasswordEqual() {
		if (password == null) {
			return passwordWdh == null;
		}
		
		return password.equals(passwordWdh);
	}
	
	public AbstractKunde() {
		super();
	}
	
	public AbstractKunde(String nachname, String email, Adresse adresse) {
		this.nachname = nachname;
		this.email = email;
		this.adresse = adresse;
		adresse.setKunde(this);
	}
	
	public AbstractKunde(String nachname, String email, Adresse adresse, String password) {
		this.nachname = nachname;
		this.email = email;
		this.adresse = adresse;
		adresse.setKunde(this);
		this.password = password;
		this.passwordWdh = password;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNachname() {
		return nachname;
	}

	public void setNachname(String nachname) {
		this.nachname = nachname;
	}

	public String getVorname() {
		return vorname;
	}

	public void setVorname(String vorname) {
		this.vorname = vorname;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getTelefon() {
		return telefon;
	}

	public void setTelefon(String telefon) {
		this.telefon = telefon;
	}

	public String getGeschlecht() {
		return geschlecht;
	}

	public void setGeschlecht(String geschlecht) {
		this.geschlecht = geschlecht;
	}

	public abstract String getArt();

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setPasswordWdh(String passwordWdh) {
		this.passwordWdh = passwordWdh;
	}

	public String getPasswordWdh() {
		return passwordWdh;
	}

	public Date getErstellt() {
		return 	(erstellt == null) ? null : (Date) erstellt.clone();
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

	public Adresse getAdresse() {
		return adresse;
	}

	public void setAdresse(Adresse adresse) {
		this.adresse = adresse;
	}

	@Valid
	public List<Bestellung> getBestellungen() {
		if (bestellungen == null) {
			return null;
		}
		
		return Collections.unmodifiableList(bestellungen);
	}

	public void setBestellungen(List<Bestellung> bestellungen) {
		if (this.bestellungen == null) {
			this.bestellungen = bestellungen;
			return;
		}
		
		bestellungen.clear();
		if (this.bestellungen != null) {
			this.bestellungen.addAll(bestellungen);
		}
	}
	
	public AbstractKunde addBestellung(Bestellung bestellung) {
		if (bestellungen == null) {
			bestellungen = new ArrayList<Bestellung>();
		}
		
		bestellungen.add(bestellung);
		return this;
	}

	public void setValues(AbstractKunde kunde) {
		nachname = kunde.getNachname();
		vorname = kunde.getVorname();
		email = kunde.getEmail();
		telefon = kunde.getTelefon();
		geschlecht = kunde.getGeschlecht();
		password = kunde.getPassword();
		passwordWdh = kunde.getPasswordWdh();
		erstellt = kunde.getErstellt();
		adresse.setValues(kunde.getAdresse());
	}

	public URI getBestellungenUri() {
		return bestellungenUri;
	}

	public void setBestellungenUri(URI bestellungenUri) {
		this.bestellungenUri = bestellungenUri;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((email == null) ? 0 : email.hashCode());
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
		AbstractKunde other = (AbstractKunde) obj;
		if (email == null) {
			if (other.email != null) {
				return false;
			}
		}
		else if (!email.equals(other.email)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "AbstractKunde [id=" + id + ", nachname=" + nachname
				+ ", vorname=" + vorname + ", email=" + email + ", telefon="
				+ telefon + ", geschlecht=" + geschlecht + ", erstellt="
				+ erstellt + ", aktualisiert=" + aktualisiert + "]";
	}
}
