package com.bluizcleber.users_reactive.service;

import com.bluizcleber.users_reactive.data.UserEntity;
import com.bluizcleber.users_reactive.data.UserRepository;
import com.bluizcleber.users_reactive.presentation.model.AlbumRest;
import com.bluizcleber.users_reactive.presentation.model.CreateUserRequest;
import com.bluizcleber.users_reactive.presentation.model.UserRest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    //criar um sink que pode enviar muitos objetos para vários inscritos e armazenar objetos extras em um buffer, se os inscritos forem muito lentos
    private final Sinks.Many<UserRest> usersSink;
    private final WebClient webClient;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, Sinks.Many<UserRest> usersSink, WebClient webClient) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.usersSink = usersSink;
        this.webClient = webClient;
    }

    //criamos um novo usuário e utilizaremos a sincronização para notificar outros inscritos e executar o código qdo o Mono for concluído com sucesso
    @Override
    public Mono<UserRest> createUser(Mono<CreateUserRequest> createUserRequestMono) {
        return createUserRequestMono
                .flatMap(this::convertToEntity) //converte o objeto de solicitação do usuário em objeto de entidade do usuário
                .flatMap(userRepository::save) //o usuário é criado com sucesso e as informações do usuário são convertidas em um objeto rest.
                .mapNotNull(this::convertToRest) //converte o objeto de entidade do usuário em um objeto UserRest
                .doOnSuccess(savedUser -> usersSink.tryEmitNext(savedUser)); //utulizar a sincronização para publicar ou emitir o objeto rest do usuário

    }

    @Override
    public Mono<UserRest> getUserById(UUID id, String include, String jwt) {
        return userRepository
                .findById(id)
                .mapNotNull(this::convertToRest) //tipo de retorno do método: UserRest. aceitar a entidade do usuário e transformmá-la em um objeto UserRest
                //mapNotNull: operador não é nulo, mas se retornar nulo, será emitido e não passará para a próxima etapa do fluxo retornando um Mono vazio
                .flatMap(user -> { //para achatar o resultado de dois Mono(UserRest) em um único Mono, utilizamos o flatMap
                    if (include != null && include.equals("albums")) { //verfica se o objeto include não é nulo e se estiver correto
                        //busca os álbuns no serviço de álbuns, relaciona os usuários para os álbuns e adiciona ao objeto user
                        return includeUserAlbums(user, jwt);
                    }
                    return Mono.just(user);
                });

    }

    @Override
    public Flux<UserRest> findAllBy(int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        return userRepository.findAllBy(pageable)
                .map(this::convertToRest); //o retorno deste método espera UserRest então precisamos converter cada objeto de entidade emitido pelo Flux em objeto UserRest;
    }

    //método para obter um novo fluxo de sincronização
    @Override
    public Flux<UserRest> streamUser() {
        return usersSink.asFlux()
                .publish() //emitirá itens para todos os inscritos ao mesmo tempo
                .autoConnect(1); //diz ao flux para começar a emitir dados assim que houver ao menos 1 inscrito
    }

    //lógica comercial que converte o objeto de solicitação de criação do usuário em objeto de entidade do usuário
    private Mono<UserEntity> convertToEntity(CreateUserRequest createUserRequest){
        return Mono.fromCallable(()-> { //este operador é usado para envolver uma operação potencialmente bloquadora, e que será executada somente quando for sugerida, mantendo a aplicação responsiva e não bloqueante
            UserEntity userEntity = new UserEntity();
            BeanUtils.copyProperties(createUserRequest, userEntity); //BeanUtils é do Spring framework, utilizado para copiar propriedades de um objeto para outro. Útil para converter DTOs em entidades ou transferir dados entre objetos semelhantes
            userEntity.setPassword(passwordEncoder.encode(createUserRequest.getPassword())); //ler o valor original da senha a partir do objeto de request do usuário e em seguida, é atualizado com a senha criptografada
            return userEntity;
        }).subscribeOn(Schedulers.boundedElastic()); //tornará a operação de codificação de senhas não bloqueante e a aplicação mais responsiva

    }

    //método para converter a entidade do usuário em objeto UserRest
    private UserRest convertToRest(UserEntity userEntity){
        UserRest userRest = new UserRest();
        BeanUtils.copyProperties(userEntity, userRest); //copiar a propriedade da entidade do usuário para o objeto de destino, que será o UserRest
        return userRest;
    }

    //o Spring Security chamará este método para autenticar o usuário. Ele pega o username que recebemos no request e o utiliza para encontrar os detalhes do usuário em um banco de dados
    @Override
    public Mono<UserDetails> findByUsername(String username) { //UserDetails é a interface que o Spring fornece para criar objetos de detalhes do usuário
        return userRepository.findByEmail(username)
                .map(userEntity -> User //o map converterá o objeto de entidade do usuário em um objeto de detalhes do usuário exigido pela interface do Spring
                        .withUsername(userEntity.getEmail())
                        .password(userEntity.getPassword())
                        .authorities(new ArrayList<>())
                        .build()); //authorities são funções do usuário e permissões que este usuário tem.
    }

    //utilizar o web client reativo para enviar uma solicitação http ao serviço de albuns
    private Mono<UserRest> includeUserAlbums(UserRest user, String jwt) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .port(8084)
                        .path("/albums") //informar ao webclient para onde enviar esta solicitação get
                        //.queryParam("userId", user.getId()) //fornece o id do usuário que estamos solicitando para os álbuns que pertencem a um determinado usuário
                        .build())
                .header("Authorization", jwt)
                .retrieve() //utilizado para iniciar uma solicitação http e obter o corpo de resposta
                .onStatus(HttpStatusCode::is4xxClientError, response -> { //para qq código de status http no intervalo 400, vamos executar esta função
                    return Mono.error(new RuntimeException("Albums not found for user"));
                })
                .onStatus(HttpStatusCode::is5xxServerError, response -> { //para qq código de status http no intervalo 400, vamos executar esta função
                    return Mono.error(new RuntimeException("Server error while fetching albums"));
                })
                .bodyToFlux(AlbumRest.class) //converter o corpo da resposta em fluxo do objeto de álbuns
                .collectList()
                .map(albums -> { //obter a lista de álbuns do fluxo e adicioná-la ao objeto user
                    user.setAlbums(albums);
                    return user;
                })
                .onErrorResume(e -> { //quando ocorre um erro, permitir que fornecemos um editor de fallback
                    logger.error("Error fetching albums", e);
                    return Mono.just(user);
                });

    }
}
