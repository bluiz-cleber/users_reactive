package com.bluizcleber.users_reactive.service;

import com.bluizcleber.users_reactive.data.UserEntity;
import com.bluizcleber.users_reactive.data.UserRepository;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthenticationServiceImpl implements AuthenticationService{

    private final ReactiveAuthenticationManager reactiveAuthenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthenticationServiceImpl(ReactiveAuthenticationManager reactiveAuthenticationManager, UserRepository userRepository, JwtService jwtService) {
        this.reactiveAuthenticationManager = reactiveAuthenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Map<String, String>> authenticate(String username, String password) {
        return reactiveAuthenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(username, password)) //passará o username e a senha para a autenticação
                .then(getUserDetails(username)) //usado para encadear operações sequencialmente ignorando o resultado da operação anterior
                .map(this::createAuthResponse);
    }

    //método privado para utilizar o repositório de usuários para encontrar um usuário por nome do usuário
    private Mono<UserEntity> getUserDetails(String username) {
        return userRepository.findByEmail(username);
    }

    //método que trata a resposta final e retornará um Hashmap do token de acesso e o userId
    private Map<String, String> createAuthResponse(UserEntity user){
        Map<String, String> result = new HashMap<>();
        result.put("userId", user.getId().toString()); //usar o objeto de entidade do usuário para obter o ID do usuário
        result.put("token", jwtService.generateJwt(user.getId().toString())); //substituir posteriormente pelo token real
        return result;
    }
}
