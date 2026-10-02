package com.example.ff1;

import java.io.File;

import com.example.ff1.motor.fuentes.FuenteContenidoJson;
import com.example.ff1.motor.fuentes.LectorArchivos;

/** Acceso de las pruebas al paquete de contenido del juego (app/src/main/assets/contenido). */
public final class PaqueteDelJuego {

    private PaqueteDelJuego() {
    }

    public static FuenteContenidoJson fuente() {
        File carpeta = new File("app/src/main/assets/contenido");
        if (!carpeta.isDirectory()) {
            carpeta = new File("src/main/assets/contenido"); // Gradle corre desde app/
        }
        return new FuenteContenidoJson(new LectorArchivos(carpeta));
    }
}
