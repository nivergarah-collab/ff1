package com.example.ff1.motor.config;

import java.util.Map;
import java.util.TreeMap;

import com.example.ff1.motor.datos.ErrorDeDatos;

/**
 * Tabla de extensiones por clave (tipos de habilidad, efecto, condición de victoria,
 * escena...). Los datos nombran la clave y el motor busca aquí la implementación,
 * así se añaden tipos nuevos sin tocar el núcleo.
 */
public final class Registro<T> {

    private final String queRegistra;
    private final Map<String, T> entradas = new TreeMap<>();

    /** @param queRegistra nombre para los mensajes de error, por ejemplo "efecto" */
    public Registro(String queRegistra) {
        this.queRegistra = queRegistra;
    }

    public Registro<T> registrar(String clave, T valor) {
        if (entradas.containsKey(clave)) {
            throw new IllegalStateException(queRegistra + " ya registrado: " + clave);
        }
        entradas.put(clave, valor);
        return this;
    }

    public boolean contiene(String clave) {
        return entradas.containsKey(clave);
    }

    public T obtener(String clave) {
        T v = entradas.get(clave);
        if (v == null) {
            throw new ErrorDeDatos(queRegistra + " desconocido \"" + clave + "\"; opciones: " + entradas.keySet());
        }
        return v;
    }
}
