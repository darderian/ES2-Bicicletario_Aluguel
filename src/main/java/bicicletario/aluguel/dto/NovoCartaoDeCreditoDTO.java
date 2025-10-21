package bicicletario.aluguel.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

public class NovoCartaoDeCreditoDTO {

@NotBlank
private String nomeTitular;

@NotBlank
@Pattern(regexp = "\\d+") // Valida que são apenas dígitos
private String numero;

@NotBlank
private String validade; // Formato "AAAA-MM-DD"

@NotBlank
@Pattern(regexp = "\\d{3,4}") // Valida 3 ou 4 dígitos
private String cvv;

// Getters e Setters
public String getNomeTitular() { return nomeTitular; }
public void setNomeTitular(String nomeTitular) { this.nomeTitular = nomeTitular; }
public String getNumero() { return numero; }
public void setNumero(String numero) { this.numero = numero; }
public String getValidade() { return validade; }
public void setValidade(String validade) { this.validade = validade; }
public String getCvv() { return cvv; }
public void setCvv(String cvv) { this.cvv = cvv; }
}