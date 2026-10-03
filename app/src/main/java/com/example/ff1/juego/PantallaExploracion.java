package com.example.ff1.juego;

import com.example.ff1.dibujo.Escena;
import com.example.ff1.entrada.Boton;
import com.example.ff1.motor.config.Registro;
import com.example.ff1.mundo.Direccion;
import com.example.ff1.mundo.Explorador;
import com.example.ff1.mundo.Mapa;
import com.example.ff1.progresion.Heroe;
import com.example.ff1.pueblo.Servicios;

import java.util.List;

/**
 * Exploración: dibuja el mapa por casillas alrededor del grupo, lo mueve con la cruceta (con
 * colisiones) y abre el combate cuando salta un encuentro aleatorio. Abajo, el estado del grupo.
 */
public final class PantallaExploracion implements Pantalla {

    static final int LADO = 22;
    static final int MAPA_Y = 30;
    static final int COLUMNAS = Escena.ANCHO / LADO;
    static final int FILAS = 13;
    static final int PANEL_Y = MAPA_Y + FILAS * LADO + 4;
    private static final int PASABLE = 0xFF5C7F4A;
    private static final int BLOQUEO = 0xFF5A5A5A;

    private final Juego juego;
    private final Partida partida;
    private final Registro<AccionLugar> lugares;
    private String mensaje = "";

    PantallaExploracion(Juego juego, Partida partida) {
        this.juego = juego;
        this.partida = partida;
        this.lugares = lugares();
    }

    @Override
    public void pulsar(Boton b) {
        if (b == Boton.CANCELAR) {
            juego.apilar(new PantallaMenu(juego, partida));
            return;
        }
        if (b == Boton.ACEPTAR) {
            hablar();
            return;
        }
        Direccion d = direccion(b);
        if (d == null) {
            return;
        }
        mensaje = "";
        Explorador ex = partida.explorador();
        int antesX = ex.x();
        int antesY = ex.y();
        List<String> enemigos = partida.encuentros().mover(ex, d);
        if (enemigos != null) {
            juego.irA(new PantallaCombate(juego, partida, partida.empezarCombate(enemigos), this));
        } else if (partida.cruzarSalida() == Partida.Cruce.CERRADA) {
            ex.colocar(ex.mapa(), antesX, antesY);
            mensaje = "Está cerrado: hace falta una llave.";
        }
    }

    /** Aceptar frente a un lugar abre lo que registró su tipo (ver {@link #lugares()}). */
    private void hablar() {
        Mapa.Lugar l = partida.lugarDelante();
        if (l != null) {
            lugares.obtener(l.tipo).abrir(partida.servicios(), l.ref);
        }
    }

    /** Qué se abre al hablar con un lugar, según su tipo. */
    interface AccionLugar {
        void abrir(Servicios sv, String ref);
    }

    /** Un tipo de lugar nuevo se registra aquí (y en {@code Mapa.TIPOS_LUGAR} y {@code Servicios}). */
    private Registro<AccionLugar> lugares() {
        return new Registro<AccionLugar>("tipo de lugar")
                .registrar(Mapa.LUGAR_TIENDA, (sv, ref) -> juego.apilar(new PantallaTienda(juego, partida, sv.tienda(ref))))
                .registrar(Mapa.LUGAR_POSADA, (sv, ref) -> juego.apilar(new PantallaPosada(juego, partida, sv.posada(ref))))
                .registrar(Mapa.LUGAR_JEFE, (sv, ref) -> encararJefe(sv.jefe(ref)))
                .registrar(Mapa.LUGAR_VECINO, (sv, ref) -> juego.irA(new PantallaEscena(juego,
                        partida.escena(sv.vecino(ref).escena), partida.nombresPorClase(), this)));
    }

    /** Escena previa → combate sin huida → escena final → regreso al sitio indicado. */
    private void encararJefe(final Servicios.Jefe jefe) {
        if (partida.jefeDerrotado(jefe.id)) {
            mensaje = "Ya no queda nada aquí.";
            return;
        }
        final Pantalla mapa = this;
        PantallaCombate combate = new PantallaCombate(juego, partida,
                partida.empezarCombate(jefe.enemigos, false), mapa);
        combate.trasVictoria(new Runnable() {
            @Override
            public void run() {
                partida.derrotarJefe(jefe);
                juego.irA(new PantallaEscena(juego, partida.escena(jefe.escenaFinal), partida.nombresPorClase(), mapa));
            }
        });
        juego.irA(new PantallaEscena(juego, partida.escena(jefe.escenaPrevia), partida.nombresPorClase(), combate));
    }

