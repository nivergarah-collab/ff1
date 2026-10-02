package com.example.ff1.android;

import android.content.res.AssetManager;

import com.example.ff1.motor.fuentes.LectorTexto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/** {@link LectorTexto} sobre los assets de la app (por ejemplo la carpeta {@code contenido}). */
public final class LectorAssets implements LectorTexto {

    private final AssetManager assets;
    private final String carpeta;

    public LectorAssets(AssetManager assets, String carpeta) {
        this.assets = assets;
        this.carpeta = carpeta;
    }

    /** Lanza {@link java.io.FileNotFoundException} si el recurso no existe (lo hace {@code open}). */
    @Override
    public String leer(String ruta) throws IOException {
        try (InputStream in = assets.open(carpeta + "/" + ruta)) {
            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            byte[] bloque = new byte[8192];
            for (int n = in.read(bloque); n >= 0; n = in.read(bloque)) {
                salida.write(bloque, 0, n);
            }
            return new String(salida.toByteArray(), StandardCharsets.UTF_8);
        }
    }
}
