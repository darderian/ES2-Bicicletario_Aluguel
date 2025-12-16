package bicicletario.aluguel.dto;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

public class NovoCiclistaDTO {

@NotBlank
private String nome;

@NotBlank
private String nascimento; // Formato "AAAA-MM-DD"

@Pattern(regexp = "\\d{11}") // Valida 11 dígitos
private String cpf;

@Valid // Diz ao Spring para validar os campos DENTRO do PassaporteDTO
private PassaporteDTO passaporte;

@NotBlank
private String nacionalidade; // Deve ser 'BRASILEIRO' ou 'ESTRANGEIRO'

@NotBlank
@Email
private String email;

private String urlFotoDocumento; // Deve ser uma URI

@NotBlank
private String senha;

// Getters e Setters
public String getNome() { return nome; }
public void setNome(String nome) { this.nome = nome; }
public String getNascimento() { return nascimento; }
public void setNascimento(String nascimento) { this.nascimento = nascimento; }
public String getCpf() { return cpf; }
public void setCpf(String cpf) { this.cpf = cpf; }
public PassaporteDTO getPassaporte() { return passaporte; }
public void setPassaporte(PassaporteDTO passaporte) { this.passaporte = passaporte; }
public String getNacionalidade() { return nacionalidade; }
public void setNacionalidade(String nacionalidade) { this.nacionalidade = nacionalidade; }
public String getEmail() { return email; }
public void setEmail(String email) { this.email = email; }
public String getUrlFotoDocumento() { return urlFotoDocumento; }
public void setUrlFotoDocumento(String urlFotoDocumento) { this.urlFotoDocumento = urlFotoDocumento; }
public String getSenha() { return senha; }
public void setSenha(String senha) { this.senha = senha; }
}