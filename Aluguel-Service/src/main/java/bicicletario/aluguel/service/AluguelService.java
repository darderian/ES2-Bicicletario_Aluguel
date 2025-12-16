package bicicletario.aluguel.service;

import bicicletario.aluguel.dto.BicicletaDTO;
import bicicletario.aluguel.dto.CobrancaDTO;
import bicicletario.aluguel.dto.DevolucaoDTO;
import bicicletario.aluguel.dto.NovoAluguelDTO;
import bicicletario.aluguel.mock.EquipamentoService;
import bicicletario.aluguel.mock.ExternoService;
import bicicletario.aluguel.model.Aluguel;
import bicicletario.aluguel.model.Ciclista;
import bicicletario.aluguel.model.Devolucao;
import bicicletario.aluguel.repository.AluguelRepository;
import bicicletario.aluguel.repository.CiclistaRepository;
import bicicletario.aluguel.repository.DevolucaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AluguelService {

@Autowired
private AluguelRepository aluguelRepository;
@Autowired
private DevolucaoRepository devolucaoRepository;
@Autowired
private CiclistaRepository ciclistaRepository;
@Autowired
private EquipamentoService equipamentoService;
@Autowired
private ExternoService externoService;

public Aluguel realizarAluguel(NovoAluguelDTO dto) {
    // UC03-Pré-condição: Ciclista autenticado (implícito) e ATIVO
    Optional<Ciclista> ciclistaOptional = ciclistaRepository.findById(dto.getCiclista());
    if (!ciclistaOptional.isPresent() || !"ATIVO".equals(ciclistaOptional.get().getStatus())) {
        throw new IllegalArgumentException("Ciclista não encontrado ou não está ativo");
    }

    // UC03-R1: "só pode pegar uma bicicleta por vez"
    Optional<Aluguel> aluguelAtivo = aluguelRepository.findByCiclistaAndHoraFimIsNull(dto.getCiclista());
    if (aluguelAtivo.isPresent()) {
        throw new IllegalArgumentException("Ciclista já possui um aluguel ativo");
    }

    // UC03-Passo 4, 6 e R5: Validar tranca e bicicleta (status "disponível")
    BicicletaDTO bicicleta = equipamentoService.getBicicletaDaTranca(dto.getTrancaInicio());
    if (bicicleta == null || !"DISPONIVEL".equals(bicicleta.getStatus())) {
        throw new IllegalArgumentException("Bicicleta indisponível ou tranca vazia");
    }

    // UC03-Passo 7 e R2: Cobrança da taxa inicial (R$ 10,00)
    CobrancaDTO cobrancaMock = externoService.realizarCobranca(10.0, dto.getCiclista());
    if (!"PAGA".equals(cobrancaMock.getStatus())) {
        throw new IllegalArgumentException("Pagamento não autorizado");
    }

    // UC03-Passo 9: Registra dados
    Aluguel novoAluguel = new Aluguel();
    novoAluguel.setCiclista(dto.getCiclista());
    novoAluguel.setTrancaInicio(dto.getTrancaInicio());
    novoAluguel.setHoraInicio(LocalDateTime.now());
    novoAluguel.setCobranca(cobrancaMock.getId());
    novoAluguel.setBicicleta(bicicleta.getId());

    Aluguel aluguelSalvo = aluguelRepository.save(novoAluguel);

    // UC03-Passo 10: Abrir tranca
    equipamentoService.destrancarTranca(aluguelSalvo.getTrancaInicio());

    // UC03-Passo 11: Enviar Email
    externoService.enviarEmail(
            ciclistaOptional.get().getEmail(),
            "Aluguel Realizado com Sucesso!",
            "Olá, " + ciclistaOptional.get().getNome() + ". Seu aluguel da bicicleta " + aluguelSalvo.getBicicleta() + " foi registrado."
    );

    return aluguelSalvo;
}

public Devolucao realizarDevolucao(DevolucaoDTO dto) {
    // UC04-Passo 1, 2: Achar aluguel ativo pela bicicleta
    Optional<Aluguel> aluguelAtivoOptional = aluguelRepository.findByBicicletaAndHoraFimIsNull(dto.getIdBicicleta());

    if (!aluguelAtivoOptional.isPresent()) {
        throw new IllegalArgumentException("Nenhum aluguel ativo encontrado para esta bicicleta");
    }

    Aluguel aluguelParaFechar = aluguelAtivoOptional.get();
    LocalDateTime horaFimDevolucao = LocalDateTime.now();

    // UC04-Passo 3 e R1: Calcular valor extra
    double valorExtra = calcularValorExtra(aluguelParaFechar.getHoraInicio(), horaFimDevolucao);
    Integer idCobrancaExtra = null;
    String msgTaxaExtra = "";

    if (valorExtra > 0.0) {
        // UC04-A1: Enviar cobrança extra
        Integer ciclistaId = aluguelParaFechar.getCiclista();
        CobrancaDTO cobrancaExtraMock = externoService.enviarParaFilaCobranca(valorExtra, ciclistaId);
        idCobrancaExtra = cobrancaExtraMock.getId();
        msgTaxaExtra = " Uma taxa extra de R$ " + valorExtra + " foi cobrada.";
    }

    // UC04-Passo 4: Registrar dados da devolução (fecha o aluguel)
    aluguelParaFechar.setTrancaFim(dto.getIdTranca());
    aluguelParaFechar.setHoraFim(horaFimDevolucao);
    aluguelParaFechar.setCobranca(idCobrancaExtra);

    Aluguel aluguelFechado = aluguelRepository.save(aluguelParaFechar);

    // UC04-Passo 5, 6: Alterar status da bicicleta e trancar
    equipamentoService.trancarTranca(aluguelFechado.getTrancaFim(), aluguelFechado.getBicicleta());
    equipamentoService.alterarStatusBicicleta(aluguelFechado.getBicicleta(), "DISPONIVEL");

    // UC04-Passo 6 (Complemento): Criar o registro de Devolução
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

    return devolucaoResposta;
}

private double calcularValorExtra(LocalDateTime inicio, LocalDateTime fim) {
    Duration duracao = Duration.between(inicio, fim);
    long minutosTotais = duracao.toMinutes();

    if (minutosTotais <= 120) {
        return 0.0;
    }
    long minutosExtras = minutosTotais - 120;
    long periodosDe30Min = (long) Math.ceil(minutosExtras / 30.0);
    return periodosDe30Min * 5.0;
}

private Devolucao converterAluguelParaDevolucao(Aluguel aluguel) {
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
}