package bicicletario.aluguel.service;

import bicicletario.aluguel.dto.CadastroCiclistaDTO;
import bicicletario.aluguel.dto.NovoCartaoDeCreditoDTO;
import bicicletario.aluguel.dto.NovoCiclistaDTO;
import bicicletario.aluguel.dto.PassaporteDTO;
import bicicletario.aluguel.mock.ExternoService;
import bicicletario.aluguel.model.CartaoDeCredito;
import bicicletario.aluguel.model.Ciclista;
import bicicletario.aluguel.model.Passaporte;
import bicicletario.aluguel.repository.CartaoDeCreditoRepository;
import bicicletario.aluguel.repository.CiclistaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CiclistaService {

@Autowired
private CiclistaRepository ciclistaRepository;
@Autowired
private CartaoDeCreditoRepository cartaoRepository;
@Autowired
private ExternoService externoService;

public Ciclista cadastrarCiclista(CadastroCiclistaDTO cadastroDTO) {
    // Gap UC01-Passo 7: Validar Cartão
    boolean cartaoValido = externoService.validarCartaoDeCredito(cadastroDTO.getMeioDePagamento());
    if (!cartaoValido) {
        throw new IllegalArgumentException("Cartão de crédito inválido");
    }

    Ciclista ciclista = converterCiclistaDtoParaEntidade(cadastroDTO.getCiclista());
    Ciclista ciclistaSalvo = ciclistaRepository.save(ciclista);

    CartaoDeCredito cartao = converterCartaoDtoParaEntidade(
            cadastroDTO.getMeioDePagamento(),
            ciclistaSalvo.getId()
    );
    cartaoRepository.save(cartao);

    // Gap UC01-Passo 9: Enviar Email
    externoService.enviarEmail(
            ciclistaSalvo.getEmail(),
            "Bem-vindo ao VáDeBicicleta!",
            "Seu cadastro foi recebido. Por favor, ative sua conta."
    );

    return ciclistaSalvo;
}

public Ciclista editarCiclista(Integer idCiclista, NovoCiclistaDTO dto) {
    Optional<Ciclista> ciclistaOptional = ciclistaRepository.findById(idCiclista);
    if (!ciclistaOptional.isPresent()) {
        throw new IllegalArgumentException("Ciclista não encontrado");
    }
    Ciclista ciclistaExistente = ciclistaOptional.get();
    atualizarEntidadeComDTO(ciclistaExistente, dto);
    Ciclista ciclistaAtualizado = ciclistaRepository.save(ciclistaExistente);

    // Gap UC06-Passo 4: Enviar Email
    externoService.enviarEmail(
            ciclistaAtualizado.getEmail(),
            "Seus dados foram alterados",
            "Olá, " + ciclistaAtualizado.getNome() + ". Seus dados de ciclista foram atualizados com sucesso."
    );
    return ciclistaAtualizado;
}

public Ciclista ativarCiclista(Integer idCiclista) {
    Optional<Ciclista> ciclistaOptional = ciclistaRepository.findById(idCiclista);
    if (!ciclistaOptional.isPresent()) {
        throw new IllegalArgumentException("Ciclista não encontrado");
    }
    Ciclista ciclistaExistente = ciclistaOptional.get();
    ciclistaExistente.setStatus("ATIVO");
    return ciclistaRepository.save(ciclistaExistente);
}

public void alterarCartaoDeCredito(Integer idCiclista, NovoCartaoDeCreditoDTO dto) {
    // Gap UC07-Passo 3: Validar Cartão
    boolean cartaoValido = externoService.validarCartaoDeCredito(dto);
    if (!cartaoValido) {
        throw new IllegalArgumentException("Cartão de crédito inválido");
    }

    Optional<CartaoDeCredito> cartaoOptional = cartaoRepository.findByIdCiclista(idCiclista);
    if (!cartaoOptional.isPresent()) {
        throw new IllegalArgumentException("Cartão não encontrado para este ciclista");
    }
    CartaoDeCredito cartaoExistente = cartaoOptional.get();
    cartaoExistente.setNomeTitular(dto.getNomeTitular());
    cartaoExistente.setNumero(dto.getNumero());
    cartaoExistente.setValidade(dto.getValidade());
    cartaoExistente.setCvv(dto.getCvv());
    cartaoRepository.save(cartaoExistente);

    // Gap UC07-Passo 5: Enviar Email
    Optional<Ciclista> ciclistaOptional = ciclistaRepository.findById(idCiclista);
    if (ciclistaOptional.isPresent()) {
        externoService.enviarEmail(
                ciclistaOptional.get().getEmail(),
                "Seu cartão de crédito foi alterado",
                "Olá. Seu cartão de crédito foi atualizado com sucesso."
        );
    }
}

// --- Métodos Auxiliares (Movidos para o Service) ---
private Ciclista converterCiclistaDtoParaEntidade(NovoCiclistaDTO dto) {
    Ciclista ciclista = new Ciclista();
    ciclista.setNome(dto.getNome());
    ciclista.setNascimento(dto.getNascimento());
    ciclista.setCpf(dto.getCpf());
    ciclista.setNacionalidade(dto.getNacionalidade());
    ciclista.setEmail(dto.getEmail());
    ciclista.setUrlFotoDocumento(dto.getUrlFotoDocumento());
    ciclista.setSenha(dto.getSenha());
    ciclista.setStatus("AGUARDANDO_CONFIRMACAO");

    if (dto.getPassaporte() != null) {
        PassaporteDTO passDto = dto.getPassaporte();
        Passaporte passaporte = new Passaporte();
        passaporte.setPassaporteNumero(passDto.getNumero());
        passaporte.setPassaporteValidade(passDto.getValidade());
        passaporte.setPassaportePais(passDto.getPais());
        ciclista.setPassaporte(passaporte);
    }
    return ciclista;
}

private CartaoDeCredito converterCartaoDtoParaEntidade(NovoCartaoDeCreditoDTO dto, Integer ciclistaId) {
    CartaoDeCredito cartao = new CartaoDeCredito();
    cartao.setIdCiclista(ciclistaId);
    cartao.setNomeTitular(dto.getNomeTitular());
    cartao.setNumero(dto.getNumero());
    cartao.setValidade(dto.getValidade());
    cartao.setCvv(dto.getCvv());
    return cartao;
}

private void atualizarEntidadeComDTO(Ciclista entidade, NovoCiclistaDTO dto) {
    entidade.setNome(dto.getNome());
    entidade.setNascimento(dto.getNascimento());
    entidade.setCpf(dto.getCpf());
    entidade.setNacionalidade(dto.getNacionalidade());
    entidade.setEmail(dto.getEmail());
    entidade.setUrlFotoDocumento(dto.getUrlFotoDocumento());

    if (dto.getPassaporte() != null) {
        PassaporteDTO passDto = dto.getPassaporte();
        Passaporte passaporte = new Passaporte();
        passaporte.setPassaporteNumero(passDto.getNumero());
        passaporte.setPassaporteValidade(passDto.getValidade());
        passaporte.setPassaportePais(passDto.getPais());
        entidade.setPassaporte(passaporte);
    } else {
        entidade.setPassaporte(null);
    }
}
}