package bicicletario.aluguel.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/aluguel") // Rota base do seu microsserviço
public class AluguelController {

// Este é o seu endpoint "alô mundo"
@GetMapping("/status")
public String getStatus() {
    return "Microsserviço de Aluguel está online! (Spring Boot)";
}
}