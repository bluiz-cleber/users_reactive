package com.bluizcleber.users_reactive.infrasctructure;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@TestConfiguration //garante ao Spring que esta classe de configuração não seja aplicada ao aplicativo principal
public class TestSecurityConfig {

    @Bean
    public SecurityWebFilterChain testSecurityFilterChain(ServerHttpSecurity http) throws Exception {
        return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(authorizeExchangeSpec -> authorizeExchangeSpec
                        .anyExchange().permitAll())
                .build();
    }
}
