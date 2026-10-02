package com.example.ff1.juego;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.example.ff1.PaqueteDelJuego;
import com.example.ff1.dibujo.Escena;
import com.example.ff1.entrada.Boton;
import com.example.ff1.motor.datos.ErrorDeDatos;
import com.example.ff1.motor.fuentes.AzarSemilla;
import com.example.ff1.mundo.Direccion;

import org.junit.Test;

/** La puerta de hierro y las dos galerías de la Cantera Hundida. */
public class CanteraTest {

    private static void ir(Partida p, Direccion d, int veces) {
        for (int i = 0; i < veces; i++) {
            assertTrue("paso bloqueado hacia " + d, p.explorador().mover(d));
            p.cruzarSalida();
        }
    }

    @Test
    public void laPuertaDeHierroSeAbreConLaLlaveYLlevaALaGaleriaAlta() {
        Partida p = Partida.nueva(PaqueteDelJuego.fuente(), new AzarSemilla(1));
        assertEquals(1, p.inventario().cantidad("llave-de-cantera"));
        p.irAMapa("campo", 9, 1);
        ir(p, Direccion.ARRIBA, 1);
        assertEquals("cantera-alta", p.explorador().mapa().id);
        assertEquals(5, p.explorador().x());
        assertEquals(7, p.explorador().y());
    }

    @Test
    public void sinLaLlaveLaPuertaNoCedeYElGrupoSeQuedaDelanteDeEla() {
        Juego j = JuegoTest.enExploracion(1);
        Partida p = j.partida();
        p.inventario().quitar("llave-de-cantera", 1);
        p.irAMapa("campo", 9, 1);
        j.pulsar(Boton.ARRIBA);
        assertEquals("campo", p.explorador().mapa().id);
        assertEquals(1, p.explorador().y());
        PantallaExploracion pantalla = (PantallaExploracion) j.pantalla();
        assertTrue(pantalla.mensaje().contains("llave"));
        Escena e = new Escena();
        j.dibujar(e);
        assertTrue(e.contieneTexto("Está cerrado: hace falta una llave."));
        p.inventario().agregar("llave-de-cantera", 1);
        j.pulsar(Boton.ARRIBA);
        assertEquals("cantera-alta", p.explorador().mapa().id);
        assertEquals("", pantalla.mensaje());
    }

    @Test
    public void laEscaleraLlevaALaGaleriaBajaYVuelve() {
        Partida p = Partida.nueva(PaqueteDelJuego.fuente(), new AzarSemilla(1));
        p.irAMapa("cantera-alta", 5, 7);
        ir(p, Direccion.IZQUIERDA, 4);
        ir(p, Direccion.ARRIBA, 2);
        ir(p, Direccion.DERECHA, 8);
        ir(p, Direccion.ARRIBA, 2);
        ir(p, Direccion.DERECHA, 1);
        ir(p, Direccion.ARRIBA, 2); // (10, 1): la escalera
        assertEquals("cantera-baja", p.explorador().mapa().id);
        assertEquals(2, p.explorador().x());
        ir(p, Direccion.IZQUIERDA, 1); // (1, 1): sube
        assertEquals("cantera-alta", p.explorador().mapa().id);
        assertEquals(10, p.explorador().x());
        assertEquals(2, p.explorador().y());
    }

    @Test
    public void lasGaleriasTienenEncuentrosConEnemigosPropios() {
        Partida p = Partida.nueva(PaqueteDelJuego.fuente(), new AzarSemilla(1));
        for (String id : new String[] { "cantera-alta", "cantera-baja" }) {
            p.irAMapa(id, p.mapa(id).inicioX, p.mapa(id).inicioY);
            boolean hubo = false;
            for (int i = 0; i < 400 && !hubo; i++) {
                hubo = p.encuentros().mover(p.explorador(), i % 2 == 0 ? Direccion.IZQUIERDA : Direccion.DERECHA) != null;
            }
            assertTrue("sin encuentros en " + id, hubo);
        }
    }

    @Test
    public void unaSalidaHaciaUnaCasillaNoPasableSeRechazaAlEmpezar() throws Exception {
        String roto = PaqueteDelJuego.texto("mapas/campo.json").replace("\"destino\": {\n        \"x\": 6,\n        \"y\": 1\n      }",
                "\"destino\": {\n        \"x\": 0,\n        \"y\": 0\n      }");
        try {
            Partida.nueva(PaqueteDelJuego.fuenteCambiando("mapas/campo.json", roto), new AzarSemilla(1));
            fail("destino no pasable");
        } catch (ErrorDeDatos esperado) {
            assertTrue(esperado.getMessage(), esperado.getMessage().contains("no es una casilla pasable"));
        }
    }

    @Test
    public void unaSalidaQueExigeUnObjetoInexistenteSeRechaza() throws Exception {
        String roto = PaqueteDelJuego.texto("mapas/campo.json").replace("llave-de-cantera", "llave-fantasma");
        try {
            Partida.nueva(PaqueteDelJuego.fuenteCambiando("mapas/campo.json", roto), new AzarSemilla(1));
            fail("objeto inexistente");
        } catch (ErrorDeDatos esperado) {
            assertTrue(esperado.getMessage(), esperado.getMessage().contains("llave-fantasma"));
        }
    }
}
