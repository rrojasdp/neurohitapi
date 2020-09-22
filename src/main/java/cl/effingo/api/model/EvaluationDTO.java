package cl.effingo.api.model;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class EvaluationDTO {
	private String preguntas;
	private String valores; 
	
	public String getPreguntas() {
		return preguntas;
	}
	public void setPreguntas(String preguntas) {
		this.preguntas = preguntas;
	}
	public String getValores() {
		return valores;
	}
	public void setValores(String valores) {
		this.valores = valores;
	}

}
