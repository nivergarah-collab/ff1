package com.example.ff1.entrada;

import com.example.ff1.motor.fuentes.Tiempo;

/**
 * Bucle de paso fijo: convierte el tiempo real de {@link Tiempo} en pasos lógicos de
 * {@code msPorPaso}. Si el dispositivo se atrasa, recupera como mucho {@code maximoPorCuadro}
 * pasos por cuadro (el resto se descarta, para no "saltar" tras una pausa larga).
 */
public final class Bucle {

    private final Tiempo tiempo;
    private final int msPorPaso;
    private final int maximoPorCuadro;
    private long anterior;
    private long acumulado;

    public Bucle(Tiempo tiempo, int msPorPaso, int maximoPorCuadro) {
        if (msPorPaso <= 0 || maximoPorCuadro <= 0) {
            throw new IllegalArgumentException("valores del bucle deben ser positivos");
        }
        this.tiempo = tiempo;
        this.msPorPaso = msPorPaso;
        this.maximoPorCuadro = maximoPorCuadro;
        this.anterior = tiempo.milisegundos();
    }

    public int msPorPaso() {
        return msPorPaso;
    }

    /** Pasos lógicos que tocan en este cuadro. */
    public int pasos() {
        long ahora = tiempo.milisegundos();
        acumulado += Math.max(0, ahora - anterior);
        anterior = ahora;
        int pasos = (int) Math.min(maximoPorCuadro, acumulado / msPorPaso);
        acumulado = acumulado >= (long) maximoPorCuadro * msPorPaso ? 0 : acumulado - (long) pasos * msPorPaso;
        return pasos;
    }

    /** Tras una pausa (la app vuelve a primer plano), olvidar el tiempo pasado. */
    public void reanudar() {
        anterior = tiempo.milisegundos();
        acumulado = 0;
    }
}
