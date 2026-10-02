package com.example.ff1.motor.fuentes;

import java.io.IOException;

/**
 * Lee el texto de un recurso por su ruta relativa (por ejemplo {@code "enemigos.json"}).
 * Implementaciones: memoria (pruebas), carpeta de archivos (JVM) y, en Android, los assets.
 * Una fuente remota o un editor se añadirían como otra implementación.
 */
public interface LectorTexto {

    /** Texto del recurso; lanza {@link java.io.FileNotFoundException} si no existe. */
    String leer(String ruta) throws IOException;
}
