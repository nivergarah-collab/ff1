package com.example.ff1.juego;

import com.example.ff1.dibujo.Escena;
import com.example.ff1.entrada.Boton;

import java.util.ArrayList;
import java.util.List;

/**
 * Menú del grupo: se abre desde la exploración con Cancelar (encima del mapa, en la pila de
 * {@link Juego}) y lista las secciones. Cada sección es otra {@link Pantalla} que se apila encima.
 * "Guardar" aparece apagada hasta que exista el guardado (H7).
 */
public final class PantallaMenu implements Pantalla {

    /** Secciones del menú, en el orden en que se muestran. */
    enum Seccion {
        OBJETOS("Objetos"), MAGIA("Magia"), EQUIPO("Equipo"), ESTADO("Estado"), FORMACION("Formación"),
        AJUSTES("Ajustes"), GUARDAR("Guardar"), SALIR("Salir al título");

        final String etiqueta;

        Seccion(String etiqueta) {
            this.etiqueta = etiqueta;
        }
    }

    private final Juego juego;
    private final Partida partida;
    private final List<String> etiquetas = new ArrayList<>();
    private final List<Boolean> habilitadas = new ArrayList<>();
    private int cursor;
    private boolean confirmandoSalida;

    PantallaMenu(Juego juego, Partida partida) {
        this.juego = juego;
        this.partida = partida;
        for (Seccion s : Seccion.values()) {
            etiquetas.add(s.etiqueta);
            habilitadas.add(habilitada(s));
        }
        cursor = Menu.mover(-1, 1, habilitadas);
    }

    /** Guardar espera a H7; las demás secciones se habilitan al implementarse. */
    static boolean habilitada(Seccion s) {
        switch (s) {
            case OBJETOS:
            case MAGIA:
            case EQUIPO:
            case SALIR:
                return true;
            default:
                return false;
        }
    }

    public int cursor() {
        return cursor;
    }

    public Seccion seccion() {
        return Seccion.values()[cursor];
    }

    public boolean confirmandoSalida() {
        return confirmandoSalida;
    }

    /** Pantalla de una sección, o {@code null} si no tiene (Guardar, Salir). */
    private Pantalla abrir(Seccion s) {
        switch (s) {
            case OBJETOS:
                return new PantallaObjetos(juego, partida);
            case MAGIA:
                return new PantallaMagia(juego, partida);
            case EQUIPO:
                return new PantallaEquipo(juego, partida);
            default:
                return null;
        }
    }

    @Override
    public void pulsar(Boton b) {
        if (confirmandoSalida) {
            if (b == Boton.ACEPTAR) {
                juego.volverAlTitulo();
            } else if (b == Boton.CANCELAR) {
                confirmandoSalida = false;
            }
            return;
        }
        if (b == Boton.ARRIBA || b == Boton.ABAJO) {
            cursor = Menu.mover(cursor, b == Boton.ABAJO ? 1 : -1, habilitadas);
        } else if (b == Boton.CANCELAR) {
            juego.cerrar();
        } else if (b == Boton.ACEPTAR && habilitadas.get(cursor)) {
            if (seccion() == Seccion.SALIR) {
                confirmandoSalida = true;
            } else {
                Pantalla p = abrir(seccion());
                if (p != null) {
                    juego.apilar(p);
                }
            }
        }
    }

    @Override
    public void avanzar(int ms) {
    }

    @Override
    public void dibujar(Escena e) {
        e.texto(8, 6, "Menú", Estilo.LETRA, Estilo.TEXTO);
        Estilo.ventana(e, 10, 30, 170, etiquetas.size() * Estilo.LINEA + 16);
        Estilo.menu(e, 20, 38, etiquetas, cursor, habilitadas);
        e.texto(Escena.ANCHO - 8, 6, "Oro " + partida.oro(), Estilo.LETRA, Estilo.RESALTE, Escena.Alineacion.DERECHA);
        PantallaExploracion.dibujarGrupo(e, partida.grupo(), PantallaExploracion.PANEL_Y);
        if (confirmandoSalida) {
            Estilo.ventana(e, 30, 200, Escena.ANCHO - 60, 90);
            e.texto(Escena.ANCHO / 2, 214, "¿Salir al título?", Estilo.LETRA, Estilo.RESALTE, Escena.Alineacion.CENTRO);
            e.texto(Escena.ANCHO / 2, 238, "Se pierde lo no guardado.", Estilo.LETRA, Estilo.TEXTO, Escena.Alineacion.CENTRO);
            e.texto(Escena.ANCHO / 2, 264, "Aceptar: sí   Cancelar: no", Estilo.LETRA, Estilo.TEXTO, Escena.Alineacion.CENTRO);
        }
    }
}
