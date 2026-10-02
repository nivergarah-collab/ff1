package com.example.ff1.juego;

import com.example.ff1.dibujo.Escena;
import com.example.ff1.entrada.Boton;

/**
 * Una pantalla del juego (título, exploración, combate...). Recibe pulsaciones y el paso del
 * tiempo, y se dibuja en una {@link Escena} por encima de la franja de controles. Para cambiar de
 * pantalla llama a {@link Juego#irA}. Una pantalla nueva no exige tocar las demás.
 */
public interface Pantalla {

    void pulsar(Boton boton);

    /** Pasó {@code ms} de tiempo de juego (un paso fijo del bucle). */
    void avanzar(int ms);

    void dibujar(Escena escena);
}
