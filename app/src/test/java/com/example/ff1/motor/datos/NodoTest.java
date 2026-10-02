package com.example.ff1.motor.datos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class NodoTest {

    private final Nodo n = LectorJson.leer(
            "{\"tipo\":\"enemigos\",\"version\":1,\"vida\":\"diez\",\"grande\":3000000000,"
                    + "\"lista\":[{\"id\":\"a\"},{\"id\":5}]}");

    @Test
    public void errorDeTipoIndicaLaRuta() {
        try {
            n.entero("vida");
            fail();
        } catch (ErrorDeDatos e) {
            assertTrue(e.getMessage(), e.getMessage().contains("vida"));
            assertTrue(e.getMessage(), e.getMessage().contains("entero"));
        }
    }

    @Test
    public void errorEnElementoDeListaIndicaElIndice() {
        try {
            n.lista("lista").get(1).texto("id");
            fail();
        } catch (ErrorDeDatos e) {
            assertTrue(e.getMessage(), e.getMessage().contains("lista[1].id"));
        }
    }

    @Test
    public void campoFaltanteEsError() {
        try {
            n.texto("nombre");
            fail();
        } catch (ErrorDeDatos e) {
            assertTrue(e.getMessage(), e.getMessage().contains("falta"));
        }
    }

    @Test(expected = ErrorDeDatos.class)
    public void enteroFueraDeRangoEsError() {
        n.entero("grande");
    }

    @Test
    public void valoresPorDefecto() {
        assertEquals("x", n.textoO("nombre", "x"));
        assertEquals(7, n.enteroO("nivel", 7));
        assertEquals(0.5, n.decimalO("factor", 0.5), 0.0);
        assertFalse(n.tiene("nombre"));
        assertTrue(n.tiene("vida"));
    }

    @Test
    public void exigirTipoYVersion() {
        Documentos.exigir(n, "enemigos", 1);
        try {
            Documentos.exigir(n, "heroes", 1);
            fail();
        } catch (ErrorDeDatos e) {
            assertTrue(e.getMessage(), e.getMessage().contains("heroes"));
        }
    }

    @Test(expected = ErrorDeDatos.class)
    public void versionMasNuevaQueLaSoportadaEsError() {
        Documentos.exigir(LectorJson.leer("{\"tipo\":\"t\",\"version\":3}"), "t", 2);
    }
}
