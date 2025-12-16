package bicicletario.aluguel.dto;

// Ele será usado como a RESPOSTA do endpoint
// GET /ciclista/{idCiclista}/bicicletaAlugada
public class BicicletaDTO {

private Integer id;
private String marca;
private String modelo;
private String ano;
private Integer numero;
private String status;

public Integer getId() {
    return id;
}

public void setId(Integer id) {
    this.id = id;
}

public String getStatus() {
    return status;
}

public void setStatus(String status) {
    this.status = status;
}

public Integer getNumero() {
    return numero;
}

public void setNumero(Integer numero) {
    this.numero = numero;
}

public String getAno() {
    return ano;
}

public void setAno(String ano) {
    this.ano = ano;
}

public String getMarca() {
    return marca;
}

public void setMarca(String marca) {
    this.marca = marca;
}

public String getModelo() {
    return modelo;
}

public void setModelo(String modelo) {
    this.modelo = modelo;
}
}