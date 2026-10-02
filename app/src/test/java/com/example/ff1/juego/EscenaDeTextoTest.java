package com.example.ff1.juego;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.example.ff1.PaqueteDelJuego;
import com.example.ff1.dibujo.Escena;
import com.example.ff1.entrada.Boton;
import com.example.ff1.guion.Guion;
import com.example.ff1.motor.datos.ErrorDeDatos;
import com.example.ff1.motor.datos.LectorJson;
import com.example.ff1.motor.datos.Nodo;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class EscenaDeTextoTest {

    private static Guion guion(String json) {
        return Guion.desde(LectorJson.leer(json));
    }

    private static void rechaza(String json, String fragmento) {
        try {
            guion(json);
            fail("debía rechazarse: " + json);
        } catch (ErrorDeDatos e) {
            assertTrue(e.getMessage(), e.getMessage().contains(fragmento));
        }
    }

    private static String escena(String lineas) {
        return "{\"tipo\":\"escena\",\"version\":1,\"id\":\"x\",\"lineas\":" + lineas + "}";
    }

    @Test
    public void laEscenaDelPaqueteCargaYTieneNarradorYPersonajes() {
        Guion g = Guion.cargar(PaqueteDelJuego.fuente(), "apertura");
        assertEquals("apertura", g.id);
        assertEquals(7, g.lineas().size());
        assertEquals("", g.lineas().get(0).quien); // narrador
        assertEquals("Ofelia", g.lineas().get(4).quien);
    }

    @Test
    public void unaEscenaInvalidaSeRechazaConLaRutaDelCampo() {
        rechaza(escena("[]"), "lineas");
        rechaza(escena("[{\"quien\":\"A\"}]"), "texto");
        rechaza(escena("[{\"texto\":\"  \"}]"), "texto");
        rechaza(escena("[{\"texto\":\"Hola {heroe:}\"}]"), "marcador mal formado");
        rechaza(escena("[{\"texto\":\"Hola {nombre}\"}]"), "marcador mal formado");
        rechaza("{\"tipo\":\"mapa\",\"version\":1,\"id\":\"x\",\"lineas\":[]}", "tipo");
    }

    @Test
    public void elIdInternoDebeCoincidirConElNombreDelArchivo() {
        com.example.ff1.motor.fuentes.FuenteContenido f = new com.example.ff1.motor.fuentes.FuenteContenidoJson(
                new com.example.ff1.motor.fuentes.LectorMemoria(Collections.singletonMap("escenas/otra.json",
                        escena("[{\"texto\":\"Hola\"}]"))));
        try {
            Guion.cargar(f, "otra");
            fail();
        } catch (ErrorDeDatos e) {
            assertTrue(e.getMessage(), e.getMessage().contains("no coincide"));
        }
    }

    @Test
    public void losMarcadoresSeSustituyenPorElNombreDeLaClase() {
        Map<String, String> n = new HashMap<>();
        n.put("guardian", "Rosa");
        assertEquals("Rosa y Rosa", Guion.resolver("{heroe:guardian} y {heroe:guardian}", n));
        assertEquals("Hola, Rosa.", Guion.resolver("Hola, {heroe:guardian}.", n));
        assertEquals("Hola, rastreador.", Guion.resolver("Hola, {heroe:rastreador}.", n)); // sin héroe de esa clase
        assertEquals("Sin marcadores", Guion.resolver("Sin marcadores", n));
    }

    @Test
    public void laPartidaNuevaMuestraLaAperturaConLosNombresDelJugadorYLuegoElMapa() {
        Juego j = JuegoTest.nuevo(1);
        j.pulsar(Boton.ACEPTAR);
        assertTrue(j.pantalla() instanceof PantallaEscena);
        PantallaEscena p = (PantallaEscena) j.pantalla();
        Escena e = new Escena();
        j.dibujar(e);
        assertTrue(e.contieneTexto("1/7  Aceptar"));
        assertTrue(e.contieneTexto(Estilo.partir("Amanece en Pozaluz. La fuente de la plaza apenas gotea.",
                PantallaEscena.COLUMNAS).get(0)));
        assertEquals(0, p.linea());

        j.pulsar(Boton.CANCELAR); // Cancelar no avanza
        assertEquals(0, p.linea());
        j.pulsar(Boton.ACEPTAR);
        j.dibujar(e);
        assertTrue(e.contieneTexto("Mirta")); // la herbolaria del grupo habla
        assertEquals(1, p.linea());

        for (int i = 0; i < 5; i++) {
            j.pulsar(Boton.ACEPTAR);
        }
        assertTrue(j.pantalla() instanceof PantallaEscena);
        j.dibujar(e);
        assertTrue(e.contieneTexto("7/7  Aceptar"));
        j.pulsar(Boton.ACEPTAR); // última línea
        assertTrue(j.pantalla() instanceof PantallaExploracion);
    }

    @Test
    public void laEscenaNoLlevaMarcadoresSinResolverEnPantalla() {
        Juego j = JuegoTest.nuevo(1);
        j.pulsar(Boton.ACEPTAR);
        Escena e = new Escena();
        for (int i = 0; i < 7; i++) {
            j.dibujar(e);
            for (Escena.Orden o : e.de(Escena.Tipo.TEXTO)) {
                assertTrue(o.texto, !o.texto.contains("{"));
            }
            j.pulsar(Boton.ACEPTAR);
        }
    }
}
