package com.example.ff1.juego;

import com.example.ff1.combate.DefinicionCombatiente;

/** Compara las estadísticas de un héroe antes y después de un cambio de equipo. */
final class Comparacion {

    static final String[] NOMBRES = {"Vida", "Magia", "Ataque", "Defensa", "Poder", "Velocidad"};

    private Comparacion() {
    }

    /** Valores en el orden de {@link #NOMBRES}. */
    static int[] valores(DefinicionCombatiente d) {
        return new int[] {d.vida, d.magia, d.ataque, d.defensa, d.poder, d.velocidad};
    }

    /** Diferencia {@code despues − antes}, en el orden de {@link #NOMBRES}. */
    static int[] diferencias(DefinicionCombatiente antes, DefinicionCombatiente despues) {
        int[] a = valores(antes);
        int[] b = valores(despues);
        int[] d = new int[a.length];
        for (int i = 0; i < a.length; i++) {
            d[i] = b[i] - a[i];
        }
        return d;
    }

    /** "+3", "-2" o "=" (sin cambio). */
    static String signo(int diferencia) {
        return diferencia > 0 ? "+" + diferencia : diferencia < 0 ? String.valueOf(diferencia) : "=";
    }
}
