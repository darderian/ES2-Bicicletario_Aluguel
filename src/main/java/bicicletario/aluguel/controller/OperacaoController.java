package bicicletario.aluguel.controller;

import bicicletario.aluguel.dto.BicicletaDTO;
import bicicletario.aluguel.dto.CobrancaDTO;
import bicicletario.aluguel.dto.DevolucaoDTO;
import bicicletario.aluguel.dto.NovoAluguelDTO;
import bicicletario.aluguel.model.Aluguel;
import bicicletario.aluguel.model.Devolucao;
import bicicletario.aluguel.model.Ciclista;
import bicicletario.aluguel.repository.*;
import bicicletario.aluguel.mock.EquipamentoService;
import bicicletario.aluguel.mock.ExternoService; // --- IMPORT ADICIONADO ---
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.Duration;
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
@Autowired
private EquipamentoService equipamentoService;

// --- INJEÇÃO ADICIONADA ---
@Autowired
private ExternoService externoService;

/**
 * Caso de Uso: UC03 - Alugar bicicleta
 * [POST /aluguel]
 */
@PostMapping("/aluguel")
public ResponseEntity<Aluguel> realizarAluguel(@Valid @RequestBody NovoAluguelDTO dto) {

    // UC03-Pré-condição: Ciclista autenticado (implícito) e ATIVO
    Optional<Ciclista> ciclistaOptional = ciclistaRepository.findById(dto.getCiclista());
    if (!ciclistaOptional.isPresent() || !ciclistaOptional.get().getStatus().equals("ATIVO")) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build(); // 422
    }

    // UC03-R1: "só pode pegar uma bicicleta por vez"
    Optional<Aluguel> aluguelAtivo = aluguelRepository.findByCiclistaAndHoraFimIsNull(dto.getCiclista());
    if (aluguelAtivo.isPresent()) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build(); // 422
    }

    // UC03-Passo 4, 6 e R5: Validar tranca e bicicleta (status "disponível")
    BicicletaDTO bicicleta = equipamentoService.getBicicletaDaTranca(dto.getTrancaInicio());
    if (bicicleta == null || !"DISPONIVEL".equals(bicicleta.getStatus())) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build(); // 422
    }

    // --- MODIFICAÇÃO (Refatoração do Mock) ---
    // UC03-Passo 7 e R2: Cobrança da taxa inicial (R$ 10,00)
    // A lógica de mock foi movida para o ExternoService
    CobrancaDTO cobrancaMock = externoService.realizarCobranca(10.0, dto.getCiclista());

    if (!cobrancaMock.getStatus().equals("PAGA")) {
        // UC03-E3: Pagamento não autorizado
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build(); // 422
    }
    // --- FIM DA MODIFICAÇÃO ---

    // UC03-Passo 9: Registra dados e altera status (implícito)
    Aluguel novoAluguel = new Aluguel();
    novoAluguel.setCiclista(dto.getCiclista());
    novoAluguel.setTrancaInicio(dto.getTrancaInicio());
    novoAluguel.setHoraInicio(LocalDateTime.now());
    novoAluguel.setCobranca(cobrancaMock.getId());
    novoAluguel.setBicicleta(bicicleta.getId());

    Aluguel aluguelSalvo = aluguelRepository.save(novoAluguel);

    // UC03-Passo 10: Abrir tranca e alterar status
    equipamentoService.destrancarTranca(aluguelSalvo.getTrancaInicio());

    // UC03-Passo 11: Enviar Email
    externoService.enviarEmail(
            ciclistaOptional.get().getEmail(),
            "Aluguel Realizado com Sucesso!",
            "Olá, " + ciclistaOptional.get().getNome() + ". Seu aluguel da bicicleta " + aluguelSalvo.getBicicleta() + " foi registrado."
    );

    return ResponseEntity.ok(aluguelSalvo);
}

/**
 * Caso de Uso: UC04 - Devolver bicicleta
 * [POST /devolucao]
 */
