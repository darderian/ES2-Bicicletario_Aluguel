package bicicletario.externo.service;

import bicicletario.externo.dto.CartaoDTO;
import bicicletario.externo.dto.EmailDTO;
import bicicletario.externo.dto.NovaCobrancaDTO;
import bicicletario.externo.model.Cobranca;
import bicicletario.externo.repository.CobrancaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ExternoService {

    @Autowired
    private CobrancaRepository cobrancaRepository;

    public Cobranca realizarCobranca(NovaCobrancaDTO dto) {
        return criarCobranca(dto, "PAGA");
    }

    public Cobranca colocarNaFila(NovaCobrancaDTO dto) {
        return criarCobranca(dto, "PENDENTE");
    }

    public Optional<Cobranca> obterCobranca(Integer id) {
        return cobrancaRepository.findById(id);
    }

    public void enviarEmail(EmailDTO dto) {
        // Lógica de envio (Mock)
        System.out.println("📧 [EXTERNOa] Enviando Email para: " + dto.getEmail());
        System.out.println("   Assunto: " + dto.getAssunto());
    }

    public boolean validarCartao(CartaoDTO dto) {
        // Regra de validação mockada
        return dto.getNumero() != null && !dto.getNumero().trim().isEmpty();
    }

    // Método auxiliar privado
    private Cobranca criarCobranca(NovaCobrancaDTO dto, String status) {
        Cobranca c = new Cobranca();
        c.setValor(dto.getValor());
        c.setCiclista(dto.getCiclista());
        c.setStatus(status);
        c.setHoraSolicitacao(LocalDateTime.now());
        if ("PAGA".equals(status)) {
            c.setHoraFinalizacao(LocalDateTime.now());
        }
        return cobrancaRepository.save(c);
    }
}