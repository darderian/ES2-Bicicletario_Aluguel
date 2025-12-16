package bicicletario.externo.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Cobranca {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String status; 
    private LocalDateTime horaSolicitacao;
    private LocalDateTime horaFinalizacao;
    private Double valor;
    private Integer ciclista;

    public Cobranca() {}

    // Getters e Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getHoraSolicitacao() { return horaSolicitacao; }
    public void setHoraSolicitacao(LocalDateTime horaSolicitacao) { this.horaSolicitacao = horaSolicitacao; }
    public LocalDateTime getHoraFinalizacao() { return horaFinalizacao; }
    public void setHoraFinalizacao(LocalDateTime horaFinalizacao) { this.horaFinalizacao = horaFinalizacao; }
    public Double getValor() { return valor; }
    public void setValor(Double valor) { this.valor = valor; }
    public Integer getCiclista() { return ciclista; }
    public void setCiclista(Integer ciclista) { this.ciclista = ciclista; }
}