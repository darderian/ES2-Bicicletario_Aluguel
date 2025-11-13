package bicicletario.aluguel;

import bicicletario.aluguel.controller.CiclistaController;
import bicicletario.aluguel.dto.CadastroCiclistaDTO;
import bicicletario.aluguel.dto.NovoCartaoDeCreditoDTO;
import bicicletario.aluguel.dto.NovoCiclistaDTO;
import bicicletario.aluguel.dto.PassaporteDTO;
import bicicletario.aluguel.model.Aluguel;
import bicicletario.aluguel.model.CartaoDeCredito;
import bicicletario.aluguel.model.Ciclista;
import bicicletario.aluguel.model.Passaporte;
import bicicletario.aluguel.mock.ExternoService; // --- IMPORTADO PARA O MOCKBEAN ---
import bicicletario.aluguel.repository.AluguelRepository;
import bicicletario.aluguel.repository.CartaoDeCreditoRepository;
import bicicletario.aluguel.repository.CiclistaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean; // --- IMPORT NECESSÁRIO ---
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any; // --- IMPORT NECESSÁRIO ---
import static org.mockito.Mockito.when; // --- IMPORT NECESSÁRIO ---
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
private AluguelRepository aluguelRepository;
@Autowired
private ObjectMapper objectMapper;

@MockBean
private ExternoService externoService;

@BeforeEach
void setUp() {
    cartaoRepository.deleteAll();
    aluguelRepository.deleteAll();
    ciclistaRepository.deleteAll();

    when(externoService.validarCartaoDeCredito(any(NovoCartaoDeCreditoDTO.class)))
            .thenReturn(true);
}

@Test
void contextLoads() {
    assertNotNull(controller);
}

// --- TESTES POST /ciclista (UC01) ---

/**
 * Testa o "Caminho Feliz" do UC01.
 * Dados válidos, cartão válido.
 */
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

/**
 * NOVO TESTE: Testa a falha do UC01 (Fluxo Alternativo A3).
 * Dados válidos, mas o mock do ExternoService retorna "Cartão Inválido".
 */
@Test
void testCadastrarCiclista_ComCartaoInvalido_DeveRetornar422() throws Exception {
    when(externoService.validarCartaoDeCredito(any(NovoCartaoDeCreditoDTO.class)))
            .thenReturn(false); // FORÇA A FALHA

    CadastroCiclistaDTO requisicaoCompleta = criarCadastroCiclistaDTOValido();
    String jsonRequisicao = objectMapper.writeValueAsString(requisicaoCompleta);

    mockMvc.perform(post("/ciclista")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequisicao))
            .andExpect(status().isUnprocessableEntity()); // Espera 422
}

/**
 * NOVO TESTE: Testa a falha do UC01 (Validação @Valid).
 * Dados do DTO são inválidos (ex: email mal formatado).
 */
@Test
void testCadastrarCiclista_ComDadosInvalidos_DeveRetornar422() throws Exception {
    CadastroCiclistaDTO requisicao = criarCadastroCiclistaDTOValido();
    requisicao.getCiclista().setEmail("email-invalido.com"); // Dado inválido
    String jsonRequisicao = objectMapper.writeValueAsString(requisicao);

    mockMvc.perform(post("/ciclista")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequisicao))
            .andExpect(status().isUnprocessableEntity()); // Espera 422
}


// --- TESTES GET /ciclista/{id} ---

/**
 * Testa GET /ciclista/{id} (Caminho Feliz)
 */
@Test
void testRecuperarCiclista_ComIdExistente_DeveRetornar200OK() throws Exception {
    Ciclista ciclistaSalvo = ciclistaRepository.save(converterDtoParaEntidade(criarNovoCiclistaDTOValido()));
    Integer idSalvo = ciclistaSalvo.getId();

    mockMvc.perform(get("/ciclista/" + idSalvo))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(idSalvo));
}

/**
 * Testa GET /ciclista/{id} (Falha 404)
 */
@Test
void testRecuperarCiclista_ComIdInexistente_DeveRetornar404NotFound() throws Exception {
    mockMvc.perform(get("/ciclista/999"))
            .andExpect(status().isNotFound());
}

// --- TESTES PUT /ciclista/{id} (UC06) ---

/**
 * Testa o "Caminho Feliz" do UC06.
 */
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

/**
 * Testa a falha 404 do UC06.
 */
