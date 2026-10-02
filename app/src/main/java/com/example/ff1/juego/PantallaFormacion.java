package com.example.ff1.juego;

import com.example.ff1.dibujo.Escena;
import com.example.ff1.entrada.Boton;
import com.example.ff1.progresion.Heroe;

import java.util.List;

/**
 * Menú → Formación: Aceptar marca un héroe, se mueve el cursor y Aceptar sobre otro los
 * intercambia. El orden del grupo es el que usa el combate. Cancelar suelta la marca o vuelve.
 */
public final class PantallaFormacion implements Pantalla {

    private final Juego juego;
    private final Partida partida;
    private int cursor;
    /** Héroe marcado para mover, o −1. */
    private int marcado = -1;

    PantallaFormacion(Juego juego, Partida partida) {
        this.juego = juego;
        this.partida = partida;
    }

    public int cursor() {
        return cursor;
    }

    public int marcado() {
        return marcado;
    }

    @Override
    public void pulsar(Boton b) {
        int n = partida.grupo().size();
        if (b == Boton.ARRIBA) {
            cursor = Menu.vuelta(cursor, -1, n);
        } else if (b == Boton.ABAJO) {
            cursor = Menu.vuelta(cursor, 1, n);
        } else if (b == Boton.CANCELAR) {
            if (marcado >= 0) {
                marcado = -1;
            } else {
                juego.cerrar();
            }
        } else if (b == Boton.ACEPTAR) {
            if (marcado < 0) {
                marcado = cursor;
            } else {
                if (marcado != cursor) {
                    partida.intercambiarHeroes(marcado, cursor);
                }
                marcado = -1;
            }
        }
    }

    @Override
    public void avanzar(int ms) {
    }

    @Override
    public void dibujar(Escena e) {
        e.texto(8, 6, "Formación", Estilo.LETRA, Estilo.TEXTO);
        List<Heroe> grupo = partida.grupo();
        Estilo.ventana(e, 4, 28, Escena.ANCHO - 8, 16 + grupo.size() * 28);
        for (int i = 0; i < grupo.size(); i++) {
            Heroe h = grupo.get(i);
            int yy = 38 + i * 28;
            int color = i == marcado ? Estilo.VIDA : i == cursor ? Estilo.RESALTE : Estilo.TEXTO;
            if (i == cursor) {
                Estilo.marcador(e, 10, yy);
            }
            e.texto(26, yy, (i + 1) + ". " + h.nombre(), Estilo.LETRA, color);
            e.texto(Escena.ANCHO - 12, yy, h.clase().nombre + "  Nv " + h.nivel(), Estilo.LETRA, color,
                    Escena.Alineacion.DERECHA);
        }
        e.texto(8, Estilo.ALTO_UTIL - 44, marcado >= 0 ? "Elige con quién cambia de sitio." : "Aceptar: marcar un héroe.",
                Estilo.LETRA, Estilo.RESALTE);
        e.texto(8, Estilo.ALTO_UTIL - 24, "El orden vale también en combate.", Estilo.LETRA, Estilo.APAGADO);
    }
}
