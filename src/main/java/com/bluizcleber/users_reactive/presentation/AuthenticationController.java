package com.bluizcleber.users_reactive.presentation;

import com.bluizcleber.users_reactive.presentation.model.AuthenticationRequest;
import com.bluizcleber.users_reactive.service.AuthenticationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController //marcar para o Spring que nesta classe definirá os pontos de extremidade da API
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    //método que processa a solicitação de login e invoca o método de autenticação
    @PostMapping("/login")
    public Mono<ResponseEntity<Object>> login(@RequestBody Mono<AuthenticationRequest> authenticationRequestMono){
        return authenticationRequestMono
                .flatMap(authenticationRequest ->
                        authenticationService.authenticate(authenticationRequest.getEmail(),
                                authenticationRequest.getPassword()))
                .map(authenticationResultMap -> ResponseEntity.ok()
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + authenticationResultMap.get("token"))
                        .header("UserId", authenticationResultMap.get("userId"))
                        .build());
    }
}