@Test
void testEditarCiclista_ComIdInexistente_DeveRetornar404NotFound() throws Exception {
    String jsonRequisicao = objectMapper.writeValueAsString(criarNovoCiclistaDTOValido());
    mockMvc.perform(put("/ciclista/999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequisicao))
            .andExpect(status().isNotFound());
}

/**
 * Testa a falha 422 (@Valid) do UC06 (Fluxo Alternativo A2).
 */
@Test
void testEditarCiclista_ComDadosInvalidos_DeveRetornar422UnprocessableEntity() throws Exception {
    Ciclista ciclistaSalvo = ciclistaRepository.save(converterDtoParaEntidade(criarNovoCiclistaDTOValido()));
    Integer idSalvo = ciclistaSalvo.getId();
    NovoCiclistaDTO dtoInvalido = criarNovoCiclistaDTOValido();
    dtoInvalido.setEmail("emailinvalido.com"); // <-- DADO INVÁLIDO
    String jsonRequisicao = objectMapper.writeValueAsString(dtoInvalido);

    mockMvc.perform(put("/ciclista/" + idSalvo)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequisicao))
            .andExpect(status().isUnprocessableEntity()); // Espera 422
}

// --- TESTES POST /ciclista/{id}/ativar (UC02) ---

/**
 * Testa o "Caminho Feliz" do UC02.
 */
@Test
void testAtivarCadastro_ComIdExistente_DeveRetornar200EStatusAtivo() throws Exception {
    NovoCiclistaDTO dto = criarNovoCiclistaDTOValido();
    Ciclista ciclista = converterDtoParaEntidade(dto);
    ciclista.setStatus("AGUARDANDO_CONFIRMACAO"); // Estado inicial
    Ciclista ciclistaSalvo = ciclistaRepository.save(ciclista);
    Integer idSalvo = ciclistaSalvo.getId();

    mockMvc.perform(post("/ciclista/" + idSalvo + "/ativar"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("ATIVO"));
}

/**
 * Testa a falha 404 do UC02.
 */
@Test
void testAtivarCadastro_ComIdInexistente_DeveRetornar404NotFound() throws Exception {
    mockMvc.perform(post("/ciclista/999/ativar"))
            .andExpect(status().isNotFound());
}


// --- TESTES GET /ciclista/{id}/permiteAluguel (Suporte UC03) ---

/**
 * Testa o "Caminho Feliz" do GET /permiteAluguel (Retorna true).
 */
@Test
void testPermiteAluguel_ComCiclistaApto_DeveRetornarTrue() throws Exception {
    Ciclista ciclista = ciclistaRepository.save(converterDtoParaEntidade(criarNovoCiclistaDTOValido()));
    Integer idSalvo = ciclista.getId();

    mockMvc.perform(get("/ciclista/" + idSalvo + "/permiteAluguel"))
            .andExpect(status().isOk())
            .andExpect(content().string("true"));
}

/**
 * Testa o "Caminho Feliz" do GET /permiteAluguel (Retorna false).
 */
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

/**
 * Testa a falha 404 do GET /permiteAluguel.
 */
@Test
void testPermiteAluguel_ComIdInexistente_DeveRetornar404NotFound() throws Exception {
    mockMvc.perform(get("/ciclista/999/permiteAluguel"))
            .andExpect(status().isNotFound());
}

// --- TESTES GET /ciclista/{id}/bicicletaAlugada ---

/**
 * Testa GET /bicicletaAlugada (Caminho Feliz, com aluguel).
 */
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

/**
 * Testa GET /bicicletaAlugada (Caminho Feliz, sem aluguel).
 */
@Test
void testGetBicicletaAlugada_ComCiclistaSemAluguel_DeveRetornarVazio() throws Exception {
    Ciclista ciclista = ciclistaRepository.save(converterDtoParaEntidade(criarNovoCiclistaDTOValido()));
    Integer idSalvo = ciclista.getId();

    mockMvc.perform(get("/ciclista/" + idSalvo + "/bicicletaAlugada"))
            .andExpect(status().isOk())
            .andExpect(content().string(""));
}

/**
 * Testa GET /bicicletaAlugada (Falha 404).
 */
@Test
void testGetBicicletaAlugada_ComIdInexistente_DeveRetornar404NotFound() throws Exception {
    mockMvc.perform(get("/ciclista/999/bicicletaAlugada"))
            .andExpect(status().isNotFound());
}

// --- TESTES GET /ciclista/existeEmail/{email} (Suporte UC01) ---

/**
 * Testa GET /existeEmail (Retorna true).
 */
@Test
void testExisteEmail_ComEmailExistente_DeveRetornarTrue() throws Exception {
    NovoCiclistaDTO dto = criarNovoCiclistaDTOValido();
    dto.setEmail("email.existe@teste.com");
    ciclistaRepository.save(converterDtoParaEntidade(dto));

    mockMvc.perform(get("/ciclista/existeEmail/email.existe@teste.com"))
            .andExpect(status().isOk())
            .andExpect(content().string("true"));
}

/**
 * Testa GET /existeEmail (Retorna false).
 */
@Test
void testExisteEmail_ComEmailInexistente_DeveRetornarFalse() throws Exception {
    mockMvc.perform(get("/ciclista/existeEmail/nao.existe@teste.com"))
            .andExpect(status().isOk())
            .andExpect(content().string("false"));
}

// --- TESTES PARA /cartaoDeCredito/{idCiclista} (UC07) ---

/**
 * Testa GET /cartaoDeCredito/{id} (Caminho Feliz).
 */
@Test
void testGetCartaoDeCredito_ComIdCiclistaExistente_DeveRetornar200OK() throws Exception {
    Ciclista ciclista = ciclistaRepository.save(converterDtoParaEntidade(criarNovoCiclistaDTOValido()));
    Integer idCiclistaSalvo = ciclista.getId();
    CartaoDeCredito cartao = new CartaoDeCredito();
    cartao.setIdCiclista(idCiclistaSalvo);
    cartao.setNumero("1111222233334444");
    cartaoRepository.save(cartao);

    mockMvc.perform(get("/cartaoDeCredito/" + idCiclistaSalvo))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.idCiclista").value(idCiclistaSalvo))
            .andExpect(jsonPath("$.numero").value("1111222233334444"));
}

/**
 * Testa GET /cartaoDeCredito/{id} (Falha 404).
 */
@Test
void testGetCartaoDeCredito_ComIdCiclistaInexistente_DeveRetornar404NotFound() throws Exception {
    mockMvc.perform(get("/cartaoDeCredito/999")) // ID inexistente
            .andExpect(status().isNotFound());
}

/**
 * Testa o "Caminho Feliz" do UC07 (PUT /cartaoDeCredito).
 */
@Test
void testAlterarCartaoDeCredito_ComIdCiclistaExistente_DeveRetornar200OK() throws Exception {
    Ciclista ciclista = ciclistaRepository.save(converterDtoParaEntidade(criarNovoCiclistaDTOValido()));
    Integer idCiclistaSalvo = ciclista.getId();
    CartaoDeCredito cartao = new CartaoDeCredito();
    cartao.setIdCiclista(idCiclistaSalvo);
    cartao.setNumero("1111222233334444");
    cartao.setNomeTitular("Nome Antigo");
    cartaoRepository.save(cartao);

    // --- CORREÇÃO ---
    NovoCartaoDeCreditoDTO dtoAtualizado = criarCadastroCiclistaDTOValido().getMeioDePagamento();
    // --- FIM DA CORREÇÃO ---

    dtoAtualizado.setNomeTitular("Nome Novo");
    String jsonRequisicao = objectMapper.writeValueAsString(dtoAtualizado);

    mockMvc.perform(put("/cartaoDeCredito/" + idCiclistaSalvo)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequisicao))
            .andExpect(status().isOk());
}

/**
 * Testa a falha 404 do UC07.
 */
@Test
void testAlterarCartaoDeCredito_ComIdCiclistaInexistente_DeveRetornar404NotFound() throws Exception {
    // --- CORREÇÃO ---
    NovoCartaoDeCreditoDTO dtoAtualizado = criarCadastroCiclistaDTOValido().getMeioDePagamento();
    // --- FIM DA CORREÇÃO ---
    String jsonRequisicao = objectMapper.writeValueAsString(dtoAtualizado);

    mockMvc.perform(put("/cartaoDeCredito/999") // ID inexistente
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequisicao))
            .andExpect(status().isNotFound());
}

