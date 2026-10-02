package com.example.ff1.juego;

import com.example.ff1.combate.DefinicionCombatiente;
import com.example.ff1.dibujo.Escena;
import com.example.ff1.entrada.Boton;
import com.example.ff1.inventario.DefinicionObjeto;
import com.example.ff1.progresion.Heroe;

import java.util.Map;

/**
 * Menú → Estado: ficha de un héroe (nivel, experiencia, vida, magia, estadísticas con equipo y
 * condición). Arriba/abajo cambia de héroe; Cancelar vuelve al menú. Fuera de combate los
 * estados alterados no se conservan, así que la condición es "en pie" o "caído".
 */
public final class PantallaEstado implements Pantalla {

    private final Juego juego;
    private final Partida partida;
    private int heroe;

    PantallaEstado(Juego juego, Partida partida) {
        this.juego = juego;
        this.partida = partida;
    }

    public int heroe() {
        return heroe;
    }

    @Override
    public void pulsar(Boton b) {
        int n = partida.grupo().size();
        if (b == Boton.CANCELAR) {
            juego.cerrar();
        } else if (b == Boton.ARRIBA || b == Boton.IZQUIERDA) {
            heroe = Menu.vuelta(heroe, -1, n);
        } else if (b == Boton.ABAJO || b == Boton.DERECHA) {
            heroe = Menu.vuelta(heroe, 1, n);
        }
    }

    @Override
    public void avanzar(int ms) {
    }

    @Override
    public void dibujar(Escena e) {
        Heroe h = partida.grupo().get(heroe);
        DefinicionCombatiente s = h.estadisticas();
        e.texto(8, 6, "Estado  " + (heroe + 1) + "/" + partida.grupo().size(), Estilo.LETRA, Estilo.TEXTO);
        Estilo.ventana(e, 4, 28, Escena.ANCHO - 8, 400);
        e.texto(16, 40, h.nombre(), Estilo.LETRA_GRANDE, Estilo.RESALTE);
        e.texto(16, 74, s.nombre + "  Nv " + h.nivel(), Estilo.LETRA, Estilo.TEXTO);
        e.texto(Escena.ANCHO - 16, 74, h.vida() > 0 ? "En pie" : "Caído", Estilo.LETRA,
                h.vida() > 0 ? Estilo.VIDA : Estilo.VIDA_BAJA, Escena.Alineacion.DERECHA);
        e.texto(16, 100, "Experiencia " + h.experiencia(), Estilo.LETRA, Estilo.TEXTO);
        e.texto(Escena.ANCHO - 16, 100, h.experienciaFaltante() > 0
                ? "Faltan " + h.experienciaFaltante() : "Nivel máximo", Estilo.LETRA, Estilo.APAGADO,
                Escena.Alineacion.DERECHA);
        e.texto(16, 128, "PV " + h.vida() + "/" + s.vida, Estilo.LETRA, Estilo.TEXTO);
        Estilo.barra(e, 150, 130, 190, 10, h.vida(), s.vida, Estilo.VIDA);
        e.texto(16, 150, "PM " + h.magia() + "/" + s.magia, Estilo.LETRA, Estilo.TEXTO);
        Estilo.barra(e, 150, 152, 190, 10, h.magia(), s.magia, Estilo.MAGIA);
        int[] v = Comparacion.valores(s);
        for (int i = 2; i < v.length; i++) {
            int yy = 184 + (i - 2) * 20;
            e.texto(16, yy, Comparacion.NOMBRES[i], Estilo.LETRA, Estilo.TEXTO);
            e.texto(Escena.ANCHO - 16, yy, String.valueOf(v[i]), Estilo.LETRA, Estilo.TEXTO,
                    Escena.Alineacion.DERECHA);
        }
        e.texto(16, 276, "Equipo", Estilo.LETRA, Estilo.RESALTE);
        int yy = 298;
        for (String ranura : partida.objetos().ranuras()) {
            DefinicionObjeto o = h.equipo().get(ranura);
            e.texto(16, yy, ranura.substring(0, 1).toUpperCase() + ranura.substring(1), Estilo.LETRA, Estilo.TEXTO);
            e.texto(Escena.ANCHO - 16, yy, o == null ? "—" : o.nombre, Estilo.LETRA,
                    o == null ? Estilo.APAGADO : Estilo.TEXTO, Escena.Alineacion.DERECHA);
            yy += 20;
        }
        e.texto(16, 372, "Habilidades", Estilo.LETRA, Estilo.RESALTE);
        StringBuilder hs = new StringBuilder();
        for (String id : s.habilidades) {
            hs.append(hs.length() > 0 ? ", " : "").append(partida.catalogo().habilidad(id).nombre);
        }
        e.texto(16, 394, hs.length() == 0 ? "—" : hs.toString(), Estilo.LETRA, Estilo.TEXTO);
    }
}
