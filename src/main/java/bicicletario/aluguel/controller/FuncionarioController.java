package bicicletario.aluguel.controller;

import org.springframework.web.bind.annotation.RestController;
import bicicletario.aluguel.dto.NovoFuncionarioDTO; // Importa seu DTO
import bicicletario.aluguel.model.Funcionario; // Importa seu Model
import bicicletario.aluguel.repository.FuncionarioRepository; // Importa seu Repository
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid; // Para validar o DTO
import java.util.List;
import java.util.Optional;

@RestController
public class FuncionarioController {

@Autowired
private FuncionarioRepository repository;

// Endpoint: POST /funcionario
// Objetivo: Cadastrar um novo funcionário
@PostMapping("/funcionario")
public ResponseEntity<Funcionario> cadastrarFuncionario(@Valid @RequestBody NovoFuncionarioDTO dto) {

    // Validação simples (em um caso real, seria mais robusta)
    if (dto.getSenha() == null || !dto.getSenha().equals(dto.getConfirmacaoSenha())) {
        // Retorna 422 Unprocessable Entity (Erro de validação)
        // Nota: O @Valid já cuida dos @NotBlank, @Email, etc.
        return ResponseEntity.unprocessableEntity().build();
    }

    // Converte o DTO para a entidade Funcionario
    Funcionario novoFuncionario = converterDtoParaEntidade(dto);

    // Salva no banco de dados
    Funcionario funcionarioSalvo = repository.save(novoFuncionario);

    // Retorna 201 Created com o funcionário salvo no corpo
    return ResponseEntity.status(HttpStatus.CREATED).body(funcionarioSalvo);
}

// Endpoint: GET /funcionario
// Objetivo: Recuperar a lista de todos os funcionários
@GetMapping("/funcionario")
public ResponseEntity<List<Funcionario>> recuperarFuncionarios() {

    // Busca todos os funcionários no banco
    List<Funcionario> funcionarios = repository.findAll();

    // Retorna 200 OK com a lista no corpo
    return ResponseEntity.ok(funcionarios);
}

// Endpoint: GET /funcionario/{idFuncionario}
// Objetivo: Recuperar um funcionário específico pelo ID
@GetMapping("/funcionario/{idFuncionario}")
public ResponseEntity<Funcionario> recuperarFuncionario(@PathVariable Integer idFuncionario) {

    // Busca no banco. Retorna um Optional (pode conter ou não um funcionário)
    Optional<Funcionario> funcionarioOptional = repository.findById(idFuncionario);

    if (funcionarioOptional.isPresent()) {
        // Se encontrou, retorna 200 OK com o funcionário
        return ResponseEntity.ok(funcionarioOptional.get());
    } else {
        // Se não encontrou, retorna 404 Not Found
        return ResponseEntity.notFound().build();
    }
}

// Endpoint: PUT /funcionario/{idFuncionario}
// Objetivo: Atualizar os dados de um funcionário
@PutMapping("/funcionario/{idFuncionario}")
public ResponseEntity<Funcionario> editarFuncionario(
        @PathVariable Integer idFuncionario,
        @Valid @RequestBody NovoFuncionarioDTO dto) {

    // 1. Verifica se o funcionário que queremos editar existe
    Optional<Funcionario> funcionarioOptional = repository.findById(idFuncionario);
    //tive que mudar de is.empty para isPresent por estar usando java 8
    if (!funcionarioOptional.isPresent()) {
        // Se não existe, retorna 404 Not Found
        return ResponseEntity.notFound().build();
    }

    // 2. Pega o funcionário existente do banco
    Funcionario funcionarioExistente = funcionarioOptional.get();

    // 3. Atualiza os dados do funcionário existente com os dados do DTO
    funcionarioExistente.setNome(dto.getNome());
    funcionarioExistente.setEmail(dto.getEmail());
    funcionarioExistente.setIdade(dto.getIdade());
    funcionarioExistente.setFuncao(dto.getFuncao());
    funcionarioExistente.setCpf(dto.getCpf());
    // (Nota: Em um app real, teríamos uma lógica para atualizar a senha)

    // 4. Salva o funcionário atualizado no banco
    Funcionario funcionarioAtualizado = repository.save(funcionarioExistente);

    // 5. Retorna 200 OK com o funcionário atualizado
    return ResponseEntity.ok(funcionarioAtualizado);
}

// Endpoint: DELETE /funcionario/{idFuncionario}
// Objetivo: Remover um funcionário
@DeleteMapping("/funcionario/{idFuncionario}")
public ResponseEntity<Void> removerFuncionario(@PathVariable Integer idFuncionario) {

    // 1. Verifica se o funcionário existe ANTES de tentar deletar
    if (!repository.existsById(idFuncionario)) {
        // Se não existe, retorna 404 Not Found
        return ResponseEntity.notFound().build();
    }

    // 2. Se existe, deleta do banco
    repository.deleteById(idFuncionario);

    // 3. Retorna 200 OK (ou 204 No Content) sem corpo
    return ResponseEntity.ok().build();
}


// --- Método Auxiliar (Helper) ---
private Funcionario converterDtoParaEntidade(NovoFuncionarioDTO dto) {
    Funcionario funcionario = new Funcionario();
    funcionario.setNome(dto.getNome());
    funcionario.setEmail(dto.getEmail());
    funcionario.setSenha(dto.getSenha()); // Lembrete: Hashear a senha!
    funcionario.setIdade(dto.getIdade());
    funcionario.setFuncao(dto.getFuncao());
    funcionario.setCpf(dto.getCpf());
    // (A 'matricula' não está no DTO, então fica nula ou é gerada depois)
    return funcionario;
}
}
