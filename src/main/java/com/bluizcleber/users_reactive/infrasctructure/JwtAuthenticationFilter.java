package com.bluizcleber.users_reactive.infrasctructure;

import com.bluizcleber.users_reactive.service.JwtService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collections;

public class JwtAuthenticationFilter implements WebFilter { //implementar esta interface para interceptarmos as solicitações http de entrada

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    //método que contém a lógica comercial do processo de autenticação. será chamado para cada solicitação que passar pelo nosso filtro
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String token = extractToken(exchange);

        if (token == null) {
            return chain.filter(exchange); //se o token for nulo, eu passo a execução para o próximo filtro da cadeia
        }
        //se o token não for nulo, poderemos validá-lo
        return validateToken(token)
                .flatMap(isValid -> isValid ? authenticateAndContinue(token, exchange, chain)
                        : handleInvalidToken(exchange));
    }

    //método que autentica o usuário com base no subject do token e permite que a solicitação prossiga com a segurança atualizada
    private Mono<Void> authenticateAndContinue(String token, ServerWebExchange exchange, WebFilterChain chain) {
        return Mono.just(jwtService.extractTokenSubject(token))
                .flatMap(subject -> {
                    Authentication auth = new UsernamePasswordAuthenticationToken(subject, null,
                            Collections.emptyList()); //criaremos o objeto de autenticação e representará o usuário autenticado no contexto do Spring
                    return chain
                            .filter(exchange)
                            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth)); //instrução de retorno que continua para o próximo filtro da cadeia
                });
    }

    //se o token for inválido, retornaremos http 401 não autorizado
    private Mono<Void> handleInvalidToken(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete(); //se este método retornar vazio, invoco o setComplete e encerrará o processamento da solicitação
    }

    //método separado para extrair o token do authorization header
    private String extractToken(ServerWebExchange exchange) {
        String authorizarionHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION); //ler o authorization header
        if (StringUtils.hasText(authorizarionHeader) && authorizarionHeader.startsWith("Bearer ")) {
            return authorizarionHeader.substring(7).trim(); //desconsiderando o prefixo inicial do Bearer: 6 caracteres + espaço
        }
        return null;
    }

    //método da lógica de negócios que valida o token
    private Mono<Boolean> validateToken(String token) {
        return jwtService.validateJwt(token);
    }
}
