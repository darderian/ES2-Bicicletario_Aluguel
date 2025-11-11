package bicicletario.aluguel;
import bicicletario.aluguel.dto.DevolucaoDTO;
import bicicletario.aluguel.dto.NovoAluguelDTO;
import bicicletario.aluguel.model.Aluguel;
import bicicletario.aluguel.model.Devolucao;
import bicicletario.aluguel.repository.AluguelRepository;
import bicicletario.aluguel.repository.DevolucaoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class OperacaoControllerTest {

@Autowired
private MockMvc mockMvc;

@Autowired
private AluguelRepository aluguelRepository;

@Autowired
private DevolucaoRepository devolucaoRepository;

@Autowired
private ObjectMapper objectMapper;

/**
 * Limpa os repositórios de Aluguel e Devolucao ANTES de cada teste.
 */
@BeforeEach
void setUp() {
    // Limpa as tabelas relacionadas a este controller
    aluguelRepository.deleteAll();
    devolucaoRepository.deleteAll();
}

// --- TESTE PARA POST /aluguel ---
@Test
void testRealizarAluguel_ComDadosValidos_DeveRetornar200OK() throws Exception {
    // --- 1. Organizar (Arrange) ---
    NovoAluguelDTO dto = new NovoAluguelDTO();
    dto.setCiclista(1); // ID do ciclista (simulado)
    dto.setTrancaInicio(10); // ID da tranca (simulado)

    String jsonRequisicao = objectMapper.writeValueAsString(dto);

    // --- 2. Agir (Act) ---
    mockMvc.perform(post("/aluguel")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequisicao))

            // --- 3. Afirmar (Assert) ---
            .andExpect(status().isOk()) // Espera 200 OK
            .andExpect(jsonPath("$.id").exists()) // Espera que a resposta tenha um ID
            .andExpect(jsonPath("$.ciclista").value(1))
            .andExpect(jsonPath("$.trancaInicio").value(10))
            .andExpect(jsonPath("$.bicicleta").value(123)) // Valor do MOCK no controller
            .andExpect(jsonPath("$.cobranca").value(100)); // Valor do MOCK no controller
}

// --- TESTE PARA POST /devolucao ---
@Test
void testRealizarDevolucao_ComDadosValidos_DeveRetornar200OK() throws Exception {
    // --- 1. Organizar (Arrange) ---
    DevolucaoDTO dto = new DevolucaoDTO();
    dto.setIdBicicleta(123); // ID da bicicleta (simulado)
    dto.setIdTranca(20); // ID da tranca (simulado)

    String jsonRequisicao = objectMapper.writeValueAsString(dto);

    // --- 2. Agir (Act) ---
    mockMvc.perform(post("/devolucao")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequisicao))

            // --- 3. Afirmar (Assert) ---
            .andExpect(status().isOk()) // Espera 200 OK
            .andExpect(jsonPath("$.id").exists()) // Espera que a resposta tenha um ID
            .andExpect(jsonPath("$.bicicleta").value(123))
            .andExpect(jsonPath("$.trancaFim").value(20))
            .andExpect(jsonPath("$.cobranca").value(101)); // Valor do MOCK no controller
}

// --- TESTE PARA GET /restaurarBanco ---
@Test
void testRestaurarBanco_DeveLimparRepositorios_DeveRetornar200OK() throws Exception {
    // --- 1. Organizar (Arrange) ---
    // Adiciona dados "sujos" no banco para garantir que o endpoint está limpando
    aluguelRepository.save(new Aluguel());
    devolucaoRepository.save(new Devolucao());

    // Verifica se os dados estão lá
    assertEquals(1, aluguelRepository.count());
    assertEquals(1, devolucaoRepository.count());


    // --- 2. Agir (Act) ---
    mockMvc.perform(get("/restaurarBanco"))

            // --- 3. Afirmar (Assert) ---
            .andExpect(status().isOk()) // Espera 200 OK
            .andExpect(content().string("Banco de dados (Aluguel) restaurado."));

    // Verifica se os dados foram realmente limpos
    assertEquals(0, aluguelRepository.count());
    assertEquals(0, devolucaoRepository.count());
}
}