package com.example.ff1.juego;

import com.example.ff1.dibujo.Escena;
import com.example.ff1.entrada.Boton;
import com.example.ff1.inventario.DefinicionObjeto;
import com.example.ff1.progresion.Heroe;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Menú → Objetos: lista lo que lleva el grupo y permite usar los consumibles sobre un héroe.
 * Arriba/abajo mueven el cursor, Aceptar elige el objeto y luego el héroe, Cancelar retrocede.
 */
public final class PantallaObjetos implements Pantalla {

    private final Juego juego;
    private final Partida partida;
    private final List<String> ids = new ArrayList<>();
    private int cursor;
    private boolean eligiendoHeroe;
    private int heroe;
    private String mensaje = "";

    PantallaObjetos(Juego juego, Partida partida) {
        this.juego = juego;
        this.partida = partida;
        refrescar();
    }

    private void refrescar() {
        ids.clear();
        for (Map.Entry<String, Integer> e : partida.inventario().contenido().entrySet()) {
            ids.add(e.getKey());
        }
        cursor = ids.isEmpty() ? 0 : Math.min(cursor, ids.size() - 1);
    }

    public int cursor() {
        return cursor;
    }

    public boolean eligiendoHeroe() {
        return eligiendoHeroe;
    }

    public int heroe() {
        return heroe;
    }

    public String mensaje() {
        return mensaje;
    }

    private DefinicionObjeto actual() {
        return ids.isEmpty() ? null : partida.objetos().objeto(ids.get(cursor));
    }

    private boolean usable(DefinicionObjeto o) {
        return o.categoria == DefinicionObjeto.Categoria.CONSUMIBLE && o.efecto != null
                && o.efecto.objetivo != com.example.ff1.combate.Habilidad.Objetivo.ENEMIGO;
    }

    @Override
    public void pulsar(Boton b) {
        if (eligiendoHeroe) {
            elegirHeroe(b);
            return;
        }
        if (b == Boton.CANCELAR) {
            juego.cerrar();
        } else if (b == Boton.ARRIBA && !ids.isEmpty()) {
            cursor = Menu.vuelta(cursor, -1, ids.size());
            mensaje = "";
        } else if (b == Boton.ABAJO && !ids.isEmpty()) {
            cursor = Menu.vuelta(cursor, 1, ids.size());
            mensaje = "";
        } else if (b == Boton.ACEPTAR && actual() != null) {
            if (usable(actual())) {
                eligiendoHeroe = true;
                heroe = 0;
                mensaje = "";
            } else {
                mensaje = "Este objeto no se usa aquí.";
            }
        }
    }

    private void elegirHeroe(Boton b) {
        int n = partida.grupo().size();
        if (b == Boton.CANCELAR) {
            eligiendoHeroe = false;
        } else if (b == Boton.ARRIBA) {
            heroe = Menu.vuelta(heroe, -1, n);
        } else if (b == Boton.ABAJO) {
            heroe = Menu.vuelta(heroe, 1, n);
        } else if (b == Boton.ACEPTAR) {
            DefinicionObjeto o = actual();
            Heroe h = partida.grupo().get(heroe);
            Partida.Uso uso = partida.usarObjeto(o.id, h);
            switch (uso) {
                case USADO:
                    mensaje = h.nombre() + " usa " + o.nombre + ".";
                    break;
                case SIN_EFECTO:
                    mensaje = "No tendría efecto en " + h.nombre() + ".";
                    break;
                default:
                    mensaje = "No se puede usar.";
            }
            refrescar();
            if (ids.isEmpty() || partida.inventario().cantidad(o.id) == 0) {
                eligiendoHeroe = false;
            }
        }
    }

    @Override
    public void avanzar(int ms) {
    }

    @Override
    public void dibujar(Escena e) {
        e.texto(8, 6, "Objetos", Estilo.LETRA, Estilo.TEXTO);
        int alto = 220;
        Estilo.ventana(e, 4, 30, Escena.ANCHO - 8, alto);
        if (ids.isEmpty()) {
            e.texto(16, 44, "No llevas objetos.", Estilo.LETRA, Estilo.APAGADO);
        }
        for (int i = 0; i < ids.size(); i++) {
            DefinicionObjeto o = partida.objetos().objeto(ids.get(i));
            int yy = 40 + i * Estilo.LINEA;
            int color = i == cursor ? Estilo.RESALTE : usable(o) ? Estilo.TEXTO : Estilo.APAGADO;
            if (i == cursor) {
                Estilo.marcador(e, 12, yy);
            }
            e.texto(26, yy, o.nombre, Estilo.LETRA, color);
            e.texto(Escena.ANCHO - 16, yy, "x" + partida.inventario().cantidad(o.id), Estilo.LETRA, color,
                    Escena.Alineacion.DERECHA);
        }
        if (eligiendoHeroe) {
            int y0 = 262;
            Estilo.ventana(e, 4, y0, Escena.ANCHO - 8, 20 + partida.grupo().size() * Estilo.LINEA);
            for (int i = 0; i < partida.grupo().size(); i++) {
                Heroe h = partida.grupo().get(i);
                int yy = y0 + 10 + i * Estilo.LINEA;
                int color = i == heroe ? Estilo.RESALTE : Estilo.TEXTO;
                if (i == heroe) {
                    Estilo.marcador(e, 12, yy);
                }
                e.texto(26, yy, h.nombre(), Estilo.LETRA, color);
                e.texto(Escena.ANCHO - 16, yy, "PV " + h.vida() + "/" + h.estadisticas().vida, Estilo.LETRA, color,
                        Escena.Alineacion.DERECHA);
            }
        }
        e.texto(8, Estilo.ALTO_UTIL - 24, mensaje, Estilo.LETRA, Estilo.RESALTE);
    }
}
