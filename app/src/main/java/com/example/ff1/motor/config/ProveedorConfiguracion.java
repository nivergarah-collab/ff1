package com.example.ff1.motor.config;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import com.example.ff1.motor.datos.Nodo;

/**
 * Configuración vigente del motor, reemplazable mientras el juego corre (por ejemplo
 * desde una API o un editor). Si la nueva no es válida, se conserva la anterior.
 */
public final class ProveedorConfiguracion {

    private volatile Configuracion actual;
    private final List<Consumer<Configuracion>> oyentes = new CopyOnWriteArrayList<>();

    public ProveedorConfiguracion(Configuracion inicial) {
        this.actual = inicial;
    }

    public Configuracion actual() {
        return actual;
    }

    public void reemplazar(Configuracion nueva) {
        if (nueva.esquema() != actual.esquema()) {
            throw new IllegalArgumentException("la configuración nueva usa otro esquema");
        }
        actual = nueva;
        for (Consumer<Configuracion> o : oyentes) {
            o.accept(nueva);
        }
    }

    /** Valida el documento y lo aplica; si no es válido lanza ErrorDeDatos sin cambiar nada. */
    public void reemplazarDesde(Nodo doc) {
        reemplazar(actual.esquema().crear(doc));
    }

    public void alCambiar(Consumer<Configuracion> oyente) {
        oyentes.add(oyente);
    }
}
