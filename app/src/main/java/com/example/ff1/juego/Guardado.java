package com.example.ff1.juego;

import com.example.ff1.combate.ConfiguracionCombate;
import com.example.ff1.inventario.DefinicionObjeto;
import com.example.ff1.motor.config.Configuracion;
import com.example.ff1.motor.datos.Documentos;
import com.example.ff1.motor.datos.ErrorDeDatos;
import com.example.ff1.motor.datos.EscritorJson;
import com.example.ff1.motor.datos.LectorJson;
import com.example.ff1.motor.datos.Nodo;
import com.example.ff1.motor.fuentes.Azar;
import com.example.ff1.motor.fuentes.FuenteContenido;
import com.example.ff1.mundo.Mapa;
import com.example.ff1.progresion.Heroe;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Documento {@code "partida"} v1: escribe y lee lo que cambia durante el juego (ver el contrato). */
final class Guardado {

    static final String TIPO = "partida";
    static final int VERSION = 1;
    /** Ajustes del menú que viajan con la partida. */
    private static final String[] AJUSTES = {
        ConfiguracionCombate.TICKS_POR_PASO, ConfiguracionJuego.MS_MENSAJE, ConfiguracionJuego.RAPIDO_AL_EMPEZAR };

    private Guardado() {
    }

    static String escribir(Partida p) {
        Map<String, Object> doc = new LinkedHashMap<>();
        doc.put("tipo", TIPO);
        doc.put("version", VERSION);
        doc.put("mapa", p.explorador().mapa().id);
        doc.put("x", p.explorador().x());
        doc.put("y", p.explorador().y());
        doc.put("oro", p.oro());
        doc.put("jefes", new ArrayList<>(p.jefesDerrotados()));
        List<Object> heroes = new ArrayList<>();
        for (Heroe h : p.grupo()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("clase", h.clase().id);
            m.put("nombre", h.nombre());
            m.put("experiencia", h.experiencia());
            m.put("vida", h.vida());
            m.put("magia", h.magia());
            Map<String, Object> equipo = new LinkedHashMap<>();
            for (Map.Entry<String, DefinicionObjeto> e : h.equipo().entrySet()) {
                equipo.put(e.getKey(), e.getValue().id);
            }
            m.put("equipo", equipo);
            heroes.add(m);
        }
        doc.put("grupo", heroes);
        doc.put("inventario", new LinkedHashMap<String, Object>(p.inventario().contenido()));
        Map<String, Object> ajustes = new LinkedHashMap<>();
        for (String a : AJUSTES) {
            ajustes.put(a, p.config().actual().entero(a));
        }
        doc.put("ajustes", ajustes);
        return EscritorJson.escribir(Nodo.desde(doc));
    }

    static Partida leer(FuenteContenido fuente, Azar azar, String texto) {
        Nodo doc = LectorJson.leer(texto);
        Documentos.exigir(doc, TIPO, VERSION);
        Partida p = Partida.nueva(fuente, azar);
        try {
            aplicar(p, doc);
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new ErrorDeDatos("partida: " + e.getMessage(), e);
        }
        return p;
    }

    /** Valida todo el documento antes de tocar la partida, para no dejarla a medias. */
    private static void aplicar(Partida p, Nodo doc) {
        List<Nodo> ng = doc.lista("grupo");
        Documentos.dentro(doc, "grupo", ng.size(), 1, 6);
        List<Heroe> heroes = new ArrayList<>();
        for (Nodo n : ng) {
            Heroe h = p.crearHeroe(n);
            Nodo eq = n.objeto("equipo");
            for (String ranura : eq.claves()) {
                DefinicionObjeto o = p.objetos().objeto(eq.texto(ranura));
                if (!o.equipablePor(h.clase().id) || !ranura.equals(o.ranura)) {
                    throw new ErrorDeDatos(eq.ruta() + "." + ranura + ": \"" + o.id + "\" no se puede equipar ahí");
                }
                h.equipar(o);
            }
            try {
                h.restaurarEstado(Documentos.rango(n, "experiencia", 0, 99_999_999),
                        Documentos.rango(n, "vida", 0, 99999), Documentos.rango(n, "magia", 0, 9999));
            } catch (IllegalArgumentException e) {
                throw new ErrorDeDatos(n.ruta() + ": " + e.getMessage());
            }
            heroes.add(h);
        }
        Nodo ni = doc.objeto("inventario");
        Map<String, Integer> cantidades = new LinkedHashMap<>();
        for (String id : ni.claves()) {
            p.objetos().objeto(id);
            cantidades.put(id, Documentos.rango(ni, id, 1, 999));
        }
        Mapa m = p.mapa(doc.texto("mapa"));
        int x = doc.entero("x");
        int y = doc.entero("y");
        if (!m.pasable(x, y)) {
            throw new ErrorDeDatos(doc.ruta() + ".x/y: (" + x + ", " + y + ") no es una casilla pasable de " + m.id);
        }
        Set<String> vencidos = new LinkedHashSet<>();
        for (Nodo j : doc.lista("jefes")) {
            p.servicios().jefe(j.comoTexto());
            vencidos.add(j.comoTexto());
        }
        int oro = Documentos.rango(doc, "oro", 0, 999_999);
        Configuracion ajustes = p.config().actual();
        if (doc.tiene("ajustes")) {
            Nodo na = doc.objeto("ajustes");
            for (String a : AJUSTES) {
                if (na.tiene(a)) {
                    ajustes = ajustes.con(a, na.entero(a));
                }
            }
        }
        p.restaurar(heroes, cantidades, oro, vencidos, ajustes, m.id, x, y);
    }
}
