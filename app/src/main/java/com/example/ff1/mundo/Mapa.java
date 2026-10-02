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
        /** Zona de encuentros (ver {@link TablaEncuentros}); {@code null} si aquí no hay. */
        public final String zona;
        /** Color de dibujo {@code #RRGGBB}; {@code null} si la presentación elige uno. */
        public final String color;

        Casilla(char simbolo, String nombre, boolean pasable, String zona, String color) {
            this.simbolo = simbolo;
            this.nombre = nombre;
            this.pasable = pasable;
            this.zona = zona;
            this.color = color;
        }
    }

    /** Casilla que lleva a otro mapa al pisarla. */
    public static final class Salida {
        public final int x;
        public final int y;
        public final String mapa;
        public final int destinoX;
        public final int destinoY;
        /** Id del objeto que hace falta llevar para cruzar; {@code null} si la salida está abierta. */
        public final String requiere;

        Salida(int x, int y, String mapa, int destinoX, int destinoY, String requiere) {
            this.requiere = requiere;
            this.x = x;
            this.y = y;
            this.mapa = mapa;
            this.destinoX = destinoX;
            this.destinoY = destinoY;
        }
    }

    /** Casilla con la que se habla estando enfrente (tienda, posada o vecino); {@code ref} es un id de {@code servicios}. */
    public static final class Lugar {
        public final int x;
        public final int y;
        public final String tipo;
        public final String ref;

        Lugar(int x, int y, String tipo, String ref) {
            this.x = x;
            this.y = y;
            this.tipo = tipo;
            this.ref = ref;
        }
    }

    public static final String LUGAR_TIENDA = "tienda";
    public static final String LUGAR_POSADA = "posada";
    public static final String LUGAR_VECINO = "vecino";

    public final String id;
    public final int ancho;
    public final int alto;
    public final int inicioX;
    public final int inicioY;
    private final List<String> filas;
    private final Map<Character, Casilla> leyenda;
    private final List<Salida> salidas = new ArrayList<>();
    private final List<Lugar> lugares = new ArrayList<>();

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
            String color = c.textoO("color", null);
            if (color != null && !color.matches("#[0-9A-Fa-f]{6}")) {
                throw new ErrorDeDatos(c.ruta() + ".color: se esperaba #RRGGBB y vino \"" + color + "\"");
            }
            leyenda.put(clave.charAt(0), new Casilla(clave.charAt(0), c.texto("nombre"), c.booleano("pasable"),
                    c.textoO("zona", null), color));
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
        if (doc.tiene("salidas")) {
            for (Nodo n : doc.lista("salidas")) {
                int sx = n.entero("x");
                int sy = n.entero("y");
                Nodo d = n.objeto("destino");
                if (!m.pasable(sx, sy)) {
                    throw new ErrorDeDatos(n.ruta() + ": (" + sx + ", " + sy + ") no es una casilla pasable del mapa");
                }
                m.salidas.add(new Salida(sx, sy, n.texto("mapa"), d.entero("x"), d.entero("y"), n.textoO("requiere", null)));
            }
        }
        if (doc.tiene("lugares")) {
            for (Nodo n : doc.lista("lugares")) {
                int lx = n.entero("x");
                int ly = n.entero("y");
                String tipo = n.texto("tipo");
                if (!m.dentro(lx, ly)) {
                    throw new ErrorDeDatos(n.ruta() + ": (" + lx + ", " + ly + ") está fuera del mapa");
                }
                if (!tipo.equals(LUGAR_TIENDA) && !tipo.equals(LUGAR_POSADA) && !tipo.equals(LUGAR_VECINO)) {
                    throw new ErrorDeDatos(n.ruta() + ".tipo: \"" + tipo + "\" no es tienda, posada ni vecino");
                }
                m.lugares.add(new Lugar(lx, ly, tipo, n.texto("ref")));
            }
        }
        return m;
    }

    /** Salida en (x, y), o {@code null}. */
    public Salida salidaEn(int x, int y) {
        for (Salida s : salidas) {
            if (s.x == x && s.y == y) {
                return s;
            }
        }
        return null;
    }

    /** Lugar en (x, y), o {@code null}. */
    public Lugar lugarEn(int x, int y) {
        for (Lugar l : lugares) {
            if (l.x == x && l.y == y) {
                return l;
            }
        }
        return null;
    }

    public List<Salida> salidas() {
        return Collections.unmodifiableList(salidas);
    }

    public List<Lugar> lugares() {
        return Collections.unmodifiableList(lugares);
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
