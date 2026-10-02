package com.example.ff1.android;

import android.content.Context;
import android.graphics.Canvas;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import com.example.ff1.dibujo.Escalado;
import com.example.ff1.dibujo.Escena;
import com.example.ff1.entrada.Boton;
import com.example.ff1.entrada.Bucle;
import com.example.ff1.juego.Juego;

/**
 * Vista propia del juego: en cada cuadro avanza el {@link Juego} los pasos fijos que tocan
 * ({@link Bucle}), lo dibuja en una {@link Escena} y la pinta con {@link LienzoCanvas}. Traduce
 * los toques a botones en pantalla y, para el emulador, las teclas de dirección, Intro/Z
 * (aceptar), X/Escape (cancelar) y F (avance rápido).
 */
public final class VistaJuego extends View {

    private final Juego juego;
    private final Bucle bucle;
    private final Escena escena = new Escena();
    private final LienzoCanvas lienzo = new LienzoCanvas();
    private Escalado escalado;
    private boolean activo;

    public VistaJuego(Context contexto, Juego juego) {
        super(contexto);
        this.juego = juego;
        this.bucle = new Bucle(SystemClock::uptimeMillis, Juego.MS_POR_PASO, 5);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setKeepScreenOn(true);
    }

    public void reanudar() {
        activo = true;
        bucle.reanudar();
        requestFocus();
        postInvalidateOnAnimation();
    }

    public void pausar() {
        activo = false;
        juego.entrada().soltar();
    }

    @Override
    protected void onSizeChanged(int ancho, int alto, int anchoAntes, int altoAntes) {
        super.onSizeChanged(ancho, alto, anchoAntes, altoAntes);
        if (ancho > 0 && alto > 0) {
            escalado = new Escalado(ancho, alto);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int pasos = bucle.pasos();
        for (int i = 0; i < pasos; i++) {
            juego.avanzar(Juego.MS_POR_PASO);
        }
        if (escalado != null) {
            juego.dibujar(escena);
            lienzo.dibujar(canvas, escena, escalado);
        }
        if (activo) {
            postInvalidateOnAnimation();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (escalado == null) {
            return true;
        }
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                presionar(ev, ev.getActionIndex());
                break;
            case MotionEvent.ACTION_MOVE:
                presionar(ev, ev.getPointerCount() - 1);
                break;
            case MotionEvent.ACTION_UP:
                juego.entrada().soltar();
                performClick();
                break;
            case MotionEvent.ACTION_CANCEL:
                juego.entrada().soltar();
                break;
            default:
                break;
        }
        return true;
    }

    private void presionar(MotionEvent ev, int indice) {
        int x = escalado.virtualX(ev.getX(indice));
        int y = escalado.virtualY(ev.getY(indice));
        juego.entrada().presionar(juego.controles().tocar(x, y));
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onKeyDown(int codigo, KeyEvent evento) {
        Boton b = boton(codigo);
        if (b == null) {
            return super.onKeyDown(codigo, evento);
        }
        if (evento.getRepeatCount() == 0 || b.esDireccion()) {
            juego.pulsar(b);
        }
        return true;
    }

    private static Boton boton(int codigo) {
        switch (codigo) {
            case KeyEvent.KEYCODE_DPAD_UP:
                return Boton.ARRIBA;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                return Boton.ABAJO;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                return Boton.IZQUIERDA;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                return Boton.DERECHA;
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_Z:
                return Boton.ACEPTAR;
            case KeyEvent.KEYCODE_X:
            case KeyEvent.KEYCODE_ESCAPE:
                return Boton.CANCELAR;
            case KeyEvent.KEYCODE_F:
                return Boton.RAPIDO;
            default:
                return null;
        }
    }
}
