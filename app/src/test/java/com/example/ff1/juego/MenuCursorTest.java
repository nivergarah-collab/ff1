package com.example.ff1.juego;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import org.junit.Test;

public class MenuCursorTest {

    @Test
    public void vueltaDaLaVueltaEnAmbosSentidos() {
        assertEquals(0, Menu.vuelta(2, 1, 3));
        assertEquals(2, Menu.vuelta(0, -1, 3));
        assertEquals(1, Menu.vuelta(0, 1, 3));
    }

    @Test
    public void moverSaltaOpcionesDeshabilitadas() {
        assertEquals(2, Menu.mover(0, 1, Arrays.asList(true, false, true)));
        assertEquals(0, Menu.mover(2, 1, Arrays.asList(true, false, true)));
    }
}
