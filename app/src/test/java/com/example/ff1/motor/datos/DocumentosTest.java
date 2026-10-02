package com.example.ff1.motor.datos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class DocumentosTest {

    private static final Nodo N = LectorJson.leer("{\"a\":5,\"b\":500}");

    @Test
    public void rangoObligatorioDevuelveElValorDentroDelRango() {
        assertEquals(5, Documentos.rango(N, "a", 1, 10));
    }

    @Test
    public void rangoOpcionalUsaElDefectoSiFalta() {
        assertEquals(7, Documentos.rangoO(N, "z", 1, 10, 7));
        assertEquals(5, Documentos.rangoO(N, "a", 1, 10, 7));
    }

    @Test
    public void fueraDelRangoDiceCampoValorYLimites() {
        try {
            Documentos.rango(N, "b", 1, 10);
            fail();
        } catch (ErrorDeDatos e) {
            assertTrue(e.getMessage(), e.getMessage().contains(".b: 500 fuera del rango [1, 10]"));
        }
    }

    @Test(expected = ErrorDeDatos.class)
    public void campoObligatorioAusenteFalla() {
        Documentos.rango(N, "z", 1, 10);
    }

    @Test(expected = ErrorDeDatos.class)
    public void elDefectoTambienSeValida() {
        Documentos.rangoO(N, "z", 1, 10, 50);
    }
}
