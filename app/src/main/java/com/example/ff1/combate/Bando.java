package com.example.ff1.combate;

import com.example.ff1.motor.datos.ErrorDeDatos;

/** Lado del combate al que pertenece un combatiente. */
public enum Bando {
    HEROE("heroe"),
    ENEMIGO("enemigo");

    private final String clave;

    Bando(String clave) {
        this.clave = clave;
    }

    public String clave() {
        return clave;
    }

    public Bando contrario() {
        return this == HEROE ? ENEMIGO : HEROE;
    }

    /** Convierte el texto de los datos; {@code ruta} se usa en el mensaje de error. */
    public static Bando desde(String texto, String ruta) {
        for (Bando b : values()) {
            if (b.clave.equals(texto)) {
                return b;
            }
        }
        throw new ErrorDeDatos(ruta + ": bando \"" + texto + "\" desconocido; opciones: [heroe, enemigo]");
    }
}
