package com.example.ff1.entrada;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Entrada del juego en Java puro. La capa Android llama a {@link #presionar} y {@link #soltar} con
 * el botón tocado; el juego recoge pulsaciones con {@link #siguiente}. Mantener una dirección la
 * repite (para caminar sin tocar una y otra vez): primero tras {@code retardo} ms y luego cada
 * {@code intervalo} ms, medidos con {@link #avanzar}.
 */
public final class Entrada {

    private final int retardo;
    private final int intervalo;
    private final Deque<Boton> pendientes = new ArrayDeque<>();
    private Boton mantenido;
    private int espera;

    public Entrada(int retardo, int intervalo) {
        if (retardo <= 0 || intervalo <= 0) {
            throw new IllegalArgumentException("retardo e intervalo deben ser positivos");
        }
        this.retardo = retardo;
        this.intervalo = intervalo;
    }

    /** El dedo pasó a estar sobre {@code b} (o sobre nada, si es {@code null}). */
    public void presionar(Boton b) {
        if (b == mantenido) {
            return;
        }
        mantenido = b;
        if (b != null) {
            pendientes.add(b);
            espera = retardo;
        }
    }

    public void soltar() {
        mantenido = null;
    }

    public Boton mantenido() {
        return mantenido;
    }

    /** Pasa el tiempo; repite la dirección mantenida cuando toca. */
    public void avanzar(int ms) {
        if (mantenido == null || !mantenido.esDireccion()) {
            return;
        }
        espera -= ms;
        while (espera <= 0) {
            pendientes.add(mantenido);
            espera += intervalo;
        }
    }

    /** Próxima pulsación, o {@code null}. */
    public Boton siguiente() {
        return pendientes.poll();
    }
}
