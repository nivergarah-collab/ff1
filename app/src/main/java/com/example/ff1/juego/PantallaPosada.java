package com.example.ff1.juego;

import com.example.ff1.dibujo.Escena;
import com.example.ff1.entrada.Boton;
import com.example.ff1.pueblo.Servicios;

/** Posada: Aceptar paga una noche (vida y magia al máximo, los caídos se levantan); Cancelar sale. */
public final class PantallaPosada implements Pantalla {

    private final Juego juego;
    private final Partida partida;
    private final Servicios.Posada posada;
    private String mensaje;

    PantallaPosada(Juego juego, Partida partida, Servicios.Posada posada) {
        this.juego = juego;
        this.partida = partida;
        this.posada = posada;
        this.mensaje = posada.posadero + ": una noche cuesta " + posada.precio + " de oro.";
    }

    public String mensaje() {
        return mensaje;
    }

    @Override
    public void pulsar(Boton b) {
        if (b == Boton.CANCELAR) {
            juego.cerrar();
        } else if (b == Boton.ACEPTAR) {
            switch (partida.descansar(posada)) {
                case HECHO:
                    mensaje = "Descansáis. Todos despiertan como nuevos.";
                    break;
                case SIN_ORO:
                    mensaje = "No te alcanza el oro.";
                    break;
                default:
                    mensaje = "Nadie necesita descanso.";
            }
        }
    }

    @Override
    public void avanzar(int ms) {
    }

    @Override
    public void dibujar(Escena e) {
        e.texto(8, 6, posada.nombre, Estilo.LETRA, Estilo.TEXTO);
        e.texto(Escena.ANCHO - 8, 6, "Oro " + partida.oro(), Estilo.LETRA, Estilo.RESALTE, Escena.Alineacion.DERECHA);
        Estilo.ventana(e, 4, 30, Escena.ANCHO - 8, 70);
        int y = 40;
        for (String linea : Estilo.partir(mensaje, 30)) {
            e.texto(14, y, linea, Estilo.LETRA, Estilo.TEXTO);
            y += Estilo.LINEA;
        }
        PantallaExploracion.dibujarGrupo(e, partida.grupo(), PantallaExploracion.PANEL_Y);
        e.texto(8, 110, "Aceptar: descansar   Cancelar: salir", 12, Estilo.APAGADO);
    }
}
