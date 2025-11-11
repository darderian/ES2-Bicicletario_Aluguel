package bicicletario.aluguel.controller;

import bicicletario.aluguel.dto.CobrancaDTO;
import bicicletario.aluguel.dto.DevolucaoDTO;
import bicicletario.aluguel.dto.NovoAluguelDTO;
import bicicletario.aluguel.model.Aluguel;
import bicicletario.aluguel.model.Devolucao;
import bicicletario.aluguel.model.Ciclista;
import bicicletario.aluguel.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.http.HttpStatus;

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

    // --- (1) VALIDAÇÃO DE NEGÓCIO: CICLISTA ATIVO ---
    Optional<Ciclista> ciclistaOptional = ciclistaRepository.findById(dto.getCiclista());
    if (!ciclistaOptional.isPresent() || !ciclistaOptional.get().getStatus().equals("ATIVO")) {
        return ResponseEntity.unprocessableEntity().build();
    }

    // --- (2) MOCK (Simulação - Tarefa 8) ---
    CobrancaDTO cobrancaMock = new CobrancaDTO();
    cobrancaMock.setId(100);
    cobrancaMock.setStatus("PAGA");
    cobrancaMock.setValor(10.0);
    cobrancaMock.setCiclista(dto.getCiclista());

    if (!cobrancaMock.getStatus().equals("PAGA")) {
        return ResponseEntity.unprocessableEntity().build();
    }

    // --- (3) LÓGICA DE ALUGUEL ---
    Aluguel novoAluguel = new Aluguel();
    novoAluguel.setCiclista(dto.getCiclista());
    novoAluguel.setTrancaInicio(dto.getTrancaInicio());
    novoAluguel.setHoraInicio(LocalDateTime.now());
    novoAluguel.setCobranca(cobrancaMock.getId());
    novoAluguel.setBicicleta(123);

    Aluguel aluguelSalvo = aluguelRepository.save(novoAluguel);
    return ResponseEntity.ok(aluguelSalvo);
}

// Endpoint: POST /devolucao (CORRIGIDO)
@PostMapping("/devolucao")
public ResponseEntity<Devolucao> realizarDevolucao(@Valid @RequestBody DevolucaoDTO dto) {

    // --- (1) ACHAR O ALUGUEL ATIVO PELA BICICLETA ---
    Optional<Aluguel> aluguelAtivoOptional = aluguelRepository.findByBicicletaAndHoraFimIsNull(dto.getIdBicicleta());

    if (!aluguelAtivoOptional.isPresent()) {
        return ResponseEntity.notFound().build();
    }

    Aluguel aluguelParaFechar = aluguelAtivoOptional.get();

    // --- (2) MOCK (Simulação de Cobrança Extra) ---
    CobrancaDTO cobrancaExtraMock = new CobrancaDTO();
    cobrancaExtraMock.setId(101);
    cobrancaExtraMock.setStatus("PAGA");
    cobrancaExtraMock.setValor(5.0);

    // --- (3) ATUALIZAR O ALUGUEL (FECHAR O REGISTRO) ---
    aluguelParaFechar.setTrancaFim(dto.getIdTranca());
    aluguelParaFechar.setHoraFim(LocalDateTime.now());
    aluguelParaFechar.setCobranca(cobrancaExtraMock.getId());

    Aluguel aluguelFechado = aluguelRepository.save(aluguelParaFechar);

    // --- (4) CRIAR A RESPOSTA 'DEVOLUCAO' ---
    Devolucao devolucaoResposta = converterAluguelParaDevolucao(aluguelFechado);
    // O Swagger não exige salvar essa Devolução, mas salvamos por completude.
    devolucaoRepository.save(devolucaoResposta);

    return ResponseEntity.ok(devolucaoResposta);
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

// --- NOVO MÉTODO HELPER ---
private Devolucao converterAluguelParaDevolucao(Aluguel aluguel) {
    Devolucao dev = new Devolucao();
    // A Devolucao usa os dados do Aluguel que acabamos de fechar
    dev.setId(aluguel.getId()); // Usar o mesmo ID do Aluguel
    dev.setCiclista(aluguel.getCiclista());
    dev.setBicicleta(aluguel.getBicicleta());
    dev.setHoraInicio(aluguel.getHoraInicio());
    dev.setTrancaFim(aluguel.getTrancaFim());
    dev.setHoraFim(aluguel.getHoraFim());
    dev.setCobranca(aluguel.getCobranca());
    return dev;
}
}