package com.example.ff1.mundo;

/**
 * Posición del grupo en un mapa y su movimiento casilla a casilla. Un paso solo ocurre si
 * la casilla de destino está dentro del mapa y es pasable; si no, el grupo solo se gira.
 */
public final class Explorador {

    private Mapa mapa;
    private int x;
    private int y;
    private Direccion mirando = Direccion.ABAJO;
    private int pasos;

    public Explorador(Mapa mapa) {
        colocar(mapa, mapa.inicioX, mapa.inicioY);
    }

    /** Pone al grupo en (x, y) de otro mapa (por ejemplo al cruzar una salida o cargar partida). */
    public void colocar(Mapa mapa, int x, int y) {
        if (!mapa.pasable(x, y)) {
            throw new IllegalArgumentException("(" + x + ", " + y + ") no es pasable en " + mapa.id);
        }
        this.mapa = mapa;
        this.x = x;
        this.y = y;
    }

    /** Intenta dar un paso; devuelve true si el grupo se movió. */
    public boolean mover(Direccion d) {
        mirando = d;
        int nx = x + d.dx;
        int ny = y + d.dy;
        if (!mapa.pasable(nx, ny)) {
            return false;
        }
        x = nx;
        y = ny;
        pasos++;
        return true;
    }

    public Mapa mapa() {
        return mapa;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public Direccion mirando() {
        return mirando;
    }

    /** Pasos dados desde que se creó el explorador (para estadísticas y encuentros). */
    public int pasos() {
        return pasos;
    }
}
