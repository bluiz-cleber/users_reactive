package com.bluizcleber.users_reactive.presentation;

import com.bluizcleber.users_reactive.infrasctructure.TestSecurityConfig;
import com.bluizcleber.users_reactive.presentation.model.CreateUserRequest;
import com.bluizcleber.users_reactive.presentation.model.UserRest;
import com.bluizcleber.users_reactive.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@WebFluxTest(UserController.class)
@Import(TestSecurityConfig.class)
class UserControllerTest {

    @MockitoBean //criar um objeto de simulação que implemente a service e a adicione ao contexto do Spring
    private UserService userService;

    @Autowired
    private WebTestClient webTestClient; //adiciona um procedimento de fachada específico de teste para facilitar a verificação de responses e realização de assertions

    @Test
    void testCreateUser_withValidRequest_returnsCreatedStatusAndUserDetails(){
        //Arrange
        CreateUserRequest createUserRequest = new CreateUserRequest(
                "Cleber",
                "Luiz",
                "test7@test.com",
                "123456789"
        );
        UUID userId = UUID.randomUUID();
        String expectedLocation = "/users/" + userId;
        UserRest expectedUserRest = new UserRest(
                userId,
                createUserRequest.getFirstName(),
                createUserRequest.getLastName(),
                createUserRequest.getEmail(),
                null
        );
        //sempre que o userService for usado para invocar o método createUser, retornaremos um objeto de resposta personalizado (userRest com valores)
        when(userService.createUser(Mockito.<Mono<CreateUserRequest>>any())).thenReturn(Mono.just(expectedUserRest)); //criar uma resposta de stub para o método create user

        //Act
        webTestClient
                .post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createUserRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().location(expectedLocation)
                .expectBody(UserRest.class)
                .value(response-> {
                    assertEquals(expectedUserRest.getId(), response.getId());
                    assertEquals(expectedUserRest.getFirstName(), response.getFirstName());
                    assertEquals(expectedUserRest.getLastName(), response.getLastName());
                    assertEquals(expectedUserRest.getEmail(), response.getEmail());
                });

        //Assert
        verify(userService, times(1)).createUser(Mockito.<Mono<CreateUserRequest>>any()); //este combinador de argumentos digo que o método create user aceita qq objeto do create user request que esteja incluído no Mono
    }

}