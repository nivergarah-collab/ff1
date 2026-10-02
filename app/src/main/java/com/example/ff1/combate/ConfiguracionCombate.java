package com.example.ff1.combate;

import com.example.ff1.motor.config.EsquemaConfiguracion;

/**
 * Parámetros del combate ajustables en caliente (ver docs/contrato-de-datos.md).
 * El motor los lee de la configuración vigente en cada tick, nunca de constantes.
 */
public final class ConfiguracionCombate {

    /** Multiplica lo que avanza la barra en cada tick (en décimas: 10 = normal). */
    public static final String VELOCIDAD_BARRA = "combate.velocidadBarra";
    /** Carga con la que la barra está llena y el combatiente puede actuar. */
    public static final String CARGA_LLENA = "combate.cargaLlena";

    private ConfiguracionCombate() {
    }

    /** Declara los parámetros del combate en el esquema. */
    public static EsquemaConfiguracion declarar(EsquemaConfiguracion esquema) {
        return esquema
                .entero(VELOCIDAD_BARRA, 1, 100, 10)
                .entero(CARGA_LLENA, 100, 100000, 1000);
    }
}
