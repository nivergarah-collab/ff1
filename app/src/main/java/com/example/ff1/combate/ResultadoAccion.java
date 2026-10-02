package com.example.ff1.combate;

/**
 * Lo que pasó al ejecutar una acción, sin textos: la interfaz arma el mensaje.
 */
public final class ResultadoAccion {

    public enum Accion {
        ATACAR, HABILIDAD, OBJETO, HUIR, PERDER_TURNO
    }

    public enum Fallo {
        NINGUNO, SIN_MAGIA, OBJETIVO_CAIDO, HUIDA_FALLIDA, HUIDA_PROHIBIDA
    }

    public final Accion accion;
    public final Combatiente actor;
    /** {@code null} para huir o perder el turno. */
    public final Combatiente objetivo;
    /** Habilidad u objeto usado, o {@code null}. */
    public final Habilidad habilidad;
    /** Vida cambiada (daño o curación). */
    public final int cantidad;
    /** Estado aplicado al objetivo, o {@code null}. */
    public final String estadoAplicado;
    public final Fallo fallo;

    ResultadoAccion(Accion accion, Combatiente actor, Combatiente objetivo, Habilidad habilidad,
            int cantidad, String estadoAplicado, Fallo fallo) {
        this.accion = accion;
        this.actor = actor;
        this.objetivo = objetivo;
        this.habilidad = habilidad;
        this.cantidad = cantidad;
        this.estadoAplicado = estadoAplicado;
        this.fallo = fallo;
    }

    public boolean exito() {
        return fallo == Fallo.NINGUNO;
    }
}
