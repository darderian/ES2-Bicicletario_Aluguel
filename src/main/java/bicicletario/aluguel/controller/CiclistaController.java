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
private AluguelRepository aluguelRepository; // Necessário para /permiteAluguel

// Endpoint: POST /ciclista
@PostMapping("/ciclista")
public ResponseEntity<Ciclista> cadastrarCiclista(@Valid @RequestBody CadastroCiclistaDTO cadastroDTO) {
    Ciclista ciclista = converterCiclistaDtoParaEntidade(cadastroDTO.getCiclista());
    Ciclista ciclistaSalvo = ciclistaRepository.save(ciclista);
    CartaoDeCredito cartao = converterCartaoDtoParaEntidade(
            cadastroDTO.getMeioDePagamento(),
            ciclistaSalvo.getId()
    );
    cartaoRepository.save(cartao);
    return ResponseEntity.status(HttpStatus.CREATED).body(ciclistaSalvo);
}

// Endpoint: GET /ciclista/{idCiclista}
@GetMapping("/ciclista/{idCiclista}")
public ResponseEntity<Ciclista> recuperarCiclista(@PathVariable Integer idCiclista) {
    Optional<Ciclista> ciclistaOptional = ciclistaRepository.findById(idCiclista);
    if (ciclistaOptional.isPresent()) {
        return ResponseEntity.ok(ciclistaOptional.get());
    } else {
        return ResponseEntity.notFound().build();
    }
}

// Endpoint: PUT /ciclista/{idCiclista}
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
    return ResponseEntity.ok(ciclistaAtualizado);
}

// Endpoint: POST /ciclista/{idCiclista}/ativar
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

// Endpoint: GET /ciclista/{idCiclista}/permiteAluguel
@GetMapping("/ciclista/{idCiclista}/permiteAluguel")
public ResponseEntity<Boolean> permiteAluguel(@PathVariable Integer idCiclista) {
    if (!ciclistaRepository.existsById(idCiclista)) {
        return ResponseEntity.notFound().build();
    }
    Optional<Aluguel> aluguelAtivo = aluguelRepository.findByCiclistaAndHoraFimIsNull(idCiclista);
    boolean podeAlugar = !aluguelAtivo.isPresent();
    return ResponseEntity.ok(podeAlugar);
}

// Endpoint: GET /ciclista/{idCiclista}/bicicletaAlugada
@GetMapping("/ciclista/{idCiclista}/bicicletaAlugada")
public ResponseEntity<?> getBicicletaAlugada(@PathVariable Integer idCiclista) {
    if (!ciclistaRepository.existsById(idCiclista)) {
        return ResponseEntity.notFound().build();
    }
    Optional<Aluguel> aluguelAtivo = aluguelRepository.findByCiclistaAndHoraFimIsNull(idCiclista);
    if (aluguelAtivo.isPresent()) {
        Integer bicicletaId = aluguelAtivo.get().getBicicleta();
        // MOCK com DTO (Tarefa 8)
        BicicletaDTO bicicletaMock = new BicicletaDTO();
        bicicletaMock.setId(bicicletaId);
        bicicletaMock.setMarca("Marca Mockada (DTO)");
        bicicletaMock.setModelo("Modelo Falso (DTO)");
        bicicletaMock.setAno("2024");
        bicicletaMock.setStatus("EM_USO");
        return ResponseEntity.ok(bicicletaMock);
    } else {
        return ResponseEntity.ok().build();
    }
}

// Endpoint: GET /ciclista/existeEmail/{email}
@GetMapping("/ciclista/existeEmail/{email}")
public ResponseEntity<Boolean> existeEmail(@PathVariable String email) {
    boolean emailEmUso = ciclistaRepository.existsByEmail(email);
    return ResponseEntity.ok(emailEmUso);
}

// Endpoint: GET /cartaoDeCredito/{idCiclista} (NOVO)
@GetMapping("/cartaoDeCredito/{idCiclista}")
public ResponseEntity<CartaoDeCredito> getCartaoDeCredito(@PathVariable Integer idCiclista) {

    // 1. Usa o novo método do repositório
    Optional<CartaoDeCredito> cartaoOptional = cartaoRepository.findByIdCiclista(idCiclista);

    if (cartaoOptional.isPresent()) {
        // 2. Retorna 200 OK com o cartão
        return ResponseEntity.ok(cartaoOptional.get());
    } else {
        // 3. Retorna 404 Not Found (Ciclista não tem cartão ou ciclista não existe)
        return ResponseEntity.notFound().build();
    }
}

// Endpoint: PUT /cartaoDeCredito/{idCiclista} (NOVO)
@PutMapping("/cartaoDeCredito/{idCiclista}")
public ResponseEntity<Void> alterarCartaoDeCredito(
        @PathVariable Integer idCiclista,
        @Valid @RequestBody NovoCartaoDeCreditoDTO dto) {

    // 1. Busca o cartão existente pelo ID DO CICLISTA
    Optional<CartaoDeCredito> cartaoOptional = cartaoRepository.findByIdCiclista(idCiclista);

    if (!cartaoOptional.isPresent()) {
        // Se não existe, retorna 404 Not Found
        return ResponseEntity.notFound().build();
    }

    // 2. Pega o cartão existente
    CartaoDeCredito cartaoExistente = cartaoOptional.get();

    // 3. Atualiza os dados
    cartaoExistente.setNomeTitular(dto.getNomeTitular());
    cartaoExistente.setNumero(dto.getNumero());
    cartaoExistente.setValidade(dto.getValidade());
    cartaoExistente.setCvv(dto.getCvv());

    // 4. Salva no banco
    cartaoRepository.save(cartaoExistente);

    // 5. Retorna 200 OK (sem corpo, como no Swagger)
    return ResponseEntity.ok().build();
}


// --- Métodos Auxiliares (Helpers) ---

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