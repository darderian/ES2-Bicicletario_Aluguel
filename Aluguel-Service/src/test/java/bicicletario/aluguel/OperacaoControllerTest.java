package bicicletario.aluguel;

import bicicletario.aluguel.dto.DevolucaoDTO;
import bicicletario.aluguel.dto.NovoAluguelDTO;
import bicicletario.aluguel.model.Aluguel;
import bicicletario.aluguel.model.Devolucao;
import bicicletario.aluguel.model.Ciclista;
import bicicletario.aluguel.repository.AluguelRepository;
import bicicletario.aluguel.repository.DevolucaoRepository;
import bicicletario.aluguel.repository.CiclistaRepository;
import bicicletario.aluguel.repository.CartaoDeCreditoRepository; // Importante para verificar se criou cartão
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
import static org.hamcrest.Matchers.containsString; // Para verificar msg de string

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
private CartaoDeCreditoRepository cartaoDeCreditoRepository; // Adicionado para validação completa
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
    cartaoDeCreditoRepository.deleteAll();
    ciclistaRepository.deleteAll();

    // 2. Cria o ciclista ATIVO (para os testes de "caminho feliz")
    Ciclista ciclista = new Ciclista();
    ciclista.setStatus("ATIVO");
    ciclista.setNome("Testador Ativo");
    ciclista.setEmail("teste@ativo.com"); // Email único para não dar conflito

    // 3. Salva e armazena o ID real gerado
    Ciclista ciclistaSalvo = ciclistaRepository.save(ciclista);
    ciclistaIdGerado = ciclistaSalvo.getId();
}

// --- TESTES PARA POST /aluguel (UC03) ---

@Test
void testRealizarAluguel_ComCiclistaAtivo_DeveRetornar200OK() throws Exception {
    NovoAluguelDTO dto = new NovoAluguelDTO();
    dto.setCiclista(ciclistaIdGerado);
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

@Test
void testRealizarAluguel_ComCiclistaInativo_DeveRetornar422Unprocessable() throws Exception {
    Ciclista ciclista = ciclistaRepository.findById(ciclistaIdGerado).get();
    ciclista.setStatus("INATIVO");
    ciclistaRepository.save(ciclista);

    NovoAluguelDTO dto = new NovoAluguelDTO();
    dto.setCiclista(ciclistaIdGerado);
    dto.setTrancaInicio(TRANCA_ID);
    String jsonRequisicao = objectMapper.writeValueAsString(dto);

    mockMvc.perform(post("/aluguel")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequisicao))
            .andExpect(status().isUnprocessableEntity());

    assertEquals(0, aluguelRepository.count());
}

@Test
void testRealizarAluguel_ComCiclistaNaoAtivo_DeveRetornar422() throws Exception {
    Ciclista ciclistaNaoAtivo = new Ciclista();
    ciclistaNaoAtivo.setStatus("AGUARDANDO_CONFIRMACAO");
    ciclistaNaoAtivo.setNome("Testador Nao Ativo");
    ciclistaNaoAtivo.setEmail("teste@naoativo.com");
    Ciclista ciclistaSalvo = ciclistaRepository.save(ciclistaNaoAtivo);
    Integer idNaoAtivo = ciclistaSalvo.getId();

    NovoAluguelDTO dto = new NovoAluguelDTO();
    dto.setCiclista(idNaoAtivo);
    dto.setTrancaInicio(TRANCA_ID);
    String jsonRequisicao = objectMapper.writeValueAsString(dto);

    mockMvc.perform(post("/aluguel")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequisicao))
            .andExpect(status().isUnprocessableEntity());
}

@Test
void testRealizarAluguel_ComCiclistaJaComAluguelAtivo_DeveRetornar422() throws Exception {
    Aluguel aluguelAtivo = new Aluguel();
    aluguelAtivo.setCiclista(ciclistaIdGerado);
    aluguelAtivo.setBicicleta(999);
    aluguelAtivo.setTrancaInicio(99);
    aluguelAtivo.setHoraInicio(LocalDateTime.now().minusMinutes(30));
    aluguelRepository.save(aluguelAtivo);

    NovoAluguelDTO dto = new NovoAluguelDTO();
    dto.setCiclista(ciclistaIdGerado);
    dto.setTrancaInicio(TRANCA_ID);
    String jsonRequisicao = objectMapper.writeValueAsString(dto);

    mockMvc.perform(post("/aluguel")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequisicao))
            .andExpect(status().isUnprocessableEntity());

    assertEquals(1, aluguelRepository.count());
}


// --- TESTES PARA POST /devolucao (UC04) ---

@Test
void testRealizarDevolucao_ComAluguelAtivo_SemTaxaExtra_DeveRetornar200() throws Exception {
    Aluguel aluguel = new Aluguel();
    aluguel.setCiclista(ciclistaIdGerado);
    aluguel.setBicicleta(BICICLETA_ID);
    aluguel.setTrancaInicio(TRANCA_ID);
    aluguel.setHoraInicio(LocalDateTime.now().minusHours(1));
    aluguelRepository.save(aluguel);

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

@Test
void testRealizarDevolucao_ComAluguelAtivo_ComTaxaExtra_DeveRetornar200() throws Exception {
    Aluguel aluguel = new Aluguel();
    aluguel.setCiclista(ciclistaIdGerado);
    aluguel.setBicicleta(BICICLETA_ID);
    aluguel.setTrancaInicio(TRANCA_ID);
    aluguel.setHoraInicio(LocalDateTime.now().minusMinutes(151));
    aluguelRepository.save(aluguel);

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

@Test
void testRealizarDevolucao_SemAluguelAtivo_DeveRetornar404NotFound() throws Exception {
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

// --- TESTES DE SUPORTE (RESTAURAR) ---

/**
 * Teste Antigo: Garante que o método de zerar banco continua funcionando.
 */
@Test
void testRestaurarBanco_DeveLimparRepositorios() throws Exception {
    aluguelRepository.save(new Aluguel());
    devolucaoRepository.save(new Devolucao());

    mockMvc.perform(get("/restaurarBanco"))
            .andExpect(status().isOk());

    assertEquals(0, aluguelRepository.count());
    assertEquals(0, devolucaoRepository.count());
}

/**
 * NOVO TESTE (CRUCIAL PARA O SONAR):
 * Garante que o novo endpoint /restaurarDados é chamado e executa a lógica de criação.
 */
@Test
void testRestaurarDados_DevePopularBancoParaTestesDoProfessor() throws Exception {
    // 1. Chama o endpoint novo
    mockMvc.perform(get("/restaurarDados")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

    // 2. Valida se os dados foram criados mesmo
    // O PDF pedia 4 ciclistas, então vamos verificar se tem pelo menos 4 no banco
    long qtdCiclistas = ciclistaRepository.count();
    // Pode ser 4 ou 5 (dependendo se ele limpa o do setUp antes), mas tem que ser > 0
    assertTrue(qtdCiclistas >= 4, "Deveria ter criado os ciclistas do PDF");

    // Verifica se criou cartões também (parte da lógica nova)
    long qtdCartoes = cartaoDeCreditoRepository.count();
    assertTrue(qtdCartoes > 0, "Deveria ter criado cartões de crédito");
}
}