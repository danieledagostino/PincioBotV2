package com.pincio.telegramwebhook.exception;

public class MaskTokenNotFoundException extends RuntimeException {
    // Costruttori
    public MaskTokenNotFoundException() {
        super("No mask_token ([MASK]) found in the input.");
    }

    public MaskTokenNotFoundException(String message) {
        super(message);
    }

    public MaskTokenNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public MaskTokenNotFoundException(Throwable cause) {
        super(cause);
    }
}

