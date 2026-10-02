package com.example.ff1.combate;

import java.util.Collections;
import java.util.List;

/**
 * Plantilla de un combatiente (clase de héroe o tipo de enemigo), leída del documento
 * {@code "combatientes"}. Es inmutable; en combate se usa un {@link Combatiente}.
 */
public final class DefinicionCombatiente {

    public final String id;
    public final String nombre;
    public final Bando bando;
    public final int vida;
    public final int magia;
    public final int ataque;
    public final int defensa;
    public final int poder;
    public final int velocidad;
    public final List<String> habilidades;
    public final int experiencia;
    public final int oro;

    public DefinicionCombatiente(String id, String nombre, Bando bando, int vida, int magia,
            int ataque, int defensa, int poder, int velocidad, List<String> habilidades,
            int experiencia, int oro) {
        this.id = id;
        this.nombre = nombre;
        this.bando = bando;
        this.vida = vida;
        this.magia = magia;
        this.ataque = ataque;
        this.defensa = defensa;
        this.poder = poder;
        this.velocidad = velocidad;
        this.habilidades = Collections.unmodifiableList(habilidades);
        this.experiencia = experiencia;
        this.oro = oro;
    }
}
