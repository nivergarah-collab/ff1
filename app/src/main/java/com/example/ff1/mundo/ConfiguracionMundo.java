package com.example.ff1.mundo;

import com.example.ff1.motor.config.EsquemaConfiguracion;

/** Parámetros de la exploración (ver docs/contrato-de-datos.md). */
public final class ConfiguracionMundo {

    /** Pasos mínimos sobre casillas con zona entre dos encuentros. */
    public static final String PASOS_MINIMOS = "mundo.pasosMinimos";
    /** Pasos máximos sobre casillas con zona entre dos encuentros. */
    public static final String PASOS_MAXIMOS = "mundo.pasosMaximos";

    private ConfiguracionMundo() {
    }

    public static EsquemaConfiguracion declarar(EsquemaConfiguracion esquema) {
        return esquema
                .entero(PASOS_MINIMOS, 1, 999, 15)
                .entero(PASOS_MAXIMOS, 1, 999, 30);
    }
}
