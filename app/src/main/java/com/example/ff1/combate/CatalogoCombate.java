package com.example.ff1.combate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.example.ff1.motor.config.Registro;
import com.example.ff1.motor.datos.Documentos;
import com.example.ff1.motor.datos.ErrorDeDatos;
import com.example.ff1.motor.datos.Nodo;
import com.example.ff1.motor.fuentes.FuenteContenido;

/**
 * Combatientes y habilidades de un paquete de contenido, validados. Se crea desde los
 * documentos {@code "combatientes"} y {@code "habilidades"} (ver docs/contrato-de-datos.md).
 * Los tipos de habilidad y los estados se validan contra los registros del motor.
 */
public final class CatalogoCombate {

    public static final String TIPO_COMBATIENTES = "combatientes";
    public static final String TIPO_HABILIDADES = "habilidades";
    public static final int VERSION = 1;

    private static final Pattern ID = Pattern.compile("[a-z0-9]+(-[a-z0-9]+)*");

    private final Map<String, DefinicionCombatiente> combatientes;
    private final Map<String, Habilidad> habilidades;

    private CatalogoCombate(Map<String, DefinicionCombatiente> combatientes,
            Map<String, Habilidad> habilidades) {
        this.combatientes = Collections.unmodifiableMap(combatientes);
        this.habilidades = Collections.unmodifiableMap(habilidades);
    }

    /** Carga {@code combatientes.json} y {@code habilidades.json} de la fuente. */
    public static CatalogoCombate cargar(FuenteContenido fuente, Registro<?> tiposHabilidad,
            Registro<?> estados) {
        Nodo docHabilidades = fuente.cargar(TIPO_HABILIDADES, TIPO_HABILIDADES, VERSION);
        Nodo docCombatientes = fuente.cargar(TIPO_COMBATIENTES, TIPO_COMBATIENTES, VERSION);
        return desde(docCombatientes, docHabilidades, tiposHabilidad, estados);
    }

    public static CatalogoCombate desde(Nodo docCombatientes, Nodo docHabilidades,
            Registro<?> tiposHabilidad, Registro<?> estados) {
        Map<String, Habilidad> habilidades = leerHabilidades(docHabilidades, tiposHabilidad, estados);
        Map<String, DefinicionCombatiente> combatientes = leerCombatientes(docCombatientes, habilidades);
        return new CatalogoCombate(combatientes, habilidades);
    }

    public DefinicionCombatiente combatiente(String id) {
        DefinicionCombatiente d = combatientes.get(id);
        if (d == null) {
            throw new ErrorDeDatos("combatiente desconocido \"" + id + "\"");
        }
        return d;
    }

    public Habilidad habilidad(String id) {
        Habilidad h = habilidades.get(id);
        if (h == null) {
            throw new ErrorDeDatos("habilidad desconocida \"" + id + "\"");
        }
        return h;
    }

    public Map<String, DefinicionCombatiente> combatientes() {
        return combatientes;
    }

    public Map<String, Habilidad> habilidades() {
        return habilidades;
    }

    private static Map<String, Habilidad> leerHabilidades(Nodo doc, Registro<?> tipos,
            Registro<?> estados) {
        Documentos.exigir(doc, TIPO_HABILIDADES, VERSION);
        Map<String, Habilidad> mapa = new LinkedHashMap<>();
        for (Nodo n : doc.lista("lista")) {
            String id = id(n, mapa);
            String tipo = n.texto("tipo");
            if (!tipos.contiene(tipo)) {
                throw new ErrorDeDatos(n.ruta() + ".tipo: tipo de habilidad \"" + tipo + "\" no registrado");
            }
            String estado = null;
            int duracion = 0;
            if (n.tiene("estado")) {
                Nodo e = n.objeto("estado");
                estado = e.texto("id");
                if (!estados.contiene(estado)) {
                    throw new ErrorDeDatos(e.ruta() + ".id: estado \"" + estado + "\" no registrado");
                }
                duracion = Documentos.rango(e, "duracion", 1, 99);
            }
            mapa.put(id, new Habilidad(id, n.texto("nombre"), tipo, Documentos.rangoO(n, "coste", 0, 999, 0),
                    Documentos.rangoO(n, "poder", 0, 9999, 0), objetivo(n), estado, duracion));
        }
        return mapa;
    }

    private static Map<String, DefinicionCombatiente> leerCombatientes(Nodo doc,
            Map<String, Habilidad> habilidades) {
        Documentos.exigir(doc, TIPO_COMBATIENTES, VERSION);
        Map<String, DefinicionCombatiente> mapa = new LinkedHashMap<>();
        for (Nodo n : doc.lista("lista")) {
            String id = id(n, mapa);
            List<String> propias = new ArrayList<>();
            if (n.tiene("habilidades")) {
                for (Nodo h : n.lista("habilidades")) {
                    String idHabilidad = h.comoTexto();
                    if (!habilidades.containsKey(idHabilidad)) {
                        throw new ErrorDeDatos(h.ruta() + ": habilidad \"" + idHabilidad + "\" no existe");
                    }
                    propias.add(idHabilidad);
                }
            }
            mapa.put(id, new DefinicionCombatiente(id, n.texto("nombre"),
                    Bando.desde(n.texto("bando"), n.ruta() + ".bando"),
                    Documentos.rango(n, "vida", 1, 99999), Documentos.rangoO(n, "magia", 0, 9999, 0),
                    Documentos.rango(n, "ataque", 0, 999), Documentos.rango(n, "defensa", 0, 999),
                    Documentos.rangoO(n, "poder", 0, 999, 0), Documentos.rango(n, "velocidad", 1, 255),
                    propias, Documentos.rangoO(n, "experiencia", 0, 999999, 0), Documentos.rangoO(n, "oro", 0, 999999, 0)));
        }
        return mapa;
    }

    private static String id(Nodo n, Map<String, ?> existentes) {
        String id = n.texto("id");
        if (!ID.matcher(id).matches()) {
            throw new ErrorDeDatos(n.ruta() + ".id: \"" + id + "\" debe ir en minúsculas con guiones");
        }
        if (existentes.containsKey(id)) {
            throw new ErrorDeDatos(n.ruta() + ".id: \"" + id + "\" repetido");
        }
        return id;
    }


    private static Habilidad.Objetivo objetivo(Nodo n) {
        String texto = n.textoO("objetivo", "enemigo");
        switch (texto) {
            case "enemigo":
                return Habilidad.Objetivo.ENEMIGO;
            case "aliado":
                return Habilidad.Objetivo.ALIADO;
            case "si-mismo":
                return Habilidad.Objetivo.SI_MISMO;
            default:
                throw new ErrorDeDatos(n.ruta() + ".objetivo: \"" + texto
                        + "\" desconocido; opciones: [enemigo, aliado, si-mismo]");
        }
    }
}
