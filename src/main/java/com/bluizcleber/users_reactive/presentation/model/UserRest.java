package com.bluizcleber.users_reactive.presentation.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.UUID;

public class UserRest {

    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    @JsonInclude(JsonInclude.Include.NON_NULL) //esta anotação excluirá o campo de álbuns da saida json se o valor for nulo
    private List<AlbumRest> albums;

    public UserRest() {
    }

    public UserRest(UUID id, String firstName, String lastName, String email, List<AlbumRest> albums) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.albums = albums;
    }

    public List<AlbumRest> getAlbums() {
        return albums;
    }

    public void setAlbums(List<AlbumRest> albums) {
        this.albums = albums;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
