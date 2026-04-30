package com.example.factoryguard.application.exception.ai;

public class AiServerException extends RuntimeException {

    private final int status;

    public AiServerException(int status, String message) {
        super(message);
        this.status = status;
    }

    public AiServerException(String message, Throwable cause) {
        super(message, cause);
        this.status = 0;
    }

    public int getStatus() {
        return status;
    }
}
