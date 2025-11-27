package bicicletario.aluguel.controller;

import bicicletario.aluguel.dto.DevolucaoDTO;
import bicicletario.aluguel.dto.NovoAluguelDTO;
import bicicletario.aluguel.model.Aluguel;
import bicicletario.aluguel.model.Devolucao;
import bicicletario.aluguel.repository.*;
import bicicletario.aluguel.service.AluguelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
public class OperacaoController {

@Autowired
private AluguelService aluguelService;

// Mantemos os repositórios AQUI apenas para o método auxiliar de teste/admin 'restaurarBanco'
@Autowired
private AluguelRepository aluguelRepository;
@Autowired
private DevolucaoRepository devolucaoRepository;
@Autowired
private FuncionarioRepository funcionarioRepository;
@Autowired
private CiclistaRepository ciclistaRepository;
@Autowired
private CartaoDeCreditoRepository cartaoDeCreditoRepository;

/**
 * Caso de Uso: UC03 - Alugar bicicleta
 * [POST /aluguel]
 */
@PostMapping("/aluguel")
public ResponseEntity<Aluguel> realizarAluguel(@Valid @RequestBody NovoAluguelDTO dto) {
    try {
        // O Controller agora apenas delega para o Service (o "Chefe de Cozinha")
        Aluguel aluguel = aluguelService.realizarAluguel(dto);
        return ResponseEntity.ok(aluguel);
    } catch (IllegalArgumentException e) {
        // Captura as exceções de negócio lançadas pelo Service e retorna 422
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();
    }
}

/**
 * Caso de Uso: UC04 - Devolver bicicleta
 * [POST /devolucao]
 */
@PostMapping("/devolucao")
public ResponseEntity<Devolucao> realizarDevolucao(@Valid @RequestBody DevolucaoDTO dto) {
    try {
        Devolucao devolucao = aluguelService.realizarDevolucao(dto);
        return ResponseEntity.ok(devolucao);
    } catch (IllegalArgumentException e) {
        // Se a mensagem for de "não encontrado", retorna 404 (mantendo o comportamento original)
        if (e.getMessage().contains("Nenhum aluguel ativo")) {
            return ResponseEntity.notFound().build();
        }
        // Para outros erros de negócio, retorna 422
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();
    }
}

/**
 * Caso de Uso: Suporte (Status da API)
 * [GET /]
 */
@GetMapping("/")
public String getRootStatus() {
    return "O Microsserviço de Aluguel está online e operacional.";
}

/**
 * Caso de Uso: Suporte (Testes/Deploy)
 * [GET /restaurarBanco]
 */
@GetMapping("/restaurarBanco")
public ResponseEntity<String> restaurarBanco() {
    try {
        cartaoDeCreditoRepository.deleteAll();
        aluguelRepository.deleteAll();
        devolucaoRepository.deleteAll();
        ciclistaRepository.deleteAll();
        funcionarioRepository.deleteAll();

        return ResponseEntity.ok("Banco de dados (Aluguel) restaurado.");
    } catch (Exception e) {
        return ResponseEntity.internalServerError().body("Erro ao restaurar banco: " + e.getMessage());
    }
}
}