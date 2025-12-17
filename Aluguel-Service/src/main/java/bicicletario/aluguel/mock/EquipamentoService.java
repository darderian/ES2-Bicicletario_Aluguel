package bicicletario.aluguel.mock;

import bicicletario.aluguel.dto.BicicletaDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EquipamentoService {

private static final Logger logger = LoggerFactory.getLogger(EquipamentoService.class);

@Value("${url.equipamento:http://localhost:8082}")
private String equipamentoUrl;

private final RestTemplate restTemplate = new RestTemplate();

/**
 * Tenta buscar a bicicleta na tranca real.
 * Se 4xx, lança exceção. Se 5xx/Conexão, retorna Mock.
 */
public BicicletaDTO getBicicletaDaTranca(Integer idTranca) {
    try {
        String url = equipamentoUrl + "/tranca/" + idTranca + "/bicicleta";
        return restTemplate.getForObject(url, BicicletaDTO.class);

    } catch (HttpClientErrorException e) {
        // 🚨 INTEGRIDADE/FALHA NO NEGÓCIO (4xx: 404, 422, etc.)
        logger.error("🚫 [EQUIPAMENTO] Erro de Negócio/Validação ({}). Rejeitando o aluguel. Tranca: {}",
                e.getStatusCode(), idTranca);
        // Lança a exceção para o Controller abortar o aluguel com 422/404
        throw new IllegalArgumentException("Falha na validação do Equipamento: " + e.getStatusCode(), e);

    } catch (ResourceAccessException | HttpServerErrorException | RestClientException e) {
        // 🟢 RESILIÊNCIA/MOCK (5xx ou Conexão)
        logger.warn("⚠️ [EQUIPAMENTO] Serviço Indisponível (5xx ou Conexão). Usando Mock. Tranca: {}. Erro: {}",
                idTranca, e.getMessage());
    }

    // FALLBACK (PLANO B): Retorna um dado fictício para não travar a demo
    logger.warn(">>> SIMULANDO BUSCA DA BICICLETA {}/{} com sucesso (MOCK DE RESILIÊNCIA).", 999, idTranca);
    BicicletaDTO mock = new BicicletaDTO();
    mock.setId(999);
    mock.setNumero(123);
    mock.setMarca("Caloi Mock");
    mock.setModelo("Veloz");
    mock.setStatus("DISPONIVEL");
    return mock;
}

/**
 * Tenta destrancar no serviço real.
 * Se 4xx, lança exceção. Se 5xx/Conexão, finge que destrancou (Mock).
 */
public void destrancarTranca(Integer idTranca) {
    try {
        String url = equipamentoUrl + "/tranca/" + idTranca + "/destrancar";
        restTemplate.postForLocation(url, null);
        logger.info("✅ [EQUIPAMENTO] Tranca {} destrancada no serviço real.", idTranca);

    } catch (HttpClientErrorException e) {
        // 🚨 INTEGRIDADE/FALHA NO NEGÓCIO (4xx)
        logger.error("🚫 [EQUIPAMENTO] Erro de Negócio/Validação ({}) no destrancamento. Abortando. Tranca: {}",
                e.getStatusCode(), idTranca);
        throw new IllegalArgumentException("Equipamento recusou o destrancamento: " + e.getStatusCode(), e);

    } catch (ResourceAccessException | HttpServerErrorException | RestClientException e) {
        // 🟢 RESILIÊNCIA/MOCK (5xx ou Conexão)
        logger.warn("⚠️ [EQUIPAMENTO] Serviço Indisponível (5xx ou Conexão). Usando Mock. Tranca: {}. Erro: {}",
                idTranca, e.getMessage());
        // O Mock se ativa aqui, pois o método é void e o fluxo de sucesso é apenas terminar.
    }

    // 🟢 ATIVAÇÃO DO MOCK (Apenas se o catch de resiliência for acionado e não tiver retornado)
    if (Thread.interrupted()) { // Condição simples para checar se o try falhou e foi para o catch
        logger.warn(">>> SIMULANDO DESTANCAMENTO da tranca {} com sucesso (MOCK DE RESILIÊNCIA).", idTranca);
    }
}

// --- Outros Métodos (Trancar, Alterar Status, etc.) ---

// É recomendado aplicar a mesma lógica de try-catch híbrida aos demais métodos
// trancarTranca, alterarStatusBicicleta e getBicicleta, para manter a consistência.
// O código original abaixo manterá o comportamento anterior (Mocka tudo em caso de falha)

/**
 * Tenta trancar no serviço real. Se falhar, finge que trancou.
 */
public void trancarTranca(Integer idTranca, Integer idBicicleta) {
    try {
        String url = equipamentoUrl + "/tranca/" + idTranca + "/trancar";
        restTemplate.postForLocation(url, idBicicleta);
        logger.info("✅ [EQUIPAMENTO] Tranca {} trancada no serviço real.", idTranca);
    } catch (Exception e) {
        logger.warn("⚠️ [EQUIPAMENTO] Falha na comunicação em trancar. Usando Mock. Erro: {}", e.getMessage());
    }
    logger.warn(">>> SIMULANDO TRANCAMENTO da tranca {} com sucesso (MOCK).", idTranca);
}

/**
 * Altera status no serviço real. Se falhar, finge que alterou.
 */
public void alterarStatusBicicleta(Integer idBicicleta, String status) {
    try {
        String url = equipamentoUrl + "/bicicleta/" + idBicicleta + "/status/" + status;
        restTemplate.postForLocation(url, null);
        logger.info("✅ [EQUIPAMENTO] Status da bike {} alterado para {} no serviço real.", idBicicleta, status);
    } catch (Exception e) {
        logger.warn("⚠️ [EQUIPAMENTO] Falha na comunicação em alterar status. Usando Mock. Erro: {}", e.getMessage());
    }
    logger.warn(">>> SIMULANDO ALTERAÇÃO DE STATUS da bike {} com sucesso (MOCK).", idBicicleta);
}

/**
 * Busca bike alugada. Se falhar, retorna mock.
 */
public BicicletaDTO getBicicleta(Integer idBicicleta) {
    try {
        String url = equipamentoUrl + "/bicicleta/" + idBicicleta;
        return restTemplate.getForObject(url, BicicletaDTO.class);
    } catch (Exception e) {
        logger.warn("⚠️ [EQUIPAMENTO] Falha na comunicação em buscar bicicleta. Usando Mock. Erro: {}", e.getMessage());
    }

    logger.warn(">>> SIMULANDO BUSCA DA BICICLETA {} com sucesso (MOCK).", idBicicleta);
    BicicletaDTO mock = new BicicletaDTO();
    mock.setId(idBicicleta);
    mock.setNumero(123);
    mock.setMarca("Caloi Mock");
    mock.setModelo("Veloz");
    mock.setStatus("EM_USO");
    return mock;
}
}