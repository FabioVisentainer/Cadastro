package com.teste.cadastro.configs;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;

import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Trata exceções genéricas do tipo RuntimeException, como por exemplo "Id não encontrado".
     * Retorna status 404 (Not Found) com a mensagem da exceção.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntime(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    /**
     * Trata exceções lançadas por violação de integridade no banco de dados,
     * como tentativa de deletar registro em uso.
     * Retorna status 409 (Conflict) com mensagem personalizada.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message = "Operação não permitida: registro está em uso em outra tabela.";
        return ResponseEntity.status(HttpStatus.CONFLICT).body(message);
    }

    /**
     * Trata erros de validação de dados provenientes da anotação @Valid,
     * retornando lista de mensagens de erro para os campos inválidos.
     * Retorna status 400 (Bad Request).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());
        return ResponseEntity.badRequest().body(errors);
    }

    /**
     * Trata exceções de entidade não encontrada, como EntityNotFoundException.
     * Retorna status 404 (Not Found) com a mensagem da exceção.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    /**
     * Trata requisições HTTP feitas com métodos não suportados pela API.
     * Retorna status 405 (Method Not Allowed) com mensagem informativa.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<String> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body("Método HTTP não suportado.");
    }

    /**
     * Trata violações de restrição, como unicidade, tamanho, etc,
     * geradas por ConstraintViolationException.
     * Retorna status 400 (Bad Request) com a mensagem da exceção.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> handleConstraintViolation(ConstraintViolationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    /**
     * Trata IllegalArgumentException, que pode ser usada para erros de negócio,
     * como tentativa de criar ou atualizar entidade com dados inválidos.
     * Retorna status 400 (Bad Request) com a mensagem da exceção.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        // Retorna uma resposta JSON com a chave "message"
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"message\":\"" + ex.getMessage() + "\"}");
    }

    /**
     * Trata exceções inesperadas não capturadas por outros handlers,
     * retornando status 500 (Internal Server Error) com mensagem genérica.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Ocorreu um erro inesperado: " + ex.getMessage());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Object> handleMissingParams(MissingServletRequestParameterException ex) {
        // Aqui você pode definir uma resposta customizada para erros de parâmetros faltando
        String message = "Parâmetro '" + ex.getParameterName() + "' está ausente.";
        return new ResponseEntity<>(new ErrorResponse(message), HttpStatus.BAD_REQUEST);
    }

    // Classe de resposta de erro
    public static class ErrorResponse {
        private String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }


}