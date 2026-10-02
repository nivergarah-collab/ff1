package com.example.ff1.combate;

import java.util.Arrays;
import java.util.Collections;

import com.example.ff1.motor.config.EsquemaConfiguracion;
import com.example.ff1.motor.config.ProveedorConfiguracion;
import com.example.ff1.motor.config.Registro;

/** Ayudas compartidas por las pruebas de combate. */
final class Datos {

    private Datos() {
    }

    static Registro<Object> tiposHabilidad() {
        Registro<Object> r = new Registro<>("tipo de habilidad");
        for (String t : Arrays.asList("danio", "curacion", "alteracion")) {
            r.registrar(t, t);
        }
        return r;
    }

    static Registro<Object> estados() {
        Registro<Object> r = new Registro<>("estado");
        for (String e : Arrays.asList("veneno", "sueno", "proteccion")) {
            r.registrar(e, e);
        }
        return r;
    }

    static DefinicionCombatiente definicion(String id, Bando bando, int vida, int magia, int velocidad) {
        return new DefinicionCombatiente(id, id, bando, vida, magia, 5, 5, 5, velocidad,
                Collections.<String>emptyList(), 0, 0);
    }

    static Combatiente combatiente(String id, int velocidad) {
        return new Combatiente(definicion(id, Bando.HEROE, 30, 10, velocidad));
    }

    static ProveedorConfiguracion config() {
        return new ProveedorConfiguracion(
                ConfiguracionCombate.declarar(new EsquemaConfiguracion()).porDefecto());
    }
}
