package com.example.ff1.dibujo;

/**
 * Convierte entre la rejilla virtual de {@link Escena} y los píxeles de una pantalla real:
 * escala uniforme (sin deformar) y centrado, con bandas a los lados o arriba y abajo.
 */
public final class Escalado {

    public final float escala;
    public final float desplazamientoX;
    public final float desplazamientoY;

    public Escalado(int anchoPantalla, int altoPantalla) {
        if (anchoPantalla <= 0 || altoPantalla <= 0) {
            throw new IllegalArgumentException("pantalla sin tamaño");
        }
        escala = Math.min(anchoPantalla / (float) Escena.ANCHO, altoPantalla / (float) Escena.ALTO);
        desplazamientoX = (anchoPantalla - Escena.ANCHO * escala) / 2f;
        desplazamientoY = (altoPantalla - Escena.ALTO * escala) / 2f;
    }

    public float pantallaX(float virtualX) {
        return desplazamientoX + virtualX * escala;
    }

    public float pantallaY(float virtualY) {
        return desplazamientoY + virtualY * escala;
    }

    /** Coordenada virtual de un toque (puede caer fuera de la rejilla, en las bandas). */
    public int virtualX(float pantallaX) {
        return (int) Math.floor((pantallaX - desplazamientoX) / escala);
    }

    public int virtualY(float pantallaY) {
        return (int) Math.floor((pantallaY - desplazamientoY) / escala);
    }
}
