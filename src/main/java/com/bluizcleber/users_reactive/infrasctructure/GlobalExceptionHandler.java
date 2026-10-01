package com.bluizcleber.users_reactive.infrasctructure;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@RestControllerAdvice //marca esta classe como uma classe controladora global de tratamento de exceções
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateKeyException.class) //indicar que este método vai tratar exceções de chave duplicada
    public Mono<ErrorResponse> handleDuplicateKeyException(DuplicateKeyException exception){
        //o método just da classe Mono envolverá a resposta em um mono e, o mono resultante emitirá um único item
        return Mono.just(ErrorResponse.builder(exception, HttpStatus.CONFLICT, exception.getMessage()).build());
    }

    @ExceptionHandler(Exception.class) //indicar que este método vai tratar exceções de chave duplicada
    public Mono<ErrorResponse> handleGeneralException(Exception exception){
        return Mono.just(ErrorResponse.builder(exception, HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage()).build());
    }

    @ExceptionHandler(WebExchangeBindException.class) //annotation para capturar excções de validação
    public Mono<ErrorResponse> handleWebExchangeBindException(WebExchangeBindException exception){
        String errorMessage =  exception.getBindingResult() //getBindingResult dá acesso a todos os erros de vinculação e validação que ocorrerem
                .getAllErrors()
                .stream()
                .map(error -> error.getDefaultMessage()) //para cada erro, vamos obter sua mensagem de erro padrão
                .collect(Collectors.joining(", ")); //juntará todas as mensagens de erro em uma única cadeia de caracteres

        return Mono.just(ErrorResponse
                .builder(exception, HttpStatus.BAD_REQUEST, errorMessage)
                .build());
    }

    //método que trata exceções de credencial inválida
    @ExceptionHandler(BadCredentialsException.class)
    public Mono<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex){
        return Mono.just(ErrorResponse.builder(ex, HttpStatus.UNAUTHORIZED, ex.getMessage()).build());
    }

    //método que vai trarar a exceção de autorização negada
    @ExceptionHandler(AuthorizationDeniedException.class)
    public Mono<ErrorResponse> authorizationDeniedException(AuthorizationDeniedException exception){
        return Mono.just(ErrorResponse.builder(exception, HttpStatus.FORBIDDEN, exception.getMessage()).build());
    }
}
