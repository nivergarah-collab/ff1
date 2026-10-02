package com.example.ff1.motor.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.function.IntUnaryOperator;

import org.junit.Test;

import com.example.ff1.motor.datos.ErrorDeDatos;

public class RegistroTest {

    @Test
    public void registraYObtienePorClave() {
        Registro<IntUnaryOperator> efectos = new Registro<>("efecto");
        efectos.registrar("duplicar", x -> x * 2);
        assertEquals(8, efectos.obtener("duplicar").applyAsInt(4));
        assertTrue(efectos.contiene("duplicar"));
    }

    @Test
    public void claveDesconocidaEsErrorDeDatosConLasOpciones() {
        Registro<String> r = new Registro<>("efecto");
        r.registrar("curar", "c");
        try {
            r.obtener("volar");
            fail();
        } catch (ErrorDeDatos e) {
            assertTrue(e.getMessage(), e.getMessage().contains("volar"));
            assertTrue(e.getMessage(), e.getMessage().contains("curar"));
        }
    }

    @Test(expected = IllegalStateException.class)
    public void noPermiteRegistrarDosVecesLaMismaClave() {
        Registro<String> r = new Registro<>("efecto");
        r.registrar("curar", "a");
        r.registrar("curar", "b");
    }
}
