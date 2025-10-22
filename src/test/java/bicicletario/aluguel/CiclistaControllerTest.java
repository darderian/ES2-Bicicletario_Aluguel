package bicicletario.aluguel;

// --- IMPORTS NECESSÁRIOS ---
import bicicletario.aluguel.controller.CiclistaController;
import bicicletario.aluguel.dto.CadastroCiclistaDTO;
import bicicletario.aluguel.dto.NovoCartaoDeCreditoDTO;
import bicicletario.aluguel.dto.NovoCiclistaDTO;
import bicicletario.aluguel.dto.PassaporteDTO;
import bicicletario.aluguel.model.Ciclista;
import bicicletario.aluguel.repository.CartaoDeCreditoRepository;
import bicicletario.aluguel.repository.CiclistaRepository;
import com.fasterxml.jackson.databind.ObjectMapper; // Para converter objetos em JSON
import org.junit.jupiter.api.BeforeEach; // Para limpar o banco antes de cada teste
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc; // Configura o MockMvc
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType; // Para definir o Content-Type
import org.springframework.test.web.servlet.MockMvc; // O simulador de HTTP
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*; // (get, post, put, delete)
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*; // (status, jsonPath)

@SpringBootTest // Carrega a aplicação Spring para o teste
@AutoConfigureMockMvc // Habilita o MockMvc para simular requisições HTTP

