package com.example.ff1.motor.fuentes;

import java.util.HashMap;
import java.util.Map;

/** Almacén en memoria, para pruebas. */
public final class AlmacenMemoria implements Almacen {

    private final Map<String, String> ranuras = new HashMap<>();

    @Override
    public void guardar(String ranura, String datos) {
        ranuras.put(ranura, datos);
    }

    @Override
    public String cargar(String ranura) {
        return ranuras.get(ranura);
    }

    @Override
    public boolean existe(String ranura) {
        return ranuras.containsKey(ranura);
    }

    @Override
    public void borrar(String ranura) {
        ranuras.remove(ranura);
    }
}
