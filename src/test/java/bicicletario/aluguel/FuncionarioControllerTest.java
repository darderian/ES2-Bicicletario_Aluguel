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

// --- TESTE PARA POST /funcionario ---
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

// --- TESTE PARA GET /funcionario (Todos) ---
@Test
void testRecuperarFuncionarios_DeveRetornar200OK() throws Exception {
    // --- 1. Organizar (Arrange) ---
    // Salva um funcionário no banco para a lista não vir vazia
    repository.save(converterDtoParaEntidade(criarNovoFuncionarioDTOValido()));

    // --- 2. Agir (Act) ---
    mockMvc.perform(get("/funcionario"))

            // --- 3. Afirmar (Assert) ---
            .andExpect(status().isOk()) // Espera 200
            .andExpect(jsonPath("$").isArray()) // Espera que seja um Array
            .andExpect(jsonPath("$[0].nome").value("Funcionario Teste")); // Pega o primeiro da lista
}

// --- TESTES PARA GET /funcionario/{id} ---
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

@Test
void testRecuperarFuncionario_ComIdInexistente_DeveRetornar404NotFound() throws Exception {
    // --- 2. Agir (Act) ---
    mockMvc.perform(get("/funcionario/999"))
            // --- 3. Afirmar (Assert) ---
            .andExpect(status().isNotFound()); // Espera 404
}

// --- TESTES PARA PUT /funcionario/{id} ---
@Test
void testEditarFuncionario_ComIdExistente_DeveRetornar200OK() throws Exception {
    // --- 1. Organizar (Arrange) ---
    Funcionario salvo = repository.save(converterDtoParaEntidade(criarNovoFuncionarioDTOValido()));
    Integer idSalvo = salvo.getId();

    // Cria um DTO com dados atualizados
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

// --- TESTES PARA DELETE /funcionario/{id} ---
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