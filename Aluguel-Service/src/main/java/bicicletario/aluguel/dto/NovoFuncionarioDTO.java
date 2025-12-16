package bicicletario.aluguel.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

// TODO: Este DTO representa o schema 'NovoFuncionario' do Swagger
public class NovoFuncionarioDTO {

@NotBlank
private String nome;

@NotBlank
@Email
private String email;

@NotBlank
private String senha;

// --- CAMPO ADICIONADO ---
@NotBlank
private String confirmacaoSenha;
// ------------------------

@NotNull
private Integer idade;

@NotBlank
private String funcao;

@NotBlank
private String cpf;

public String getNome() {
    return nome;
}

public void setNome(String nome) {
    this.nome = nome;
}

public String getCpf() {
    return cpf;
}

public void setCpf(String cpf) {
    this.cpf = cpf;
}

public Integer getIdade() {
    return idade;
}

public void setIdade(Integer idade) {
    this.idade = idade;
}

public String getFuncao() {
    return funcao;
}

public void setFuncao(String funcao) {
    this.funcao = funcao;
}

public String getConfirmacaoSenha() {
    return confirmacaoSenha;
}

public void setConfirmacaoSenha(String confirmacaoSenha) {
    this.confirmacaoSenha = confirmacaoSenha;
}

public String getSenha() {
    return senha;
}

public void setSenha(String senha) {
    this.senha = senha;
}

public String getEmail() {
    return email;
}

public void setEmail(String email) {
    this.email = email;
}
}