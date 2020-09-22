package cl.effingo.api.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class FechaIniDTO {

	private String fechaIni;

	public String getFechaIni() {
		return fechaIni;
	}

	public void setFechaIni(String fechaIni) {
		this.fechaIni = fechaIni;
	}
}
