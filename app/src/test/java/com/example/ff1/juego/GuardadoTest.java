package com.example.ff1.juego;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.example.ff1.PaqueteDelJuego;
import com.example.ff1.combate.Combatiente;
import com.example.ff1.dibujo.Escena;
import com.example.ff1.entrada.Boton;
import com.example.ff1.motor.datos.ErrorDeDatos;
import com.example.ff1.motor.fuentes.Almacen;
import com.example.ff1.motor.fuentes.AlmacenArchivos;
import com.example.ff1.motor.fuentes.AlmacenMemoria;
import com.example.ff1.motor.fuentes.AzarSemilla;
import com.example.ff1.progresion.Heroe;

import java.io.File;
import java.nio.file.Files;

import org.junit.Test;

public class GuardadoTest {

    private static Partida nueva() {
        return Partida.nueva(PaqueteDelJuego.fuente(), new AzarSemilla(1));
    }

    private static Partida cargar(String texto) {
        return Partida.cargar(PaqueteDelJuego.fuente(), new AzarSemilla(2), texto);
    }

    /** Una partida con de todo cambiado respecto a la inicial. */
    private static Partida jugada() {
        Partida p = nueva();
        Heroe h0 = p.grupo().get(0);
        Combatiente c = h0.entrarEnCombate();
        c.recibirDanio(7);
        h0.salirDeCombate(c);
        h0.ganarExperiencia(60);
        p.equipar(h0, "hoja-de-ensayo");
        p.equipar(p.grupo().get(1), "vara-de-sauce");
        p.comprar("frasco-de-brasas");
        p.vender("agua-de-roca");
        p.intercambiarHeroes(0, 3);
        p.derrotarJefe(p.servicios().jefe("soterrado"));
        p.irAMapa("cantera-baja", 3, 3);
        p.config().reemplazar(p.config().actual().con("combate.ticksPorPaso", 9).con("juego.rapidoAlEmpezar", 1));
        return p;
    }

    @Test
    public void idaYVueltaConservaTodoYElTextoEsEstable() {
        Partida p = jugada();
        String texto = p.guardar();
        Partida q = cargar(texto);
        assertEquals(texto, q.guardar());
        assertEquals(p.oro(), q.oro());
        assertEquals("cantera-baja", q.explorador().mapa().id);
        assertEquals(3, q.explorador().x());
        assertEquals(3, q.explorador().y());
        assertTrue(q.jefeDerrotado("soterrado"));
        assertEquals(p.inventario().contenido(), q.inventario().contenido());
        assertEquals(p.grupo().size(), q.grupo().size());
        for (int i = 0; i < p.grupo().size(); i++) {
            Heroe a = p.grupo().get(i);
            Heroe b = q.grupo().get(i);
            assertEquals(a.nombre(), b.nombre());
            assertEquals(a.clase().id, b.clase().id);
            assertEquals(a.nivel(), b.nivel());
            assertEquals(a.experiencia(), b.experiencia());
            assertEquals(a.vida(), b.vida());
            assertEquals(a.magia(), b.magia());
            assertEquals(a.equipo().keySet(), b.equipo().keySet());
        }
        assertEquals(9, q.config().actual().entero("combate.ticksPorPaso"));
        assertEquals(1, q.config().actual().entero("juego.rapidoAlEmpezar"));
        assertTrue(q.grupo().get(3).nivel() >= 1);
        assertEquals("Mirta", q.grupo().get(0).nombre()); // la formación se conserva
    }

    @Test
    public void cargarEnUnaPartidaNuevaNoDejaRestosDeLaAnterior() {
        String vacio = nueva().guardar();
        Partida q = cargar(jugada().guardar());
        assertEquals(vacio, cargar(vacio).guardar());
        assertFalse(q.guardar().equals(vacio));
    }

    private static void rechaza(String texto, String enElMensaje) {
        try {
            cargar(texto);
            fail("debía rechazarse: " + enElMensaje);
        } catch (ErrorDeDatos e) {
            assertTrue(e.getMessage(), e.getMessage().contains(enElMensaje));
        }
    }

    @Test
    public void unGuardadoDanadoSeRechazaConLaRutaDelCampo() {
        String bueno = nueva().guardar();
        rechaza("{}", "tipo");
        rechaza("no es json", "");
        rechaza(bueno.replace("\"guardian\"", "\"dragon\""), "dragon");
        rechaza(bueno.replace("\"mapa\":\"pozaluz\"", "\"mapa\":\"inexistente\""), "inexistente");
        rechaza(bueno.replace("\"x\":6", "\"x\":0"), "pasable");
        rechaza(bueno.replace("\"vida\":48", "\"vida\":9999"), "grupo[0]");
        rechaza(bueno.replace("\"tonico-de-raiz\":3", "\"tonico-fantasma\":3"), "tonico-fantasma");
        rechaza(bueno.replace("\"jefes\":[]", "\"jefes\":[\"nadie\"]"), "nadie");
        rechaza(bueno.replace("\"combate.ticksPorPaso\":4", "\"combate.ticksPorPaso\":9999"), "ticksPorPaso");
    }

