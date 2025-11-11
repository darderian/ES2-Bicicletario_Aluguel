package bicicletario.aluguel;

import bicicletario.aluguel.controller.CiclistaController;
import bicicletario.aluguel.dto.CadastroCiclistaDTO;
import bicicletario.aluguel.dto.NovoCartaoDeCreditoDTO;
import bicicletario.aluguel.dto.NovoCiclistaDTO;
import bicicletario.aluguel.dto.PassaporteDTO;
import bicicletario.aluguel.model.Aluguel; // Import necessário
import bicicletario.aluguel.model.CartaoDeCredito; // Import necessário
import bicicletario.aluguel.model.Ciclista;
import bicicletario.aluguel.model.Passaporte;
import bicicletario.aluguel.repository.AluguelRepository; // Import necessário
import bicicletario.aluguel.repository.CartaoDeCreditoRepository;
import bicicletario.aluguel.repository.CiclistaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CiclistaControllerTest {

@Autowired
private MockMvc mockMvc;
@Autowired
private CiclistaController controller;
@Autowired
private CiclistaRepository ciclistaRepository;
@Autowired
private CartaoDeCreditoRepository cartaoRepository;
@Autowired
private AluguelRepository aluguelRepository; // Necessário para /permiteAluguel
@Autowired
private ObjectMapper objectMapper;

@BeforeEach
void setUp() {
    // Ordem de limpeza (filhos primeiro)
    cartaoRepository.deleteAll();
    aluguelRepository.deleteAll();
    ciclistaRepository.deleteAll();
}

@Test
void contextLoads() {
    assertNotNull(controller);
}

// --- TESTE POST /ciclista ---
@Test
void testCadastrarCiclista_ComDadosValidos_DeveRetornar201Created() throws Exception {
    CadastroCiclistaDTO requisicaoCompleta = criarCadastroCiclistaDTOValido();
    String jsonRequisicao = objectMapper.writeValueAsString(requisicaoCompleta);

    mockMvc.perform(post("/ciclista")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequisicao))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.nome").value("Ciclista Teste"))
            .andExpect(jsonPath("$.status").value("AGUARDANDO_CONFIRMACAO"));

    assertTrue(cartaoRepository.count() > 0);
}

// --- TESTES GET /ciclista/{id} ---
@Test
void testRecuperarCiclista_ComIdExistente_DeveRetornar200OK() throws Exception {
    Ciclista ciclistaSalvo = ciclistaRepository.save(converterDtoParaEntidade(criarNovoCiclistaDTOValido()));
    Integer idSalvo = ciclistaSalvo.getId();

    mockMvc.perform(get("/ciclista/" + idSalvo))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(idSalvo));
}

@Test
void testRecuperarCiclista_ComIdInexistente_DeveRetornar404NotFound() throws Exception {
    mockMvc.perform(get("/ciclista/999"))
            .andExpect(status().isNotFound());
}

// --- TESTES PUT /ciclista/{id} ---
@Test
void testEditarCiclista_ComIdExistente_DeveRetornar200OK() throws Exception {
    Ciclista ciclistaSalvo = ciclistaRepository.save(converterDtoParaEntidade(criarNovoCiclistaDTOValido()));
    Integer idSalvo = ciclistaSalvo.getId();
    NovoCiclistaDTO dtoAtualizado = criarNovoCiclistaDTOValido();
    dtoAtualizado.setNome("Ciclista Nome Atualizado");
    String jsonRequisicao = objectMapper.writeValueAsString(dtoAtualizado);

    mockMvc.perform(put("/ciclista/" + idSalvo)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequisicao))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome").value("Ciclista Nome Atualizado"));
}

