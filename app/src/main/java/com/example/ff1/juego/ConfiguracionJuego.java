package com.example.ff1.juego;

import com.example.ff1.motor.config.EsquemaConfiguracion;

/** Parámetros de la interfaz del juego, ajustables en caliente desde el menú Ajustes. */
public final class ConfiguracionJuego {

    /** Milisegundos que se muestra cada mensaje del combate. */
    public static final String MS_MENSAJE = "juego.msMensaje";
    /** 1 si el avance rápido está encendido al empezar una partida; 0 si no. */
    public static final String RAPIDO_AL_EMPEZAR = "juego.rapidoAlEmpezar";

    private ConfiguracionJuego() {
    }

    public static EsquemaConfiguracion declarar(EsquemaConfiguracion esquema) {
        return esquema
                .entero(MS_MENSAJE, 100, 5000, 900)
                .entero(RAPIDO_AL_EMPEZAR, 0, 1, 0);
    }
}
