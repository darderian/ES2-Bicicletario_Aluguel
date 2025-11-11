package bicicletario.aluguel.dto;

import java.time.LocalDateTime;

// Este DTO representa a RESPOSTA do microsserviço Externo
// quando pedimos uma cobrança (schema 'Cobranca')
public class CobrancaDTO {

private Integer id;
private String status; // ex: 'PENDENTE', 'PAGA', 'FALHA'
private Double valor;
private Integer ciclista;
private LocalDateTime horaSolicitacao;
private LocalDateTime horaFinalizacao;

// Getters e Setters
public Integer getId() { return id; }
public void setId(Integer id) { this.id = id; }
public String getStatus() { return status; }
public void setStatus(String status) { this.status = status; }
public Double getValor() { return valor; }
public void setValor(Double valor) { this.valor = valor; }
public Integer getCiclista() { return ciclista; }
public void setCiclista(Integer ciclista) { this.ciclista = ciclista; }
public LocalDateTime getHoraSolicitacao() { return horaSolicitacao; }
public void setHoraSolicitacao(LocalDateTime horaSolicitacao) { this.horaSolicitacao = horaSolicitacao; }
public LocalDateTime getHoraFinalizacao() { return horaFinalizacao; }
public void setHoraFinalizacao(LocalDateTime horaFinalizacao) { this.horaFinalizacao = horaFinalizacao; }
}