package bicicletario.aluguel.mock;

import bicicletario.aluguel.dto.CobrancaDTO;
import bicicletario.aluguel.dto.NovoCartaoDeCreditoDTO;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Service
public class ExternoService {

// Pega a URL do application.properties (que aponta para o localhost:8081)
@Value("${url.externo:http://localhost:8081}")
private String externoUrl;

private final RestTemplate restTemplate = new RestTemplate();

public boolean validarCartaoDeCredito(NovoCartaoDeCreditoDTO cartaoDTO) {
    try {
        // CHAMA O SERVIÇO REAL AGORA!
        restTemplate.postForEntity(externoUrl + "/validaCartaoDeCredito", cartaoDTO, Void.class);
        return true;
    } catch (HttpClientErrorException e) {
        return false;
    } catch (Exception e) {
        System.err.println("Erro ao conectar no Externo: " + e.getMessage());
        return false;
    }
}

public void enviarEmail(String email, String assunto, String mensagem) {
    Map<String, String> body = new HashMap<>();
    body.put("email", email);
    body.put("assunto", assunto);
    body.put("mensagem", mensagem);

    try {
        restTemplate.postForEntity(externoUrl + "/enviarEmail", body, Void.class);
    } catch (Exception e) {
        System.err.println("Erro ao enviar email: " + e.getMessage());
    }
}

public CobrancaDTO realizarCobranca(Double valor, Integer ciclistaId) {
    Map<String, Object> body = new HashMap<>();
    body.put("valor", valor);
    body.put("ciclista", ciclistaId);
    // Chama o POST /cobranca real
    return restTemplate.postForObject(externoUrl + "/cobranca", body, CobrancaDTO.class);
}

public CobrancaDTO enviarParaFilaCobranca(Double valor, Integer ciclistaId) {
    Map<String, Object> body = new HashMap<>();
    body.put("valor", valor);
    body.put("ciclista", ciclistaId);
    // Chama o POST /filaCobranca real
    return restTemplate.postForObject(externoUrl + "/filaCobranca", body, CobrancaDTO.class);
}
}