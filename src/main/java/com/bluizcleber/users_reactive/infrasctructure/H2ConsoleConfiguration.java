package com.bluizcleber.users_reactive.infrasctructure;

import org.h2.tools.Server;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;

import java.sql.SQLException;

@Configuration //informa ao spring boot que é uma classe de configuração
@Profile({"!prod & !production"}) //esta configuração só estará ativa para todos os demais ambientes, fora estes
public class H2ConsoleConfiguration {

    private Server webServer;

    @EventListener(ApplicationStartedEvent.class) //informa ao spring para executar este método qdo a aplicação for totalmente iniciada
    public void start() throws SQLException {
        String WEB_PORT = "8082";
        this.webServer = Server.createWebServer("-webPort", WEB_PORT).start();
    }

    //método para interromper o servidor da web qdo o contexto do spring for fechado
    @EventListener(ContextClosedEvent.class)
    public void stop(){
        this.webServer.stop();
    }
}
