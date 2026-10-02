package com.example.ff1.juego;

import com.example.ff1.dibujo.Escena;
import com.example.ff1.entrada.Boton;
import com.example.ff1.entrada.Controles;
import com.example.ff1.entrada.Entrada;
import com.example.ff1.motor.datos.ErrorDeDatos;
import com.example.ff1.motor.fuentes.Azar;
import com.example.ff1.motor.fuentes.FuenteContenido;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Máquina de pantallas en Java puro: guarda la pantalla actual, le pasa las pulsaciones y el
 * tiempo, y la dibuja junto con los controles. La capa Android solo crea el juego, le entrega los
 * toques ({@link #entrada()}) y el tiempo ({@link #avanzar}) y pinta la escena.
 */
public final class Juego {

    /** Duración de un paso lógico del bucle. */
    public static final int MS_POR_PASO = 50;

    private final FuenteContenido fuente;
    private final Azar azar;
    private final Controles controles = new Controles();
    private final Entrada entrada = new Entrada(250, 140);
    private final Deque<Pantalla> pila = new ArrayDeque<>();
    private Pantalla pantalla;
    private Partida partida;
    private boolean rapido;

    public Juego(FuenteContenido fuente, Azar azar) {
        this.fuente = fuente;
        this.azar = azar;
        this.pantalla = new PantallaTitulo(this, titulo(fuente));
    }

    public Controles controles() {
        return controles;
    }

    public Entrada entrada() {
        return entrada;
    }

    public Pantalla pantalla() {
        return pantalla;
    }

    public Partida partida() {
        return partida;
    }

    /** Interruptor de avance rápido (vale para todos los combates). */
    public boolean rapido() {
        return rapido;
    }

    /** Cambia de pantalla sin guardar la actual. */
    public void irA(Pantalla nueva) {
        pantalla = nueva;
    }

    /** Abre una pantalla encima de la actual, que queda guardada en la pila hasta {@link #cerrar()}. */
    public void apilar(Pantalla nueva) {
        pila.push(pantalla);
        pantalla = nueva;
    }

    /** Cierra la pantalla actual y vuelve a la que estaba debajo; sin nada debajo, no hace nada. */
    public void cerrar() {
        if (!pila.isEmpty()) {
            pantalla = pila.pop();
        }
    }

    /** Pantallas guardadas debajo de la actual. */
    public int profundidad() {
        return pila.size();
    }

    /** Una pulsación lógica. El avance rápido se atiende aquí, en cualquier pantalla. */
    public void pulsar(Boton b) {
        if (b == Boton.RAPIDO) {
            rapido = !rapido;
        } else if (b != null) {
            pantalla.pulsar(b);
        }
    }

    /** Un paso de tiempo: entrega las pulsaciones pendientes y avanza la pantalla actual. */
    public void avanzar(int ms) {
        entrada.avanzar(ms);
        for (Boton b = entrada.siguiente(); b != null; b = entrada.siguiente()) {
            pulsar(b);
        }
        pantalla.avanzar(ms);
    }

    public void dibujar(Escena escena) {
        escena.limpiar();
        escena.rectangulo(0, 0, Escena.ANCHO, Estilo.ALTO_UTIL, Estilo.FONDO);
        pantalla.dibujar(escena);
        controles.dibujar(escena, rapido, entrada.mantenido());
    }

    /** Empieza una partida nueva; si el paquete de contenido no es válido, muestra el error. */
    void nuevaPartida() {
        try {
            partida = Partida.nueva(fuente, azar);
            pila.clear();
            irA(new PantallaExploracion(this, partida));
        } catch (ErrorDeDatos e) {
            irA(new PantallaError(this, e.getMessage()));
        }
    }

    void volverAlTitulo() {
        partida = null;
        pila.clear();
        irA(new PantallaTitulo(this, titulo(fuente)));
    }

    private static String titulo(FuenteContenido fuente) {
        try {
            return fuente.cargar(Partida.TIPO_INICIO, Partida.TIPO_INICIO, Partida.VERSION_INICIO).textoO("titulo", "");
        } catch (ErrorDeDatos e) {
            return "";
        }
    }
}
