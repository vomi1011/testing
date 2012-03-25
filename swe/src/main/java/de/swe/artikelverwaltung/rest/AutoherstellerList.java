package de.swe.artikelverwaltung.rest;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import de.swe.artikelverwaltung.domain.Autohersteller;;

@XmlRootElement(name = "autohersteller")
@XmlAccessorType(XmlAccessType.FIELD)
public class AutoherstellerList {

		@XmlElementRef
		private List<Autohersteller> autohersteller;
		
		public AutoherstellerList() {
			super();
		}
		
		public AutoherstellerList(List<Autohersteller> autohersteller) {
			this.autohersteller = autohersteller;
		}
		
		public List<Autohersteller> getAutohersteller() {
			return autohersteller;
		}

		public void setAutohersteller(List<Autohersteller> autohersteller) {
			this.autohersteller = autohersteller;
		}

		@Override
		public String toString() {
			return "AutoherstellerList [autohersteller=" + autohersteller + "]";
		}
}


