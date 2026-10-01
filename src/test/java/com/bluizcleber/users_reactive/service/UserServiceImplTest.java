package com.bluizcleber.users_reactive.service;

import com.bluizcleber.users_reactive.data.UserEntity;
import com.bluizcleber.users_reactive.data.UserRepository;
import com.bluizcleber.users_reactive.presentation.model.CreateUserRequest;
import com.bluizcleber.users_reactive.presentation.model.UserRest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class) //
class UserServiceImplTest {

    //Mock é uitilizado quando estamos testando uma única classe isoladamente;
    // MockitoBean é específico para o Spring Boot e é utilizado para testes de integração. Adiciona ou substitue um compartimento no contexto do aplicativo Spring por um objeto de simulaçao
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private WebClient webClient;

    private Sinks.Many<UserRest> usersSink;
    private UserServiceImpl userService;


    @BeforeEach
    void setup(){
        usersSink = Sinks.many().multicast().onBackpressureBuffer();
        userService = new UserServiceImpl(userRepository, passwordEncoder, usersSink, webClient);
    }


    @Test // test<Name or System under test>_<Condition or state change>_<Expected result>
    void testCreateUser_withValidRequest_returnsCreatedUserDetails() {
        //Arrange
        CreateUserRequest request = new CreateUserRequest(
                "Cleber",
                "Luiz",
                "test@test.com",
                "123456789"
        );
        UserEntity savedEntity = new UserEntity();
                savedEntity.setId(UUID.randomUUID());
                savedEntity.setFirstName(request.getFirstName());
                savedEntity.setLastName(request.getLastName());
                savedEntity.setEmail(request.getEmail());
                savedEntity.setPassword(request.getPassword());

        Mockito.when(passwordEncoder.encode(Mockito.any())).thenReturn("encodedPassword"); //o codificador de senha é invocado aceitando qq objeto. retorna um valor codificado que eu forneci
        Mockito.when(userRepository.save(Mockito.any(UserEntity.class))).thenReturn(Mono.just(savedEntity)); //qdo o objeto de repositório do usuário é chamado para invocar o método salvar e este método aceita qq objeto de dados da entidade do usuário

        //Act
        Mono<UserRest> result = userService.createUser(Mono.just(request));

        //Assert
       StepVerifier.create(result)
               .expectNextMatches(userRest -> userRest.getId().equals(savedEntity.getId()) &&
                       userRest.getFirstName().equals(request.getFirstName()) &&
                       userRest.getLastName().equals(request.getLastName()) &&
                       userRest.getEmail().equals(request.getEmail()))
               .verifyComplete();
       Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(UserEntity.class)); //verificará se o método save do repository foi chamado uma vez durante a execução do método createUser

//       //pegar o resultado e chamar o método de bloqueio nele, que bloqueará o thread atual até que o Mono emita o volume e até que ele seja concluído
//        //quando o valor estiver disponível, ele retornará esse valor e continuará a execução
//        UserRest userRest = result.block();
//        assertEquals(savedEntity.getId(), userRest.getId());
//        assertEquals(savedEntity.getFirstName(), userRest.getFirstName());
    }
}