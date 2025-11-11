package bicicletario.aluguel.controller;

import bicicletario.aluguel.dto.CobrancaDTO;
import bicicletario.aluguel.dto.DevolucaoDTO;
import bicicletario.aluguel.dto.NovoAluguelDTO;
import bicicletario.aluguel.model.Aluguel;
import bicicletario.aluguel.model.Devolucao;
import bicicletario.aluguel.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;

@RestController
public class OperacaoController {

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

// Endpoint: POST /aluguel
@PostMapping("/aluguel")
public ResponseEntity<Aluguel> realizarAluguel(@Valid @RequestBody NovoAluguelDTO dto) {

    // --- INÍCIO DO MOCK (Simulação - Tarefa 8) ---
    // Agora o mock cria um DTO completo, como você sugeriu!
    CobrancaDTO cobrancaMock = new CobrancaDTO();
    cobrancaMock.setId(100);
    cobrancaMock.setStatus("PAGA"); // Simula que foi PAGA
    cobrancaMock.setValor(10.0); // Valor fixo do aluguel
    cobrancaMock.setCiclista(dto.getCiclista());

    if (!cobrancaMock.getStatus().equals("PAGA")) {
        return ResponseEntity.unprocessableEntity().build();
    }
    // --- FIM DO MOCK ---

    Aluguel novoAluguel = new Aluguel();
    novoAluguel.setCiclista(dto.getCiclista());
    novoAluguel.setTrancaInicio(dto.getTrancaInicio());
    novoAluguel.setHoraInicio(LocalDateTime.now());
    novoAluguel.setCobranca(cobrancaMock.getId()); // <-- Usamos o ID do DTO
    novoAluguel.setBicicleta(123); // Simulado

    Aluguel aluguelSalvo = aluguelRepository.save(novoAluguel);
    return ResponseEntity.ok(aluguelSalvo);
}

// Endpoint: POST /devolucao
@PostMapping("/devolucao")
public ResponseEntity<Devolucao> realizarDevolucao(@Valid @RequestBody DevolucaoDTO dto) {

    // --- INÍCIO DO MOCK (Simulação - Tarefa 8) ---
    // Simula a cobrança de taxa extra
    CobrancaDTO cobrancaExtraMock = new CobrancaDTO();
    cobrancaExtraMock.setId(101);
    cobrancaExtraMock.setStatus("PAGA");
    cobrancaExtraMock.setValor(5.0); // Taxa extra
    // --- FIM DO MOCK ---

    Devolucao novaDevolucao = new Devolucao();
    novaDevolucao.setBicicleta(dto.getIdBicicleta());
    novaDevolucao.setTrancaFim(dto.getIdTranca());
    novaDevolucao.setHoraFim(LocalDateTime.now());
    novaDevolucao.setCobranca(cobrancaExtraMock.getId()); // <-- Usamos o ID do DTO
    novaDevolucao.setCiclista(1); // Simulado
    novaDevolucao.setHoraInicio(LocalDateTime.now().minusHours(1)); // Simulado

    Devolucao devolucaoSalva = devolucaoRepository.save(novaDevolucao);
    return ResponseEntity.ok(devolucaoSalva);
}

// Endpoint GET / (Raiz)
@GetMapping("/")
public String getRootStatus() {
    return "O Microsserviço de Aluguel está online e operacional.";
}

// Endpoint: GET /restaurarBanco
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