package bicicletario.aluguel;

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
private Integer ciclistaIdGerado; // Este será o ciclista "ATIVO"

@BeforeEach
void setUp() {
    // 1. Limpa tudo
    aluguelRepository.deleteAll();
    devolucaoRepository.deleteAll();
    ciclistaRepository.deleteAll();

    // 2. Cria o ciclista ATIVO (para os testes de "caminho feliz")
    Ciclista ciclista = new Ciclista();
    ciclista.setStatus("ATIVO"); // Status "ATIVO" para os testes padrão
    ciclista.setNome("Testador Ativo");

    // 3. Salva e armazena o ID real gerado
    Ciclista ciclistaSalvo = ciclistaRepository.save(ciclista);
    ciclistaIdGerado = ciclistaSalvo.getId();
}

// --- TESTES PARA POST /aluguel (UC03) ---

/**
 * Testa o "Caminho Feliz" do UC03.
 * Usa o ciclista ATIVO criado no setUp().
 */
@Test
void testRealizarAluguel_ComCiclistaAtivo_DeveRetornar200OK() throws Exception {
    NovoAluguelDTO dto = new NovoAluguelDTO();
    dto.setCiclista(ciclistaIdGerado); // Usa o ciclista ATIVO
    dto.setTrancaInicio(TRANCA_ID);
    String jsonRequisicao = objectMapper.writeValueAsString(dto);

    mockMvc.perform(post("/aluguel")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequisicao))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cobranca").value(100));

    assertEquals(1, aluguelRepository.count());
    assertTrue(aluguelRepository.findByCiclistaAndHoraFimIsNull(ciclistaIdGerado).isPresent());
}

/**
 * Testa a falha do UC03 (Pré-condição).
 * Ciclista com status "INATIVO".
 */
@Test
void testRealizarAluguel_ComCiclistaInativo_DeveRetornar422Unprocessable() throws Exception {
    // 1. Altera o status do ciclista do setUp() para INATIVO
    Ciclista ciclista = ciclistaRepository.findById(ciclistaIdGerado).get();
    ciclista.setStatus("INATIVO");
    ciclistaRepository.save(ciclista);

    // 2. Tenta alugar
    NovoAluguelDTO dto = new NovoAluguelDTO();
    dto.setCiclista(ciclistaIdGerado);
    dto.setTrancaInicio(TRANCA_ID);
    String jsonRequisicao = objectMapper.writeValueAsString(dto);

    mockMvc.perform(post("/aluguel")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequisicao))
            .andExpect(status().isUnprocessableEntity()); // Espera 422

    assertEquals(0, aluguelRepository.count());
}

/**
 * NOVO TESTE DE FALHA (Cobre o Bug do Postman)
 * Testa a falha do UC03 (Pré-condição).
 * Ciclista com status "AGUARDANDO_CONFIRMACAO".
 */
@Test
void testRealizarAluguel_ComCiclistaNaoAtivo_DeveRetornar422() throws Exception {
    // --- 1. Organizar (Arrange) ---
    // Cria um NOVO ciclista separado do setUp()
    Ciclista ciclistaNaoAtivo = new Ciclista();
    ciclistaNaoAtivo.setStatus("AGUARDANDO_CONFIRMACAO"); // O status real pós-cadastro
    ciclistaNaoAtivo.setNome("Testador Nao Ativo");
    Ciclista ciclistaSalvo = ciclistaRepository.save(ciclistaNaoAtivo);
    Integer idNaoAtivo = ciclistaSalvo.getId();

    // --- 2. Agir (Act) ---
    NovoAluguelDTO dto = new NovoAluguelDTO();
    dto.setCiclista(idNaoAtivo); // Tenta alugar com o ID não ativo
    dto.setTrancaInicio(TRANCA_ID);
    String jsonRequisicao = objectMapper.writeValueAsString(dto);

    mockMvc.perform(post("/aluguel")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequisicao))
            // --- 3. Afirmar (Assert) ---
            .andExpect(status().isUnprocessableEntity()); // Espera 422
}


/**
 * NOVO TESTE DE FALHA (Cobre o Gap UC03-R1)
 * Testa a regra "só pode pegar uma bicicleta por vez".
 */
