package cl.effingo.api.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class AnwsersHabitDTO {

	
private String alternativas;
private String capsulas;
private String puntajes;
private String preguntas;
private String respuestas;
private String frecuencia;

public String getFrecuencia() {
	return frecuencia;
}
public void setFrecuencia(String frecuencia) {
	this.frecuencia = frecuencia;
}
public String getRespuestas() {
	return respuestas;
}
public void setRespuestas(String respuestas) {
	this.respuestas = respuestas;
}
public String getAlternativas() {
	return alternativas;
}
public void setAlternativas(String alternativas) {
	this.alternativas = alternativas;
}

public String getCapsulas() {
	return capsulas;
}
public void setCapsulas(String capsulas) {
	this.capsulas = capsulas;
}

public String getPuntajes() {
	return puntajes;
}
public void setPuntajes(String puntajes) {
	this.puntajes = puntajes;
}
public String getPreguntas() {
	return preguntas;
}
public void setPreguntas(String preguntas) {
	this.preguntas = preguntas;
}


}

