package bicicletario.aluguel.dto;

import javax.validation.constraints.NotNull;

public class NovoAluguelDTO {

@NotNull
private Integer ciclista;

@NotNull
private Integer trancaInicio;

public Integer getTrancaInicio() {
    return trancaInicio;
}

public void setTrancaInicio(Integer trancaInicio) {
    this.trancaInicio = trancaInicio;
}

public Integer getCiclista() {
    return ciclista;
}

public void setCiclista(Integer ciclista) {
    this.ciclista = ciclista;
}
}