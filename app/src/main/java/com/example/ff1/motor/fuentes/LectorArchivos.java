package com.example.ff1.motor.fuentes;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import com.example.ff1.motor.datos.ErrorDeDatos;

/** Recursos en una carpeta del disco (UTF-8). No permite leer fuera de esa carpeta. */
public final class LectorArchivos implements LectorTexto {

    private final File base;

    public LectorArchivos(File base) {
        try {
            this.base = base.getCanonicalFile();
        } catch (IOException e) {
            throw new ErrorDeDatos("carpeta de contenido inválida: " + base, e);
        }
    }

    @Override
    public String leer(String ruta) throws IOException {
        File f = new File(base, ruta).getCanonicalFile();
        if (!f.toPath().startsWith(base.toPath())) {
            throw new ErrorDeDatos(ruta + ": ruta fuera de la carpeta de contenido");
        }
        if (!f.isFile()) {
            throw new java.io.FileNotFoundException(ruta);
        }
        return new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
    }
}
