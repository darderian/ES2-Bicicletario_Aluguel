package bicicletario.aluguel;

import bicicletario.aluguel.dto.NovoFuncionarioDTO;
import bicicletario.aluguel.model.Funcionario;
import bicicletario.aluguel.repository.FuncionarioRepository;
import bicicletario.aluguel.service.FuncionarioService;
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
class FuncionarioServiceTest {

@InjectMocks
private FuncionarioService funcionarioService;

@Mock
private FuncionarioRepository funcionarioRepository;

// --- TESTES DE CADASTRO (UC15) ---

@Test
void cadastrarFuncionario_Sucesso() {
    // Arrange
    NovoFuncionarioDTO dto = new NovoFuncionarioDTO();
    dto.setSenha("123");
    dto.setConfirmacaoSenha("123"); // Senhas iguais
    dto.setNome("Teste");
    // --- CORREÇÃO: Preenchendo dados obrigatórios para evitar NPE ---
    dto.setIdade(30);
    dto.setFuncao("Reparador");
    dto.setCpf("12345678900");
    dto.setEmail("teste@email.com");
    // ----------------------------------------------------------------

    when(funcionarioRepository.save(any(Funcionario.class))).thenAnswer(i -> {
        Funcionario f = i.getArgument(0);
        f.setId(1);
        return f;
    });

    // Act
    Funcionario resultado = funcionarioService.cadastrarFuncionario(dto);

    // Assert
    assertNotNull(resultado);
    assertEquals(1, resultado.getId());
    assertEquals("Teste", resultado.getNome());
    verify(funcionarioRepository).save(any(Funcionario.class));
}

@Test
void cadastrarFuncionario_Falha_SenhasDiferentes() {
    // Arrange
    NovoFuncionarioDTO dto = new NovoFuncionarioDTO();
    dto.setSenha("123");
    dto.setConfirmacaoSenha("456"); // Senhas diferentes

    // Act & Assert
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
        funcionarioService.cadastrarFuncionario(dto);
    });

    assertEquals("A senha e a confirmação de senha devem ser iguais", ex.getMessage());
    verify(funcionarioRepository, never()).save(any());
}

// --- TESTES DE EDIÇÃO ---

@Test
void editarFuncionario_Sucesso() {
    // Arrange
    NovoFuncionarioDTO dto = new NovoFuncionarioDTO();
    dto.setNome("Nome Novo");
    // --- CORREÇÃO: Preenchendo dados obrigatórios para evitar NPE ---
    dto.setIdade(35);
    dto.setFuncao("Administrativo");
    dto.setCpf("11122233344");
    dto.setEmail("novo@email.com");
    // ----------------------------------------------------------------

    Funcionario funcionarioExistente = new Funcionario();
    funcionarioExistente.setId(1);
    funcionarioExistente.setNome("Nome Antigo");

    when(funcionarioRepository.findById(1)).thenReturn(Optional.of(funcionarioExistente));
    when(funcionarioRepository.save(any(Funcionario.class))).thenAnswer(i -> i.getArgument(0));

    // Act
    Funcionario resultado = funcionarioService.editarFuncionario(1, dto);

    // Assert
    assertEquals("Nome Novo", resultado.getNome());
}

@Test
void editarFuncionario_Falha_NaoEncontrado() {
    NovoFuncionarioDTO dto = new NovoFuncionarioDTO();
    when(funcionarioRepository.findById(99)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class, () -> {
        funcionarioService.editarFuncionario(99, dto);
    });
}

// --- TESTES DE REMOÇÃO ---

@Test
void removerFuncionario_Sucesso() {
    when(funcionarioRepository.existsById(1)).thenReturn(true);

    funcionarioService.removerFuncionario(1);

    verify(funcionarioRepository).deleteById(1);
}

@Test
void removerFuncionario_Falha_NaoEncontrado() {
    when(funcionarioRepository.existsById(99)).thenReturn(false);

    assertThrows(IllegalArgumentException.class, () -> {
        funcionarioService.removerFuncionario(99);
    });

    verify(funcionarioRepository, never()).deleteById(anyInt());
}
}