package com.example.ff1.combate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.example.ff1.motor.config.ProveedorConfiguracion;
import com.example.ff1.motor.datos.ErrorDeDatos;

public class AvanceRapidoTest {

    private final ProveedorConfiguracion config = Datos.config();

    @Test
    public void apagadoNoMultiplica() {
        AvanceRapido a = new AvanceRapido(config);
        assertFalse(a.activo());
        assertEquals(5, a.ticks(5));
    }

    @Test
    public void encendidoMultiplicaPorLaConfiguracion() {
        AvanceRapido a = new AvanceRapido(config);
        assertTrue(a.alternar());
        assertEquals(10, a.ticks(5));
        config.reemplazar(config.actual().con(ConfiguracionCombate.AVANCE_RAPIDO, 4));
        assertEquals(20, a.ticks(5));
        assertFalse(a.alternar());
        assertEquals(5, a.ticks(5));
    }

    @Test(expected = ErrorDeDatos.class)
    public void elMultiplicadorTieneLimite() {
        config.reemplazar(config.actual().con(ConfiguracionCombate.AVANCE_RAPIDO, 9));
    }
}
