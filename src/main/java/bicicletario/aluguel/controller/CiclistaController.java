package bicicletario.aluguel.controller;

import bicicletario.aluguel.dto.BicicletaDTO;
import bicicletario.aluguel.dto.CadastroCiclistaDTO;
import bicicletario.aluguel.dto.NovoCartaoDeCreditoDTO;
import bicicletario.aluguel.dto.NovoCiclistaDTO;
import bicicletario.aluguel.dto.PassaporteDTO;
import bicicletario.aluguel.model.Aluguel;
import bicicletario.aluguel.model.CartaoDeCredito;
import bicicletario.aluguel.model.Ciclista;
import bicicletario.aluguel.model.Passaporte;
import bicicletario.aluguel.repository.AluguelRepository;
import bicicletario.aluguel.repository.CartaoDeCreditoRepository;
import bicicletario.aluguel.repository.CiclistaRepository;
import bicicletario.aluguel.mock.EquipamentoService;
import bicicletario.aluguel.mock.ExternoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.*;

@RestController
public class CiclistaController {

@Autowired
private CiclistaRepository ciclistaRepository;

@Autowired
private CartaoDeCreditoRepository cartaoRepository;

@Autowired
private AluguelRepository aluguelRepository;

@Autowired
private EquipamentoService equipamentoService;

@Autowired
private ExternoService externoService;

/**
 * Caso de Uso: UC01 - Cadastrar Ciclista
 * [POST /ciclista]
 */
@PostMapping("/ciclista")
public ResponseEntity<Ciclista> cadastrarCiclista(@Valid @RequestBody CadastroCiclistaDTO cadastroDTO) {

    // Gap UC01-Passo 7: Validar Cartão
    boolean cartaoValido = externoService.validarCartaoDeCredito(cadastroDTO.getMeioDePagamento());
    if (!cartaoValido) {
        // UC01-A3: Cartão reprovado
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build(); // 422
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

    return ResponseEntity.status(HttpStatus.CREATED).body(ciclistaSalvo);
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

    Optional<Ciclista> ciclistaOptional = ciclistaRepository.findById(idCiclista);
    if (!ciclistaOptional.isPresent()) {
        return ResponseEntity.notFound().build();
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

    return ResponseEntity.ok(ciclistaAtualizado);
}

/**
 * Caso de Uso: UC02 - Confirmar email (Ativação)
 * [POST /ciclista/{idCiclista}/ativar]
 */
@PostMapping("/ciclista/{idCiclista}/ativar")
public ResponseEntity<Ciclista> ativarCiclista(@PathVariable Integer idCiclista) {
    Optional<Ciclista> ciclistaOptional = ciclistaRepository.findById(idCiclista);
    if (!ciclistaOptional.isPresent()) {
        return ResponseEntity.notFound().build();
    }
    Ciclista ciclistaExistente = ciclistaOptional.get();
    ciclistaExistente.setStatus("ATIVO");
    Ciclista ciclistaAtivado = ciclistaRepository.save(ciclistaExistente);
    return ResponseEntity.ok(ciclistaAtivado);
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

    // Gap UC07-Passo 3: Validar Cartão
    boolean cartaoValido = externoService.validarCartaoDeCredito(dto);
    if (!cartaoValido) {
        // UC07-A2: Cartão reprovado
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build(); // 422
    }

    Optional<CartaoDeCredito> cartaoOptional = cartaoRepository.findByIdCiclista(idCiclista);
    if (!cartaoOptional.isPresent()) {
        return ResponseEntity.notFound().build();
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

    return ResponseEntity.ok().build();
}

// --- Métodos Auxiliares (Helpers) ---
private Ciclista converterCiclistaDtoParaEntidade(NovoCiclistaDTO dto) {
    //... (código inalterado)
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
    //... (código inalterado)
    CartaoDeCredito cartao = new CartaoDeCredito();
    cartao.setIdCiclista(ciclistaId);
    cartao.setNomeTitular(dto.getNomeTitular());
    cartao.setNumero(dto.getNumero());
    cartao.setValidade(dto.getValidade());
    cartao.setCvv(dto.getCvv());
    return cartao;
}

private void atualizarEntidadeComDTO(Ciclista entidade, NovoCiclistaDTO dto) {
    //... (código inalterado)
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