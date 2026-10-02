package com.example.ff1.juego;

import com.example.ff1.combate.ConfiguracionCombate;
import com.example.ff1.dibujo.Escena;
import com.example.ff1.entrada.Boton;
import com.example.ff1.motor.config.ProveedorConfiguracion;
import com.example.ff1.motor.datos.ErrorDeDatos;

/**
 * Menú → Ajustes: velocidad del combate, tiempo de los mensajes y avance rápido al empezar.
 * Izquierda/derecha cambian el valor y se aplican al instante en la configuración vigente
 * (en caliente), validados por su esquema: fuera de rango no cambia nada.
 */
public final class PantallaAjustes implements Pantalla {

    /** Un ajuste: parámetro de la configuración, etiqueta y cuánto cambia por pulsación. */
    enum Ajuste {
        COMBATE("Velocidad del combate", ConfiguracionCombate.TICKS_POR_PASO, 1),
        MENSAJES("Tiempo de mensajes (ms)", ConfiguracionJuego.MS_MENSAJE, 100),
        RAPIDO("Avance rápido al empezar", ConfiguracionJuego.RAPIDO_AL_EMPEZAR, 1);

        final String etiqueta;
        final String parametro;
        final int paso;

        Ajuste(String etiqueta, String parametro, int paso) {
            this.etiqueta = etiqueta;
            this.parametro = parametro;
            this.paso = paso;
        }
    }

    private final Juego juego;
    private final ProveedorConfiguracion config;
    private int cursor;

    PantallaAjustes(Juego juego, Partida partida) {
        this.juego = juego;
        this.config = partida.config();
    }

    public int cursor() {
        return cursor;
    }

    int valor(Ajuste a) {
        return config.actual().entero(a.parametro);
    }

    @Override
    public void pulsar(Boton b) {
        int n = Ajuste.values().length;
        if (b == Boton.CANCELAR) {
            juego.cerrar();
        } else if (b == Boton.ARRIBA) {
            cursor = Math.floorMod(cursor - 1, n);
        } else if (b == Boton.ABAJO) {
            cursor = Math.floorMod(cursor + 1, n);
        } else if (b == Boton.IZQUIERDA || b == Boton.DERECHA || b == Boton.ACEPTAR) {
            Ajuste a = Ajuste.values()[cursor];
            int paso = b == Boton.IZQUIERDA ? -a.paso : a.paso;
            if (a == Ajuste.RAPIDO) {
                paso = valor(a) == 1 ? -1 : 1; // interruptor
            }
            try {
                config.reemplazar(config.actual().con(a.parametro, valor(a) + paso));
            } catch (ErrorDeDatos fueraDeRango) {
                // en el límite: se queda como está
            }
        }
    }

    @Override
    public void avanzar(int ms) {
    }

    @Override
    public void dibujar(Escena e) {
        e.texto(8, 6, "Ajustes", Estilo.LETRA, Estilo.TEXTO);
        Estilo.ventana(e, 4, 28, Escena.ANCHO - 8, 16 + Ajuste.values().length * 40);
        for (int i = 0; i < Ajuste.values().length; i++) {
            Ajuste a = Ajuste.values()[i];
            int yy = 38 + i * 40;
            int color = i == cursor ? Estilo.RESALTE : Estilo.TEXTO;
            if (i == cursor) {
                e.texto(10, yy, ">", Estilo.LETRA, Estilo.RESALTE);
            }
            e.texto(26, yy, a.etiqueta, Estilo.LETRA, color);
            e.texto(Escena.ANCHO - 14, yy + 18, a == Ajuste.RAPIDO ? (valor(a) == 1 ? "Sí" : "No") : "< " + valor(a) + " >",
                    Estilo.LETRA, color, Escena.Alineacion.DERECHA);
        }
        e.texto(8, Estilo.ALTO_UTIL - 44, "Izquierda/derecha cambian el valor.", Estilo.LETRA, Estilo.APAGADO);
        e.texto(8, Estilo.ALTO_UTIL - 24, "Más pasos = combate más rápido.", Estilo.LETRA, Estilo.APAGADO);
    }
}
