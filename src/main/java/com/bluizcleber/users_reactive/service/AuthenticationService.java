package com.bluizcleber.users_reactive.service;

import reactor.core.publisher.Mono;

import java.util.Map;

public interface AuthenticationService {

    //se a autenticação for bem sucedida, este método retornará um Hashmap com o token JWT e o id do usuário
    Mono<Map<String, String>> authenticate(String username, String password);
}
