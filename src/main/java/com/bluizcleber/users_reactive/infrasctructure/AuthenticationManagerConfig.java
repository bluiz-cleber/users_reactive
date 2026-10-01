package com.bluizcleber.users_reactive.infrasctructure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AuthenticationManagerConfig {

    @Bean
    public ReactiveAuthenticationManager reactiveAuthenticationManager(ReactiveUserDetailsService reactiveUserDetailsService,
                                                                       PasswordEncoder passwordEncoder) {
        UserDetailsRepositoryReactiveAuthenticationManager authenticationManager =
                new UserDetailsRepositoryReactiveAuthenticationManager(reactiveUserDetailsService); //cria um novo repositório de detalhes do usuário
        authenticationManager.setPasswordEncoder(passwordEncoder); //fornecendo um codificador de senha que deve ser gerado para criptografar a senha fornecida na solicitação de login. Em seguida, ele comparará com a senha recém criptografada que está armazenada na tabela do banco de dados
        return authenticationManager; //retornar o gerenciador de autenticação configurado a partir deste método
    }
}
