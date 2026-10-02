package com.example.ff1.juego;

import com.example.ff1.combate.DefinicionCombatiente;
import com.example.ff1.dibujo.Escena;
import com.example.ff1.entrada.Boton;
import com.example.ff1.inventario.DefinicionObjeto;
import com.example.ff1.progresion.Heroe;

import java.util.ArrayList;
import java.util.List;

/**
 * Menú → Equipo: héroe → ranura → pieza. Al elegir una pieza se ve la comparación de
 * estadísticas (actual → nueva) y Aceptar confirma; la primera opción, si la ranura está
 * ocupada, es quitar la pieza.
 */
public final class PantallaEquipo implements Pantalla {

    enum Fase {
        HEROE, RANURA, PIEZA
    }

    private final Juego juego;
    private final Partida partida;
    private final List<String> ranuras;
    private Fase fase = Fase.HEROE;
    private int heroe;
    private int ranura;
    private int pieza;
    /** Opciones de la fase PIEZA; {@code null} significa "quitar". */
    private final List<DefinicionObjeto> opciones = new ArrayList<>();
    private String mensaje = "";

    PantallaEquipo(Juego juego, Partida partida) {
        this.juego = juego;
        this.partida = partida;
        this.ranuras = partida.objetos().ranuras();
    }

    public Fase fase() {
        return fase;
    }

    public String mensaje() {
        return mensaje;
    }

    private Heroe heroeActual() {
        return partida.grupo().get(heroe);
    }

    private String ranuraActual() {
        return ranuras.get(ranura);
    }

    private void armarOpciones() {
        opciones.clear();
        if (heroeActual().equipo().containsKey(ranuraActual())) {
            opciones.add(null);
        }
        opciones.addAll(partida.piezasPara(heroeActual(), ranuraActual()));
        pieza = Math.min(pieza, Math.max(0, opciones.size() - 1));
    }

    private static int vuelta(int actual, Boton b, int n) {
        return Menu.vuelta(actual, b == Boton.ABAJO ? 1 : -1, n);
    }

    @Override
    public void pulsar(Boton b) {
        boolean vertical = b == Boton.ARRIBA || b == Boton.ABAJO;
        switch (fase) {
            case HEROE:
                if (b == Boton.CANCELAR) {
                    juego.cerrar();
                } else if (vertical) {
                    heroe = vuelta(heroe, b, partida.grupo().size());
                } else if (b == Boton.ACEPTAR) {
                    fase = Fase.RANURA;
                    ranura = 0;
                    mensaje = "";
                }
                break;
            case RANURA:
                if (b == Boton.CANCELAR) {
                    fase = Fase.HEROE;
                } else if (vertical) {
                    ranura = vuelta(ranura, b, ranuras.size());
                } else if (b == Boton.ACEPTAR) {
                    armarOpciones();
                    if (opciones.isEmpty()) {
                        mensaje = "No hay piezas para esta ranura.";
                    } else {
                        fase = Fase.PIEZA;
                        pieza = 0;
                        mensaje = "";
                    }
                }
                break;
            default:
                if (b == Boton.CANCELAR) {
                    fase = Fase.RANURA;
                } else if (vertical) {
                    pieza = vuelta(pieza, b, opciones.size());
                } else if (b == Boton.ACEPTAR) {
                    confirmar();
                }
        }
    }

    private void confirmar() {
        DefinicionObjeto elegida = opciones.get(pieza);
        Partida.Cambio c = elegida == null ? partida.quitarEquipo(heroeActual(), ranuraActual())
                : partida.equipar(heroeActual(), elegida.id);
        mensaje = c == Partida.Cambio.HECHO ? "Listo." : c == Partida.Cambio.SIN_ESPACIO
                ? "No hay espacio en el inventario." : "No se puede.";
        if (c == Partida.Cambio.HECHO) {
            fase = Fase.RANURA;
        }
    }

    @Override
    public void avanzar(int ms) {
    }

