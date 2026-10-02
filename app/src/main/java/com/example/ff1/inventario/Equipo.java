package com.example.ff1.inventario;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Lo que lleva puesto un héroe: una pieza por ranura. */
public final class Equipo {

    private final String clase;
    private final Map<String, DefinicionObjeto> puestas = new LinkedHashMap<>();

    /** @param clase id de la clase del héroe, para validar qué puede equipar */
    public Equipo(String clase) {
        this.clase = clase;
    }

    /**
     * Pone la pieza en su ranura y devuelve la que estaba (o {@code null}). Lanza
     * {@link IllegalArgumentException} si la clase no la puede equipar.
     */
    public DefinicionObjeto equipar(DefinicionObjeto pieza) {
        if (!pieza.equipablePor(clase)) {
            throw new IllegalArgumentException(clase + " no puede equipar " + pieza.id);
        }
        return puestas.put(pieza.ranura, pieza);
    }

    /** Quita la pieza de la ranura y la devuelve (o {@code null}). */
    public DefinicionObjeto quitar(String ranura) {
        return puestas.remove(ranura);
    }

    public DefinicionObjeto en(String ranura) {
        return puestas.get(ranura);
    }

    public Map<String, DefinicionObjeto> puestas() {
        return Collections.unmodifiableMap(puestas);
    }

    /** Suma de los bonos de todas las piezas puestas. */
    public Bonos bonos() {
        Bonos total = Bonos.NINGUNO;
        for (DefinicionObjeto p : puestas.values()) {
            total = total.mas(p.bonos);
        }
        return total;
    }
}
