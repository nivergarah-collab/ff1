package com.example.ff1.motor.datos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class LectorJsonTest {

    @Test
    public void leeObjetoConTodosLosTipos() {
        Nodo n = LectorJson.leer("{\"nombre\":\"Guardia\",\"vida\":30,\"factor\":1.5,"
                + "\"jefe\":false,\"nada\":null,\"lista\":[1,2,3],\"sub\":{\"x\":-4}}");
        assertEquals("Guardia", n.texto("nombre"));
        assertEquals(30, n.entero("vida"));
        assertEquals(1.5, n.decimal("factor"), 0.0);
        assertFalse(n.booleano("jefe"));
        assertTrue(n.objeto("sub").esObjeto());
        assertEquals(-4, n.objeto("sub").entero("x"));
        assertEquals(3, n.lista("lista").size());
        assertEquals(2, n.lista("lista").get(1).comoEntero());
    }

    @Test
    public void conservaElOrdenDeLasClaves() {
        Nodo n = LectorJson.leer("{\"c\":1,\"a\":2,\"b\":3}");
        assertEquals(Arrays.asList("c", "a", "b"), n.claves());
    }

    @Test
    public void decodificaEscapes() {
        Nodo n = LectorJson.leer("{\"t\":\"a\\\"b\\\\c\\n\\u00f1\"}");
        assertEquals("a\"b\\c\nñ", n.texto("t"));
    }

    @Test
    public void aceptaEspaciosYSaltosDeLinea() {
        Nodo n = LectorJson.leer("  {\n  \"a\" : [ 1 , 2 ]\n}\n ");
        List<Nodo> a = n.lista("a");
        assertEquals(2, a.size());
    }

    @Test
    public void informaLineaYColumnaDelError() {
        try {
            LectorJson.leer("{\n\"a\": 1,\n\"b\" 2}");
            fail("debió rechazar el texto");
        } catch (ErrorDeDatos e) {
            assertTrue(e.getMessage(), e.getMessage().contains("línea 3"));
        }
    }

    @Test(expected = ErrorDeDatos.class)
    public void rechazaTextoSobrante() {
        LectorJson.leer("{} {}");
    }

    @Test(expected = ErrorDeDatos.class)
    public void rechazaTextoSinCerrar() {
        LectorJson.leer("{\"a\":\"sin cierre}");
    }

    @Test(expected = ErrorDeDatos.class)
    public void rechazaClavesRepetidas() {
        LectorJson.leer("{\"a\":1,\"a\":2}");
    }

    @Test
    public void idaYVueltaConElEscritor() {
        String original = "{\"a\":[1,2.5,\"x\\n\",true,null],\"b\":{\"c\":\"ñ\"}}";
        Nodo n = LectorJson.leer(original);
        assertEquals(original, EscritorJson.escribir(n));
        assertEquals(n, LectorJson.leer(EscritorJson.escribir(n)));
    }
}
