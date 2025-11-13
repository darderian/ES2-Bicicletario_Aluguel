package bicicletario.aluguel.mock;

import bicicletario.aluguel.dto.CobrancaDTO;
import bicicletario.aluguel.dto.NovoCartaoDeCreditoDTO;
import org.springframework.stereotype.Service;

@Service // Diz ao Spring para gerenciar esta classe
public class ExternoServiceMock implements ExternoService {

@Override
public boolean validarCartaoDeCredito(NovoCartaoDeCreditoDTO cartaoDTO) {
    // MOCK LÓGICA (Gap UC01 e UC07)
    // Para a Entrega 8, vamos simular que TODO cartão é válido.
    // (Em um teste, poderíamos simular um cartão inválido)

    System.out.println("[MOCK EXTERNO] Validando cartão: " + cartaoDTO.getNumero());
    return true; // Simula "APROVADO"
}

@Override
public void enviarEmail(String email, String assunto, String mensagem) {
    // MOCK LÓGICA (Gaps de todos os UCs)
    // Apenas simula o envio.
    System.out.println("=============================================");
    System.out.println("[MOCK EXTERNO] Enviando Email...");
    System.out.println("Para: " + email);
    System.out.println("Assunto: " + assunto);
    // System.out.println("Mensagem: " + mensagem); // (Opcional)
    System.out.println("=============================================");
}
@Override
public CobrancaDTO realizarCobranca(Double valor, Integer ciclistaId) {
    System.out.println("[MOCK EXTERNO] Realizando cobrança imediata de R$ " + valor + " para ciclista " + ciclistaId);

    // Simula a cobrança (UC03)
    CobrancaDTO cobrancaMock = new CobrancaDTO();
    cobrancaMock.setId(100); // ID fixo do mock de aluguel

    // IMPORTANTE: Por padrão, a cobrança é PAGA.
    // Nossos testes poderão sobrepor esse mock para simular uma FALHA.
    cobrancaMock.setStatus("PAGA");

    cobrancaMock.setValor(valor);
    cobrancaMock.setCiclista(ciclistaId);
    cobrancaMock.setHoraSolicitacao(java.time.LocalDateTime.now());
    return cobrancaMock;
}

@Override
public CobrancaDTO enviarParaFilaCobranca(Double valor, Integer ciclistaId) {
    System.out.println("[MOCK EXTERNO] Enviando para fila cobrança de R$ " + valor + " para ciclista " + ciclistaId);

    // Simula a cobrança extra (UC04)
    CobrancaDTO cobrancaMock = new CobrancaDTO();
    cobrancaMock.setId(101); // ID fixo do mock de devolução

    // (UC04-A2) Mesmo que falhe, a devolução continua (RF9).
    cobrancaMock.setStatus("PAGA");

    cobrancaMock.setValor(valor);
    cobrancaMock.setCiclista(ciclistaId);
    cobrancaMock.setHoraSolicitacao(java.time.LocalDateTime.now());
    return cobrancaMock;
}
}