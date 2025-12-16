package bicicletario.aluguel.controller;

import bicicletario.aluguel.dto.DevolucaoDTO;
import bicicletario.aluguel.dto.NovoAluguelDTO;
import bicicletario.aluguel.model.Aluguel;
import bicicletario.aluguel.model.CartaoDeCredito;
import bicicletario.aluguel.model.Ciclista;
import bicicletario.aluguel.model.Devolucao;
import bicicletario.aluguel.repository.*;
import bicicletario.aluguel.service.AluguelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
public class OperacaoController {

@Autowired
private AluguelService aluguelService;

// Repositórios para o Restaurar Dados
@Autowired private AluguelRepository aluguelRepository;
@Autowired private DevolucaoRepository devolucaoRepository;
@Autowired private FuncionarioRepository funcionarioRepository;
@Autowired private CiclistaRepository ciclistaRepository;
@Autowired private CartaoDeCreditoRepository cartaoDeCreditoRepository;

// Utilitário para resetar os IDs do banco (Essencial para o H2)
@Autowired private JdbcTemplate jdbcTemplate;

/**
 * Caso de Uso: UC03 - Alugar bicicleta
 */
@PostMapping("/aluguel")
public ResponseEntity<Aluguel> realizarAluguel(@Valid @RequestBody NovoAluguelDTO dto) {
    try {
        Aluguel aluguel = aluguelService.realizarAluguel(dto);
        return ResponseEntity.ok(aluguel);
    } catch (IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();
    }
}

/**
 * Caso de Uso: UC04 - Devolver bicicleta
 */
@PostMapping("/devolucao")
public ResponseEntity<Devolucao> realizarDevolucao(@Valid @RequestBody DevolucaoDTO dto) {
    try {
        Devolucao devolucao = aluguelService.realizarDevolucao(dto);
        return ResponseEntity.ok(devolucao);
    } catch (IllegalArgumentException e) {
        if (e.getMessage() != null && e.getMessage().contains("Nenhum aluguel ativo")) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();
    }
}

@GetMapping("/")
public String getRootStatus() {
    return "O Microsserviço de Aluguel está online e operacional.";
}

/**
 * Endpoint MÁGICO para os testes do professor.
 * Limpa tudo e recria os dados iniciais obrigatórios.
 */
@GetMapping("/restaurarDados")
public ResponseEntity<String> restaurarDados() {
    try {
        // 1. Limpar tabelas (Ordem importa para não dar erro de chave estrangeira)
        devolucaoRepository.deleteAll();
        aluguelRepository.deleteAll();
        cartaoDeCreditoRepository.deleteAll();
        ciclistaRepository.deleteAll();
        funcionarioRepository.deleteAll();

        // 2. Resetar os contadores de ID do H2 para começar do 1 novamente
        // Isso garante que o Ciclista 1 seja o ID 1, e não 5, 6, etc.
        jdbcTemplate.execute("ALTER TABLE ciclista ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE cartao_de_credito ALTER COLUMN id RESTART WITH 1");

        // 3. Recriar os dados do PDF
        criarDadosIniciais();

        return ResponseEntity.ok("Dados restaurados com sucesso.");
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.internalServerError().body("Erro ao restaurar banco: " + e.getMessage());
    }
}

private void criarDadosIniciais() {
    // --- CICLISTA 1 ---
    criarCiclistaComCartao("Fulano Beltrano", "CONFIRMADO", "user@example.com", "78804034009");

    // --- CICLISTA 2 ---
    criarCiclistaComCartao("Fulano Beltrano", "AGUARDANDO_CONFIRMACAO", "user2@example.com", "43943488039");

    // --- CICLISTA 3 ---
    criarCiclistaComCartao("Fulano Beltrano", "CONFIRMADO", "user3@example.com", "10243164084");

    // --- CICLISTA 4 ---
    criarCiclistaComCartao("Fulano Beltrano", "CONFIRMADO", "user4@example.com", "30880150017");
}

private void criarCiclistaComCartao(String nome, String status, String email, String cpf) {
    // 1. Criar Ciclista
    Ciclista c = new Ciclista();
    c.setNome(nome);
    c.setStatus(status);
    c.setEmail(email);
    c.setCpf(cpf);
    c.setNascimento("2021-05-02");
    c.setNacionalidade("Brasileiro");
    c.setSenha("ABC123");
    c.setUrlFotoDocumento("http://foto.com");

    // Salva e o banco gera o ID (Vai ser 1, 2, 3, 4 por causa do reset)
    c = ciclistaRepository.save(c);

    // 2. Criar Cartão vinculado a este ciclista
    CartaoDeCredito cartao = new CartaoDeCredito();
    cartao.setIdCiclista(c.getId()); // Pega o ID gerado (ex: 1)
    cartao.setNomeTitular(nome);
    cartao.setNumero("4012001037141112");
    cartao.setValidade("2022-12");
    cartao.setCvv("132");

    cartaoDeCreditoRepository.save(cartao);
}
}