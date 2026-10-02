package com.example.ff1.juego;

import com.example.ff1.dibujo.Escena;
import com.example.ff1.entrada.Boton;
import com.example.ff1.guion.Guion;

import java.util.List;
import java.util.Map;

/**
 * Escena de texto: muestra una línea del {@link Guion} en una ventana inferior, con quien habla,
 * y avanza con Aceptar. Al terminar la última línea pasa a la pantalla {@code despues}.
 */
public final class PantallaEscena implements Pantalla {

    static final int COLUMNAS = 34;
    static final int VENTANA_Y = 290;

    private final Juego juego;
    private final Guion guion;
    private final Map<String, String> nombres;
    private final Pantalla despues;
    private int linea;

    PantallaEscena(Juego juego, Guion guion, Map<String, String> nombresPorClase, Pantalla despues) {
        this.juego = juego;
        this.guion = guion;
        this.nombres = nombresPorClase;
        this.despues = despues;
    }

    /** Índice (desde 0) de la línea que se ve. */
    public int linea() {
        return linea;
    }

    public Guion guion() {
        return guion;
    }

    @Override
    public void pulsar(Boton b) {
        if (b == Boton.ACEPTAR) {
            linea++;
            if (linea >= guion.lineas().size()) {
                juego.irA(despues);
            }
        }
    }

    @Override
    public void avanzar(int ms) {
    }

    @Override
    public void dibujar(Escena e) {
        if (linea >= guion.lineas().size()) {
            return;
        }
        Guion.Linea l = guion.lineas().get(linea);
        Estilo.ventana(e, 6, VENTANA_Y, Escena.ANCHO - 12, Estilo.ALTO_UTIL - VENTANA_Y - 6);
        int y = VENTANA_Y + 10;
        String quien = Guion.resolver(l.quien, nombres);
        if (!quien.isEmpty()) {
            e.texto(18, y, quien, Estilo.LETRA, Estilo.RESALTE);
            y += Estilo.LINEA + 4;
        }
        List<String> partes = Estilo.partir(Guion.resolver(l.texto, nombres), COLUMNAS);
        for (int i = 0; i < partes.size() && i < 5; i++) {
            e.texto(18, y + i * Estilo.LINEA, partes.get(i), Estilo.LETRA, Estilo.TEXTO);
        }
        e.texto(Escena.ANCHO - 16, Estilo.ALTO_UTIL - 26, (linea + 1) + "/" + guion.lineas().size() + "  Aceptar",
                14, Estilo.APAGADO, Escena.Alineacion.DERECHA);
    }
}
