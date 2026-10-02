package com.example.ff1.inventario;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Objetos del grupo con su cantidad, en orden de llegada. Cada objeto se acumula hasta
 * {@code maximo} unidades; lo que no cabe no se guarda.
 */
public final class Inventario {

    private final int maximo;
    private final Map<String, Integer> cantidades = new LinkedHashMap<>();

    public Inventario(int maximoPorObjeto) {
        if (maximoPorObjeto < 1) {
            throw new IllegalArgumentException("máximo inválido: " + maximoPorObjeto);
        }
        this.maximo = maximoPorObjeto;
    }

    /** Agrega unidades; devuelve cuántas cupieron. */
    public int agregar(String id, int cantidad) {
        if (cantidad < 0) {
            throw new IllegalArgumentException("cantidad negativa: " + cantidad);
        }
        int actual = cantidad(id);
        int caben = Math.min(cantidad, maximo - actual);
        if (caben > 0) {
            cantidades.put(id, actual + caben);
        }
        return caben;
    }

    /** Cuántas unidades más de {@code id} caben. */
    public int espacio(String id) {
        return maximo - cantidad(id);
    }

    /** Quita unidades si las hay todas; devuelve {@code false} sin cambiar nada si no. */
    public boolean quitar(String id, int cantidad) {
        if (cantidad < 1) {
            throw new IllegalArgumentException("cantidad inválida: " + cantidad);
        }
        int actual = cantidad(id);
        if (actual < cantidad) {
            return false;
        }
        if (actual == cantidad) {
            cantidades.remove(id);
        } else {
            cantidades.put(id, actual - cantidad);
        }
        return true;
    }

    public int cantidad(String id) {
        Integer c = cantidades.get(id);
        return c == null ? 0 : c;
    }

    /** Objeto → cantidad (solo lectura). */
    public Map<String, Integer> contenido() {
        return Collections.unmodifiableMap(cantidades);
    }
}
