package com.example.ff1.juego;

import com.example.ff1.dibujo.Escena;
import com.example.ff1.entrada.Boton;

import java.util.Arrays;
import java.util.List;

/** Título: "Nueva partida" y "Continuar" (deshabilitado hasta que exista el guardado, H7). */
public final class PantallaTitulo implements Pantalla {

    static final List<String> OPCIONES = Arrays.asList("Nueva partida", "Continuar");
    private static final List<Boolean> HABILITADAS = Arrays.asList(true, false);

    private final Juego juego;
    private final String titulo;
    private int cursor;

    PantallaTitulo(Juego juego, String titulo) {
        this.juego = juego;
        this.titulo = titulo;
    }

    public int cursor() {
        return cursor;
    }

    @Override
    public void pulsar(Boton b) {
        if (b == Boton.ARRIBA || b == Boton.ABAJO) {
            cursor = Menu.mover(cursor, b == Boton.ABAJO ? 1 : -1, HABILITADAS);
        } else if (b == Boton.ACEPTAR && HABILITADAS.get(cursor) && cursor == 0) {
            juego.nuevaPartida();
        }
    }

    @Override
    public void avanzar(int ms) {
    }

    @Override
    public void dibujar(Escena e) {
        e.texto(Escena.ANCHO / 2, 110, titulo, Estilo.LETRA_GRANDE, Estilo.RESALTE, Escena.Alineacion.CENTRO);
        Estilo.ventana(e, 90, 250, 180, 70);
        Estilo.menu(e, 104, 262, OPCIONES, cursor, HABILITADAS);
    }
}
