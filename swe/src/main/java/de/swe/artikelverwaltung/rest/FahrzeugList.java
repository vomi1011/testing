package de.swe.artikelverwaltung.rest;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import de.swe.artikelverwaltung.domain.Fahrzeug;

@XmlRootElement(name = "fahrzeuge")
@XmlAccessorType(XmlAccessType.FIELD)
public class FahrzeugList {

		@XmlElementRef
		private List<Fahrzeug> fahrzeug;
		
		public FahrzeugList() {
			super();
		}
		
		public FahrzeugList(List<Fahrzeug> fahrzeug) {
			this.fahrzeug = fahrzeug;
		}
		
		public List<Fahrzeug> getFahrzeug() {
			return fahrzeug;
		}

		public void setFahrzeug(List<Fahrzeug> fahrzeug) {
			this.fahrzeug = fahrzeug;
		}

		@Override
		public String toString() {
			return "FahrzeugList [fahrzeug=" + fahrzeug + "]";
		}
}


