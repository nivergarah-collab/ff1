package com.example.ff1.motor.fuentes;

/** Fuente de azar del motor. Toda tirada pasa por aquí para que las pruebas la controlen. */
public interface Azar {

    /** Entero en [0, limite). {@code limite} debe ser mayor que 0. */
    int entero(int limite);
}
