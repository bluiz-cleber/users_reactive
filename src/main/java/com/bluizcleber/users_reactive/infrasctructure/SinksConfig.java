package com.bluizcleber.users_reactive.infrasctructure;

import com.bluizcleber.users_reactive.presentation.model.UserRest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Sinks;

@Configuration
public class SinksConfig {

    @Bean
    public Sinks.Many<UserRest> usersSink() {
        return Sinks.many().multicast().onBackpressureBuffer();
    }
}
