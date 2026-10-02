package com.example.ff1.inventario;

import java.util.Collections;
import java.util.List;

import com.example.ff1.combate.Habilidad;

/** Objeto leído del documento {@code "objetos"}. Inmutable. */
public final class DefinicionObjeto {

    public enum Categoria {
        CONSUMIBLE, EQUIPO, CLAVE
    }

    public final String id;
    public final String nombre;
    public final Categoria categoria;
    /** Precio de compra; se vende por la mitad. 0 = no se vende ni se compra. */
    public final int precio;
    /** Efecto al usarlo (consumibles), resuelto como una habilidad; o {@code null}. */
    public final Habilidad efecto;
    /** Ranura que ocupa (equipo), o {@code null}. */
    public final String ranura;
    public final Bonos bonos;
    /** Clases que lo pueden equipar; vacía = todas. */
    public final List<String> clases;

    public DefinicionObjeto(String id, String nombre, Categoria categoria, int precio,
            Habilidad efecto, String ranura, Bonos bonos, List<String> clases) {
        this.id = id;
        this.nombre = nombre;
        this.categoria = categoria;
        this.precio = precio;
        this.efecto = efecto;
        this.ranura = ranura;
        this.bonos = bonos;
        this.clases = Collections.unmodifiableList(clases);
    }

    public boolean equipablePor(String clase) {
        return categoria == Categoria.EQUIPO && (clases.isEmpty() || clases.contains(clase));
    }
}
