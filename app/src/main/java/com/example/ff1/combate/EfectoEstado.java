package com.example.ff1.combate;

import com.example.ff1.motor.config.Configuracion;

/**
 * Regla de un estado alterado (la clave {@code estado.id} de los datos). Se registra en un
 * {@code Registro<EfectoEstado>}; cada método tiene un comportamiento neutro por defecto.
 */
public interface EfectoEstado {

    /** Si el combatiente pierde su turno mientras dura. */
    default boolean impideActuar() {
        return false;
    }

    /** Si el estado se quita cuando el combatiente recibe daño. */
    default boolean seQuitaConDanio() {
        return false;
    }

    /** Ajusta el daño que recibe el combatiente (antes de restarlo). */
    default int ajustarDanio(int danio, Configuracion config) {
        return danio;
    }

    /** Daño que sufre al terminar su turno (0 si ninguno). */
    default int danioAlTerminarTurno(Combatiente c, Configuracion config) {
        return 0;
    }
}