@PostMapping("/devolucao")
public ResponseEntity<Devolucao> realizarDevolucao(@Valid @RequestBody DevolucaoDTO dto) {

    // UC04-Passo 1, 2: Achar aluguel ativo pela bicicleta
    Optional<Aluguel> aluguelAtivoOptional = aluguelRepository.findByBicicletaAndHoraFimIsNull(dto.getIdBicicleta());

    if (!aluguelAtivoOptional.isPresent()) {
        return ResponseEntity.notFound().build(); // 404
    }

    Aluguel aluguelParaFechar = aluguelAtivoOptional.get();
    LocalDateTime horaFimDevolucao = LocalDateTime.now();

    // UC04-Passo 3 e R1: Calcular valor extra
    double valorExtra = calcularValorExtra(aluguelParaFechar.getHoraInicio(), horaFimDevolucao);
    Integer idCobrancaExtra = null;
    String msgTaxaExtra = "";

    if (valorExtra > 0.0) {
        // UC04-A1: Enviar cobrança extra
        // A lógica de mock foi movida para o ExternoService
        Integer ciclistaId = aluguelParaFechar.getCiclista();
        CobrancaDTO cobrancaExtraMock = externoService.enviarParaFilaCobranca(valorExtra, ciclistaId);

        idCobrancaExtra = cobrancaExtraMock.getId();
        msgTaxaExtra = " Uma taxa extra de R$ " + valorExtra + " foi cobrada.";
        // UC04-A2 (Falha) é ignorada aqui (RF9)
    }

    // UC04-Passo 4: Registrar dados da devolução
    aluguelParaFechar.setTrancaFim(dto.getIdTranca());
    aluguelParaFechar.setHoraFim(horaFimDevolucao);
    aluguelParaFechar.setCobranca(idCobrancaExtra);

    Aluguel aluguelFechado = aluguelRepository.save(aluguelParaFechar);

    // UC04-Passo 5, 6: Alterar status da bicicleta e trancar
    equipamentoService.trancarTranca(aluguelFechado.getTrancaFim(), aluguelFechado.getBicicleta());
    equipamentoService.alterarStatusBicicleta(aluguelFechado.getBicicleta(), "DISPONIVEL");
    // UC04-A3 (Reparo) não está no Swagger, então é assumido "DISPONIVEL"

    Devolucao devolucaoResposta = converterAluguelParaDevolucao(aluguelFechado);
    devolucaoRepository.save(devolucaoResposta);

    // UC04-Passo 7: Enviar Email
    Optional<Ciclista> ciclistaOptional = ciclistaRepository.findById(aluguelFechado.getCiclista());
    if (ciclistaOptional.isPresent()) {
        externoService.enviarEmail(
                ciclistaOptional.get().getEmail(),
                "Devolução Concluída",
                "Sua bicicleta foi devolvida com sucesso." + msgTaxaExtra
        );
    }

    return ResponseEntity.ok(devolucaoResposta);
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
// --- MÉTODOS HELPER ---
private Devolucao converterAluguelParaDevolucao(Aluguel aluguel) {
    //... (código inalterado)
    Devolucao dev = new Devolucao();
    dev.setId(aluguel.getId());
    dev.setCiclista(aluguel.getCiclista());
    dev.setBicicleta(aluguel.getBicicleta());
    dev.setHoraInicio(aluguel.getHoraInicio());
    dev.setTrancaFim(aluguel.getTrancaFim());
    dev.setHoraFim(aluguel.getHoraFim());
    dev.setCobranca(aluguel.getCobranca());
    return dev;
}

private double calcularValorExtra(LocalDateTime inicio, LocalDateTime fim) {
    //... (código inalterado)
    Duration duracao = Duration.between(inicio, fim);
    long minutosTotais = duracao.toMinutes();

    if (minutosTotais <= 120) {
        return 0.0;
    }

    long minutosExtras = minutosTotais - 120;
    long periodosDe30Min = (long) Math.ceil(minutosExtras / 30.0);

    return periodosDe30Min * 5.0;
}
}