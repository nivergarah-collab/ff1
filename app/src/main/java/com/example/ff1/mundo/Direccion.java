package com.example.ff1.mundo;

/** Las cuatro direcciones de movimiento por casillas. El eje y crece hacia abajo. */
public enum Direccion {
    ARRIBA(0, -1),
    ABAJO(0, 1),
    IZQUIERDA(-1, 0),
    DERECHA(1, 0);

    public final int dx;
    public final int dy;

    Direccion(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }
}
