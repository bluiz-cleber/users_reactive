package com.bluizcleber.users_reactive.data;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserRepository extends ReactiveCrudRepository<UserEntity, UUID> { //Reactive crud repository é uma interface que vem do módulo de conectividade do banco de dados relacional reativo do Spring Data (Spring R2DBC)

    Flux<UserEntity> findAllBy(Pageable pageable);

    Mono<UserEntity> findByEmail(String email);
}
