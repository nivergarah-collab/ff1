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
    /** Peso del ataque en el daño físico (en décimas: 10 = ×1). */
    public static final String FUERZA_FISICA = "combate.fuerzaFisica";
    /** Peso de la potencia mágica en daño y curación (en décimas: 10 = ×1). */
    public static final String FUERZA_MAGICA = "combate.fuerzaMagica";
    /** Variación al azar del daño y la curación, en ± por ciento. */
    public static final String VARIANZA = "combate.varianza";
    /** Probabilidad base de huir, en por ciento, con velocidades iguales. */
    public static final String HUIDA_BASE = "combate.huidaBase";
    /** Por ciento de la vida máxima que quita el veneno al terminar cada turno. */
    public static final String VENENO_POR_CIENTO = "combate.venenoPorCiento";
    /** Por ciento del daño que llega a un combatiente con protección. */
    public static final String PROTECCION_POR_CIENTO = "combate.proteccionPorCiento";

    private ConfiguracionCombate() {
    }

    /** Declara los parámetros del combate en el esquema. */
    public static EsquemaConfiguracion declarar(EsquemaConfiguracion esquema) {
        return esquema
                .entero(VELOCIDAD_BARRA, 1, 100, 10)
                .entero(CARGA_LLENA, 100, 100000, 1000)
                .entero(FUERZA_FISICA, 1, 100, 10)
                .entero(FUERZA_MAGICA, 1, 100, 10)
                .entero(VARIANZA, 0, 50, 10)
                .entero(HUIDA_BASE, 0, 100, 50)
                .entero(VENENO_POR_CIENTO, 1, 50, 8)
                .entero(PROTECCION_POR_CIENTO, 0, 100, 50);
    }
}
