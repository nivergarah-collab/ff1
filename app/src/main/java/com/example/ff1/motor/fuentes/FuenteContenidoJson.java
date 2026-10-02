package com.example.ff1.motor.fuentes;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.example.ff1.motor.datos.Documentos;
import com.example.ff1.motor.datos.ErrorDeDatos;
import com.example.ff1.motor.datos.LectorJson;
import com.example.ff1.motor.datos.Nodo;

/** Fuente de contenido en archivos {@code .json} leídos con un {@link LectorTexto}. */
public final class FuenteContenidoJson implements FuenteContenido {

    private final LectorTexto lector;

    public FuenteContenidoJson(LectorTexto lector) {
        this.lector = lector;
    }

    @Override
    public Nodo cargar(String recurso, String tipo, int versionMaxima) {
        String ruta = recurso + ".json";
        String texto;
        try {
            texto = lector.leer(ruta);
        } catch (FileNotFoundException e) {
            throw new ErrorDeDatos(ruta + ": no existe");
        } catch (IOException e) {
            throw new ErrorDeDatos(ruta + ": no se pudo leer (" + e.getMessage() + ")", e);
        }
        try {
            Nodo doc = LectorJson.leer(texto);
            Documentos.exigir(doc, tipo, versionMaxima);
            return doc;
        } catch (ErrorDeDatos e) {
            throw new ErrorDeDatos(ruta + ": " + e.getMessage(), e);
        }
    }
}
