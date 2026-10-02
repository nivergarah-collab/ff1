package com.example.ff1.entrada;

import com.example.ff1.dibujo.Escena;

import java.util.EnumMap;
import java.util.Map;

/**
 * Botones táctiles en pantalla, en coordenadas de la rejilla virtual de {@link Escena}: cruceta a
 * la izquierda y aceptar, cancelar y avance rápido a la derecha, en la franja inferior. Traduce un
 * toque a un {@link Boton} y se dibuja en la escena.
 */
public final class Controles {

    /** Borde superior de la franja de controles; las pantallas dibujan por encima. */
    public static final int FRANJA_Y = 450;

    private static final int FONDO = 0xFF101018;
    private static final int TECLA = 0xFF2C2C3C;
    private static final int TECLA_ACTIVA = 0xFF5A4A1E;
    private static final int LETRA = 0xFFE0E0E0;

    private final Map<Boton, int[]> zonas = new EnumMap<>(Boton.class);

    public Controles() {
        zonas.put(Boton.ARRIBA, new int[] {70, 465, 55, 55});
        zonas.put(Boton.ABAJO, new int[] {70, 575, 55, 55});
        zonas.put(Boton.IZQUIERDA, new int[] {15, 520, 55, 55});
        zonas.put(Boton.DERECHA, new int[] {125, 520, 55, 55});
        zonas.put(Boton.ACEPTAR, new int[] {225, 470, 120, 65});
        zonas.put(Boton.CANCELAR, new int[] {225, 545, 120, 45});
        zonas.put(Boton.RAPIDO, new int[] {225, 600, 120, 32});
    }

    /** Botón bajo el punto virtual {@code (x, y)}, o {@code null}. */
    public Boton tocar(int x, int y) {
        for (Map.Entry<Boton, int[]> z : zonas.entrySet()) {
            int[] r = z.getValue();
            if (x >= r[0] && x < r[0] + r[2] && y >= r[1] && y < r[1] + r[3]) {
                return z.getKey();
            }
        }
        return null;
    }

    /** Rectángulo {x, y, ancho, alto} de un botón (copia). */
    public int[] zona(Boton b) {
        return zonas.get(b).clone();
    }

    public void dibujar(Escena escena, boolean rapidoActivo, Boton pulsado) {
        escena.rectangulo(0, FRANJA_Y, Escena.ANCHO, Escena.ALTO - FRANJA_Y, FONDO);
        for (Map.Entry<Boton, int[]> z : zonas.entrySet()) {
            Boton b = z.getKey();
            int[] r = z.getValue();
            boolean activa = b == pulsado || (b == Boton.RAPIDO && rapidoActivo);
            escena.rectangulo(r[0], r[1], r[2], r[3], activa ? TECLA_ACTIVA : TECLA);
            escena.marco(r[0], r[1], r[2], r[3], LETRA);
            escena.texto(r[0] + r[2] / 2, r[1] + (r[3] - 16) / 2, etiqueta(b, rapidoActivo), 16, LETRA,
                    Escena.Alineacion.CENTRO);
        }
    }

    static String etiqueta(Boton b, boolean rapidoActivo) {
        switch (b) {
            case ARRIBA:
                return "^";
            case ABAJO:
                return "v";
            case IZQUIERDA:
                return "<";
            case DERECHA:
                return ">";
            case ACEPTAR:
                return "Aceptar";
            case CANCELAR:
                return "Cancelar";
            default:
                return rapidoActivo ? "Rápido: SÍ" : "Rápido: no";
        }
    }
}
