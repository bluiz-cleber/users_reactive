package com.bluizcleber.users_reactive.infrasctructure;

import com.bluizcleber.users_reactive.service.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.config.EnableWebFlux;

import java.util.Arrays;

@Configuration
@EnableWebFlux //marcador que permite importar configuração de segurança do spring em aplicativos de fluxo reativo
@EnableReactiveMethodSecurity //usado para ativar a segurança em nível de método
public class WebSecurity {

    @Bean //utilizado para indicar que este método produzirá um Java bean que será gerenciado pelo conteiner do Spring e ficará disponível em outros componentes do Spring
    SecurityWebFilterChain httpSecurityFilterChain(ServerHttpSecurity http, ReactiveAuthenticationManager authenticationManager,
                                                   JwtService jwtService) {
        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtService);
        return http.authorizeExchange(exchanges -> exchanges
                        .pathMatchers(HttpMethod.POST, "/users").permitAll() //permitir o acesso não autenticado ao endpoint de registro do usuário
                        .pathMatchers(HttpMethod.POST, "/login").permitAll()
                        .pathMatchers(HttpMethod.GET, "/users/stream").permitAll()
                .anyExchange().authenticated()) //esta configuração garantirá que qq outra solicitação, exigirá autenticação
                .cors(corsSpec -> corsSpec.configurationSource(corsConfigurationSource()))
                .csrf(ServerHttpSecurity.CsrfSpec::disable) //Cross-site Request Forgery (Falsificação de solicitação entre sites): vulnerabilidades de segurança que induz o usuário a realizar ações em um site sem o seu consentimento
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable) //desativar a autenticação básica
                .authenticationManager(authenticationManager) //informamos ao Spring qual gerenciador de autenticação utilizar para verificar as credenciais do usuário
                .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION) //executar este filtro qdo for o momento de fazer a autenticação
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance()) //tprnar este aplicativo stateless. não armazenar nenhum dado de sessão
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); //criar uma nova instância do codificador de senha Bcrypt. Uma vez no contexto do Spring, se torna disponível para injeção de dependências em outros componentes
    }

    //método para configurar o compartilhamento de solicitações de origem cruzada (CORS)
    private CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*")); //desta forma, todas as origens estão permitidas
        configuration.setAllowedMethods(Arrays.asList("*")); //configurar quais métodos http são permitidos
        configuration.setAllowedHeaders(Arrays.asList("*")); //configurar quais headers http são permitidos

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(); //esta classe é utilizada para aplicar a configuração de CORS a um padrão de URL específico
        source.registerCorsConfiguration("/**", configuration); //aplica a configyração para todos os caminhos de URL
        return source;
    }
}
