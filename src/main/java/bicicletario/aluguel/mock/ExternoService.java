package bicicletario.aluguel.mock;

import bicicletario.aluguel.dto.CobrancaDTO;
import bicicletario.aluguel.dto.NovoCartaoDeCreditoDTO;

// Esta interface define o CONTRATO do que precisamos do Microserviço Externo
public interface ExternoService {

/**
 * Simula a validação de um cartão de crédito.
 * (Necessário para UC01 e UC07)
 *
 * @param cartaoDTO O DTO do cartão a ser validado
 * @return true se válido, false se reprovado
 */
boolean validarCartaoDeCredito(NovoCartaoDeCreditoDTO cartaoDTO);

/**
 * Simula o envio de um email.
 * (Necessário para UC01, UC03, UC04, UC06, UC07)
 *
 * @param email O email do destinatário
 * @param assunto O assunto
 * @param mensagem O corpo da mensagem
 */
void enviarEmail(String email, String assunto, String mensagem);

/**
 * Simula a realização de uma cobrança imediata.
 * (Necessário para UC03 - Aluguel)
 *
 * @param valor O valor a ser cobrado
 * @param ciclistaId O ID do ciclista
 * @return O DTO da Cobrança (simulada)
 */
CobrancaDTO realizarCobranca(Double valor, Integer ciclistaId);

/**
 * Simula o envio de uma cobrança para a fila (para taxas extras).
 * (Necessário para UC04 - Devolução)
 *
 * @param valor O valor a ser cobrado
 * @param ciclistaId O ID do ciclista
 * @return O DTO da Cobrança (simulada)
 */
CobrancaDTO enviarParaFilaCobranca(Double valor, Integer ciclistaId);
}