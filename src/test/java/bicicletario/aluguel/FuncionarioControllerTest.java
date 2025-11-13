package bicicletario.aluguel;

import bicicletario.aluguel.dto.NovoFuncionarioDTO; // Seu DTO
import bicicletario.aluguel.model.Funcionario; // Seu Model
import bicicletario.aluguel.repository.FuncionarioRepository; // Seu Repository
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class FuncionarioControllerTest {

@Autowired
private MockMvc mockMvc; // Simulador de requisições HTTP

@Autowired
private FuncionarioRepository repository; // Acesso ao banco

@Autowired
private ObjectMapper objectMapper; // Converte Objetos Java <-> JSON

/**
 * Limpa o banco de dados H2 ANTES de cada teste.
 */
@BeforeEach
void setUp() {
    repository.deleteAll();
}

// --- TESTE PARA POST /funcionario (UC15 - Incluir) ---

/**
 * Testa o "Caminho Feliz" do UC15 (Fluxo Principal, Passo 3-8).
 * POST /funcionario com dados válidos.
 */
@Test
void testCadastrarFuncionario_ComDadosValidos_DeveRetornar201Created() throws Exception {
    // --- 1. Organizar (Arrange) ---
    NovoFuncionarioDTO dto = criarNovoFuncionarioDTOValido();
    String jsonRequisicao = objectMapper.writeValueAsString(dto);

    // --- 2. Agir (Act) ---
    mockMvc.perform(post("/funcionario")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequisicao))

            // --- 3. Afirmar (Assert) ---
            .andExpect(status().isCreated()) // Espera 201
            .andExpect(jsonPath("$.id").exists()) // Espera que a resposta tenha um ID
            .andExpect(jsonPath("$.nome").value("Funcionario Teste"))
            .andExpect(jsonPath("$.email").value("func@teste.com"));
}

/**
 * NOVO TESTE: Testa a falha 422 (Validação de Negócio) do UC15.
 * Senha e confirmação de senha não batem.
 */
@Test
void testCadastrarFuncionario_ComSenhasDiferentes_DeveRetornar422() throws Exception {
    // --- 1. Organizar (Arrange) ---
    NovoFuncionarioDTO dto = criarNovoFuncionarioDTOValido();
    dto.setConfirmacaoSenha("senhaErrada"); // Senhas diferentes
    String jsonRequisicao = objectMapper.writeValueAsString(dto);

    // --- 2. Agir (Act) ---
    mockMvc.perform(post("/funcionario")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequisicao))

            // --- 3. Afirmar (Assert) ---
            .andExpect(status().isUnprocessableEntity()); // Espera 422
}

/**
 * NOVO TESTE: Testa a falha 422 (Validação @Valid) do UC15.
 * DTO com dados inválidos (ex: email em branco).
 */
@Test
void testCadastrarFuncionario_ComDadosInvalidos_DeveRetornar422() throws Exception {
    // --- 1. Organizar (Arrange) ---
    NovoFuncionarioDTO dto = criarNovoFuncionarioDTOValido();
    dto.setEmail(""); // Inválido (@NotBlank)
    String jsonRequisicao = objectMapper.writeValueAsString(dto);

    // --- 2. Agir (Act) ---
    mockMvc.perform(post("/funcionario")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequisicao))

            // --- 3. Afirmar (Assert) ---
            .andExpect(status().isUnprocessableEntity()); // Espera 422
}


// --- TESTE PARA GET /funcionario (UC15 - Listar) ---

/**
 * Testa o "Caminho Feliz" do UC15 (Fluxo Principal, Passo 1).
 * GET /funcionario.
 */
@Test
void testRecuperarFuncionarios_DeveRetornar200OK() throws Exception {
    // --- 1. Organizar (Arrange) ---
    repository.save(converterDtoParaEntidade(criarNovoFuncionarioDTOValido()));

    // --- 2. Agir (Act) ---
    mockMvc.perform(get("/funcionario"))

            // --- 3. Afirmar (Assert) ---
            .andExpect(status().isOk()) // Espera 200
            .andExpect(jsonPath("$").isArray()) // Espera que seja um Array
            .andExpect(jsonPath("$[0].nome").value("Funcionario Teste")); // Pega o primeiro da lista
}

// --- TESTES PARA GET /funcionario/{id} ---

/**
 * Testa a consulta de um funcionário (UC15).
 * GET /funcionario/{id}
 */
