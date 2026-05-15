package com.cursoonline.exception.evaluacion;
public class MaxIntentosAlcanzadosException extends RuntimeException {
    public MaxIntentosAlcanzadosException(short max) {
        super("Has alcanzado el número máximo de intentos permitidos (" + max + ").");
    }
}