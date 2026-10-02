package com.example.ff1.juego;

import java.util.List;

/** Movimiento del cursor de un menú, saltando opciones deshabilitadas y dando la vuelta. */
final class Menu {

    private Menu() {
    }

    /** Siguiente opción habilitada en el sentido {@code paso} (+1 o −1); si no hay otra, se queda. */
    static int mover(int cursor, int paso, List<Boolean> habilitadas) {
        int n = habilitadas.size();
        for (int i = 1; i <= n; i++) {
            int c = Math.floorMod(cursor + paso * i, n);
            if (habilitadas.get(c)) {
                return c;
            }
        }
        return cursor;
    }
}
