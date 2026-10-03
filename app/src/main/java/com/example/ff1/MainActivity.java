package com.example.ff1;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowInsets;
import android.view.WindowInsetsController;

import com.example.ff1.android.LectorAssets;
import com.example.ff1.android.VistaJuego;
import com.example.ff1.juego.Juego;
import com.example.ff1.motor.fuentes.AzarSemilla;
import com.example.ff1.motor.fuentes.FuenteContenidoJson;

/**
 * Actividad de lanzamiento: crea el {@link Juego} (Java puro) con el contenido de
 * {@code assets/contenido} y lo muestra en una {@link VistaJuego} que lo dibuja con Canvas.
 */
public class MainActivity extends Activity {

    private VistaJuego vista;

    @Override
    protected void onCreate(Bundle estado) {
        super.onCreate(estado);
        Juego juego = new Juego(new FuenteContenidoJson(new LectorAssets(getAssets(), "contenido")),
                new AzarSemilla(System.nanoTime()),
                new com.example.ff1.motor.fuentes.AlmacenArchivos(getFilesDir()));
        vista = new VistaJuego(this, juego);
        setContentView(vista);
    }

    @Override
    protected void onResume() {
        super.onResume();
        vista.reanudar();
    }

    @Override
    protected void onPause() {
        vista.pausar();
        super.onPause();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            ocultarBarrasDelSistema();
        }
    }

    private void ocultarBarrasDelSistema() {
        WindowInsetsController controlador = getWindow().getInsetsController();
        if (controlador != null) {
            controlador.hide(WindowInsets.Type.systemBars());
            controlador.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }
    }
}
