package com.example.ff1.combate;

import com.example.ff1.motor.config.ProveedorConfiguracion;

/**
 * Interruptor de avance rápido: multiplica los ticks que avanza el combate en cada paso
 * (cuadro de animación) por {@code combate.avanceRapido}, leído de la configuración vigente.
 * No cambia las reglas: solo cuántos ticks caben en un paso.
 */
public final class AvanceRapido {

    private final ProveedorConfiguracion config;
    private boolean activo;

    public AvanceRapido(ProveedorConfiguracion config) {
        this.config = config;
    }

    public boolean activo() {
        return activo;
    }

    public void activar(boolean activo) {
        this.activo = activo;
    }

    /** Cambia el interruptor; devuelve el nuevo valor. */
    public boolean alternar() {
        activo = !activo;
        return activo;
    }

    /** Multiplicador vigente: 1 si está apagado. */
    public int multiplicador() {
        return activo ? config.actual().entero(ConfiguracionCombate.AVANCE_RAPIDO) : 1;
    }

    /** Ticks que corresponden a {@code ticksBase} ticks de un paso normal. */
    public int ticks(int ticksBase) {
        if (ticksBase < 0) {
            throw new IllegalArgumentException("ticks negativos: " + ticksBase);
        }
        return ticksBase * multiplicador();
    }
}