@Test
void testRecuperarFuncionario_ComIdExistente_DeveRetornar200OK() throws Exception {
    // --- 1. Organizar (Arrange) ---
    Funcionario salvo = repository.save(converterDtoParaEntidade(criarNovoFuncionarioDTOValido()));
    Integer idSalvo = salvo.getId();

    // --- 2. Agir (Act) ---
    mockMvc.perform(get("/funcionario/" + idSalvo))

            // --- 3. Afirmar (Assert) ---
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(idSalvo))
            .andExpect(jsonPath("$.nome").value("Funcionario Teste"));
}

/**
 * Testa a consulta de um funcionário (Falha 404).
 */
@Test
void testRecuperarFuncionario_ComIdInexistente_DeveRetornar404NotFound() throws Exception {
    // --- 2. Agir (Act) ---
    mockMvc.perform(get("/funcionario/999"))
            // --- 3. Afirmar (Assert) ---
            .andExpect(status().isNotFound()); // Espera 404
}

// --- TESTES PARA PUT /funcionario/{id} (UC15 - Editar) ---

/**
 * Testa o "Caminho Feliz" do UC15 (Fluxo Alternativo A1 - Editar).
 * PUT /funcionario/{id}
 */
@Test
void testEditarFuncionario_ComIdExistente_DeveRetornar200OK() throws Exception {
    // --- 1. Organizar (Arrange) ---
    Funcionario salvo = repository.save(converterDtoParaEntidade(criarNovoFuncionarioDTOValido()));
    Integer idSalvo = salvo.getId();

    NovoFuncionarioDTO dtoAtualizado = criarNovoFuncionarioDTOValido();
    dtoAtualizado.setNome("Funcionario Nome Atualizado");
    String jsonRequisicao = objectMapper.writeValueAsString(dtoAtualizado);

    // --- 2. Agir (Act) ---
    mockMvc.perform(put("/funcionario/" + idSalvo)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequisicao))

            // --- 3. Afirmar (Assert) ---
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(idSalvo))
            .andExpect(jsonPath("$.nome").value("Funcionario Nome Atualizado"));
}

/**
 * Testa a falha 404 do UC15 (Editar).
 */
@Test
void testEditarFuncionario_ComIdInexistente_DeveRetornar404NotFound() throws Exception {
    // --- 1. Organizar (Arrange) ---
    String jsonRequisicao = objectMapper.writeValueAsString(criarNovoFuncionarioDTOValido());

    // --- 2. Agir (Act) ---
    mockMvc.perform(put("/funcionario/999") // ID inexistente
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequisicao))
            // --- 3. Afirmar (Assert) ---
            .andExpect(status().isNotFound()); // Espera 404
}

// --- TESTES PARA DELETE /funcionario/{id} (UC15 - Excluir) ---

/**
 * Testa o "Caminho Feliz" do UC15 (Fluxo Alternativo A2 - Excluir).
 * DELETE /funcionario/{id}
 */
@Test
void testRemoverFuncionario_ComIdExistente_DeveRetornar200OK() throws Exception {
    // --- 1. Organizar (Arrange) ---
    Funcionario salvo = repository.save(converterDtoParaEntidade(criarNovoFuncionarioDTOValido()));
    Integer idSalvo = salvo.getId();

    // --- 2. Agir (Act) ---
    mockMvc.perform(delete("/funcionario/" + idSalvo))
            // --- 3. Afirmar (Assert) ---
            .andExpect(status().isOk()); // Espera 200
}

/**
 * Testa a falha 404 do UC15 (Excluir).
 */
@Test
void testRemoverFuncionario_ComIdInexistente_DeveRetornar404NotFound() throws Exception {
    // --- 2. Agir (Act) ---
    mockMvc.perform(delete("/funcionario/999")) // ID inexistente
            // --- 3. Afirmar (Assert) ---
            .andExpect(status().isNotFound()); // Espera 404
}


// --- Métodos Auxiliares (Helpers) para os Testes ---

private NovoFuncionarioDTO criarNovoFuncionarioDTOValido() {
    NovoFuncionarioDTO dto = new NovoFuncionarioDTO();
    dto.setNome("Funcionario Teste");
    dto.setEmail("func@teste.com");
    dto.setSenha("senha123");
    dto.setConfirmacaoSenha("senha123");
    dto.setIdade(30);
    dto.setFuncao("Reparador");
    dto.setCpf("12345678900");
    return dto;
}

private Funcionario converterDtoParaEntidade(NovoFuncionarioDTO dto) {
    Funcionario funcionario = new Funcionario();
    funcionario.setNome(dto.getNome());
    funcionario.setEmail(dto.getEmail());
    funcionario.setSenha(dto.getSenha());
    funcionario.setIdade(dto.getIdade());
    funcionario.setFuncao(dto.getFuncao());
    funcionario.setCpf(dto.getCpf());
    return funcionario;
}
}