    @Override
    public void dibujar(Escena e) {
        Heroe h = heroeActual();
        e.texto(8, 6, "Equipo", Estilo.LETRA, Estilo.TEXTO);
        List<Heroe> grupo = partida.grupo();
        Estilo.ventana(e, 4, 28, Escena.ANCHO - 8, 12 + grupo.size() * 20);
        for (int i = 0; i < grupo.size(); i++) {
            boolean activo = i == heroe;
            int yy = 34 + i * 20;
            if (activo) {
                Estilo.marcador(e, 10, yy);
            }
            e.texto(24, yy, grupo.get(i).nombre(), Estilo.LETRA, activo ? Estilo.RESALTE : Estilo.TEXTO);
            e.texto(Escena.ANCHO - 12, yy, grupo.get(i).clase().nombre, Estilo.LETRA, Estilo.APAGADO,
                    Escena.Alineacion.DERECHA);
        }
        int y0 = 28 + 12 + grupo.size() * 20 + 6;
        Estilo.ventana(e, 4, y0, Escena.ANCHO - 8, 12 + ranuras.size() * 20);
        for (int i = 0; i < ranuras.size(); i++) {
            String r = ranuras.get(i);
            DefinicionObjeto puesta = h.equipo().get(r);
            boolean activo = fase != Fase.HEROE && i == ranura;
            int yy = y0 + 6 + i * 20;
            if (activo) {
                Estilo.marcador(e, 10, yy);
            }
            e.texto(24, yy, mayuscula(r), Estilo.LETRA, activo ? Estilo.RESALTE : Estilo.TEXTO);
            e.texto(Escena.ANCHO - 12, yy, puesta == null ? "—" : puesta.nombre, Estilo.LETRA,
                    puesta == null ? Estilo.APAGADO : Estilo.TEXTO, Escena.Alineacion.DERECHA);
        }
        int y1 = y0 + 12 + ranuras.size() * 20 + 6;
        if (fase == Fase.PIEZA) {
            Estilo.ventana(e, 4, y1, Escena.ANCHO - 8, 12 + opciones.size() * 20);
            for (int i = 0; i < opciones.size(); i++) {
                DefinicionObjeto o = opciones.get(i);
                int yy = y1 + 6 + i * 20;
                if (i == pieza) {
                    Estilo.marcador(e, 10, yy);
                }
                e.texto(24, yy, o == null ? "(Quitar)" : o.nombre, Estilo.LETRA,
                        i == pieza ? Estilo.RESALTE : Estilo.TEXTO);
            }
            dibujarComparacion(e, y1 + 12 + opciones.size() * 20 + 6);
        }
        e.texto(8, Estilo.ALTO_UTIL - 24, mensaje, Estilo.LETRA, Estilo.RESALTE);
    }

    private void dibujarComparacion(Escena e, int y) {
        Heroe h = heroeActual();
        DefinicionObjeto o = opciones.get(pieza);
        DefinicionCombatiente antes = h.estadisticas();
        DefinicionCombatiente despues = h.estadisticasCon(ranuraActual(), o);
        int[] a = Comparacion.valores(antes);
        int[] d = Comparacion.diferencias(antes, despues);
        Estilo.ventana(e, 4, y, Escena.ANCHO - 8, 12 + Comparacion.NOMBRES.length * 16);
        for (int i = 0; i < a.length; i++) {
            int yy = y + 6 + i * 16;
            int color = d[i] > 0 ? Estilo.VIDA : d[i] < 0 ? Estilo.VIDA_BAJA : Estilo.APAGADO;
            e.texto(14, yy, Comparacion.NOMBRES[i], 14, Estilo.TEXTO);
            e.texto(170, yy, String.valueOf(a[i]), 14, Estilo.TEXTO, Escena.Alineacion.DERECHA);
            e.texto(200, yy, "→ " + (a[i] + d[i]), 14, color);
            e.texto(Escena.ANCHO - 14, yy, Comparacion.signo(d[i]), 14, color, Escena.Alineacion.DERECHA);
        }
    }

    private static String mayuscula(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
