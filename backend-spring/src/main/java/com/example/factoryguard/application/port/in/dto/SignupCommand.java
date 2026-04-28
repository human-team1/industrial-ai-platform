package com.example.factoryguard.application.port.in.dto;

public class SignupCommand {
    private final String email;
    private final String password;
    private final String name;
    private final String googleSub;
    private final String picture;
    private final String company;
    private final String position;
    private final String phone;

    public SignupCommand(String email, String password, String name, String googleSub, 
                         String picture, String company, String position, String phone) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.googleSub = googleSub;
        this.picture = picture;
        this.company = company;
        this.position = position;
        this.phone = phone;
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

    public String getGoogleSub() {
        return googleSub;
    }

    public String getPicture() {
        return picture;
    }

    public String getCompany() {
        return company;
    }

    public String getPosition() {
        return position;
    }

    public String getPhone() {
        return phone;
    }
}