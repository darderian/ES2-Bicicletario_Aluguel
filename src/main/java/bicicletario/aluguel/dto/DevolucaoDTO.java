package bicicletario.aluguel.dto;

import javax.validation.constraints.NotNull;


public class DevolucaoDTO {

@NotNull
private Integer idTranca;

@NotNull
private Integer idBicicleta;

public Integer getIdTranca() {
    return idTranca;
}

public void setIdTranca(Integer idTranca) {
    this.idTranca = idTranca;
}

public Integer getIdBicicleta() {
    return idBicicleta;
}

public void setIdBicicleta(Integer idBicicleta) {
    this.idBicicleta = idBicicleta;
}
}