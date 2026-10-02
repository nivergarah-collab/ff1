package com.example.ff1.juego;

import com.example.ff1.dibujo.Escena;
import com.example.ff1.entrada.Boton;
import com.example.ff1.inventario.DefinicionObjeto;
import com.example.ff1.pueblo.Servicios;

import java.util.ArrayList;
import java.util.List;

/**
 * Tienda: izquierda/derecha cambian entre Comprar y Vender, arriba/abajo mueven el cursor,
 * Aceptar compra o vende una unidad y Cancelar sale.
 */
public final class PantallaTienda implements Pantalla {

    private final Juego juego;
    private final Partida partida;
    private final Servicios.Tienda tienda;
    private final List<String> ids = new ArrayList<>();
    private boolean vendiendo;
    private int cursor;
    private String mensaje;

    PantallaTienda(Juego juego, Partida partida, Servicios.Tienda tienda) {
        this.juego = juego;
        this.partida = partida;
        this.tienda = tienda;
        this.mensaje = tienda.vendedor + ": ¿qué necesitáis?";
        refrescar();
    }

    private void refrescar() {
        ids.clear();
        if (vendiendo) {
            ids.addAll(partida.inventario().contenido().keySet());
        } else {
            ids.addAll(tienda.objetos);
        }
        cursor = ids.isEmpty() ? 0 : Math.min(cursor, ids.size() - 1);
    }

    public boolean vendiendo() {
        return vendiendo;
    }

    public int cursor() {
        return cursor;
    }

    public String mensaje() {
        return mensaje;
    }

    @Override
    public void pulsar(Boton b) {
        if (b == Boton.CANCELAR) {
            juego.cerrar();
        } else if (b == Boton.IZQUIERDA || b == Boton.DERECHA) {
            vendiendo = !vendiendo;
            cursor = 0;
            mensaje = "";
            refrescar();
        } else if ((b == Boton.ARRIBA || b == Boton.ABAJO) && !ids.isEmpty()) {
            cursor = Menu.vuelta(cursor, b == Boton.ARRIBA ? -1 : 1, ids.size());
            mensaje = "";
        } else if (b == Boton.ACEPTAR && !ids.isEmpty()) {
            DefinicionObjeto o = partida.objetos().objeto(ids.get(cursor));
            if (vendiendo) {
                switch (partida.vender(o.id)) {
                    case HECHA:
                        mensaje = "Vendes " + o.nombre + ".";
                        break;
                    case NO_VENDIBLE:
                        mensaje = tienda.vendedor + ": eso no lo compro.";
                        break;
                    default:
                        mensaje = "No lo llevas.";
                }
            } else {
                switch (partida.comprar(o.id)) {
                    case HECHA:
                        mensaje = "Compras " + o.nombre + ".";
                        break;
                    case SIN_ORO:
                        mensaje = "No te alcanza el oro.";
                        break;
                    default:
                        mensaje = "No cabe más en la bolsa.";
                }
            }
            refrescar();
        }
    }

    @Override
    public void avanzar(int ms) {
    }

    @Override
    public void dibujar(Escena e) {
        e.texto(8, 6, tienda.nombre, Estilo.LETRA, Estilo.TEXTO);
        e.texto(Escena.ANCHO - 8, 6, "Oro " + partida.oro(), Estilo.LETRA, Estilo.RESALTE, Escena.Alineacion.DERECHA);
        e.texto(Escena.ANCHO / 2, 28, vendiendo ? "< Comprar | [Vender] >" : "< [Comprar] | Vender >", Estilo.LETRA,
                Estilo.TEXTO, Escena.Alineacion.CENTRO);
        Estilo.ventana(e, 4, 50, Escena.ANCHO - 8, 250);
        if (ids.isEmpty()) {
            e.texto(16, 64, vendiendo ? "No llevas nada." : "Sin existencias.", Estilo.LETRA, Estilo.APAGADO);
        }
        for (int i = 0; i < ids.size(); i++) {
            DefinicionObjeto o = partida.objetos().objeto(ids.get(i));
            int yy = 60 + i * Estilo.LINEA;
            int precio = vendiendo ? partida.precioVenta(o.id) : o.precio;
            boolean activo = vendiendo ? precio > 0 : precio <= partida.oro();
            int color = i == cursor ? Estilo.RESALTE : activo ? Estilo.TEXTO : Estilo.APAGADO;
            if (i == cursor) {
                Estilo.marcador(e, 12, yy);
            }
            e.texto(26, yy, o.nombre + (vendiendo ? " x" + partida.inventario().cantidad(o.id) : ""),
                    Estilo.LETRA, color);
            e.texto(Escena.ANCHO - 16, yy, precio + " o", Estilo.LETRA, color, Escena.Alineacion.DERECHA);
        }
        e.texto(8, 310, mensaje, Estilo.LETRA, Estilo.RESALTE);
    }
}
