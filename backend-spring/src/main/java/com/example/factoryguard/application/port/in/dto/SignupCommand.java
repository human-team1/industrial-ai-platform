package com.example.factoryguard.application.port.in.dto;

public class SignupCommand {
    private final String email;
    private final String password;
    private final String name;

    public SignupCommand(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }
}