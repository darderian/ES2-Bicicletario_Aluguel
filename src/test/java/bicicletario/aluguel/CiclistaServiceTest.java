package bicicletario.aluguel;

import bicicletario.aluguel.dto.CadastroCiclistaDTO;
import bicicletario.aluguel.dto.NovoCartaoDeCreditoDTO;
import bicicletario.aluguel.dto.NovoCiclistaDTO;
import bicicletario.aluguel.dto.PassaporteDTO;
import bicicletario.aluguel.mock.ExternoService;
import bicicletario.aluguel.model.CartaoDeCredito;
import bicicletario.aluguel.model.Ciclista;
import bicicletario.aluguel.repository.CartaoDeCreditoRepository;
import bicicletario.aluguel.repository.CiclistaRepository;
import bicicletario.aluguel.service.CiclistaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CiclistaServiceTest {

@InjectMocks
private CiclistaService ciclistaService;

@Mock
private CiclistaRepository ciclistaRepository;
@Mock
private CartaoDeCreditoRepository cartaoRepository;
@Mock
private ExternoService externoService;

// --- TESTES DE CADASTRO (UC01) ---

@Test
void cadastrarCiclista_Sucesso() {
    // Arrange
    CadastroCiclistaDTO dto = criarCadastroCiclistaDTO();

    // Simula cartão válido
    when(externoService.validarCartaoDeCredito(any())).thenReturn(true);
    // Simula salvamento no banco (retorna o objeto salvo)
    when(ciclistaRepository.save(any(Ciclista.class))).thenAnswer(i -> {
        Ciclista c = i.getArgument(0);
        c.setId(1); // Simula ID gerado pelo banco
        return c;
    });

    // Act
    Ciclista resultado = ciclistaService.cadastrarCiclista(dto);

    // Assert
    assertNotNull(resultado);
    assertEquals("AGUARDANDO_CONFIRMACAO", resultado.getStatus());

    // Verifica se salvou o cartão e enviou email
    verify(cartaoRepository).save(any(CartaoDeCredito.class));
    verify(externoService).enviarEmail(eq("teste@email.com"), anyString(), anyString());
}

@Test
void cadastrarCiclista_Falha_CartaoInvalido() {
    // Arrange
    CadastroCiclistaDTO dto = criarCadastroCiclistaDTO();
    // Simula cartão INVÁLIDO
    when(externoService.validarCartaoDeCredito(any())).thenReturn(false);

    // Act & Assert
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
        ciclistaService.cadastrarCiclista(dto);
    });

    assertEquals("Cartão de crédito inválido", ex.getMessage());
    // Garante que NÃO salvou nada
    verify(ciclistaRepository, never()).save(any());
    verify(externoService, never()).enviarEmail(any(), any(), any());
}

// --- TESTES DE EDIÇÃO (UC06) ---

@Test
void editarCiclista_Sucesso() {
    // Arrange
    NovoCiclistaDTO dto = new NovoCiclistaDTO();
    dto.setNome("Nome Novo");
    dto.setEmail("email@teste.com"); // --- CORREÇÃO: Definindo o email no DTO ---

    Ciclista ciclistaExistente = new Ciclista();
    ciclistaExistente.setId(1);
    ciclistaExistente.setEmail("email@teste.com");

    when(ciclistaRepository.findById(1)).thenReturn(Optional.of(ciclistaExistente));
    when(ciclistaRepository.save(any(Ciclista.class))).thenAnswer(i -> i.getArgument(0));

    // Act
    Ciclista resultado = ciclistaService.editarCiclista(1, dto);

    // Assert
    assertEquals("Nome Novo", resultado.getNome());
    // Agora o mock vai receber o email correto "email@teste.com"
    verify(externoService).enviarEmail(eq("email@teste.com"), anyString(), anyString());
}

@Test
void editarCiclista_Falha_NaoEncontrado() {
    NovoCiclistaDTO dto = new NovoCiclistaDTO();
    when(ciclistaRepository.findById(99)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class, () -> {
        ciclistaService.editarCiclista(99, dto);
    });
}

// --- TESTES DE ATIVAÇÃO (UC02) ---

@Test
void ativarCiclista_Sucesso() {
    Ciclista ciclista = new Ciclista();
    ciclista.setStatus("AGUARDANDO_CONFIRMACAO");

    when(ciclistaRepository.findById(1)).thenReturn(Optional.of(ciclista));
    when(ciclistaRepository.save(any(Ciclista.class))).thenAnswer(i -> i.getArgument(0));

    Ciclista resultado = ciclistaService.ativarCiclista(1);

    assertEquals("ATIVO", resultado.getStatus());
}

// --- TESTES DE CARTÃO (UC07) ---

@Test
void alterarCartao_Sucesso() {
    NovoCartaoDeCreditoDTO dto = new NovoCartaoDeCreditoDTO();
    dto.setNumero("1234");

    Ciclista ciclista = new Ciclista();
    ciclista.setEmail("ciclista@email.com");

    when(externoService.validarCartaoDeCredito(any())).thenReturn(true);
    when(cartaoRepository.findByIdCiclista(1)).thenReturn(Optional.of(new CartaoDeCredito()));
    when(ciclistaRepository.findById(1)).thenReturn(Optional.of(ciclista));

    ciclistaService.alterarCartaoDeCredito(1, dto);

    verify(cartaoRepository).save(any(CartaoDeCredito.class));
    verify(externoService).enviarEmail(eq("ciclista@email.com"), anyString(), anyString());
}

// --- HELPER ---
private CadastroCiclistaDTO criarCadastroCiclistaDTO() {
    NovoCiclistaDTO ciclista = new NovoCiclistaDTO();
    ciclista.setEmail("teste@email.com");
    ciclista.setNome("Teste");

    NovoCartaoDeCreditoDTO cartao = new NovoCartaoDeCreditoDTO();

    CadastroCiclistaDTO cadastro = new CadastroCiclistaDTO();
    cadastro.setCiclista(ciclista);
    cadastro.setMeioDePagamento(cartao);
    return cadastro;
}
}