package bicicletario.aluguel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Esta anotação mágica diz ao Spring:
// 1. Que esta é a classe principal
// 2. Para escanear e encontrar todos os @RestController, etc.,
//    neste pacote e em sub-pacotes.
@SpringBootApplication
public class AluguelApplication {

public static void main(String[] args) {
    SpringApplication.run(AluguelApplication.class, args);
}
}