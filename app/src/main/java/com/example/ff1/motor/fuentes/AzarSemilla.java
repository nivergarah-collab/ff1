package com.example.ff1.motor.fuentes;

import java.util.Random;

/** Azar reproducible a partir de una semilla (la semilla se puede guardar con la partida). */
public final class AzarSemilla implements Azar {

    private final Random random;

    public AzarSemilla(long semilla) {
        this.random = new Random(semilla);
    }

    @Override
    public int entero(int limite) {
        return random.nextInt(limite);
    }
}
