package com.gservices.exception;

/** Entité introuvable pour l'identifiant / la clé fournie. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(Class<?> type, Object id) {
        super(type.getSimpleName() + " introuvable (id = " + id + ")");
    }
}
