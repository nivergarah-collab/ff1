package com.example.ff1.motor.fuentes;

/** Azar de prueba: devuelve los valores dados en orden y vuelve a empezar. */
public final class AzarSecuencia implements Azar {

    private final int[] valores;
    private int siguiente;

    public AzarSecuencia(int... valores) {
        if (valores.length == 0) {
            throw new IllegalArgumentException("se necesita al menos un valor");
        }
        this.valores = valores.clone();
    }

    @Override
    public int entero(int limite) {
        int v = valores[siguiente];
        siguiente = (siguiente + 1) % valores.length;
        if (v < 0 || v >= limite) {
            throw new IllegalStateException("valor " + v + " fuera de [0, " + limite + ")");
        }
        return v;
    }
}
