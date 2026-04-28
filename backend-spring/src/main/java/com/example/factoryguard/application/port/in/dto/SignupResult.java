package com.example.factoryguard.application.port.in.dto;

public class SignupResult {
    private final Long id; 
    private final String email;
    private final String name;
    private final String picture;
    private final String status; 

    public SignupResult(Long id, String email, String name, String picture, String status) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.picture = picture;
        this.status = status;
    }

    public Long getId() { return id; } 
    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getPicture() { return picture; }
    public String getStatus() { return status; } 
}