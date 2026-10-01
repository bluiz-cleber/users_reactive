package com.bluizcleber.users_reactive.infrasctructure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Configuration
public class WebCkientConfig {

    //criando a constante baseUri e dizendo para o spring inicializá-lo com o valor do arquivo properties
    @Value("${base-uri}")
    private String baseUri;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(baseUri)
                //.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeaders(headers -> {
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    headers.setAccept(List.of(MediaType.APPLICATION_JSON));
                })
                .build();
    }
}
