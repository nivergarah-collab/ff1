package com.example.ff1.motor.fuentes;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Almacén en archivos {@code <ranura>.json} dentro de una carpeta (en Android, {@code getFilesDir()}).
 * Escribe a un archivo temporal y lo renombra, para no dejar un guardado a medias si algo falla.
 */
public final class AlmacenArchivos implements Almacen {

    private final File carpeta;

    public AlmacenArchivos(File carpeta) {
        this.carpeta = carpeta;
    }

    private File archivo(String ranura) {
        if (ranura == null || !ranura.matches("[A-Za-z0-9_-]{1,40}")) {
            throw new IllegalArgumentException("ranura inválida: " + ranura);
        }
        return new File(carpeta, ranura + ".json");
    }

    @Override
    public void guardar(String ranura, String datos) {
        File destino = archivo(ranura);
        File temporal = new File(carpeta, ranura + ".json.tmp");
        try {
            if (!carpeta.isDirectory() && !carpeta.mkdirs()) {
                throw new IOException("no se pudo crear " + carpeta);
            }
            Files.write(temporal.toPath(), datos.getBytes(StandardCharsets.UTF_8));
            Files.move(temporal.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("no se pudo guardar la ranura " + ranura + ": " + e.getMessage(), e);
        }
    }

    @Override
    public String cargar(String ranura) {
        File f = archivo(ranura);
        if (!f.isFile()) {
            return null;
        }
        try {
            return new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public boolean existe(String ranura) {
        return archivo(ranura).isFile();
    }

    @Override
    public void borrar(String ranura) {
        archivo(ranura).delete();
    }
}
