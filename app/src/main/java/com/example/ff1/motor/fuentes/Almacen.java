package com.example.ff1.motor.fuentes;

/**
 * Guardado de partidas como texto por ranura. El formato del texto lo define el motor
 * (ver docs/contrato-de-datos.md); el almacén solo lo conserva.
 */
public interface Almacen {

    void guardar(String ranura, String datos);

    /** Texto guardado o {@code null} si la ranura está vacía. */
    String cargar(String ranura);

    boolean existe(String ranura);

    void borrar(String ranura);
}
