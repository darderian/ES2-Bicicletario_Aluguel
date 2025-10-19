package bicicletario.aluguel;

import bicicletario.aluguel.controller.AluguelController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest // Carrega a aplicação Spring para o teste
class AluguelControllerTest {

@Autowired
private AluguelController controller;

// Este é o teste automatizado
// Ele verifica se o Spring consegue carregar o Controller sem erros.
@Test
void contextLoads() {
    assertNotNull(controller);
}
}