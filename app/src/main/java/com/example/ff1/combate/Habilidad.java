package com.example.ff1.combate;

/**
 * Habilidad leída del documento {@code "habilidades"}. Su {@code tipo} dice qué regla la
 * resuelve (registrada en el motor); puede aplicar un estado durante unos turnos.
 */
public final class Habilidad {

    /** A quién se dirige la habilidad. */
    public enum Objetivo {
        ENEMIGO, ALIADO, SI_MISMO
    }

    public final String id;
    public final String nombre;
    public final String tipo;
    public final int coste;
    public final int poder;
    public final Objetivo objetivo;
    /** Estado que aplica, o {@code null}. */
    public final String estado;
    public final int duracionEstado;

    public Habilidad(String id, String nombre, String tipo, int coste, int poder,
            Objetivo objetivo, String estado, int duracionEstado) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.coste = coste;
        this.poder = poder;
        this.objetivo = objetivo;
        this.estado = estado;
        this.duracionEstado = duracionEstado;
    }
}
