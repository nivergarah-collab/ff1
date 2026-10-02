package com.example.ff1.motor.config;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Valores validados de los parámetros del motor. Es inmutable: para cambiarla se crea
 * otra con {@link #con} y se entrega a {@link ProveedorConfiguracion#reemplazar}.
 */
public final class Configuracion {

    private final EsquemaConfiguracion esquema;
    private final Map<String, Double> valores;

    Configuracion(EsquemaConfiguracion esquema, Map<String, Double> valores) {
        this.esquema = esquema;
        this.valores = valores;
    }

    public EsquemaConfiguracion esquema() {
        return esquema;
    }

    public int entero(String nombre) {
        EsquemaConfiguracion.Parametro p = esquema.parametro(nombre);
        if (!p.entero) {
            throw new IllegalArgumentException("el parámetro es decimal: " + nombre);
        }
        return (int) Math.round(valores.get(nombre));
    }

    public double decimal(String nombre) {
        esquema.parametro(nombre);
        return valores.get(nombre);
    }

    /** Copia con un valor cambiado; lanza ErrorDeDatos si no es válido. */
    public Configuracion con(String nombre, Number valor) {
        double v = esquema.parametro(nombre).validar(valor);
        Map<String, Double> copia = new LinkedHashMap<>(valores);
        copia.put(nombre, v);
        return new Configuracion(esquema, copia);
    }
}
