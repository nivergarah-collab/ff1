package com.example.ff1.android;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;

import com.example.ff1.dibujo.Escalado;
import com.example.ff1.dibujo.Escena;

/**
 * Única clase que traduce una {@link Escena} (Java puro) a {@link Canvas}, escalando la rejilla
 * virtual a la pantalla real con {@link Escalado}. No decide nada del juego: solo dibuja órdenes.
 */
public final class LienzoCanvas {

    private static final int FONDO = 0xFF000000;
    private static final int SIMBOLO_CASILLA = 0x55000000;

    private final Paint pincel = new Paint(Paint.ANTI_ALIAS_FLAG);

    public LienzoCanvas() {
        pincel.setTypeface(Typeface.MONOSPACE);
    }

    public void dibujar(Canvas canvas, Escena escena, Escalado e) {
        canvas.drawColor(FONDO);
        for (Escena.Orden o : escena.ordenes()) {
            switch (o.tipo) {
                case RECTANGULO:
                    pincel.setColor(o.color);
                    pincel.setStyle(o.relleno ? Paint.Style.FILL : Paint.Style.STROKE);
                    pincel.setStrokeWidth(Math.max(1f, e.escala));
                    canvas.drawRect(e.pantallaX(o.x), e.pantallaY(o.y),
                            e.pantallaX(o.x + o.ancho), e.pantallaY(o.y + o.alto), pincel);
                    break;
                case CASILLA:
                    pincel.setColor(o.color);
                    pincel.setStyle(Paint.Style.FILL);
                    canvas.drawRect(e.pantallaX(o.x), e.pantallaY(o.y),
                            e.pantallaX(o.x + o.ancho), e.pantallaY(o.y + o.alto), pincel);
                    if (o.texto != null) {
                        int tamano = o.alto * 3 / 4;
                        texto(canvas, e, o.texto, o.x + o.ancho / 2f, o.y + (o.alto - tamano) / 2f,
                                tamano, SIMBOLO_CASILLA, Paint.Align.CENTER);
                    }
                    break;
                case TEXTO:
                    texto(canvas, e, o.texto, o.x, o.y, o.alto, o.color, alinear(o.alineacion));
                    break;
                default:
                    break;
            }
        }
    }

    private void texto(Canvas canvas, Escalado e, String texto, float x, float y, int tamano, int color,
            Paint.Align alineacion) {
        pincel.setColor(color);
        pincel.setStyle(Paint.Style.FILL);
        pincel.setTextAlign(alineacion);
        pincel.setTextSize(tamano * e.escala);
        // y es el borde superior del texto: se baja hasta la línea base.
        canvas.drawText(texto, e.pantallaX(x), e.pantallaY(y) - pincel.ascent(), pincel);
    }

    private static Paint.Align alinear(Escena.Alineacion a) {
        switch (a) {
            case CENTRO:
                return Paint.Align.CENTER;
            case DERECHA:
                return Paint.Align.RIGHT;
            default:
                return Paint.Align.LEFT;
        }
    }
}
