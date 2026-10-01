package com.bluizcleber.users_reactive.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

@Service //para que o Spring Framework adicione esta classe ao contexto do aplicativo Spring e a torne disponpivel para injeção de dependência em outros componentes do Spring no meu app
public class JwtServiceImpl implements JwtService {

    private final Environment environment;

    public JwtServiceImpl(Environment environment) {
        this.environment = environment;
    }

    @Override
    public String generateJwt(String subject) {
        return Jwts
                .builder()
                .subject(subject) //subject é o ID do usuário para qual estou gerando o token (informação que vc inclui no json web token)
                .issuedAt(new Date(System.currentTimeMillis())) //indica a hora que o token é emitido
                .expiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS))) //será expirado em 1 hora
                .signWith(getSigningKey())
                .compact();
    }

    @Override
    public Mono<Boolean> validateJwt(String token) {
        return Mono.just(token)
                .map(jwt -> parseToken(jwt)) //preciso pegar o token e analisá-lo para ver se é válido
                .map(claims -> !claims.getExpiration().before(new Date())) //verificar se o token não expirou
                .onErrorReturn(false);
    }

    @Override
    public String extractTokenSubject(String token) {
        return parseToken(token).getSubject();
    }

    //método que analisará o token para garantir que não foi adulterado de foi de fato emitido pelo nosso app
    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey()) //precisamos da chave secreta que foi inicialmente usada para assinar esse token
                .build()
                .parseSignedClaims(token) //analisar o token e extrair suas reivindicações assinadas
                .getPayload();
    }

    private SecretKey getSigningKey() {
        return Optional.ofNullable(environment.getProperty("token.secret")) //se o valor do token não for fornecido, eu posso lançar uma exceção
                .map(tokenSecret -> tokenSecret.getBytes()) //converter o valor do token.secret do properties em uma matriz de bytes
                .map(tokenSecretBytes -> Keys.hmacShaKeyFor(tokenSecretBytes))
                .orElseThrow(() -> new IllegalArgumentException("token.secret must be configured in application properties file"));
    }
}
