package com.example.ff1.motor.fuentes;

/** Tiempo de prueba que solo avanza cuando se le pide. */
public final class TiempoManual implements Tiempo {

    private long ms;

    @Override
    public long milisegundos() {
        return ms;
    }

    public void avanzar(long delta) {
        if (delta < 0) {
            throw new IllegalArgumentException("el tiempo no retrocede");
        }
        ms += delta;
    }
}
