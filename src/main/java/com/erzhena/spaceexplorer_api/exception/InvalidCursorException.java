package com.erzhena.spaceexplorer_api.exception;

public class InvalidCursorException extends RuntimeException {
    public InvalidCursorException(String cursor) {
        super("Invalid Cursor: " + cursor);
    }
}