    @Test
    public void elMenuGuardaYElTituloContinuaDondeSeQued() {
        Almacen almacen = new AlmacenMemoria();
        Juego j = new Juego(PaqueteDelJuego.fuente(), new AzarSemilla(1), almacen);
        assertFalse(j.hayGuardado());
        j.pulsar(Boton.ACEPTAR); // Nueva partida
        for (int i = 0; i < 50 && (j.pantalla() instanceof PantallaNombres || j.pantalla() instanceof PantallaEscena); i++) {
            j.pulsar(Boton.ACEPTAR);
        }
        j.pulsar(Boton.ARRIBA); // (6, 5) → (6, 4) es la fuente: solo gira
        j.pulsar(Boton.DERECHA);
        j.pulsar(Boton.CANCELAR); // menú
        PantallaMenu menu = (PantallaMenu) j.pantalla();
        while (menu.seccion() != PantallaMenu.Seccion.GUARDAR) {
            j.pulsar(Boton.ABAJO);
        }
        j.pulsar(Boton.ACEPTAR);
        assertEquals("Partida guardada.", menu.mensaje());
        Escena e = new Escena();
        j.dibujar(e);
        assertTrue(e.contieneTexto("Partida guardada."));
        assertTrue(almacen.existe(Juego.RANURA));

        Juego k = new Juego(PaqueteDelJuego.fuente(), new AzarSemilla(9), almacen);
        assertTrue(k.hayGuardado());
        k.pulsar(Boton.ABAJO); // Continuar
        k.pulsar(Boton.ACEPTAR);
        assertTrue(k.pantalla() instanceof PantallaExploracion);
        assertEquals(7, k.partida().explorador().x());
        assertEquals(5, k.partida().explorador().y());
        assertEquals("pozaluz", k.partida().explorador().mapa().id);
    }

    @Test
    public void continuarSinGuardadoNoHaceNadaYUnGuardadoRotoMuestraElError() {
        Almacen almacen = new AlmacenMemoria();
        Juego j = new Juego(PaqueteDelJuego.fuente(), new AzarSemilla(1), almacen);
        j.pulsar(Boton.ABAJO); // Continuar está apagado: el cursor no baja
        assertEquals(0, ((PantallaTitulo) j.pantalla()).cursor());

        almacen.guardar(Juego.RANURA, "{\"tipo\":\"partida\",\"version\":1}");
        Juego k = new Juego(PaqueteDelJuego.fuente(), new AzarSemilla(1), almacen);
        k.pulsar(Boton.ABAJO);
        k.pulsar(Boton.ACEPTAR);
        assertTrue(k.pantalla() instanceof PantallaError);
        k.pulsar(Boton.ACEPTAR);
        assertTrue(k.pantalla() instanceof PantallaTitulo);
        assertNull(k.partida());
    }

    @Test
    public void elAlmacenEnArchivosGuardaCargaYBorra() throws Exception {
        File carpeta = Files.createTempDirectory("ff1-guardado").toFile();
        AlmacenArchivos a = new AlmacenArchivos(new File(carpeta, "sub"));
        assertFalse(a.existe("partida1"));
        assertNull(a.cargar("partida1"));
        a.guardar("partida1", "{\"a\":\"ñandú\"}");
        assertTrue(a.existe("partida1"));
        assertEquals("{\"a\":\"ñandú\"}", a.cargar("partida1"));
        a.guardar("partida1", "{}"); // sobrescribe
        assertEquals("{}", a.cargar("partida1"));
        a.borrar("partida1");
        assertFalse(a.existe("partida1"));
        try {
            a.guardar("../fuera", "x");
            fail("ranura inválida");
        } catch (IllegalArgumentException esperado) {
            // bien
        }
    }

    @Test
    public void unaPartidaGuardadaEnArchivosSeCargaIgual() throws Exception {
        AlmacenArchivos a = new AlmacenArchivos(Files.createTempDirectory("ff1-guardado").toFile());
        Partida p = jugada();
        a.guardar("partida1", p.guardar());
        assertEquals(p.guardar(), cargar(a.cargar("partida1")).guardar());
    }
}
