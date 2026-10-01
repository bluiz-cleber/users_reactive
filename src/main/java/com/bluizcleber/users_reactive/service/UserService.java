package com.bluizcleber.users_reactive.service;

import com.bluizcleber.users_reactive.presentation.model.CreateUserRequest;
import com.bluizcleber.users_reactive.presentation.model.UserRest;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserService extends ReactiveUserDetailsService {

    Mono<UserRest> createUser(Mono<CreateUserRequest> createUserRequestMono); //o método contém os dados originais do usuário recebidos da solicitação e retornará um objeto userRest com os dados do response

    Mono<UserRest> getUserById(UUID id, String include, String jwt); //qdo trabalhamos com um único item no fluxo reativo, usamos o Mono

    Flux<UserRest> findAllBy(int page, int limit);

    Flux<UserRest> streamUser();

}
