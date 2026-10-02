package com.example.ff1.inventario;

import com.example.ff1.motor.config.EsquemaConfiguracion;

/** Parámetros del inventario (ver docs/contrato-de-datos.md). */
public final class ConfiguracionInventario {

    /** Unidades máximas de un mismo objeto. */
    public static final String MAXIMO_POR_OBJETO = "inventario.maximoPorObjeto";

    private ConfiguracionInventario() {
    }

    public static EsquemaConfiguracion declarar(EsquemaConfiguracion esquema) {
        return esquema.entero(MAXIMO_POR_OBJETO, 1, 999, 99);
    }
}