@Test
void testRealizarAluguel_ComCiclistaJaComAluguelAtivo_DeveRetornar422() throws Exception {
    // (O ciclista do setUp() está ATIVO, o que é necessário para este teste)
    // --- 1. Organizar (Arrange) ---
    Aluguel aluguelAtivo = new Aluguel();
    aluguelAtivo.setCiclista(ciclistaIdGerado); // Usa o ciclista ATIVO
    aluguelAtivo.setBicicleta(999);
    aluguelAtivo.setTrancaInicio(99);
    aluguelAtivo.setHoraInicio(LocalDateTime.now().minusMinutes(30));
    aluguelRepository.save(aluguelAtivo);

    // Prepara a DTO para uma *nova* tentativa de aluguel
    NovoAluguelDTO dto = new NovoAluguelDTO();
    dto.setCiclista(ciclistaIdGerado);
    dto.setTrancaInicio(TRANCA_ID);
    String jsonRequisicao = objectMapper.writeValueAsString(dto);

    // --- 2. Agir (Act) ---
    mockMvc.perform(post("/aluguel")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequisicao))
            // --- 3. Afirmar (Assert) ---
            .andExpect(status().isUnprocessableEntity()); // Espera 422

    assertEquals(1, aluguelRepository.count());
}


// --- TESTES PARA POST /devolucao (UC04) ---

/**
 * Testa o UC04 (Caminho Feliz) SEM taxa extra.
 */
@Test
void testRealizarDevolucao_ComAluguelAtivo_SemTaxaExtra_DeveRetornar200() throws Exception {
    // (O ciclista do setUp() está ATIVO)
    // --- 1. Organizar (Cria um aluguel de 1 hora) ---
    Aluguel aluguel = new Aluguel();
    aluguel.setCiclista(ciclistaIdGerado);
    aluguel.setBicicleta(BICICLETA_ID);
    aluguel.setTrancaInicio(TRANCA_ID);
    aluguel.setHoraInicio(LocalDateTime.now().minusHours(1)); // 1 hora de aluguel
    aluguelRepository.save(aluguel);

    assertTrue(aluguelRepository.findByBicicletaAndHoraFimIsNull(BICICLETA_ID).isPresent());

    // --- 2. Agir (Devolução) ---
    DevolucaoDTO dto = new DevolucaoDTO();
    dto.setIdBicicleta(BICICLETA_ID);
    dto.setIdTranca(20);
    String jsonRequisicao = objectMapper.writeValueAsString(dto);

    mockMvc.perform(post("/devolucao")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequisicao))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cobranca").isEmpty());

    assertFalse(aluguelRepository.findByBicicletaAndHoraFimIsNull(BICICLETA_ID).isPresent());
    assertEquals(1, devolucaoRepository.count());
}

/**
 * Testa o UC04 (Fluxo Alternativo A1) COM taxa extra.
 */
@Test
void testRealizarDevolucao_ComAluguelAtivo_ComTaxaExtra_DeveRetornar200() throws Exception {
    // (O ciclista do setUp() está ATIVO)
    // --- 1. Organizar (Cria um aluguel de 151 minutos) ---
    Aluguel aluguel = new Aluguel();
    aluguel.setCiclista(ciclistaIdGerado);
    aluguel.setBicicleta(BICICLETA_ID);
    aluguel.setTrancaInicio(TRANCA_ID);
    aluguel.setHoraInicio(LocalDateTime.now().minusMinutes(151)); // 2h 31m de aluguel
    aluguelRepository.save(aluguel);

    assertTrue(aluguelRepository.findByBicicletaAndHoraFimIsNull(BICICLETA_ID).isPresent());

    // --- 2. Agir (Devolução) ---
    DevolucaoDTO dto = new DevolucaoDTO();
    dto.setIdBicicleta(BICICLETA_ID);
    dto.setIdTranca(20);
    String jsonRequisicao = objectMapper.writeValueAsString(dto);

    mockMvc.perform(post("/devolucao")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequisicao))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cobranca").value(101));

    assertFalse(aluguelRepository.findByBicicletaAndHoraFimIsNull(BICICLETA_ID).isPresent());
    assertEquals(1, devolucaoRepository.count());
}

/**
 * Testa o UC04 (Fluxo de Exceção).
 */
@Test
void testRealizarDevolucao_SemAluguelAtivo_DeveRetornar404NotFound() throws Exception {
    // (Código inalterado)
    DevolucaoDTO dto = new DevolucaoDTO();
    dto.setIdBicicleta(BICICLETA_ID);
    dto.setIdTranca(20);
    String jsonRequisicao = objectMapper.writeValueAsString(dto);

    mockMvc.perform(post("/devolucao")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequisicao))
            .andExpect(status().isNotFound());

    assertEquals(0, devolucaoRepository.count());
}

// --- TESTE PARA GET /restaurarBanco ---
@Test
void testRestaurarBanco_DeveLimparRepositorios() throws Exception {
    // (Código inalterado)
    aluguelRepository.save(new Aluguel());
    devolucaoRepository.save(new Devolucao());

    mockMvc.perform(get("/restaurarBanco"))
            .andExpect(status().isOk());

    assertEquals(0, aluguelRepository.count());
    assertEquals(0, devolucaoRepository.count());
}
}