package cl.effingo.api.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ProgressDTO {

	private String itemtocomplete;

	public String getItemtocomplete() {
		return itemtocomplete;
	}

	public void setItemtocomplete(String itemtocomplete) {
		this.itemtocomplete = itemtocomplete;
	}
	
}
