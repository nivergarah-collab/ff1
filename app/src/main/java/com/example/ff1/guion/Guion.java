package com.example.ff1.guion;

import com.example.ff1.motor.datos.Documentos;
import com.example.ff1.motor.datos.ErrorDeDatos;
import com.example.ff1.motor.datos.Nodo;
import com.example.ff1.motor.fuentes.FuenteContenido;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Escena de texto leída del documento {@code "escena"} ({@code escenas/<id>.json}): una lista de
 * líneas (quién habla y qué dice) que el jugador avanza una a una. Inmutable. El texto puede
 * llevar marcadores {@code {heroe:<clase>}}, que se sustituyen por el nombre que el jugador
 * puso al héroe de esa clase (ver {@link #resolver}).
 */
public final class Guion {

    public static final String TIPO = "escena";
    public static final int VERSION = 1;
    public static final String CARPETA = "escenas/";

    private static final Pattern MARCADOR = Pattern.compile("\\{heroe:([a-z0-9-]+)\\}");

    /** Una línea: {@code quien} vacío es el narrador. */
    public static final class Linea {
        public final String quien;
        public final String texto;

        Linea(String quien, String texto) {
            this.quien = quien;
            this.texto = texto;
        }
    }

    public final String id;
    private final List<Linea> lineas;

    Guion(String id, List<Linea> lineas) {
        this.id = id;
        this.lineas = Collections.unmodifiableList(lineas);
    }

    public List<Linea> lineas() {
        return lineas;
    }

    /** Carga {@code escenas/<id>.json}; lanza {@link ErrorDeDatos} si falta o no es válida. */
    public static Guion cargar(FuenteContenido fuente, String id) {
        Guion g = desde(fuente.cargar(CARPETA + id, TIPO, VERSION));
        if (!g.id.equals(id)) {
            throw new ErrorDeDatos(CARPETA + id + ": el id interno \"" + g.id + "\" no coincide con el nombre");
        }
        return g;
    }

    public static Guion desde(Nodo doc) {
        Documentos.exigir(doc, TIPO, VERSION);
        String id = doc.texto("id");
        List<Nodo> nl = doc.lista("lineas");
        Documentos.dentro(doc, "lineas", nl.size(), 1, 300);
        List<Linea> lineas = new ArrayList<>();
        for (Nodo n : nl) {
            String texto = n.texto("texto").trim();
            if (texto.isEmpty() || texto.length() > 300) {
                throw new ErrorDeDatos(n.ruta() + ".texto: debe tener entre 1 y 300 caracteres");
            }
            String quien = n.textoO("quien", "").trim();
            if (quien.length() > 40) {
                throw new ErrorDeDatos(n.ruta() + ".quien: máximo 40 caracteres");
            }
            comprobarMarcadores(n.ruta() + ".texto", texto);
            comprobarMarcadores(n.ruta() + ".quien", quien);
            lineas.add(new Linea(quien, texto));
        }
        return new Guion(id, lineas);
    }

    private static void comprobarMarcadores(String ruta, String texto) {
        String resto = MARCADOR.matcher(texto).replaceAll("");
        if (resto.contains("{") || resto.contains("}")) {
            throw new ErrorDeDatos(ruta + ": marcador mal formado (se espera {heroe:<clase>})");
        }
    }

    /**
     * Sustituye los marcadores {@code {heroe:<clase>}} por el nombre de esa clase en
     * {@code nombresPorClase}; si no hay un héroe de esa clase, deja el id de la clase.
     */
    public static String resolver(String texto, Map<String, String> nombresPorClase) {
        Matcher m = MARCADOR.matcher(texto);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String nombre = nombresPorClase.get(m.group(1));
            m.appendReplacement(sb, Matcher.quoteReplacement(nombre == null ? m.group(1) : nombre));
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
