package bicicletario.aluguel.dto;

// Este DTO representa o schema 'Erro' do Swagger
public class ErroDTO {

private String codigo; // O campo que falhou (ex: "email")
private String mensagem; // A mensagem de erro (ex: "must be a well-formed email address")

public ErroDTO(String codigo, String mensagem) {
    this.codigo = codigo;
    this.mensagem = mensagem;
}

// Getters
public String getCodigo() {
    return codigo;
}
public String getMensagem() {
    return mensagem;
}
}