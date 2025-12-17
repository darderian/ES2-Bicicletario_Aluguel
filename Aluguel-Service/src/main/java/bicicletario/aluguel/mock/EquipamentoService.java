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

// ⚠️ ADICIONE ESSA LINHA PARA LOGAR OS AVISOS
private static final Logger logger = LoggerFactory.getLogger(EquipamentoService.class);

// Se o colega te passar a URL, configure no application.properties.
// Se não, ele tenta conectar no localhost:8082 (ou o valor padrão)
@Value("${url.equipamento:http://localhost:8082}")
private String equipamentoUrl;

private final RestTemplate restTemplate = new RestTemplate();

/**
 * Tenta buscar a bicicleta na tranca real. Se falhar, retorna uma bicicleta Mock.
 */
public BicicletaDTO getBicicletaDaTranca(Integer idTranca) {
    try {
        // Tenta conectar no serviço real do colega
        String url = equipamentoUrl + "/tranca/" + idTranca + "/bicicleta";
        return restTemplate.getForObject(url, BicicletaDTO.class);

    } catch (ResourceAccessException e) {
        // CENÁRIO 1: FALHA DE CONEXÃO (Não achou o servidor: localhost ou URL errada/off)
        logger.warn("⚠️ [EQUIPAMENTO] Conexão falhou (Timeout/Recusada). Usando Mock. Tranca: {}. Causa: {}", idTranca, e.getMessage());

    } catch (HttpClientErrorException | HttpServerErrorException e) {
        // CENÁRIO 2: ERRO HTTP RETORNADO (4xx ou 5xx - INCLUI O 502 BAD GATEWAY HTML)
        logger.error("⚠️ [EQUIPAMENTO] Falha HTTP. Status: {}. Usando Mock. Tranca: {}. Corpo da Resposta: {}",
                e.getStatusCode(), idTranca, e.getResponseBodyAsString());

    } catch (RestClientException e) {
        // CENÁRIO 3: QUALQUER OUTRA FALHA DE REST TEMPLATE
        logger.error("⚠️ [EQUIPAMENTO] Erro inesperado do RestTemplate. Usando Mock. Tranca: {}. Erro: {}", idTranca, e.getMessage());
    }

    // 🟢 FALLBACK (PLANO B): Retorna um dado fictício para não travar a demo
    logger.warn(">>> SIMULANDO BUSCA DA BICICLETA {}/{} com sucesso (MOCK).", 999, idTranca);
    BicicletaDTO mock = new BicicletaDTO();
    mock.setId(999); // ID fixo para teste
    mock.setNumero(123);
    mock.setMarca("Caloi Mock");
    mock.setModelo("Veloz");
    mock.setStatus("DISPONIVEL");
    return mock;
}

/**
 * Tenta destrancar no serviço real. Se falhar, finge que destrancou.
 */
public void destrancarTranca(Integer idTranca) {
    try {
        String url = equipamentoUrl + "/tranca/" + idTranca + "/destrancar";
        restTemplate.postForLocation(url, null);
        logger.info("✅ [EQUIPAMENTO] Tranca {} destrancada no serviço real.", idTranca);

    } catch (ResourceAccessException e) {
        // CENÁRIO 1: FALHA DE CONEXÃO
        logger.warn("⚠️ [EQUIPAMENTO] Conexão falhou (Timeout/Recusada). Usando Mock. Tranca: {}. Causa: {}", idTranca, e.getMessage());

    } catch (HttpClientErrorException | HttpServerErrorException e) {
        // CENÁRIO 2: ERRO HTTP RETORNADO (4xx ou 5xx - INCLUI O 502 BAD GATEWAY HTML)
        logger.error("⚠️ [EQUIPAMENTO] Falha HTTP. Status: {}. Usando Mock. Tranca: {}. Corpo da Resposta: {}",
                e.getStatusCode(), idTranca, e.getResponseBodyAsString());

    } catch (RestClientException e) {
        // CENÁRIO 3: QUALQUER OUTRA FALHA
        logger.error("⚠️ [EQUIPAMENTO] Erro inesperado do RestTemplate. Usando Mock. Tranca: {}. Erro: {}", idTranca, e.getMessage());
    }

    // 🟢 ATIVAÇÃO DO MOCK
    logger.warn(">>> SIMULANDO DESTANCAMENTO da tranca {} com sucesso (MOCK).", idTranca);
}

