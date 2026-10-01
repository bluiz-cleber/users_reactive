package com.bluizcleber.users_reactive.data;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.r2dbc.test.autoconfigure.DataR2dbcTest;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.UUID;

//configurar um contexto mínimo para testar o repositório reativo de dados do Spring, apenas com componentes relacionados ao R2-d2
@DataR2dbcTest
//irá alterar o ciclo de vida da instância da classe de teste por padrão
//com esta configuração, o Junit criará apenas uma instância de uma classe de teste e de todos os métodos de teste
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserRepositoryTest {

    //databaseClient é um cliente de banco de dados reativo que é fornecido pelo Spring Framework, e nos permite interagir com o banco de dados relacional de forma não bloqueante
    @Autowired
    private DatabaseClient databaseClient;

    @Autowired
    private UserRepository userRepository;

    @BeforeAll
    void setUp() {
        UserEntity user1 = new UserEntity(
                UUID.randomUUID(),
                "John",
                "Doe",
                "john.doe@example.com",
                "123456789");

        UserEntity user2 = new UserEntity(
                UUID.randomUUID(),
                "Jane",
                "Doe",
                "jane.doe@example.com",
                "123456789"
        );
        String insertSql = "INSERT INTO users (id, first_name, last_name, email, password) " +
                "VALUES (:id, :firstName, :lastName, :email, :password)";

        //criar um fluxo reativo para que a execução permaneça sem bloqueio
        Flux.just(user1, user2)
                .concatMap(user -> databaseClient.sql(insertSql) //concatMap garante que cada um destes usuários seja processado sequencialmente, um de cada vez. O flapMap iria processar os usuários em paralelo
                        .bind("id", user.getId())
                        .bind("firstName", user.getFirstName())
                        .bind("lastName", user.getLastName())
                        .bind("email", user.getEmail())
                        .bind("password", user.getPassword())
                        .fetch() //executará a query no banco de dados
                        .rowsUpdated()) //retornará o número de linhas afetadas
                .then()
                .as(StepVerifier::create) //permite utilizar o verificador de etapas para verificar como o fluxo reativo se comporta
                .verifyComplete(); //ele assina o fluxo reativo e verifica se o fluxo termina normalmente e sem nenhum erro

    }

    //fará com que este método seja executado apenas uma vez, após a execução de todos os métodos de teste
    //e vai limpar a tabela do banco de dados depois que todos os testes foram concluídos
    @AfterAll
    void tearDown() {
        databaseClient.sql("TRUNCATE TABLE users")
                .then()
                .as(StepVerifier::create) //representa um fluxo reativo que é concluído quando a consulta é finalizada
                .verifyComplete();
    }

    @Test
    void testFindByEmail_WithEmailThatExists_ReturnsMatchingUser() {
        //Arrange
        String emailToFind = "john.doe@example.com";

        //Act and Assert
        StepVerifier.create(userRepository.findByEmail(emailToFind))
                .expectNextMatches(user -> user.getEmail().equals(emailToFind))
                .verifyComplete();

    }
}