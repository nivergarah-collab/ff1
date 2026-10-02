package com.example.ff1;

import java.io.File;

import com.example.ff1.motor.fuentes.FuenteContenidoJson;
import com.example.ff1.motor.fuentes.LectorArchivos;
import com.example.ff1.motor.fuentes.LectorTexto;

/** Acceso de las pruebas al paquete de contenido del juego (app/src/main/assets/contenido). */
public final class PaqueteDelJuego {

    private PaqueteDelJuego() {
    }

    public static FuenteContenidoJson fuente() {
        return new FuenteContenidoJson(new LectorArchivos(carpeta()));
    }

    /** El paquete del juego, pero con otro {@code configuracion.json} (texto JSON completo). */
    public static FuenteContenidoJson fuenteConConfiguracion(final String configuracionJson) {
        final LectorArchivos base = new LectorArchivos(carpeta());
        return new FuenteContenidoJson(new LectorTexto() {
            @Override
            public String leer(String ruta) throws java.io.IOException {
                return ruta.startsWith("configuracion") ? configuracionJson : base.leer(ruta);
            }
        });
    }

    /** El paquete del juego, pero con otro texto para un recurso (ruta con extensión, p. ej. {@code mapas/campo.json}). */
    public static FuenteContenidoJson fuenteCambiando(final String rutaRecurso, final String json) {
        final LectorArchivos base = new LectorArchivos(carpeta());
        return new FuenteContenidoJson(new LectorTexto() {
            @Override
            public String leer(String ruta) throws java.io.IOException {
                return ruta.equals(rutaRecurso) ? json : base.leer(ruta);
            }
        });
    }

    /** Texto original de un recurso del paquete. */
    public static String texto(String ruta) throws java.io.IOException {
        return new LectorArchivos(carpeta()).leer(ruta);
    }

    private static File carpeta() {
        File carpeta = new File("app/src/main/assets/contenido");
        if (!carpeta.isDirectory()) {
            carpeta = new File("src/main/assets/contenido"); // Gradle corre desde app/
        }
        return carpeta;
    }
}
