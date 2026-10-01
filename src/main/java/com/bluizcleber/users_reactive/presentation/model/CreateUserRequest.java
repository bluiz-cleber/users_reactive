package com.bluizcleber.users_reactive.presentation.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateUserRequest {

    @NotBlank(message = "First name cannot be empty")
    @Size(min = 2, max = 50, message = "First name cannot be shorter than 2 and longer than 50 characters")
    private String firstName;
    @NotBlank(message = "Last name cannot be empty")
    @Size(min = 2, max = 50, message = "Last name cannot be shorter than 2 and longer than 50 characters")
    private String lastName;
    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Please enter a valid email address")
    private String email;
    @NotBlank(message = "Password cannot be empty")
    @Size(min = 8, max = 16, message = "Password cannot be shorter than 8 and longer than 16 characters")
    private String password;

    public CreateUserRequest() {
    }

    public CreateUserRequest(String password, String email, String lastName, String firstName) {
        this.password = password;
        this.email = email;
        this.lastName = lastName;
        this.firstName = firstName;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
