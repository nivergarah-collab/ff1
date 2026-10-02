package com.example.ff1.inventario;

import com.example.ff1.motor.datos.ErrorDeDatos;
import com.example.ff1.motor.datos.Nodo;

/** Suma a las estadísticas que da una pieza de equipo. */
public final class Bonos {

    public static final Bonos NINGUNO = new Bonos(0, 0, 0, 0, 0, 0);

    public final int vida;
    public final int magia;
    public final int ataque;
    public final int defensa;
    public final int poder;
    public final int velocidad;

    public Bonos(int vida, int magia, int ataque, int defensa, int poder, int velocidad) {
        this.vida = vida;
        this.magia = magia;
        this.ataque = ataque;
        this.defensa = defensa;
        this.poder = poder;
        this.velocidad = velocidad;
    }

    public Bonos mas(Bonos o) {
        return new Bonos(vida + o.vida, magia + o.magia, ataque + o.ataque, defensa + o.defensa,
                poder + o.poder, velocidad + o.velocidad);
    }

    /** Lee un objeto de bonos; cada campo es un entero en [0, 999], defecto 0. */
    static Bonos desde(Nodo n) {
        return new Bonos(campo(n, "vida"), campo(n, "magia"), campo(n, "ataque"),
                campo(n, "defensa"), campo(n, "poder"), campo(n, "velocidad"));
    }

    private static int campo(Nodo n, String clave) {
        int v = n.enteroO(clave, 0);
        if (v < 0 || v > 999) {
            throw new ErrorDeDatos(n.ruta() + "." + clave + ": " + v + " fuera del rango [0, 999]");
        }
        return v;
    }
}
