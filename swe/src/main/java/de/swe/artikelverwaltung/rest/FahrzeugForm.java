package de.swe.artikelverwaltung.rest;
import javax.ws.rs.FormParam;

public class FahrzeugForm {

	@FormParam("modell")
	private String modell;
	@FormParam("baujahr")
	private short baujahr;
	@FormParam("beschreibung")
	private String beschreibung;
	@FormParam("lieferbar")
	private Boolean lieferbar;
	@FormParam("preis")
	private int preis;
	@FormParam("aId")
	private Long aId;
	
	public String getModell() {
		return modell;
	}
	public void setModell(String modell) {
		this.modell = modell;
	}
	public short getBaujahr() {
		return baujahr;
	}
	public void setBaujahr(short baujahr) {
		this.baujahr = baujahr;
	}
	public String getBeschreibung() {
		return beschreibung;
	}
	public void setBeschreibung(String beschreibung) {
		this.beschreibung = beschreibung;
	}
	public Boolean getLieferbar() {
		return lieferbar;
	}
	public void setLieferbar(Boolean lieferbar) {
		this.lieferbar = lieferbar;
	}
	public int getPreis() {
		return preis;
	}
	public void setPreis(int preis) {
		this.preis = preis;
	}

	public Long getaId() {
		return aId;
	}
	public void setaId(Long aId) {
		this.aId = aId;
	}
	
	
	@Override
	public String toString() {
		return "FahrzeugForm [modell=" + modell + ", baujahr=" + baujahr
				+ ", beschreibung=" + beschreibung + ", lieferbar=" + lieferbar + ", preis="
				+ preis + "]";
	}


}


