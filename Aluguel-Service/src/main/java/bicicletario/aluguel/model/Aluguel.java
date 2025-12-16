package bicicletario.aluguel.model;

// Esta entidade representa o schema 'Aluguel' do Swagger
// (Note que o schema 'Devolucao' é quase idêntico, talvez possamos
// usar esta única entidade para ambos os casos)

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class Aluguel {

@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Integer id;

private Integer ciclista;
private Integer trancaInicio;
private Integer bicicleta;
private LocalDateTime horaInicio;
private Integer trancaFim;
private LocalDateTime horaFim;
private Integer cobranca; // ID da cobrança gerada

// Construtor vazio (Requerido pelo JPA)
public Aluguel() {
    // Requerido pelo framework JPA
}

public Integer getId() {
    return id;
}

public void setId(Integer id) {
    this.id = id;
}

public Integer getCobranca() {
    return cobranca;
}

public void setCobranca(Integer cobranca) {
    this.cobranca = cobranca;
}

public LocalDateTime getHoraFim() {
    return horaFim;
}

public void setHoraFim(LocalDateTime horaFim) {
    this.horaFim = horaFim;
}

public LocalDateTime getHoraInicio() {
    return horaInicio;
}

public void setHoraInicio(LocalDateTime horaInicio) {
    this.horaInicio = horaInicio;
}

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

public Integer getBicicleta() {
    return bicicleta;
}

public void setBicicleta(Integer bicicleta) {
    this.bicicleta = bicicleta;
}

public Integer getTrancaFim() {
    return trancaFim;
}

public void setTrancaFim(Integer trancaFim) {
    this.trancaFim = trancaFim;
}
}