/**
 * NOVO TESTE: Testa a falha do UC07 (Fluxo Alternativo A2).
 * Cartão é reprovado pelo mock do ExternoService.
 */
@Test
void testAlterarCartaoDeCredito_ComCartaoInvalido_DeveRetornar422() throws Exception {
    Ciclista ciclista = ciclistaRepository.save(converterDtoParaEntidade(criarNovoCiclistaDTOValido()));
    Integer idCiclistaSalvo = ciclista.getId();
    CartaoDeCredito cartao = new CartaoDeCredito();
    cartao.setIdCiclista(idCiclistaSalvo);
    cartao.setNumero("1111222233334444");
    cartaoRepository.save(cartao);

    when(externoService.validarCartaoDeCredito(any(NovoCartaoDeCreditoDTO.class)))
            .thenReturn(false); // FORÇA A FALHA

    // --- CORREÇÃO ---
    NovoCartaoDeCreditoDTO dtoAtualizado = criarCadastroCiclistaDTOValido().getMeioDePagamento();
    // --- FIM DA CORREÇÃO ---
    String jsonRequisicao = objectMapper.writeValueAsString(dtoAtualizado);

    mockMvc.perform(put("/cartaoDeCredito/" + idCiclistaSalvo)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequisicao))
            .andExpect(status().isUnprocessableEntity()); // Espera 422
}

/**
 * NOVO TESTE: Testa a falha 422 (@Valid) do UC07.
 */
@Test
void testAlterarCartaoDeCredito_ComDadosInvalidos_DeveRetornar422() throws Exception {
    Ciclista ciclista = ciclistaRepository.save(converterDtoParaEntidade(criarNovoCiclistaDTOValido()));
    Integer idCiclistaSalvo = ciclista.getId();
    CartaoDeCredito cartao = new CartaoDeCredito();
    cartao.setIdCiclista(idCiclistaSalvo);
    cartao.setNumero("1111222233334444");
    cartaoRepository.save(cartao);

    // --- CORREÇÃO ---
    NovoCartaoDeCreditoDTO dtoInvalido = criarCadastroCiclistaDTOValido().getMeioDePagamento();
    // --- FIM DA CORREÇÃO ---
    dtoInvalido.setCvv(""); // Dado inválido (NotBlank)
    String jsonRequisicao = objectMapper.writeValueAsString(dtoInvalido);

    mockMvc.perform(put("/cartaoDeCredito/" + idCiclistaSalvo)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequisicao))
            .andExpect(status().isUnprocessableEntity()); // Espera 422
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