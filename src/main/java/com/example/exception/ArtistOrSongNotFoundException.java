package com.example.exception;

public class ArtistOrSongNotFoundException extends Exception {
    public ArtistOrSongNotFoundException(String message) {
        super(message);
    }
}
