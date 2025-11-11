package bicicletario.aluguel.exception;
import bicicletario.aluguel.dto.ErroDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Esta classe captura exceções de validação (@Valid) de todos os controllers
 * e as transforma em uma resposta HTTP 422, como exigido pelo Swagger.
 */
@ControllerAdvice
public class RestExceptionHandler {

@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<List<ErroDTO>> handleValidationExceptions(MethodArgumentNotValidException ex) {

    // Pega todos os erros de validação (ex: "email must be valid", "nome is blank")
    List<ErroDTO> erros = ex.getBindingResult().getAllErrors().stream()
            .map(error -> {
                String campo = ((FieldError) error).getField();
                String mensagem = error.getDefaultMessage();
                return new ErroDTO(campo, mensagem); // Cria nosso DTO de Erro
            })
            .collect(Collectors.toList());

    // Retorna 422 Unprocessable Entity com a lista de erros no corpo
    return new ResponseEntity<>(erros, HttpStatus.UNPROCESSABLE_ENTITY);
}
}