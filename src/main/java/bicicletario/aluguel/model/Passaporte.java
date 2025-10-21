package bicicletario.aluguel.model;

import javax.persistence.Embeddable;

@Embeddable // Diz ao JPA para "embutir" estes campos na tabela do Ciclista
public class Passaporte {

private String passaporteNumero;
private String passaporteValidade;
private String passaportePais;

// Construtor vazio
public Passaporte() {}

// Getters e Setters
public String getPassaporteNumero() { return passaporteNumero; }
public void setPassaporteNumero(String passaporteNumero) { this.passaporteNumero = passaporteNumero; }
public String getPassaporteValidade() { return passaporteValidade; }
public void setPassaporteValidade(String passaporteValidade) { this.passaporteValidade = passaporteValidade; }
public String getPassaportePais() { return passaportePais; }
public void setPassaportePais(String passaportePais) { this.passaportePais = passaportePais; }
}