package bicicletario.aluguel.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class CadastroCiclistaDTO {

@NotNull
@Valid // Valida o objeto ciclista interno
private NovoCiclistaDTO ciclista;

@NotNull
@Valid // Valida o objeto meioDePagamento interno
private NovoCartaoDeCreditoDTO meioDePagamento;

// Getters e Setters
public NovoCiclistaDTO getCiclista() { return ciclista; }
public void setCiclista(NovoCiclistaDTO ciclista) { this.ciclista = ciclista; }
public NovoCartaoDeCreditoDTO getMeioDePagamento() { return meioDePagamento; }
public void setMeioDePagamento(NovoCartaoDeCreditoDTO meioDePagamento) { this.meioDePagamento = meioDePagamento; }
}