package bicicletario.externo.controller;

import bicicletario.externo.dto.CartaoDTO;
import bicicletario.externo.dto.EmailDTO;
import bicicletario.externo.dto.NovaCobrancaDTO;
import bicicletario.externo.model.Cobranca;
import bicicletario.externo.repository.CobrancaRepository; // Apenas para o restaurarDados
import bicicletario.externo.service.ExternoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
public class ExternoController {

    @Autowired
    private ExternoService externoService;
    
    @Autowired
    private CobrancaRepository cobrancaRepository; // Acesso direto permitido apenas para ferramentas de teste

    // --- COBRANÇA ---
    @PostMapping("/cobranca")
    public ResponseEntity<Cobranca> realizarCobranca(@RequestBody NovaCobrancaDTO dto) {
        Cobranca nova = externoService.realizarCobranca(dto);
        return ResponseEntity.ok(nova);
    }

    @GetMapping("/cobranca/{id}")
    public ResponseEntity<Cobranca> obterCobranca(@PathVariable Integer id) {
        return externoService.obterCobranca(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // --- FILA ---
    @PostMapping("/filaCobranca")
    public ResponseEntity<Cobranca> colocarNaFila(@RequestBody NovaCobrancaDTO dto) {
        Cobranca nova = externoService.colocarNaFila(dto);
        return ResponseEntity.ok(nova);
    }

    // --- EMAIL ---
    @PostMapping("/enviarEmail")
    public ResponseEntity<Void> enviarEmail(@RequestBody EmailDTO dto) {
        externoService.enviarEmail(dto);
        return ResponseEntity.ok().build();
    }

    // --- CARTÃO ---
    @PostMapping("/validaCartaoDeCredito")
    public ResponseEntity<Void> validarCartao(@RequestBody CartaoDTO dto) {
        if (externoService.validarCartao(dto)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.unprocessableEntity().build();
        }
    }

    // --- RESTAURAR DADOS ---
    // Mantemos a lógica aqui ou num serviço de "Admin", 
    // mas para fins de teste, o acesso direto ao repo aqui é aceitável.
    @GetMapping({"/restaurarDados", "/restaurarBanco"})
    public void restaurarDados() {
        cobrancaRepository.deleteAll();
        salvarCobrancaManual(1, "PENDENTE", 10.0, 3);
        salvarCobrancaManual(2, "FALHA", 25.5, 4);
        System.out.println("♻️ [EXTERNO] Banco restaurado.");
    }
    
    private void salvarCobrancaManual(Integer id, String status, Double valor, Integer ciclista) {
        Cobranca c = new Cobranca();
        c.setId(id);
        c.setStatus(status);
        c.setValor(valor);
        c.setCiclista(ciclista);
        c.setHoraSolicitacao(LocalDateTime.now());
        cobrancaRepository.save(c);
    }
}