@Test
void testEditarCiclista_ComIdInexistente_DeveRetornar404NotFound() throws Exception {
    String jsonRequisicao = objectMapper.writeValueAsString(criarNovoCiclistaDTOValido());
    mockMvc.perform(put("/ciclista/999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequisicao))
            .andExpect(status().isNotFound());
}

// --- TESTES POST /ciclista/{id}/ativar ---
@Test
void testAtivarCadastro_ComIdExistente_DeveRetornar200EStatusAtivo() throws Exception {
    NovoCiclistaDTO dto = criarNovoCiclistaDTOValido();
    Ciclista ciclista = converterDtoParaEntidade(dto);
    ciclista.setStatus("AGUARDANDO_CONFIRMACAO");
    Ciclista ciclistaSalvo = ciclistaRepository.save(ciclista);
    Integer idSalvo = ciclistaSalvo.getId();

    mockMvc.perform(post("/ciclista/" + idSalvo + "/ativar"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("ATIVO"));
}

@Test
void testAtivarCadastro_ComIdInexistente_DeveRetornar404NotFound() throws Exception {
    mockMvc.perform(post("/ciclista/999/ativar"))
            .andExpect(status().isNotFound());
}
// TESTE DE VALIDAÇÃO (NOVO)
@Test
void testEditarCiclista_ComDadosInvalidos_DeveRetornar422UnprocessableEntity() throws Exception {
    // --- 1. Organizar (Arrange) ---
    // Salva um ciclista no banco
    Ciclista ciclistaSalvo = ciclistaRepository.save(converterDtoParaEntidade(criarNovoCiclistaDTOValido()));
    Integer idSalvo = ciclistaSalvo.getId();

    // Cria um DTO com dados INVÁLIDOS (e-mail sem '@')
    NovoCiclistaDTO dtoInvalido = criarNovoCiclistaDTOValido();
    dtoInvalido.setEmail("emailinvalido.com"); // <-- DADO INVÁLIDO

    String jsonRequisicao = objectMapper.writeValueAsString(dtoInvalido);

    // --- 2. Agir (Act) ---
    mockMvc.perform(put("/ciclista/" + idSalvo)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequisicao))

            // --- 3. Afirmar (Assert) ---
            .andExpect(status().isUnprocessableEntity()); // Espera 422
}
// --- TESTES GET /ciclista/{id}/permiteAluguel ---
@Test
void testPermiteAluguel_ComCiclistaApto_DeveRetornarTrue() throws Exception {
    Ciclista ciclista = ciclistaRepository.save(converterDtoParaEntidade(criarNovoCiclistaDTOValido()));
    Integer idSalvo = ciclista.getId();

    mockMvc.perform(get("/ciclista/" + idSalvo + "/permiteAluguel"))
            .andExpect(status().isOk())
            .andExpect(content().string("true"));
}

@Test
void testPermiteAluguel_ComCiclistaInapto_DeveRetornarFalse() throws Exception {
    Ciclista ciclista = ciclistaRepository.save(converterDtoParaEntidade(criarNovoCiclistaDTOValido()));
    Integer idSalvo = ciclista.getId();
    Aluguel aluguelAtivo = new Aluguel();
    aluguelAtivo.setCiclista(idSalvo);
    aluguelAtivo.setHoraInicio(java.time.LocalDateTime.now());
    aluguelAtivo.setHoraFim(null);
    aluguelRepository.save(aluguelAtivo);

    mockMvc.perform(get("/ciclista/" + idSalvo + "/permiteAluguel"))
            .andExpect(status().isOk())
            .andExpect(content().string("false"));
}

@Test
void testPermiteAluguel_ComIdInexistente_DeveRetornar404NotFound() throws Exception {
    mockMvc.perform(get("/ciclista/999/permiteAluguel"))
            .andExpect(status().isNotFound());
}

// --- TESTES GET /ciclista/{id}/bicicletaAlugada ---
@Test
void testGetBicicletaAlugada_ComCiclistaComAluguel_DeveRetornarBicicletaMock() throws Exception {
    Ciclista ciclista = ciclistaRepository.save(converterDtoParaEntidade(criarNovoCiclistaDTOValido()));
    Integer idSalvo = ciclista.getId();
    Aluguel aluguelAtivo = new Aluguel();
    aluguelAtivo.setCiclista(idSalvo);
    aluguelAtivo.setBicicleta(123);
    aluguelAtivo.setHoraInicio(java.time.LocalDateTime.now());
    aluguelAtivo.setHoraFim(null);
    aluguelRepository.save(aluguelAtivo);

    mockMvc.perform(get("/ciclista/" + idSalvo + "/bicicletaAlugada"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(123))
            .andExpect(jsonPath("$.modelo").value("Modelo Falso (DTO)"));
}

@Test
void testGetBicicletaAlugada_ComCiclistaSemAluguel_DeveRetornarVazio() throws Exception {
    Ciclista ciclista = ciclistaRepository.save(converterDtoParaEntidade(criarNovoCiclistaDTOValido()));
    Integer idSalvo = ciclista.getId();

    mockMvc.perform(get("/ciclista/" + idSalvo + "/bicicletaAlugada"))
            .andExpect(status().isOk())
            .andExpect(content().string(""));
}

@Test
void testGetBicicletaAlugada_ComIdInexistente_DeveRetornar404NotFound() throws Exception {
    mockMvc.perform(get("/ciclista/999/bicicletaAlugada"))
            .andExpect(status().isNotFound());
}

// --- TESTES GET /ciclista/existeEmail/{email} ---
@Test
void testExisteEmail_ComEmailExistente_DeveRetornarTrue() throws Exception {
    NovoCiclistaDTO dto = criarNovoCiclistaDTOValido();
    dto.setEmail("email.existe@teste.com");
    ciclistaRepository.save(converterDtoParaEntidade(dto));

    mockMvc.perform(get("/ciclista/existeEmail/email.existe@teste.com"))
            .andExpect(status().isOk())
            .andExpect(content().string("true"));
}

@Test
void testExisteEmail_ComEmailInexistente_DeveRetornarFalse() throws Exception {
    mockMvc.perform(get("/ciclista/existeEmail/nao.existe@teste.com"))
            .andExpect(status().isOk())
            .andExpect(content().string("false"));
}

// --- TESTES PARA /cartaoDeCredito/{idCiclista} (NOVOS) ---

@Test
void testGetCartaoDeCredito_ComIdCiclistaExistente_DeveRetornar200OK() throws Exception {
    // --- 1. Organizar (Arrange) ---
    // Salva um ciclista e um cartão para ele
    Ciclista ciclista = ciclistaRepository.save(converterDtoParaEntidade(criarNovoCiclistaDTOValido()));
    Integer idCiclistaSalvo = ciclista.getId();

    CartaoDeCredito cartao = new CartaoDeCredito();
    cartao.setIdCiclista(idCiclistaSalvo);
    cartao.setNumero("1111222233334444");
    cartaoRepository.save(cartao);

    // --- 2. Agir (Act) ---
    mockMvc.perform(get("/cartaoDeCredito/" + idCiclistaSalvo))
            // --- 3. Afirmar (Assert) ---
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.idCiclista").value(idCiclistaSalvo))
            .andExpect(jsonPath("$.numero").value("1111222233334444"));
}

@Test
void testGetCartaoDeCredito_ComIdCiclistaInexistente_DeveRetornar404NotFound() throws Exception {
    // --- 2. Agir (Act) ---
    mockMvc.perform(get("/cartaoDeCredito/999")) // ID inexistente
            // --- 3. Afirmar (Assert) ---
            .andExpect(status().isNotFound());
}

@Test
void testAlterarCartaoDeCredito_ComIdCiclistaExistente_DeveRetornar200OK() throws Exception {
    // --- 1. Organizar (Arrange) ---
    // Salva um ciclista e um cartão para ele
    Ciclista ciclista = ciclistaRepository.save(converterDtoParaEntidade(criarNovoCiclistaDTOValido()));
    Integer idCiclistaSalvo = ciclista.getId();

    CartaoDeCredito cartao = new CartaoDeCredito();
    cartao.setIdCiclista(idCiclistaSalvo);
    cartao.setNumero("1111222233334444");
    cartao.setNomeTitular("Nome Antigo");
    cartaoRepository.save(cartao);

    // Cria o DTO de atualização
    NovoCartaoDeCreditoDTO dtoAtualizado = new NovoCartaoDeCreditoDTO();
    dtoAtualizado.setNomeTitular("Nome Novo");
    dtoAtualizado.setNumero("9999888877776666");
    dtoAtualizado.setValidade("2030-12-01");
    dtoAtualizado.setCvv("123");
    String jsonRequisicao = objectMapper.writeValueAsString(dtoAtualizado);

    // --- 2. Agir (Act) ---
    mockMvc.perform(put("/cartaoDeCredito/" + idCiclistaSalvo)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequisicao))
            // --- 3. Afirmar (Assert) ---
            .andExpect(status().isOk()); // Espera 200 OK (sem corpo)
}

@Test
void testAlterarCartaoDeCredito_ComIdCiclistaInexistente_DeveRetornar404NotFound() throws Exception {
    // --- 1. Organizar (Arrange) ---
    NovoCartaoDeCreditoDTO dtoAtualizado = new NovoCartaoDeCreditoDTO();
    dtoAtualizado.setNomeTitular("Nome Novo");
    dtoAtualizado.setNumero("9999888877776666");
    dtoAtualizado.setValidade("2030-12-01");
    dtoAtualizado.setCvv("123");
    String jsonRequisicao = objectMapper.writeValueAsString(dtoAtualizado);

    // --- 2. Agir (Act) ---
    mockMvc.perform(put("/cartaoDeCredito/999") // ID inexistente
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequisicao))
            // --- 3. Afirmar (Assert) ---
            .andExpect(status().isNotFound());
}


// --- Métodos Auxiliares (Helpers) para os Testes ---

private CadastroCiclistaDTO criarCadastroCiclistaDTOValido() {
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
    return requisicaoCompleta;
}

private NovoCiclistaDTO criarNovoCiclistaDTOValido() {
    NovoCiclistaDTO ciclistaDTO = new NovoCiclistaDTO();
    ciclistaDTO.setNome("Ciclista Para Editar");
    ciclistaDTO.setNascimento("1990-01-01");
    ciclistaDTO.setNacionalidade("BRASILEIRO");
    ciclistaDTO.setCpf("11122233344");
    ciclistaDTO.setEmail("editar@email.com");
    ciclistaDTO.setSenha("senha123");
    return ciclistaDTO;
}

private Ciclista converterDtoParaEntidade(NovoCiclistaDTO dto) {
    Ciclista ciclista = new Ciclista();
    ciclista.setNome(dto.getNome());
    ciclista.setNascimento(dto.getNascimento());
    ciclista.setCpf(dto.getCpf());
    ciclista.setNacionalidade(dto.getNacionalidade());
    ciclista.setEmail(dto.getEmail());
    ciclista.setSenha(dto.getSenha());
    ciclista.setStatus("ATIVO");

    if (dto.getPassaporte() != null) {
        PassaporteDTO passDto = dto.getPassaporte();
        Passaporte passaporte = new Passaporte();
        passaporte.setPassaporteNumero(passDto.getNumero());
        passaporte.setPassaporteValidade(passDto.getValidade());
        passaporte.setPassaportePais(passDto.getPais());
        ciclista.setPassaporte(passaporte);
    }
    return ciclista;
}
}