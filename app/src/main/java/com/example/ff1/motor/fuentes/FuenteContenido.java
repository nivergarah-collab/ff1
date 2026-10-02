package com.example.ff1.motor.fuentes;

import com.example.ff1.motor.datos.Nodo;

/**
 * Entrega al motor los documentos de contenido (combatientes, habilidades, objetos,
 * mapas, escenas, textos, configuración). El motor no sabe de dónde vienen.
 */
public interface FuenteContenido {

    /**
     * Carga el recurso y comprueba su tipo y versión.
     *
     * @param recurso       nombre sin extensión, por ejemplo {@code "enemigos"} o {@code "mapas/pueblo"}
     * @param tipo          valor esperado del campo {@code "tipo"}
     * @param versionMaxima versión más nueva que el motor sabe leer
     * @throws com.example.ff1.motor.datos.ErrorDeDatos si falta, no se puede leer o no es válido
     */
    Nodo cargar(String recurso, String tipo, int versionMaxima);

    /** Si el recurso opcional existe (por defecto, sí; las fuentes que pueden faltar lo sobrescriben). */
    default boolean existe(String recurso) {
        return true;
    }
}
