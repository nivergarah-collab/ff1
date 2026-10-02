package com.example.ff1.juego;

import com.example.ff1.combate.ResultadoAccion;

/**
 * Textos de la interfaz para el resultado de una acción. El motor devuelve datos
 * ({@link ResultadoAccion}) y la presentación arma la frase; nombres y habilidades vienen del
 * contenido. Si el resultado es de un aliado sobre un aliado, es curación; si no, daño.
 */
final class Mensajes {

    private Mensajes() {
    }

    static String de(ResultadoAccion r) {
        String actor = r.actor.nombre();
        switch (r.accion) {
            case HUIR:
                switch (r.fallo) {
                    case NINGUNO:
                        return "¡El grupo huye!";
                    case HUIDA_PROHIBIDA:
                        return "¡No hay escapatoria!";
                    default:
                        return actor + " intenta huir, pero no lo logra.";
                }
            case PERDER_TURNO:
                return actor + " no puede actuar.";
            case ATACAR:
                if (!r.exito()) {
                    return actor + " ataca, pero el objetivo ya cayó.";
                }
                return actor + " ataca a " + r.objetivo.nombre() + ": " + r.cantidad + " de daño.";
            default:
                break;
        }
        if (r.fallo == ResultadoAccion.Fallo.SIN_MAGIA) {
            return actor + " no tiene magia suficiente.";
        }
        if (!r.exito()) {
            return actor + " usa " + r.habilidad.nombre + ", pero el objetivo ya cayó.";
        }
        StringBuilder s = new StringBuilder(actor).append(" usa ").append(r.habilidad.nombre);
        String objetivo = r.objetivo.nombre();
        if (r.cantidad > 0) {
            boolean aliado = r.objetivo.bando() == r.actor.bando();
            s.append(aliado ? ": " + objetivo + " recupera " + r.cantidad + "."
                    : ": " + r.cantidad + " de daño a " + objetivo + ".");
        } else if (r.estadoAplicado == null) {
            s.append(" sobre ").append(objetivo).append(".");
        }
        if (r.estadoAplicado != null) {
            s.append(" ").append(objetivo).append(" queda con ").append(r.estadoAplicado).append(".");
        }
        return s.toString();
    }
}