    static Direccion direccion(Boton b) {
        switch (b) {
            case ARRIBA:
                return Direccion.ARRIBA;
            case ABAJO:
                return Direccion.ABAJO;
            case IZQUIERDA:
                return Direccion.IZQUIERDA;
            case DERECHA:
                return Direccion.DERECHA;
            default:
                return null;
        }
    }

    @Override
    public void avanzar(int ms) {
    }

    public String mensaje() {
        return mensaje;
    }

    /** Primera columna (o fila) visible: centra al grupo sin salirse del mapa. */
    static int origen(int posicion, int tamanoMapa, int visibles) {
        return Math.max(0, Math.min(posicion - visibles / 2, tamanoMapa - visibles));
    }

    @Override
    public void dibujar(Escena e) {
        Explorador ex = partida.explorador();
        Mapa m = ex.mapa();
        e.texto(8, 6, m.id, Estilo.LETRA, Estilo.TEXTO);
        e.texto(Escena.ANCHO - 8, 6, "Oro " + partida.oro(), Estilo.LETRA, Estilo.RESALTE, Escena.Alineacion.DERECHA);

        int cols = Math.min(COLUMNAS, m.ancho);
        int filas = Math.min(FILAS, m.alto);
        int ox = origen(ex.x(), m.ancho, cols);
        int oy = origen(ex.y(), m.alto, filas);
        int px0 = (Escena.ANCHO - cols * LADO) / 2;
        int py0 = MAPA_Y + (FILAS - filas) * LADO / 2;
        for (int fy = 0; fy < filas; fy++) {
            for (int fx = 0; fx < cols; fx++) {
                Mapa.Casilla c = m.casilla(ox + fx, oy + fy);
                int color = Estilo.color(c.color, c.pasable ? PASABLE : BLOQUEO);
                e.casilla(px0 + fx * LADO, py0 + fy * LADO, LADO, color,
                        c.color == null ? String.valueOf(c.simbolo) : null);
            }
        }
        int jx = px0 + (ex.x() - ox) * LADO;
        int jy = py0 + (ex.y() - oy) * LADO;
        e.rectangulo(jx + 4, jy + 4, LADO - 8, LADO - 8, Estilo.RESALTE);
        e.texto(jx + LADO / 2, jy + 5, flecha(ex.mirando()), 12, Estilo.FONDO, Escena.Alineacion.CENTRO);

        dibujarGrupo(e, partida.grupo(), PANEL_Y);
        if (!mensaje.isEmpty()) {
            e.texto(Escena.ANCHO / 2, 6, mensaje, Estilo.LETRA, Estilo.RESALTE, Escena.Alineacion.CENTRO);
        }
        Mapa.Lugar l = partida.lugarDelante();
        if (mensaje.isEmpty() && l != null) {
            String nombre = partida.servicios().servicio(l).nombre;
            e.texto(Escena.ANCHO / 2, 6, nombre + " (Aceptar)", Estilo.LETRA, Estilo.RESALTE, Escena.Alineacion.CENTRO);
        }
    }

    static void dibujarGrupo(Escena e, List<Heroe> grupo, int y) {
        Estilo.ventana(e, 4, y, Escena.ANCHO - 8, Estilo.ALTO_UTIL - y - 4);
        for (int i = 0; i < grupo.size(); i++) {
            Heroe h = grupo.get(i);
            int yy = y + 6 + i * 18;
            int color = h.vida() > 0 ? Estilo.TEXTO : Estilo.APAGADO;
            e.texto(12, yy, h.nombre(), 14, color);
            e.texto(110, yy, "Nv" + h.nivel(), 14, color);
            e.texto(160, yy, "PV " + h.vida() + "/" + h.estadisticas().vida, 14, color);
            e.texto(270, yy, "PM " + h.magia(), 14, color);
        }
    }

    private static String flecha(Direccion d) {
        switch (d) {
            case ARRIBA:
                return "^";
            case ABAJO:
                return "v";
            case IZQUIERDA:
                return "<";
            default:
                return ">";
        }
    }
}
