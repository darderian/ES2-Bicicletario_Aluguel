package bicicletario.aluguel.controller;

import bicicletario.aluguel.dto.CadastroCiclistaDTO;
import bicicletario.aluguel.dto.NovoCartaoDeCreditoDTO;
import bicicletario.aluguel.dto.NovoCiclistaDTO;
import bicicletario.aluguel.dto.PassaporteDTO;
import bicicletario.aluguel.model.CartaoDeCredito;
import bicicletario.aluguel.model.Ciclista;
import bicicletario.aluguel.model.Passaporte;
import bicicletario.aluguel.repository.CartaoDeCreditoRepository;
import bicicletario.aluguel.repository.CiclistaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.Optional;

@RestController
public class CiclistaController {

@Autowired
private CiclistaRepository ciclistaRepository;

@Autowired
private CartaoDeCreditoRepository cartaoRepository;

// Endpoint POST /ciclista (COMPLETO)
@PostMapping("/ciclista")
public ResponseEntity<Ciclista> cadastrarCiclista(@Valid @RequestBody CadastroCiclistaDTO cadastroDTO) {

    // 1. Converter DTOs para Entidades
    Ciclista ciclista = converterCiclistaDtoParaEntidade(cadastroDTO.getCiclista());

    // 2. Salvar o Ciclista PRIMEIRO para obter o ID
    Ciclista ciclistaSalvo = ciclistaRepository.save(ciclista);

    // 3. Converter e associar o Cartão de Crédito
    CartaoDeCredito cartao = converterCartaoDtoParaEntidade(
            cadastroDTO.getMeioDePagamento(),
            ciclistaSalvo.getId() // Associa o cartão ao ID do ciclista
    );
    cartaoRepository.save(cartao);

    // 4. Retornar 201 Created com o Ciclista salvo
    return ResponseEntity.status(HttpStatus.CREATED).body(ciclistaSalvo);
}

// Implementa: GET /ciclista/{idCiclista}
@GetMapping("/ciclista/{idCiclista}")
public ResponseEntity<Ciclista> recuperarCiclista(@PathVariable Integer idCiclista) {

    // 1. Usa o repositório para buscar o ciclista pelo ID
    //    O .findById() retorna um "Optional", que é um container
    //    que pode ou não ter um ciclista dentro.
    Optional<Ciclista> ciclistaOptional = ciclistaRepository.findById(idCiclista);

    // 2. Verifica se o ciclista foi encontrado
    if (ciclistaOptional.isPresent()) {
        // 3. Se sim, retorna 200 OK com o ciclista no corpo
        return ResponseEntity.ok(ciclistaOptional.get());
    } else {
        // 4. Se não, retorna 404 Not Found (Não Encontrado)
        return ResponseEntity.notFound().build();
    }
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
    ciclista.setSenha(dto.getSenha()); // Lembrete: Hashear a senha em produção!
    ciclista.setStatus("AGUARDANDO_CONFIRMACAO"); // Status inicial

    // Converte o DTO do passaporte em Entidade Embutida
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
    cartao.setIdCiclista(ciclistaId); // Chave estrangeira
    cartao.setNomeTitular(dto.getNomeTitular());
    cartao.setNumero(dto.getNumero()); // Lembrete: Não salve o número todo!
    cartao.setValidade(dto.getValidade());
    cartao.setCvv(dto.getCvv()); // Lembrete: Não salve o CVV!

    return cartao;
}
}