package com.example.ff1.dibujo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Lo que se ve en un cuadro, como una lista de órdenes en Java puro sobre una rejilla virtual
 * fija de {@link #ANCHO} × {@link #ALTO} unidades (vertical). La lógica de las pantallas llena la
 * escena y una sola clase Android la traduce a {@code Canvas}, escalando a la pantalla real; así
 * las pruebas comparan órdenes, no píxeles. Los colores son ARGB de 32 bits.
 */
public final class Escena {

    public static final int ANCHO = 360;
    public static final int ALTO = 640;

    public enum Tipo {
        /** Rectángulo relleno o solo el borde. */
        RECTANGULO,
        /** Texto de una línea; {@code x, y} es la esquina superior según la alineación. */
        TEXTO,
        /** Casilla de mapa: cuadrado de lado {@code ancho} con color y símbolo opcional. */
        CASILLA
    }

    public enum Alineacion {
        IZQUIERDA, CENTRO, DERECHA
    }

    /** Una orden de dibujo. Inmutable. */
    public static final class Orden {
        public final Tipo tipo;
        public final int x;
        public final int y;
        public final int ancho;
        public final int alto;
        public final int color;
        public final boolean relleno;
        public final String texto;
        public final Alineacion alineacion;

        Orden(Tipo tipo, int x, int y, int ancho, int alto, int color, boolean relleno, String texto,
                Alineacion alineacion) {
            this.tipo = tipo;
            this.x = x;
            this.y = y;
            this.ancho = ancho;
            this.alto = alto;
            this.color = color;
            this.relleno = relleno;
            this.texto = texto;
            this.alineacion = alineacion;
        }

        @Override
        public String toString() {
            return tipo + "(" + x + "," + y + " " + ancho + "x" + alto
                    + (texto != null ? " \"" + texto + "\"" : "") + ")";
        }
    }

    private final List<Orden> ordenes = new ArrayList<>();

    /** Vacía la escena para el cuadro siguiente. */
    public void limpiar() {
        ordenes.clear();
    }

    public List<Orden> ordenes() {
        return Collections.unmodifiableList(ordenes);
    }

    public Escena rectangulo(int x, int y, int ancho, int alto, int color) {
        return agregar(new Orden(Tipo.RECTANGULO, x, y, medida(ancho), medida(alto), color, true, null, null));
    }

    /** Solo el borde, de una unidad de grosor. */
    public Escena marco(int x, int y, int ancho, int alto, int color) {
        return agregar(new Orden(Tipo.RECTANGULO, x, y, medida(ancho), medida(alto), color, false, null, null));
    }

    /** Texto de una línea con altura {@code tamano} (unidades virtuales). */
    public Escena texto(int x, int y, String texto, int tamano, int color, Alineacion alineacion) {
        if (texto == null || alineacion == null) {
            throw new IllegalArgumentException("texto y alineación son obligatorios");
        }
        return agregar(new Orden(Tipo.TEXTO, x, y, 0, medida(tamano), color, true, texto, alineacion));
    }

    public Escena texto(int x, int y, String texto, int tamano, int color) {
        return texto(x, y, texto, tamano, color, Alineacion.IZQUIERDA);
    }

    /** Casilla de mapa; {@code simbolo} puede ser {@code null} si basta el color. */
    public Escena casilla(int x, int y, int lado, int color, String simbolo) {
        return agregar(new Orden(Tipo.CASILLA, x, y, medida(lado), medida(lado), color, true, simbolo,
                Alineacion.CENTRO));
    }

    /** Órdenes de un tipo, en orden de dibujo (para las pruebas). */
    public List<Orden> de(Tipo tipo) {
        List<Orden> r = new ArrayList<>();
        for (Orden o : ordenes) {
            if (o.tipo == tipo) {
                r.add(o);
            }
        }
        return r;
    }

    /** ¿Hay un texto exactamente igual? (para las pruebas). */
    public boolean contieneTexto(String texto) {
        for (Orden o : ordenes) {
            if (o.tipo == Tipo.TEXTO && o.texto.equals(texto)) {
                return true;
            }
        }
        return false;
    }

    private Escena agregar(Orden o) {
        ordenes.add(o);
        return this;
    }

    private static int medida(int v) {
        if (v < 0) {
            throw new IllegalArgumentException("medida negativa: " + v);
        }
        return v;
    }
}
