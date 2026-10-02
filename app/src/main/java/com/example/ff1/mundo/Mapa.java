package com.example.ff1.mundo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.example.ff1.motor.datos.Documentos;
import com.example.ff1.motor.datos.ErrorDeDatos;
import com.example.ff1.motor.datos.Nodo;
import com.example.ff1.motor.fuentes.FuenteContenido;

/**
 * Mapa por casillas (documento {@code "mapa"}, recurso {@code "mapas/<id>"}). Las filas son
 * textos donde cada carácter es una casilla descrita en la leyenda.
 */
public final class Mapa {

    public static final String TIPO = "mapa";
    public static final int VERSION = 1;

    /** Lo que significa un carácter de la leyenda. */
    public static final class Casilla {
        public final char simbolo;
        public final String nombre;
        public final boolean pasable;

        Casilla(char simbolo, String nombre, boolean pasable) {
            this.simbolo = simbolo;
            this.nombre = nombre;
            this.pasable = pasable;
        }
    }

    public final String id;
    public final int ancho;
    public final int alto;
    public final int inicioX;
    public final int inicioY;
    private final List<String> filas;
    private final Map<Character, Casilla> leyenda;

    private Mapa(String id, List<String> filas, Map<Character, Casilla> leyenda, int inicioX, int inicioY) {
        this.id = id;
        this.filas = filas;
        this.leyenda = leyenda;
        this.alto = filas.size();
        this.ancho = filas.get(0).length();
        this.inicioX = inicioX;
        this.inicioY = inicioY;
    }

    public static Mapa cargar(FuenteContenido fuente, String id) {
        Mapa m = desde(fuente.cargar("mapas/" + id, TIPO, VERSION));
        if (!m.id.equals(id)) {
            throw new ErrorDeDatos("mapas/" + id + ".id: se esperaba \"" + id + "\" y vino \"" + m.id + "\"");
        }
        return m;
    }

    public static Mapa desde(Nodo doc) {
        Documentos.exigir(doc, TIPO, VERSION);
        String id = doc.texto("id");

        Map<Character, Casilla> leyenda = new LinkedHashMap<>();
        Nodo nl = doc.objeto("leyenda");
        for (String clave : nl.claves()) {
            if (clave.length() != 1) {
                throw new ErrorDeDatos(nl.ruta() + "." + clave + ": la clave debe ser un solo carácter");
            }
            Nodo c = nl.objeto(clave);
            leyenda.put(clave.charAt(0), new Casilla(clave.charAt(0), c.texto("nombre"), c.booleano("pasable")));
        }

        List<Nodo> nf = doc.lista("filas");
        if (nf.isEmpty()) {
            throw new ErrorDeDatos(doc.ruta() + ".filas: el mapa no tiene filas");
        }
        List<String> filas = new ArrayList<>();
        for (Nodo f : nf) {
            String fila = f.comoTexto();
            if (fila.isEmpty() || (!filas.isEmpty() && fila.length() != filas.get(0).length())) {
                throw new ErrorDeDatos(f.ruta() + ": todas las filas deben tener el mismo ancho, no vacío");
            }
            for (int x = 0; x < fila.length(); x++) {
                if (!leyenda.containsKey(fila.charAt(x))) {
                    throw new ErrorDeDatos(f.ruta() + ": el carácter '" + fila.charAt(x) + "' (columna " + x
                            + ") no está en la leyenda");
                }
            }
            filas.add(fila);
        }

        Nodo ni = doc.objeto("inicio");
        int x = ni.entero("x");
        int y = ni.entero("y");
        Mapa m = new Mapa(id, Collections.unmodifiableList(filas), Collections.unmodifiableMap(leyenda), x, y);
        if (!m.dentro(x, y) || !m.pasable(x, y)) {
            throw new ErrorDeDatos(ni.ruta() + ": (" + x + ", " + y + ") no es una casilla pasable del mapa");
        }
        return m;
    }

    public boolean dentro(int x, int y) {
        return x >= 0 && y >= 0 && x < ancho && y < alto;
    }

    /** Casilla en (x, y); fuera del mapa lanza {@link IndexOutOfBoundsException}. */
    public Casilla casilla(int x, int y) {
        if (!dentro(x, y)) {
            throw new IndexOutOfBoundsException("(" + x + ", " + y + ") fuera del mapa " + id);
        }
        return leyenda.get(filas.get(y).charAt(x));
    }

    /** Pasable si está dentro del mapa y su casilla lo es. */
    public boolean pasable(int x, int y) {
        return dentro(x, y) && casilla(x, y).pasable;
    }
}
