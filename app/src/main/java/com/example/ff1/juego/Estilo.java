package com.example.ff1.juego;

import com.example.ff1.dibujo.Escena;
import com.example.ff1.entrada.Controles;

import java.util.ArrayList;
import java.util.List;

/** Colores, tamaños y piezas de dibujo comunes a las pantallas (ventanas, menús, barras). */
public final class Estilo {

    public static final int FONDO = 0xFF0B0B14;
    public static final int VENTANA = 0xFF1B2450;
    public static final int BORDE = 0xFFD8D8E8;
    public static final int TEXTO = 0xFFF2F2F2;
    public static final int APAGADO = 0xFF7A7A8A;
    public static final int RESALTE = 0xFFFFD25A;
    public static final int VIDA = 0xFF4CC36A;
    public static final int VIDA_BAJA = 0xFFE0533D;
    public static final int MAGIA = 0xFF4A90E2;
    public static final int TIEMPO = 0xFFE8C547;
    public static final int VACIO = 0xFF30303C;

    public static final int LETRA = 16;
    public static final int LETRA_GRANDE = 28;
    public static final int LINEA = 22;
    /** Alto útil para las pantallas: encima de los controles. */
    public static final int ALTO_UTIL = Controles.FRANJA_Y;

    private Estilo() {
    }

    public static void ventana(Escena e, int x, int y, int ancho, int alto) {
        e.rectangulo(x, y, ancho, alto, VENTANA);
        e.marco(x, y, ancho, alto, BORDE);
    }

    /**
     * Lista de opciones con cursor; las deshabilitadas se ven apagadas. {@code habilitadas} puede
     * ser {@code null} (todas habilitadas).
     */
    public static void menu(Escena e, int x, int y, List<String> opciones, int cursor, List<Boolean> habilitadas) {
        for (int i = 0; i < opciones.size(); i++) {
            boolean activa = habilitadas == null || habilitadas.get(i);
            int yy = y + i * LINEA;
            if (i == cursor) {
                e.texto(x, yy, ">", LETRA, RESALTE);
            }
            e.texto(x + 14, yy, opciones.get(i), LETRA, activa ? (i == cursor ? RESALTE : TEXTO) : APAGADO);
        }
    }

    /** Barra horizontal llena en proporción {@code valor / maximo}. */
    public static void barra(Escena e, int x, int y, int ancho, int alto, int valor, int maximo, int color) {
        e.rectangulo(x, y, ancho, alto, VACIO);
        int lleno = maximo <= 0 ? 0 : (int) ((long) ancho * Math.max(0, Math.min(valor, maximo)) / maximo);
        if (lleno > 0) {
            e.rectangulo(x, y, lleno, alto, color);
        }
    }

    /** Corta un texto en líneas de hasta {@code columnas} caracteres, por palabras. */
    public static List<String> partir(String texto, int columnas) {
        List<String> lineas = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        for (String palabra : texto.split(" ")) {
            if (actual.length() > 0 && actual.length() + 1 + palabra.length() > columnas) {
                lineas.add(actual.toString());
                actual.setLength(0);
            }
            if (actual.length() > 0) {
                actual.append(' ');
            }
            actual.append(palabra);
        }
        if (actual.length() > 0) {
            lineas.add(actual.toString());
        }
        return lineas;
    }

    /** Color ARGB de un texto {@code #RRGGBB}, o {@code defecto} si es {@code null}. */
    public static int color(String rrggbb, int defecto) {
        return rrggbb == null ? defecto : 0xFF000000 | Integer.parseInt(rrggbb.substring(1), 16);
    }
}
