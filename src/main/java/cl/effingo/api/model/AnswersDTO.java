package cl.effingo.api.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class AnswersDTO {

	
private String nombreQuizz;
private String alternativas;
private String correctas;
private String capsulas;
private String quizzes;
private String puntajes;
private String preguntas;

public String getAlternativas() {
	return alternativas;
}
public void setAlternativas(String alternativas) {
	this.alternativas = alternativas;
}
public String getCorrectas() {
	return correctas;
}
public void setCorrectas(String correctas) {
	this.correctas = correctas;
}
public String getCapsulas() {
	return capsulas;
}
public void setCapsulas(String capsulas) {
	this.capsulas = capsulas;
}
public String getQuizzes() {
	return quizzes;
}
public void setQuizzes(String quizzes) {
	this.quizzes = quizzes;
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
public String getNombreQuizz() {
	return nombreQuizz;
}
public void setNombreQuizz(String nombreQuizz) {
	this.nombreQuizz = nombreQuizz;
}

}
