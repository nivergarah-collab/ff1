package com.example.ff1.juego;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.ff1.PaqueteDelJuego;
import com.example.ff1.dibujo.Escena;
import com.example.ff1.entrada.Boton;
import com.example.ff1.motor.fuentes.AzarSemilla;

import org.junit.Test;

public class NombresTest {

    @Test
    public void elEditorEmpiezaConElNombrePorDefectoYElCursorEnFin() {
        EditorNombre ed = new EditorNombre("Bruna", 8);
        assertEquals("Bruna", ed.texto());
        assertEquals(EditorNombre.FIN, ed.celda());
        assertTrue(ed.aceptar()); // Fin con un nombre válido termina
    }

    @Test
    public void seEscribeConLaRejillaYSeBorraConCancelarOBorrar() {
        EditorNombre ed = new EditorNombre("Bo", 3);
        ed.mover(Boton.ABAJO); // de la fila de Fin a la primera fila (vuelta)
        assertEquals("B", ed.celda()); // la columna se conserva (1)
        assertFalse(ed.aceptar());
        assertEquals("BoB", ed.texto());
        assertFalse(ed.aceptar()); // al largo máximo no escribe más
        assertEquals("BoB", ed.texto());
        ed.borrar();
        assertEquals("Bo", ed.texto());
        ed.mover(Boton.IZQUIERDA);
        ed.mover(Boton.IZQUIERDA); // vuelta: última letra de la fila
        assertEquals("I", ed.celda());
        ed.mover(Boton.ARRIBA); // vuelta a la fila de Borrar/Fin: la columna se ajusta
        assertEquals(EditorNombre.FIN, ed.celda());
        ed.mover(Boton.IZQUIERDA);
        assertEquals(EditorNombre.BORRAR, ed.celda());
        assertFalse(ed.aceptar());
        assertFalse(ed.aceptar());
        assertFalse(ed.aceptar()); // borrar sin letras no falla
        assertEquals("", ed.texto());
        ed.mover(Boton.DERECHA);
        assertFalse(ed.aceptar()); // Fin sin nombre no termina
    }

    @Test
    public void laPartidaNuevaPideLosNombresAntesDeLaApertura() {
        Juego j = new Juego(PaqueteDelJuego.fuente(), new AzarSemilla(1));
        j.pulsar(Boton.ACEPTAR); // Nueva partida
        assertTrue(j.pantalla() instanceof PantallaNombres);
        Escena e = new Escena();
        j.dibujar(e);
        assertTrue(e.contieneTexto("Bruna"));
        assertTrue(e.contieneTexto("Fin"));

        // Bruna pasa a llamarse "BrunaA": baja a la primera fila, columna de "Fin" (1) → "B"; izquierda → "A".
        j.pulsar(Boton.ABAJO);
        j.pulsar(Boton.IZQUIERDA);
        j.pulsar(Boton.ACEPTAR);
        j.pulsar(Boton.ARRIBA); // vuelve a la fila de Borrar/Fin, columna 0: Borrar
        j.pulsar(Boton.DERECHA); // Fin
        j.pulsar(Boton.ACEPTAR);
        assertTrue(j.pantalla() instanceof PantallaNombres);
        j.pulsar(Boton.CANCELAR); // Tadeo → Tade
        j.pulsar(Boton.ACEPTAR);
        j.pulsar(Boton.ACEPTAR); // Ilke
        j.pulsar(Boton.ACEPTAR); // Mirta
        assertTrue(j.pantalla() instanceof PantallaEscena);
        assertEquals("BrunaA", j.partida().grupo().get(0).nombre());
        assertEquals("Tade", j.partida().grupo().get(1).nombre());
        assertEquals("Tade", j.partida().nombresPorClase().get("arcanista")); // la apertura usa el nombre nuevo
    }

    @Test
    public void elLargoMaximoVieneDeLaConfiguracion() {
        Juego j = new Juego(PaqueteDelJuego.fuente(), new AzarSemilla(1));
        j.pulsar(Boton.ACEPTAR);
        assertEquals(8, ((PantallaNombres) j.pantalla()).largoMaximo());
        assertEquals(8, j.partida().config().actual().entero(ConfiguracionJuego.LARGO_NOMBRE));
    }
}
