package com.gservices.exception;

/**
 * Violation d'une règle de gestion (donnée invalide, doublon, transition
 * interdite…). Portée par un message destiné à l'utilisateur (souvent une clé
 * i18n déjà résolue).
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
