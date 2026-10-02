package com.example.ff1.combate;

import java.util.Collections;

import com.example.ff1.motor.config.EsquemaConfiguracion;
import com.example.ff1.motor.config.ProveedorConfiguracion;
import com.example.ff1.motor.config.Registro;

/** Ayudas compartidas por las pruebas de combate. */
final class Datos {

    private Datos() {
    }

    static Registro<TipoHabilidad> tiposHabilidad() {
        return ReglasCombate.tiposHabilidad();
    }

    static Registro<EfectoEstado> estados() {
        return ReglasCombate.estados();
    }

    /** Definición con ataque, defensa y poder dados. */
    static DefinicionCombatiente definicion(String id, Bando bando, int vida, int magia,
            int ataque, int defensa, int poder, int velocidad) {
        return new DefinicionCombatiente(id, id, bando, vida, magia, ataque, defensa, poder,
                velocidad, Collections.<String>emptyList(), 0, 0);
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
