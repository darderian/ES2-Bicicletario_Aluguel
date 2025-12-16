package bicicletario.aluguel.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Funcionario {

@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Integer id; // Este é o 'idFuncionario' (Integer) usado nos paths da API
private String matricula;
private String nome;
private String email;
private String senha;
private int idade;
private String funcao;
private String cpf;

public Funcionario() {
    // Requerido pelo framework JPA
}

public Integer getId() {
    return id;
}

public void setId(Integer id) {
    this.id = id;
}

public String getMatricula() {
    return matricula;
}

public void setMatricula(String matricula) {
    this.matricula = matricula;
}

public String getNome() {
    return nome;
}

public void setNome(String nome) {
    this.nome = nome;
}

public String getEmail() {
    return email;
}

public void setEmail(String email) {
    this.email = email;
}

public String getSenha() {
    return senha;
}

public void setSenha(String senha) {
    this.senha = senha;
}

public String getFuncao() {
    return funcao;
}

public void setFuncao(String funcao) {
    this.funcao = funcao;
}

public int getIdade() {
    return idade;
}

public void setIdade(int idade) {
    this.idade = idade;
}

public String getCpf() {
    return cpf;
}

public void setCpf(String cpf) {
    this.cpf = cpf;
}
}