/**
 * Tenta trancar no serviço real. Se falhar, finge que trancou.
 */
public void trancarTranca(Integer idTranca, Integer idBicicleta) {
    try {
        String url = equipamentoUrl + "/tranca/" + idTranca + "/trancar"; // Ajuste conforme a rota do colega
        restTemplate.postForLocation(url, idBicicleta);
        logger.info("✅ [EQUIPAMENTO] Tranca {} trancada no serviço real.", idTranca);
    } catch (ResourceAccessException e) {
        // CENÁRIO 1: FALHA DE CONEXÃO
        logger.warn("⚠️ [EQUIPAMENTO] Conexão falhou (Timeout/Recusada). Usando Mock. Tranca: {}. Causa: {}", idTranca, e.getMessage());

    } catch (HttpClientErrorException | HttpServerErrorException e) {
        // CENÁRIO 2: ERRO HTTP RETORNADO (4xx ou 5xx)
        logger.error("⚠️ [EQUIPAMENTO] Falha HTTP. Status: {}. Usando Mock. Tranca: {}. Corpo da Resposta: {}",
                e.getStatusCode(), idTranca, e.getResponseBodyAsString());

    } catch (RestClientException e) {
        // CENÁRIO 3: QUALQUER OUTRA FALHA
        logger.error("⚠️ [EQUIPAMENTO] Erro inesperado do RestTemplate. Usando Mock. Tranca: {}. Erro: {}", idTranca, e.getMessage());
    }

    // 🟢 ATIVAÇÃO DO MOCK
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
    } catch (ResourceAccessException e) {
        logger.warn("⚠️ [EQUIPAMENTO] Conexão falhou. Usando Mock. Bike: {}. Causa: {}", idBicicleta, e.getMessage());
    } catch (HttpClientErrorException | HttpServerErrorException e) {
        logger.error("⚠️ [EQUIPAMENTO] Falha HTTP. Status: {}. Usando Mock. Bike: {}. Corpo da Resposta: {}",
                e.getStatusCode(), idBicicleta, e.getResponseBodyAsString());
    } catch (RestClientException e) {
        logger.error("⚠️ [EQUIPAMENTO] Erro inesperado do RestTemplate. Usando Mock. Bike: {}. Erro: {}", idBicicleta, e.getMessage());
    }
    // 🟢 ATIVAÇÃO DO MOCK
    logger.warn(">>> SIMULANDO ALTERAÇÃO DE STATUS da bike {} com sucesso (MOCK).", idBicicleta);
}

/**
 * Busca bike alugada. Se falhar, retorna mock.
 */
public BicicletaDTO getBicicleta(Integer idBicicleta) {
    try {
        String url = equipamentoUrl + "/bicicleta/" + idBicicleta;
        return restTemplate.getForObject(url, BicicletaDTO.class);
    } catch (ResourceAccessException e) {
        logger.warn("⚠️ [EQUIPAMENTO] Conexão falhou. Usando Mock. Bike: {}. Causa: {}", idBicicleta, e.getMessage());
    } catch (HttpClientErrorException | HttpServerErrorException e) {
        logger.error("⚠️ [EQUIPAMENTO] Falha HTTP. Status: {}. Usando Mock. Bike: {}. Corpo da Resposta: {}",
                e.getStatusCode(), idBicicleta, e.getResponseBodyAsString());
    } catch (RestClientException e) {
        logger.error("⚠️ [EQUIPAMENTO] Erro inesperado do RestTemplate. Usando Mock. Bike: {}. Erro: {}", idBicicleta, e.getMessage());
    }

    // 🟢 FALLBACK (PLANO B): Retorna um dado fictício para não travar a demo
    logger.warn(">>> SIMULANDO BUSCA DA BICICLETA {} com sucesso (MOCK).", idBicicleta);
    BicicletaDTO mock = new BicicletaDTO();
    mock.setId(idBicicleta);
    mock.setNumero(123);
    mock.setMarca("Caloi Mock");
    mock.setModelo("Veloz");
    mock.setStatus("EM_USO"); // Assumindo que se buscou, está alugada
    return mock;
}
}