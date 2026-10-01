package com.bluizcleber.users_reactive.presentation;

import com.bluizcleber.users_reactive.presentation.model.CreateUserRequest;
import com.bluizcleber.users_reactive.presentation.model.UserRest;
import com.bluizcleber.users_reactive.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.UUID;

@RestController //informa ao spring que esta classe vai lidar com solicitações de serviços web restfull
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public Mono<ResponseEntity<UserRest>> createUser(@RequestBody @Valid Mono<CreateUserRequest> createUserRequest) { //Mono é utilizado na programação reativa para lidar com um único valor que pode não estar disponível imediatamente
        return userService.createUser(createUserRequest) //o método create retorna um Mono, que emitirá o usuário como objeto. E o retorno deste método é Mono de entidade de resposta
                .map(userRest -> ResponseEntity //utilizando o map para aceitar o usuário como objeto, e converter o objeto do usuário em um objeto de entidade de resposta
                        .status(HttpStatus.CREATED)
                        .location(URI.create("/users/" + userRest.getId()))
                        .body(userRest));
    }

    @GetMapping("/{userId}")
//    @PreAuthorize("authentication.principal.equals(#userId.toString()) or hasRole('ROLE_ADMIN')") //utilizado para realizar verificações de autorização antes que o método seja executado
    @PostAuthorize("returnObject.body!=null and (returnObject.body.id.toString().equals(authentication.principal))") //anotação executada após a execução do método
    public Mono<ResponseEntity<UserRest>> getUser(@PathVariable("userId") UUID userId,
                                                  @RequestParam(name = "include", required = false) String include, //incluir o parâmetro na URI para buscar os álbums
                                                  @RequestHeader(name = "Authorization") String jwt) {
        return userService.getUserById(userId, include, jwt)
                .map(userRest -> ResponseEntity.status(HttpStatus.OK).body(userRest))
                .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).build()));//se o usuário não for encontrado, vamos devolver um Mono diferente como resposta: retornar um HTTP diferente
    }

    //o método vai usar os dois parâmetros de string abaixo para solicitar a lista de usuários em grupos de paginação limitados
    @GetMapping
    public Flux<UserRest> getUsers(@RequestParam(value = "page", defaultValue="0") int page, //page é o primeiro parâmetro da paginação: vai ler o parâmetro da string de consulta e atribuirá seu valor a um meta-argumento tbm chamado de page
                                   @RequestParam(value = "limit", defaultValue="50" ) int limit) { //limit é o parâmetro limite da paginação

        return userService.findAllBy(page, limit);
    }

    //método de configuração para enviar serviços e eventos
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<UserRest> streamUsers(){
        return userService.streamUser(); //para fazer com que este método emitirá os detalhes do usuário

    }
}
