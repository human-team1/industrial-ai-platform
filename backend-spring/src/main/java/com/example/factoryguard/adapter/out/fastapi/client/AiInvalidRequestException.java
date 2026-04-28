package com.example.factoryguard.adapter.out.fastapi.client;

public class AiInvalidRequestException extends RuntimeException {

    private final int status;

    public AiInvalidRequestException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
