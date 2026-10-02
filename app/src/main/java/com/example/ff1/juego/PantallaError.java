package com.example.ff1.juego;

import com.example.ff1.dibujo.Escena;
import com.example.ff1.entrada.Boton;

/** Muestra un error de datos sin cerrar el juego; "Aceptar" vuelve al título. */
public final class PantallaError implements Pantalla {

    private final Juego juego;
    private final String mensaje;

    PantallaError(Juego juego, String mensaje) {
        this.juego = juego;
        this.mensaje = mensaje == null ? "error desconocido" : mensaje;
    }

    public String mensaje() {
        return mensaje;
    }

    @Override
    public void pulsar(Boton b) {
        if (b == Boton.ACEPTAR || b == Boton.CANCELAR) {
            juego.volverAlTitulo();
        }
    }

    @Override
    public void avanzar(int ms) {
    }

    @Override
    public void dibujar(Escena e) {
        Estilo.ventana(e, 10, 40, Escena.ANCHO - 20, 360);
        e.texto(20, 52, "Los datos del juego no son válidos:", Estilo.LETRA, Estilo.VIDA_BAJA);
        int y = 82;
        for (String linea : Estilo.partir(mensaje, 36)) {
            e.texto(20, y, linea, Estilo.LETRA, Estilo.TEXTO);
            y += Estilo.LINEA;
        }
    }
}
