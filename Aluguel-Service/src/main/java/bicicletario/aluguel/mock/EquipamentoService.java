package bicicletario.aluguel.mock;

import bicicletario.aluguel.dto.BicicletaDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class EquipamentoService {

// Se o colega te passar a URL, configure no application.properties.
// Se não, ele tenta conectar na porta 8082 (que provavelmente não existe).
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
    } catch (Exception e) {
        // FALLBACK (PLANO B): Retorna um dado fictício para não travar a demo
        System.err.println("⚠️ [EQUIPAMENTO] Offline ou Erro. Usando Mock para Tranca " + idTranca);

        BicicletaDTO mock = new BicicletaDTO();
        mock.setId(999); // ID fixo para teste
        mock.setNumero(123);
        mock.setMarca("Caloi Mock");
        mock.setModelo("Veloz");
        mock.setStatus("DISPONIVEL");
        return mock;
    }
}

/**
 * Tenta destrancar no serviço real. Se falhar, finge que destrancou.
 */
public void destrancarTranca(Integer idTranca) {
    try {
        String url = equipamentoUrl + "/tranca/" + idTranca + "/destrancar";
        restTemplate.postForLocation(url, null);
        System.out.println("✅ [EQUIPAMENTO] Tranca " + idTranca + " destrancada no serviço real.");
    } catch (Exception e) {
        System.err.println("⚠️ [EQUIPAMENTO] Offline. Simulando destranca da tranca " + idTranca);
    }
}

/**
 * Tenta trancar no serviço real. Se falhar, finge que trancou.
 */
public void trancarTranca(Integer idTranca, Integer idBicicleta) {
    try {
        String url = equipamentoUrl + "/tranca/" + idTranca + "/trancar"; // Ajuste conforme a rota do colega
        // Enviaria o objeto bicicleta ou só o ID, dependendo da API dele.
        // Aqui estamos assumindo um POST simples.
        restTemplate.postForLocation(url, idBicicleta);
        System.out.println("✅ [EQUIPAMENTO] Tranca " + idTranca + " trancada.");
    } catch (Exception e) {
        System.err.println("⚠️ [EQUIPAMENTO] Offline. Simulando trancamento.");
    }
}

/**
 * Altera status no serviço real. Se falhar, finge que alterou.
 */
public void alterarStatusBicicleta(Integer idBicicleta, String status) {
    try {
        String url = equipamentoUrl + "/bicicleta/" + idBicicleta + "/status/" + status;
        restTemplate.postForLocation(url, null);
    } catch (Exception e) {
        System.err.println("⚠️ [EQUIPAMENTO] Offline. Simulando mudança de status da bike " + idBicicleta);
    }
}

/**
 * Busca bike alugada. Se falhar, retorna mock.
 */
public BicicletaDTO getBicicleta(Integer idBicicleta) {
    try {
        String url = equipamentoUrl + "/bicicleta/" + idBicicleta;
        return restTemplate.getForObject(url, BicicletaDTO.class);
    } catch (Exception e) {
        System.err.println("⚠️ [EQUIPAMENTO] Offline. Retornando bike mock " + idBicicleta);
        BicicletaDTO mock = new BicicletaDTO();
        mock.setId(idBicicleta);
        mock.setStatus("EM_USO"); // Assumindo que se buscou, está alugada
        return mock;
    }
}
}