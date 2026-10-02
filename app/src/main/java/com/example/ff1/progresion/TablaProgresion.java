package com.example.ff1.progresion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.example.ff1.combate.Bando;
import com.example.ff1.combate.CatalogoCombate;
import com.example.ff1.combate.DefinicionCombatiente;
import com.example.ff1.motor.datos.Documentos;
import com.example.ff1.motor.datos.ErrorDeDatos;
import com.example.ff1.motor.datos.Nodo;
import com.example.ff1.motor.fuentes.FuenteContenido;

/**
 * Experiencia por nivel y crecimiento de cada clase de héroe, leídos del documento
 * {@code "progresion"} (ver docs/contrato-de-datos.md). El nivel máximo es el largo de la
 * tabla de experiencia.
 */
public final class TablaProgresion {

    public static final String TIPO = "progresion";
    public static final int VERSION = 1;

    /** Lo que suma una clase por cada nivel sobre el 1. */
    public static final class Crecimiento {
        public final int vida;
        public final int magia;
        public final int ataque;
        public final int defensa;
        public final int poder;
        public final int velocidad;

        public Crecimiento(int vida, int magia, int ataque, int defensa, int poder, int velocidad) {
            this.vida = vida;
            this.magia = magia;
            this.ataque = ataque;
            this.defensa = defensa;
            this.poder = poder;
            this.velocidad = velocidad;
        }
    }

    /** Experiencia acumulada para llegar a cada nivel: índice 0 = nivel 1 (siempre 0). */
    private final List<Integer> experiencia;
    private final Map<String, Crecimiento> crecimientos;

    private TablaProgresion(List<Integer> experiencia, Map<String, Crecimiento> crecimientos) {
        this.experiencia = Collections.unmodifiableList(experiencia);
        this.crecimientos = Collections.unmodifiableMap(crecimientos);
    }

    public static TablaProgresion cargar(FuenteContenido fuente, CatalogoCombate catalogo) {
        return desde(fuente.cargar(TIPO, TIPO, VERSION), catalogo);
    }

    public static TablaProgresion desde(Nodo doc, CatalogoCombate catalogo) {
        Documentos.exigir(doc, TIPO, VERSION);
        List<Integer> xp = new ArrayList<>();
        for (Nodo n : doc.lista("experiencia")) {
            int v = n.comoEntero();
            int anterior = xp.isEmpty() ? -1 : xp.get(xp.size() - 1);
            if (xp.isEmpty() ? v != 0 : v <= anterior) {
                throw new ErrorDeDatos(n.ruta() + ": " + v + (xp.isEmpty()
                        ? " debe ser 0 (nivel 1)" : " debe ser mayor que " + anterior));
            }
            xp.add(v);
        }
        if (xp.isEmpty() || xp.size() > 99) {
            throw new ErrorDeDatos(doc.ruta() + ".experiencia: entre 1 y 99 niveles");
        }
        Map<String, Crecimiento> mapa = new LinkedHashMap<>();
        for (Nodo n : doc.lista("clases")) {
            String id = n.texto("id");
            DefinicionCombatiente def = catalogo.combatiente(id);
            if (def.bando != Bando.HEROE) {
                throw new ErrorDeDatos(n.ruta() + ".id: \"" + id + "\" no es una clase de héroe");
            }
            if (mapa.containsKey(id)) {
                throw new ErrorDeDatos(n.ruta() + ".id: \"" + id + "\" repetido");
            }
            Nodo c = n.objeto("crecimiento");
            mapa.put(id, new Crecimiento(Documentos.rangoO(c, "vida", 0, 999, 0), Documentos.rangoO(c, "magia", 0, 999, 0), Documentos.rangoO(c, "ataque", 0, 999, 0),
                    Documentos.rangoO(c, "defensa", 0, 999, 0), Documentos.rangoO(c, "poder", 0, 999, 0), Documentos.rangoO(c, "velocidad", 0, 999, 0)));
        }
        return new TablaProgresion(xp, mapa);
    }

    public int nivelMaximo() {
        return experiencia.size();
    }

    /** Experiencia acumulada necesaria para estar en {@code nivel}. */
    public int experienciaPara(int nivel) {
        if (nivel < 1 || nivel > nivelMaximo()) {
            throw new IllegalArgumentException("nivel fuera de rango: " + nivel);
        }
        return experiencia.get(nivel - 1);
    }

    /** Nivel que corresponde a una experiencia acumulada. */
    public int nivelCon(int experienciaTotal) {
        int nivel = 1;
        while (nivel < nivelMaximo() && experiencia.get(nivel) <= experienciaTotal) {
            nivel++;
        }
        return nivel;
    }

    /** Crecimiento de la clase; una clase sin entrada no crece. */
    public Crecimiento crecimiento(String clase) {
        Crecimiento c = crecimientos.get(clase);
        return c != null ? c : new Crecimiento(0, 0, 0, 0, 0, 0);
    }

    /** Estadísticas de una clase en un nivel: base + crecimiento × (nivel − 1). */
    public DefinicionCombatiente enNivel(DefinicionCombatiente clase, int nivel) {
        experienciaPara(nivel); // valida el rango
        Crecimiento c = crecimiento(clase.id);
        int n = nivel - 1;
        return new DefinicionCombatiente(clase.id, clase.nombre, clase.bando,
                Math.min(99999, clase.vida + c.vida * n), Math.min(9999, clase.magia + c.magia * n),
                Math.min(999, clase.ataque + c.ataque * n), Math.min(999, clase.defensa + c.defensa * n),
                Math.min(999, clase.poder + c.poder * n), Math.min(255, clase.velocidad + c.velocidad * n),
                clase.habilidades, clase.experiencia, clase.oro);
    }

}
