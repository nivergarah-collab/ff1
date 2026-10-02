package com.example.ff1.juego;

import com.example.ff1.dibujo.Escena;
import com.example.ff1.entrada.Boton;

import java.util.Arrays;
import java.util.List;

/** Título: "Nueva partida" y "Continuar" (solo si hay una partida guardada). */
public final class PantallaTitulo implements Pantalla {

    static final List<String> OPCIONES = Arrays.asList("Nueva partida", "Continuar");

    private final Juego juego;
    private final String titulo;
    private final List<Boolean> habilitadas;
    private int cursor;

    PantallaTitulo(Juego juego, String titulo) {
        this.juego = juego;
        this.titulo = titulo;
        this.habilitadas = Arrays.asList(true, juego.hayGuardado());
    }

    public int cursor() {
        return cursor;
    }

    @Override
    public void pulsar(Boton b) {
        if (b == Boton.ARRIBA || b == Boton.ABAJO) {
            cursor = Menu.mover(cursor, b == Boton.ABAJO ? 1 : -1, habilitadas);
        } else if (b == Boton.ACEPTAR && habilitadas.get(cursor)) {
            if (cursor == 0) {
                juego.nuevaPartida();
            } else {
                juego.continuarPartida();
            }
        }
    }

    @Override
    public void avanzar(int ms) {
    }

    @Override
    public void dibujar(Escena e) {
        e.texto(Escena.ANCHO / 2, 110, titulo, Estilo.LETRA_GRANDE, Estilo.RESALTE, Escena.Alineacion.CENTRO);
        Estilo.ventana(e, 90, 250, 180, 70);
        Estilo.menu(e, 104, 262, OPCIONES, cursor, habilitadas);
    }
}
