package com.example.ff1.motor.fuentes;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

/** Recursos en memoria, para pruebas y paquetes de contenido mínimos. */
public final class LectorMemoria implements LectorTexto {

    private final Map<String, String> recursos;

    public LectorMemoria(Map<String, String> recursos) {
        this.recursos = new HashMap<>(recursos);
    }

    @Override
    public String leer(String ruta) throws FileNotFoundException {
        String texto = recursos.get(ruta);
        if (texto == null) {
            throw new FileNotFoundException(ruta);
        }
        return texto;
    }
}
