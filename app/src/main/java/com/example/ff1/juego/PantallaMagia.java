package com.example.ff1.juego;

import com.example.ff1.combate.Habilidad;
import com.example.ff1.dibujo.Escena;
import com.example.ff1.entrada.Boton;
import com.example.ff1.progresion.Heroe;

import java.util.ArrayList;
import java.util.List;

/**
 * Menú → Magia: se elige un héroe, una de sus habilidades y, si sirve fuera de combate (cura a
 * aliados, sin estados), el aliado que la recibe. Las demás se ven apagadas.
 */
public final class PantallaMagia implements Pantalla {

    enum Fase {
        HEROE, HABILIDAD, OBJETIVO
    }

    private final Juego juego;
    private final Partida partida;
    private Fase fase = Fase.HEROE;
    private int heroe;
    private int habilidad;
    private int objetivo;
    private String mensaje = "";

    PantallaMagia(Juego juego, Partida partida) {
        this.juego = juego;
        this.partida = partida;
    }

    public Fase fase() {
        return fase;
    }

    public String mensaje() {
        return mensaje;
    }

    private Heroe lanzador() {
        return partida.grupo().get(heroe);
    }

    private List<Habilidad> habilidades() {
        List<Habilidad> r = new ArrayList<>();
        for (String id : lanzador().estadisticas().habilidades) {
            r.add(partida.catalogo().habilidad(id));
        }
        return r;
    }

    @Override
    public void pulsar(Boton b) {
        switch (fase) {
            case HEROE:
                if (b == Boton.CANCELAR) {
                    juego.cerrar();
                } else if (mover(b, partida.grupo().size())) {
                    mensaje = "";
                } else if (b == Boton.ACEPTAR) {
                    if (habilidades().isEmpty()) {
                        mensaje = lanzador().nombre() + " no conoce magia.";
                    } else {
                        fase = Fase.HABILIDAD;
                        habilidad = 0;
                        mensaje = "";
                    }
                }
                break;
            case HABILIDAD:
                if (b == Boton.CANCELAR) {
                    fase = Fase.HEROE;
                } else if (b == Boton.ARRIBA || b == Boton.ABAJO) {
                    habilidad = Menu.vuelta(habilidad, b == Boton.ABAJO ? 1 : -1, habilidades().size());
                    mensaje = "";
                } else if (b == Boton.ACEPTAR) {
                    Habilidad h = habilidades().get(habilidad);
                    if (!Partida.sirveFueraDeCombate(h)) {
                        mensaje = h.nombre + " solo se usa en combate.";
                    } else if (h.objetivo == Habilidad.Objetivo.SI_MISMO) {
                        lanzar(h, lanzador());
                    } else {
                        fase = Fase.OBJETIVO;
                        objetivo = heroe;
                        mensaje = "";
                    }
                }
                break;
            default:
                if (b == Boton.CANCELAR) {
                    fase = Fase.HABILIDAD;
                } else if (b == Boton.ARRIBA || b == Boton.ABAJO) {
                    objetivo = Menu.vuelta(objetivo, b == Boton.ABAJO ? 1 : -1, partida.grupo().size());
                } else if (b == Boton.ACEPTAR) {
                    lanzar(habilidades().get(habilidad), partida.grupo().get(objetivo));
                }
        }
    }

    private boolean mover(Boton b, int n) {
        if (b != Boton.ARRIBA && b != Boton.ABAJO) {
            return false;
        }
        heroe = Menu.vuelta(heroe, b == Boton.ABAJO ? 1 : -1, n);
        return true;
    }

    private void lanzar(Habilidad h, Heroe sobre) {
        switch (partida.usarHabilidad(lanzador(), h, sobre)) {
            case USADO:
                mensaje = lanzador().nombre() + " usa " + h.nombre + ".";
                fase = Fase.HABILIDAD;
                break;
            case SIN_MAGIA:
                mensaje = "No alcanza la magia.";
                fase = Fase.HABILIDAD;
                break;
            case SIN_EFECTO:
                mensaje = "No tendría efecto en " + sobre.nombre() + ".";
                break;
            default:
                mensaje = "No se puede usar.";
                fase = Fase.HABILIDAD;
        }
    }

    @Override
    public void avanzar(int ms) {
    }

    @Override
    public void dibujar(Escena e) {
        e.texto(8, 6, "Magia", Estilo.LETRA, Estilo.TEXTO);
        List<Heroe> grupo = partida.grupo();
        Estilo.ventana(e, 4, 30, Escena.ANCHO - 8, 20 + grupo.size() * Estilo.LINEA);
        for (int i = 0; i < grupo.size(); i++) {
            Heroe h = grupo.get(i);
            int yy = 40 + i * Estilo.LINEA;
            boolean activo = fase == Fase.HEROE ? i == heroe : fase == Fase.OBJETIVO ? i == objetivo : i == heroe;
            int color = activo ? Estilo.RESALTE : Estilo.TEXTO;
            if (activo) {
                Estilo.marcador(e, 12, yy);
            }
            e.texto(26, yy, h.nombre(), Estilo.LETRA, color);
            e.texto(150, yy, "PV " + h.vida() + "/" + h.estadisticas().vida, Estilo.LETRA, color);
            e.texto(Escena.ANCHO - 16, yy, "PM " + h.magia() + "/" + h.estadisticas().magia, Estilo.LETRA, color,
                    Escena.Alineacion.DERECHA);
        }
        int y0 = 30 + 20 + grupo.size() * Estilo.LINEA + 10;
        if (fase != Fase.HEROE) {
            List<Habilidad> hs = habilidades();
            Estilo.ventana(e, 4, y0, Escena.ANCHO - 8, 20 + hs.size() * Estilo.LINEA);
            for (int i = 0; i < hs.size(); i++) {
                Habilidad h = hs.get(i);
                int yy = y0 + 10 + i * Estilo.LINEA;
                int color = !Partida.sirveFueraDeCombate(h) ? Estilo.APAGADO
                        : i == habilidad ? Estilo.RESALTE : Estilo.TEXTO;
                if (i == habilidad) {
                    Estilo.marcador(e, 12, yy);
                }
                e.texto(26, yy, h.nombre, Estilo.LETRA, color);
                e.texto(Escena.ANCHO - 16, yy, "PM " + h.coste, Estilo.LETRA, color, Escena.Alineacion.DERECHA);
            }
        }
        e.texto(8, Estilo.ALTO_UTIL - 24, mensaje, Estilo.LETRA, Estilo.RESALTE);
    }
}
