package bicicletario.aluguel.controller;

import bicicletario.aluguel.dto.NovoFuncionarioDTO;
import bicicletario.aluguel.model.Funcionario;
import bicicletario.aluguel.repository.FuncionarioRepository;
import bicicletario.aluguel.service.FuncionarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
public class FuncionarioController {

@Autowired
private FuncionarioService funcionarioService;

// Mantido apenas para leituras diretas se necessário, mas o ideal é usar o Service
@Autowired
private FuncionarioRepository repository;

/**
 * Caso de Uso: UC15 - Manter Cadastro de Funcionário (Fluxo Principal, Passo 3-8)
 * [POST /funcionario]
 */
@PostMapping("/funcionario")
public ResponseEntity<Funcionario> cadastrarFuncionario(@Valid @RequestBody NovoFuncionarioDTO dto) {
    try {
        Funcionario funcionarioSalvo = funcionarioService.cadastrarFuncionario(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(funcionarioSalvo);
    } catch (IllegalArgumentException e) {
        // Captura erro de senha diferente
        return ResponseEntity.unprocessableEntity().build();
    }
}

/**
 * Caso de Uso: UC15 - Manter Cadastro de Funcionário (Fluxo Principal, Passo 1)
 * [GET /funcionario]
 */
@GetMapping("/funcionario")
public ResponseEntity<List<Funcionario>> recuperarFuncionarios() {
    List<Funcionario> funcionarios = funcionarioService.recuperarTodos();
    return ResponseEntity.ok(funcionarios);
}

/**
 * Caso de Uso: UC15 - Manter Cadastro de Funcionário (Consulta)
 * [GET /funcionario/{idFuncionario}]
 */
@GetMapping("/funcionario/{idFuncionario}")
public ResponseEntity<Funcionario> recuperarFuncionario(@PathVariable Integer idFuncionario) {
    Optional<Funcionario> funcionarioOptional = funcionarioService.recuperarPorId(idFuncionario);

    if (funcionarioOptional.isPresent()) {
        return ResponseEntity.ok(funcionarioOptional.get());
    } else {
        return ResponseEntity.notFound().build();
    }
}

/**
 * Caso de Uso: UC15 - Manter Cadastro de Funcionário (Fluxo Alternativo A1)
 * [PUT /funcionario/{idFuncionario}]
 */
@PutMapping("/funcionario/{idFuncionario}")
public ResponseEntity<Funcionario> editarFuncionario(
        @PathVariable Integer idFuncionario,
        @Valid @RequestBody NovoFuncionarioDTO dto) {
    try {
        Funcionario funcionarioAtualizado = funcionarioService.editarFuncionario(idFuncionario, dto);
        return ResponseEntity.ok(funcionarioAtualizado);
    } catch (IllegalArgumentException e) {
        if (e.getMessage().contains("não encontrado")) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();
    }
}

/**
 * Caso de Uso: UC15 - Manter Cadastro de Funcionário (Fluxo Alternativo A2)
 * [DELETE /funcionario/{idFuncionario}]
 */
@DeleteMapping("/funcionario/{idFuncionario}")
public ResponseEntity<Void> removerFuncionario(@PathVariable Integer idFuncionario) {
    try {
        funcionarioService.removerFuncionario(idFuncionario);
        return ResponseEntity.ok().build();
    } catch (IllegalArgumentException e) {
        return ResponseEntity.notFound().build();
    }
}
}