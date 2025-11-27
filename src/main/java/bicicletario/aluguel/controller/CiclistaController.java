package bicicletario.aluguel.controller;

import bicicletario.aluguel.dto.BicicletaDTO;
import bicicletario.aluguel.dto.CadastroCiclistaDTO;
import bicicletario.aluguel.dto.NovoCartaoDeCreditoDTO;
import bicicletario.aluguel.dto.NovoCiclistaDTO;
import bicicletario.aluguel.mock.EquipamentoService;
import bicicletario.aluguel.model.Aluguel;
import bicicletario.aluguel.model.CartaoDeCredito;
import bicicletario.aluguel.model.Ciclista;
import bicicletario.aluguel.repository.AluguelRepository;
import bicicletario.aluguel.repository.CartaoDeCreditoRepository;
import bicicletario.aluguel.repository.CiclistaRepository;
import bicicletario.aluguel.service.CiclistaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

@RestController
public class CiclistaController {

@Autowired
private CiclistaService ciclistaService; // O novo Service

// Repositórios mantidos apenas para LEITURA (GET) direta
@Autowired
private CiclistaRepository ciclistaRepository;
@Autowired
private CartaoDeCreditoRepository cartaoRepository;
@Autowired
private AluguelRepository aluguelRepository;
@Autowired
private EquipamentoService equipamentoService;

/**
 * Caso de Uso: UC01 - Cadastrar Ciclista
 * [POST /ciclista]
 */
@PostMapping("/ciclista")
public ResponseEntity<Ciclista> cadastrarCiclista(@Valid @RequestBody CadastroCiclistaDTO cadastroDTO) {
    try {
        Ciclista ciclistaSalvo = ciclistaService.cadastrarCiclista(cadastroDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(ciclistaSalvo);
    } catch (IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();
    }
}

/**
 * Caso de Uso: Suporte (Consulta de dados para outros UCs)
 * [GET /ciclista/{idCiclista}]
 */
@GetMapping("/ciclista/{idCiclista}")
public ResponseEntity<Ciclista> recuperarCiclista(@PathVariable Integer idCiclista) {
    Optional<Ciclista> ciclistaOptional = ciclistaRepository.findById(idCiclista);
    if (ciclistaOptional.isPresent()) {
        return ResponseEntity.ok(ciclistaOptional.get());
    } else {
        return ResponseEntity.notFound().build();
    }
}

/**
 * Caso de Uso: UC06 - Alterar Dados do Ciclista
 * [PUT /ciclista/{idCiclista}]
 */
@PutMapping("/ciclista/{idCiclista}")
public ResponseEntity<Ciclista> editarCiclista(
        @PathVariable Integer idCiclista,
        @Valid @RequestBody NovoCiclistaDTO dto) {
    try {
        Ciclista ciclistaAtualizado = ciclistaService.editarCiclista(idCiclista, dto);
        return ResponseEntity.ok(ciclistaAtualizado);
    } catch (IllegalArgumentException e) {
        if (e.getMessage().contains("não encontrado")) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();
    }
}

/**
 * Caso de Uso: UC02 - Confirmar email (Ativação)
 * [POST /ciclista/{idCiclista}/ativar]
 */
@PostMapping("/ciclista/{idCiclista}/ativar")
public ResponseEntity<Ciclista> ativarCiclista(@PathVariable Integer idCiclista) {
    try {
        Ciclista ciclistaAtivado = ciclistaService.ativarCiclista(idCiclista);
        return ResponseEntity.ok(ciclistaAtivado);
    } catch (IllegalArgumentException e) {
        return ResponseEntity.notFound().build();
    }
}

/**
 * Caso de Uso: Suporte ao UC03 (Verifica R1 - "só pode pegar uma bicicleta por vez")
 * [GET /ciclista/{idCiclista}/permiteAluguel]
 */
@GetMapping("/ciclista/{idCiclista}/permiteAluguel")
public ResponseEntity<Boolean> permiteAluguel(@PathVariable Integer idCiclista) {
    if (!ciclistaRepository.existsById(idCiclista)) {
        return ResponseEntity.notFound().build();
    }
    Optional<Aluguel> aluguelAtivo = aluguelRepository.findByCiclistaAndHoraFimIsNull(idCiclista);
    boolean podeAlugar = !aluguelAtivo.isPresent();
    return ResponseEntity.ok(podeAlugar);
}

/**
 * Caso de Uso: Suporte (Consulta bicicleta alugada)
 * [GET /ciclista/{idCiclista}/bicicletaAlugada]
 */
@GetMapping("/ciclista/{idCiclista}/bicicletaAlugada")
public ResponseEntity<?> getBicicletaAlugada(@PathVariable Integer idCiclista) {
    if (!ciclistaRepository.existsById(idCiclista)) {
        return ResponseEntity.notFound().build();
    }
    Optional<Aluguel> aluguelAtivo = aluguelRepository.findByCiclistaAndHoraFimIsNull(idCiclista);
    if (aluguelAtivo.isPresent()) {
        Integer bicicletaId = aluguelAtivo.get().getBicicleta();
        BicicletaDTO bicicletaMock = equipamentoService.getBicicleta(bicicletaId);
        return ResponseEntity.ok(bicicletaMock);
    } else {
        return ResponseEntity.ok().build();
    }
}

/**
 * Caso de Uso: Suporte ao UC01 (Verifica R3 - "email deve ser unico")
 * [GET /ciclista/existeEmail/{email}]
 */
@GetMapping("/ciclista/existeEmail/{email}")
public ResponseEntity<Boolean> existeEmail(@PathVariable String email) {
    boolean emailEmUso = ciclistaRepository.existsByEmail(email);
    return ResponseEntity.ok(emailEmUso);
}

/**
 * Caso de Uso: Suporte ao UC07 (Consulta cartão)
 * [GET /cartaoDeCredito/{idCiclista}]
 */
@GetMapping("/cartaoDeCredito/{idCiclista}")
public ResponseEntity<CartaoDeCredito> getCartaoDeCredito(@PathVariable Integer idCiclista) {
    Optional<CartaoDeCredito> cartaoOptional = cartaoRepository.findByIdCiclista(idCiclista);
    if (cartaoOptional.isPresent()) {
        return ResponseEntity.ok(cartaoOptional.get());
    } else {
        return ResponseEntity.notFound().build();
    }
}

/**
 * Caso de Uso: UC07 - Alterar Cartão
 * [PUT /cartaoDeCredito/{idCiclista}]
 */
@PutMapping("/cartaoDeCredito/{idCiclista}")
public ResponseEntity<Void> alterarCartaoDeCredito(
        @PathVariable Integer idCiclista,
        @Valid @RequestBody NovoCartaoDeCreditoDTO dto) {
    try {
        ciclistaService.alterarCartaoDeCredito(idCiclista, dto);
        return ResponseEntity.ok().build();
    } catch (IllegalArgumentException e) {
        if (e.getMessage().contains("não encontrado")) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();
    }
}
}