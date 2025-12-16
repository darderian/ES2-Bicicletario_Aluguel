package bicicletario.aluguel.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class Devolucao {

@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Integer id; // Boa prática, mesmo que o schema não liste um 'id'

// Campos baseados nos schemas 'NovoDevolucao' e 'Devolucao'
private Integer ciclista;
private Integer bicicleta;
private LocalDateTime horaInicio;
private Integer trancaFim;
private LocalDateTime horaFim;
private Integer cobranca; // ID da cobrança de taxa extra (se houver)

// Construtor vazio (Requerido pelo JPA)
public Devolucao() {
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

public Integer getTrancaFim() {
    return trancaFim;
}

public void setTrancaFim(Integer trancaFim) {
    this.trancaFim = trancaFim;
}

public LocalDateTime getHoraInicio() {
    return horaInicio;
}

public void setHoraInicio(LocalDateTime horaInicio) {
    this.horaInicio = horaInicio;
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
}