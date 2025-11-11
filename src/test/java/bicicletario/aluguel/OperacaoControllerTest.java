package bicicletario.aluguel;

import bicicletario.aluguel.controller.OperacaoController;
import bicicletario.aluguel.dto.DevolucaoDTO;
import bicicletario.aluguel.dto.NovoAluguelDTO;
import bicicletario.aluguel.model.Aluguel;
import bicicletario.aluguel.model.Devolucao;
import bicicletario.aluguel.model.Ciclista;
import bicicletario.aluguel.repository.AluguelRepository;
import bicicletario.aluguel.repository.DevolucaoRepository;
import bicicletario.aluguel.repository.CiclistaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;


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
private CiclistaRepository ciclistaRepository;
@Autowired
private ObjectMapper objectMapper;

// IDs para simulação
private static final Integer BICICLETA_ID = 123;
private static final Integer TRANCA_ID = 10;

// VARIÁVEL PARA O ID GERADO PELO BANCO
private Integer ciclistaIdGerado;

@BeforeEach
void setUp() {
    // 1. Limpa tudo
    aluguelRepository.deleteAll();
    devolucaoRepository.deleteAll();
    ciclistaRepository.deleteAll();

    // 2. Cria o ciclista ativo e DEIXA O BANCO GERAR O ID
    Ciclista ciclista = new Ciclista();
    // Não setamos o ID, o banco o fará automaticamente
    ciclista.setStatus("ATIVO");
    ciclista.setNome("Testador Ativo");

    // 3. Salva e armazena o ID real gerado
    Ciclista ciclistaSalvo = ciclistaRepository.save(ciclista);
    ciclistaIdGerado = ciclistaSalvo.getId();
}

// --- TESTE PARA POST /aluguel ---
@Test
void testRealizarAluguel_ComCiclistaAtivo_DeveRetornar200OK() throws Exception {
    NovoAluguelDTO dto = new NovoAluguelDTO();
    dto.setCiclista(ciclistaIdGerado); // Usa o ID GERADO
    dto.setTrancaInicio(TRANCA_ID);

    String jsonRequisicao = objectMapper.writeValueAsString(dto);

    mockMvc.perform(post("/aluguel")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequisicao))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cobranca").value(100));

    // Verifica se o aluguel foi salvo (1)
    assertEquals(1, aluguelRepository.count());
    // Verifica se é um aluguel ativo
    assertTrue(aluguelRepository.findByCiclistaAndHoraFimIsNull(ciclistaIdGerado).isPresent());
}

@Test
void testRealizarAluguel_ComCiclistaInativo_DeveRetornar422Unprocessable() throws Exception {
    // 1. Altera o status do ciclista para INATIVO
    Ciclista ciclista = ciclistaRepository.findById(ciclistaIdGerado).get();
    ciclista.setStatus("INATIVO");
    ciclistaRepository.save(ciclista);

    // 2. Tenta alugar
    NovoAluguelDTO dto = new NovoAluguelDTO();
    dto.setCiclista(ciclistaIdGerado); // Usa o ID GERADO
    dto.setTrancaInicio(TRANCA_ID);
    String jsonRequisicao = objectMapper.writeValueAsString(dto);

    mockMvc.perform(post("/aluguel")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequisicao))
            .andExpect(status().isUnprocessableEntity());

    // 3. Verifica se o aluguel NÃO foi criado
    assertEquals(0, aluguelRepository.count());
}


// --- TESTE PARA POST /devolucao (CORRIGIDO) ---
@Test
void testRealizarDevolucao_ComAluguelAtivo_DeveRetornar200EFecharRegistro() throws Exception {
    // --- 1. Organizar (Cria um aluguel ativo para devolver) ---
    Aluguel aluguel = new Aluguel();
    aluguel.setCiclista(ciclistaIdGerado); // Usa o ID GERADO
    aluguel.setBicicleta(BICICLETA_ID);
    aluguel.setTrancaInicio(TRANCA_ID);
    aluguel.setHoraInicio(LocalDateTime.now().minusHours(1));
    aluguelRepository.save(aluguel);

    // Garante que o aluguel está ativo ANTES da devolução
    assertTrue(aluguelRepository.findByBicicletaAndHoraFimIsNull(BICICLETA_ID).isPresent());

    // --- 2. Agir (Devolução) ---
    DevolucaoDTO dto = new DevolucaoDTO();
    dto.setIdBicicleta(BICICLETA_ID);
    dto.setIdTranca(20); // Tranca Fim
    String jsonRequisicao = objectMapper.writeValueAsString(dto);

    mockMvc.perform(post("/devolucao")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequisicao))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cobranca").value(101));

    // --- 3. Afirmar (Checa o estado do banco) ---
    // Verifica se o aluguel foi FECHADO (findByBicicletaAndHoraFimIsNull deve retornar vazio)
    assertFalse(aluguelRepository.findByBicicletaAndHoraFimIsNull(BICICLETA_ID).isPresent());
    // Verifica se um registro de Devolução foi criado
    assertEquals(1, devolucaoRepository.count());
}

@Test
void testRealizarDevolucao_SemAluguelAtivo_DeveRetornar404NotFound() throws Exception {
    // --- 1. Organizar (Banco limpo, sem aluguel ativo) ---
    DevolucaoDTO dto = new DevolucaoDTO();
    dto.setIdBicicleta(BICICLETA_ID);
    dto.setIdTranca(20);
    String jsonRequisicao = objectMapper.writeValueAsString(dto);

    // --- 2. Agir (Devolução) ---
    mockMvc.perform(post("/devolucao")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequisicao))
            .andExpect(status().isNotFound());

    // 3. Verifica se nada foi criado
    assertEquals(0, devolucaoRepository.count());
}

// --- TESTE PARA GET /restaurarBanco ---
@Test
void testRestaurarBanco_DeveLimparRepositorios() throws Exception {
    aluguelRepository.save(new Aluguel());
    devolucaoRepository.save(new Devolucao());

    mockMvc.perform(get("/restaurarBanco"))
            .andExpect(status().isOk());

    assertEquals(0, aluguelRepository.count());
    assertEquals(0, devolucaoRepository.count());
}
}