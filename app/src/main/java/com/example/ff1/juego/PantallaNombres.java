package com.example.ff1.juego;

import com.example.ff1.dibujo.Escena;
import com.example.ff1.entrada.Boton;
import com.example.ff1.progresion.Heroe;

/**
 * Partida nueva: el jugador pone nombre a cada héroe del grupo, uno tras otro, con la rejilla de
 * {@link EditorNombre}. El nombre por defecto es el del paquete ({@code inicio.json}) y el largo
 * máximo, {@link ConfiguracionJuego#LARGO_NOMBRE}. Cancelar borra la última letra. Al terminar
 * sigue con {@link Juego#empezarRecorrido()}.
 */
public final class PantallaNombres implements Pantalla {

    private static final int X_REJILLA = 45;
    private static final int Y_REJILLA = 200;
    private static final int PASO = 30;

    private final Juego juego;
    private final Partida partida;
    private final int largoMaximo;
    private int indice;
    private EditorNombre editor;

    PantallaNombres(Juego juego, Partida partida) {
        this.juego = juego;
        this.partida = partida;
        this.largoMaximo = partida.config().actual().entero(ConfiguracionJuego.LARGO_NOMBRE);
        this.editor = new EditorNombre(heroe().nombre(), largoMaximo);
    }

    public int largoMaximo() {
        return largoMaximo;
    }

    private Heroe heroe() {
        return partida.grupo().get(indice);
    }

    @Override
    public void pulsar(Boton b) {
        if (b == Boton.CANCELAR) {
            editor.borrar();
        } else if (b == Boton.ACEPTAR) {
            if (editor.aceptar()) {
                heroe().renombrar(editor.texto());
                indice++;
                if (indice < partida.grupo().size()) {
                    editor = new EditorNombre(heroe().nombre(), largoMaximo);
                } else {
                    juego.empezarRecorrido();
                }
            }
        } else {
            editor.mover(b);
        }
    }

    @Override
    public void avanzar(int ms) {
    }

    @Override
    public void dibujar(Escena e) {
        int n = partida.grupo().size();
        e.texto(Escena.ANCHO / 2, 40, "Nombre del héroe " + (indice + 1) + " de " + n,
                Estilo.LETRA, Estilo.TEXTO, Escena.Alineacion.CENTRO);
        e.texto(Escena.ANCHO / 2, 70, heroe().clase().nombre, Estilo.LETRA, Estilo.APAGADO, Escena.Alineacion.CENTRO);
        Estilo.ventana(e, 70, 105, 220, 50);
        e.texto(Escena.ANCHO / 2, 116, editor.texto(), Estilo.LETRA_GRANDE, Estilo.RESALTE, Escena.Alineacion.CENTRO);
        e.texto(284, 136, editor.texto().length() + "/" + largoMaximo, Estilo.LETRA, Estilo.APAGADO,
                Escena.Alineacion.DERECHA);

        Estilo.ventana(e, X_REJILLA - 20, Y_REJILLA - 12, 290, EditorNombre.filas() * PASO + 14);
        for (int f = 0; f < EditorNombre.filas(); f++) {
            for (int c = 0; c < EditorNombre.columnas(f); c++) {
                boolean letra = f < EditorNombre.filas() - 1;
                int x = letra ? X_REJILLA + c * PASO : X_REJILLA + c * 150;
                int y = Y_REJILLA + f * PASO;
                boolean bajoCursor = f == editor.fila() && c == editor.columna();
                if (bajoCursor) {
                    Estilo.marcador(e, x - 12, y);
                }
                e.texto(x, y, EditorNombre.celda(f, c), Estilo.LETRA, bajoCursor ? Estilo.RESALTE : Estilo.TEXTO);
            }
        }
    }
}
