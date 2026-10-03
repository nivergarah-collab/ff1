package com.example.ff1.juego;

import com.example.ff1.entrada.Boton;

import java.util.Arrays;
import java.util.List;

/**
 * Rejilla de letras para escribir un nombre con la cruceta, en Java puro. Las filas de letras van
 * seguidas de una fila con "Borrar" y "Fin". El cursor empieza en "Fin", porque el nombre ya trae
 * el del paquete: quien no quiera cambiarlo solo pulsa Aceptar.
 */
public final class EditorNombre {

    static final List<String> LETRAS = Arrays.asList(
            "ABCDEFGHI", "JKLMNÑOPQ", "RSTUVWXYZ", "abcdefghi", "jklmnñopq", "rstuvwxyz");
    public static final String BORRAR = "Borrar";
    public static final String FIN = "Fin";

    private final int largoMaximo;
    private final StringBuilder texto;
    private int fila;
    private int columna;

    public EditorNombre(String inicial, int largoMaximo) {
        this.largoMaximo = largoMaximo;
        this.texto = new StringBuilder(inicial == null ? "" : inicial);
        this.fila = LETRAS.size();
        this.columna = 1;
    }

    public String texto() {
        return texto.toString();
    }

    public int largoMaximo() {
        return largoMaximo;
    }

    public int fila() {
        return fila;
    }

    public int columna() {
        return columna;
    }

    /** Filas de la rejilla, contando la de "Borrar" y "Fin". */
    public static int filas() {
        return LETRAS.size() + 1;
    }

    public static int columnas(int fila) {
        return fila < LETRAS.size() ? LETRAS.get(fila).length() : 2;
    }

    public static String celda(int fila, int columna) {
        if (fila < LETRAS.size()) {
            return String.valueOf(LETRAS.get(fila).charAt(columna));
        }
        return columna == 0 ? BORRAR : FIN;
    }

    /** Celda bajo el cursor. */
    public String celda() {
        return celda(fila, columna);
    }

    /** Mueve el cursor con vuelta; al cambiar de fila la columna se ajusta a la fila nueva. */
    public void mover(Boton b) {
        if (b == Boton.ARRIBA || b == Boton.ABAJO) {
            fila = Menu.vuelta(fila, b == Boton.ABAJO ? 1 : -1, filas());
            columna = Math.min(columna, columnas(fila) - 1);
        } else if (b == Boton.IZQUIERDA || b == Boton.DERECHA) {
            columna = Menu.vuelta(columna, b == Boton.DERECHA ? 1 : -1, columnas(fila));
        }
    }

    /** Aceptar sobre la celda actual; devuelve true solo si fue "Fin" con un nombre no vacío. */
    public boolean aceptar() {
        String c = celda();
        if (c.equals(FIN)) {
            return !texto().trim().isEmpty();
        }
        if (c.equals(BORRAR)) {
            borrar();
        } else if (texto.length() < largoMaximo) {
            texto.append(c);
        }
        return false;
    }

    /** Quita la última letra, si hay. */
    public void borrar() {
        if (texto.length() > 0) {
            texto.setLength(texto.length() - 1);
        }
    }
}