class CiclistaControllerTest {

@Autowired
private MockMvc mockMvc; // Nosso simulador de requisições

@Autowired
private CiclistaController controller; // O controller que estamos testando

@Autowired
private CiclistaRepository ciclistaRepository; // Acesso ao banco

@Autowired
private CartaoDeCreditoRepository cartaoRepository; // Acesso ao banco

@Autowired
private ObjectMapper objectMapper; // Converte Objetos Java <-> JSON

/**
 * Este método roda ANTES de CADA teste (@Test).
 * Ele limpa o banco de dados (H2) para garantir que um teste não
 * interfira no resultado do próximo.
 */
@BeforeEach
void setUp() {
    // Ordem importa: delete cartões primeiro por causa da chave estrangeira
    cartaoRepository.deleteAll();
    ciclistaRepository.deleteAll();
}

@Test
void contextLoads() {
    // Teste original: verifica se o controller foi injetado
    assertNotNull(controller);
}

// --- TESTE PARA POST /ciclista ---
@Test
void testCadastrarCiclista_ComDadosValidos_DeveRetornar201Created() throws Exception {
    // --- 1. Organizar (Arrange) ---
    // Cria todos os DTOs necessários para a requisição
    PassaporteDTO passaporte = new PassaporteDTO();
    passaporte.setNumero("123456");
    passaporte.setValidade("2030-01-01");
    passaporte.setPais("BR");

    NovoCartaoDeCreditoDTO cartao = new NovoCartaoDeCreditoDTO();
    cartao.setNomeTitular("Ciclista Teste");
    cartao.setNumero("1234567890123456");
    cartao.setValidade("2030-12-01");
    cartao.setCvv("123");

    NovoCiclistaDTO ciclistaDTO = new NovoCiclistaDTO();
    ciclistaDTO.setNome("Ciclista Teste");
    ciclistaDTO.setNascimento("1990-01-01");
    ciclistaDTO.setNacionalidade("BRASILEIRO");
    ciclistaDTO.setCpf("11122233344");
    ciclistaDTO.setEmail("teste@email.com");
    ciclistaDTO.setSenha("senha123");
    ciclistaDTO.setPassaporte(passaporte);
    ciclistaDTO.setUrlFotoDocumento("http://foto.com/doc");

    CadastroCiclistaDTO requisicaoCompleta = new CadastroCiclistaDTO();
    requisicaoCompleta.setCiclista(ciclistaDTO);
    requisicaoCompleta.setMeioDePagamento(cartao);

    // Converte o objeto de requisição para uma String JSON
    String jsonRequisicao = objectMapper.writeValueAsString(requisicaoCompleta);

    // --- 2. Agir (Act) ---
    // Simula a requisição POST para /ciclista
    mockMvc.perform(post("/ciclista")
                    .contentType(MediaType.APPLICATION_JSON) // Define o tipo de conteúdo
                    .content(jsonRequisicao)) // Define o corpo (body) da requisição

            // --- 3. Afirmar (Assert) ---
            .andExpect(status().isCreated()) // Espera o status 201 Created
            .andExpect(jsonPath("$.id").exists()) // Espera que a resposta tenha um ID
            .andExpect(jsonPath("$.nome").value("Ciclista Teste"))
            .andExpect(jsonPath("$.status").value("AGUARDANDO_CONFIRMACAO"));

    // Verifica também se o cartão foi salvo no banco
    assertTrue(cartaoRepository.count() > 0);
}

// --- TESTES PARA GET /ciclista/{idCiclista} ---

@Test
void testRecuperarCiclista_ComIdExistente_DeveRetornar200OK() throws Exception {
    // --- 1. Organizar (Arrange) ---
    // Salva um ciclista no banco H2 para podermos buscá-lo
    Ciclista ciclista = new Ciclista();
    ciclista.setNome("Ciclista Para Buscar");
    ciclista.setEmail("buscar@email.com");
    ciclista.setStatus("ATIVO");
    Ciclista ciclistaSalvo = ciclistaRepository.save(ciclista);
    Integer idSalvo = ciclistaSalvo.getId();

    // --- 2. Agir (Act) ---
    // Simula a requisição GET para /ciclista/{id}
    mockMvc.perform(get("/ciclista/" + idSalvo))

            // --- 3. Afirmar (Assert) ---
            .andExpect(status().isOk()) // Espera o status 200 OK
            .andExpect(jsonPath("$.id").value(idSalvo))
            .andExpect(jsonPath("$.nome").value("Ciclista Para Buscar"));
}

@Test
void testRecuperarCiclista_ComIdInexistente_DeveRetornar404NotFound() throws Exception {
    // --- 1. Organizar (Arrange) ---
    // O banco está limpo (garantido pelo @BeforeEach)
    Integer idInexistente = 999;

    // --- 2. Agir (Act) ---
    // Simula a requisição GET para um ID que não existe
    mockMvc.perform(get("/ciclista/" + idInexistente))

            // --- 3. Afirmar (Assert) ---
            .andExpect(status().isNotFound()); // Espera o status 404 Not Found
}
//TODO: testAlterarCiclista_ComIdExistente_DeveRetornar200()
//
//TODO: testAlterarCiclista_ComIdInexistente_DeveRetornar404()
//
//TODO: testAlterarCiclista_ComDadosInvalidos_DeveRetornar422() (Ex: email sem @)
//
//TODO: testAtivarCadastro_ComIdExistente_DeveRetornar200()
//
//TODO: testPermiteAluguel_ComCiclistaApto_DeveRetornarTrue()
//
//TODO: testPermiteAluguel_ComCiclistaInapto_DeveRetornarFalse()
//
//TODO: testGetBicicletaAlugada_ComCiclistaComAluguel_DeveRetornarBicicleta()
//
//TODO: testGetBicicletaAlugada_SemAluguel_DeveRetornarVazio()
//
//TODO: testExisteEmail_ComEmailExistente_DeveRetornarTrue()
//
//TODO: testExisteEmail_ComEmailInexistente_DeveRetornarFalse()
//
//TODO: testGetCartaoDeCredito_ComIdCiclistaExistente_DeveRetornar200()
//
//TODO: testGetCartaoDeCredito_ComIdCiclistaInexistente_DeveRetornar404()
//
//TODO: testAlterarCartaoDeCredito_ComIdCiclistaExistente_DeveRetornar200()
}