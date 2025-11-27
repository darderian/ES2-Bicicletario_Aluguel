package bicicletario.aluguel;

import bicicletario.aluguel.dto.BicicletaDTO;
import bicicletario.aluguel.dto.CobrancaDTO;
import bicicletario.aluguel.dto.NovoAluguelDTO;
import bicicletario.aluguel.mock.EquipamentoService;
import bicicletario.aluguel.mock.ExternoService;
import bicicletario.aluguel.model.Aluguel;
import bicicletario.aluguel.model.Ciclista;
import bicicletario.aluguel.repository.AluguelRepository;
import bicicletario.aluguel.repository.CiclistaRepository;
import bicicletario.aluguel.repository.DevolucaoRepository;
import bicicletario.aluguel.service.AluguelService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // ISSO define um Teste Unitário puro (sem Spring Context pesado)
class AluguelServiceTest {

@InjectMocks
private AluguelService aluguelService; // A classe que estamos testando

@Mock
private AluguelRepository aluguelRepository; // Dependência Mockada
@Mock
private DevolucaoRepository devolucaoRepository; // Dependência Mockada
@Mock
private CiclistaRepository ciclistaRepository; // Dependência Mockada
@Mock
private EquipamentoService equipamentoService; // Dependência Mockada
@Mock
private ExternoService externoService; // Dependência Mockada

@Test
void realizarAluguel_Sucesso() {
    // 1. Arrange (Preparar os dados e o comportamento dos mocks)
    NovoAluguelDTO dto = new NovoAluguelDTO();
    dto.setCiclista(1);
    dto.setTrancaInicio(10);

    Ciclista ciclistaMock = new Ciclista();
    ciclistaMock.setId(1);
    ciclistaMock.setStatus("ATIVO"); // Cenário de sucesso

    BicicletaDTO bicicletaMock = new BicicletaDTO();
    bicicletaMock.setId(100);
    bicicletaMock.setStatus("DISPONIVEL");

    CobrancaDTO cobrancaMock = new CobrancaDTO();
    cobrancaMock.setStatus("PAGA");
    cobrancaMock.setId(500);

    // Ensinando os mocks a responderem
    when(ciclistaRepository.findById(1)).thenReturn(Optional.of(ciclistaMock));
    when(aluguelRepository.findByCiclistaAndHoraFimIsNull(1)).thenReturn(Optional.empty()); // Não tem aluguel ativo
    when(equipamentoService.getBicicletaDaTranca(10)).thenReturn(bicicletaMock);
    when(externoService.realizarCobranca(10.0, 1)).thenReturn(cobrancaMock);
    when(aluguelRepository.save(any(Aluguel.class))).thenAnswer(i -> i.getArguments()[0]); // Retorna o próprio objeto salvo

    // 2. Act (Executar a lógica)
    Aluguel resultado = aluguelService.realizarAluguel(dto);

    // 3. Assert (Verificar se a lógica funcionou)
    assertNotNull(resultado);
    assertEquals(1, resultado.getCiclista());
    assertEquals(100, resultado.getBicicleta()); // ID veio do mock de equipamento

    // Verificar se os serviços externos foram chamados corretamente
    verify(equipamentoService).destrancarTranca(10);
    verify(externoService).enviarEmail(any(), anyString(), anyString());
}

@Test
void realizarAluguel_Falha_CiclistaInativo() {
    // 1. Arrange
    NovoAluguelDTO dto = new NovoAluguelDTO();
    dto.setCiclista(1);

    Ciclista ciclistaMock = new Ciclista();
    ciclistaMock.setStatus("PENDENTE"); // Cenário de falha

    when(ciclistaRepository.findById(1)).thenReturn(Optional.of(ciclistaMock));

    // 2. Act & Assert (Esperamos uma exceção)
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
        aluguelService.realizarAluguel(dto);
    });

    assertEquals("Ciclista não encontrado ou não está ativo", exception.getMessage());

    // Garante que NADA foi salvo e NENHUM email foi enviado
    verify(aluguelRepository, never()).save(any());
    verify(externoService, never()).enviarEmail(any(), any(), any());
}

@Test
void realizarAluguel_Falha_AluguelDuplicado() {
    // 1. Arrange
    NovoAluguelDTO dto = new NovoAluguelDTO();
    dto.setCiclista(1);

    Ciclista ciclistaMock = new Ciclista();
    ciclistaMock.setStatus("ATIVO");

    when(ciclistaRepository.findById(1)).thenReturn(Optional.of(ciclistaMock));
    // Simulando que JÁ EXISTE um aluguel ativo
    when(aluguelRepository.findByCiclistaAndHoraFimIsNull(1)).thenReturn(Optional.of(new Aluguel()));

    // 2. Act & Assert
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
        aluguelService.realizarAluguel(dto);
    });

    assertEquals("Ciclista já possui um aluguel ativo", exception.getMessage());
}
}