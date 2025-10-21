package bicicletario.aluguel.dto;

import javax.validation.constraints.Pattern;

public class PassaporteDTO {

private String numero;

private String validade; // Formato: "AAAA-MM-DD"

@Pattern(regexp = "[A-Z]{2}") // Valida o Padrão de 2 letras maiúsculas
private String pais;

// Getters e Setters
public String getNumero() { return numero; }
public void setNumero(String numero) { this.numero = numero; }
public String getValidade() { return validade; }
public void setValidade(String validade) { this.validade = validade; }
public String getPais() { return pais; }
public void setPais(String pais) { this.pais = pais; }
}