package bicicletario.aluguel.mock;

import bicicletario.aluguel.dto.BicicletaDTO;

// Esta interface define o CONTRATO do que precisamos do Microserviço de Equipamento
public interface EquipamentoService {

/**
 * Simula a busca da bicicleta que está em uma tranca específica.
 * (Necessário para UC03 - Aluguel)
 *
 * @param idTranca O ID da tranca
 * @return O DTO da bicicleta encontrada
 */
BicicletaDTO getBicicletaDaTranca(Integer idTranca);

/**
 * Simula o destrancamento da tranca.
 * (Necessário para UC03 - Aluguel)
 *
 * @param idTranca O ID da tranca
 */
void destrancarTranca(Integer idTranca);

/**
 * Simula o trancamento da tranca.
 * (Necessário para UC04 - Devolução)
 *
 * @param idTranca O ID da tranca
 * @param idBicicleta O ID da bicicleta sendo trancada
 */
void trancarTranca(Integer idTranca, Integer idBicicleta);

/**
 * Simula a alteração de status da bicicleta.
 * (Necessário para UC04 - Devolução)
 *
 * @param idBicicleta O ID da bicicleta
 * @param status O novo status (ex: "DISPONIVEL" ou "REPARO_SOLICITADO")
 */
void alterarStatusBicicleta(Integer idBicicleta, String status);

/**
 * Simula a busca de uma bicicleta alugada por um ciclista.
 * (Necessário para GET /ciclista/{id}/bicicletaAlugada)
 *
 * @param idBicicleta O ID da bicicleta
 * @return O DTO da bicicleta
 */
BicicletaDTO getBicicleta(Integer idBicicleta);
}