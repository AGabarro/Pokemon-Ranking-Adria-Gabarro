package com.alea.pokemon.exception;

public class PokeApiException extends RuntimeException {

    public PokeApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
