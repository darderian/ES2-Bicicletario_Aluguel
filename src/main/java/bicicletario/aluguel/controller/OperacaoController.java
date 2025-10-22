package bicicletario.aluguel.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
// ... (outros imports futuros)

@RestController
public class OperacaoController {

// TODO: Implementar POST /aluguel aqui
@GetMapping("/") // Mapeia para a rota raiz (ex: ...onrender.com/)
public String getRootStatus() {
    return "O Microsserviço de Aluguel está online e operacional.";
}
// TODO: Implementar POST /devolucao aqui

// TODO: Implementar GET /restaurarBanco aqui

}