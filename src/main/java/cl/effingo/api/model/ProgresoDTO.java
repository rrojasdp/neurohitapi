package cl.effingo.api.model;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class ProgresoDTO {
	
	private String id_ciclo;
	private String itemtocomplete;
	
	public String getId_ciclo() {
		return id_ciclo;
	}
	public void setId_ciclo(String id_ciclo) {
		this.id_ciclo = id_ciclo;
	}
	public String getItemtoComplete() {
		return itemtocomplete;
	}
	public void setItemtoComplete(String itemtocomplete) {
		this.itemtocomplete = itemtocomplete;
	}

	
}
