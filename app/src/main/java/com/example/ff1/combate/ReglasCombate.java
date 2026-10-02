package com.example.ff1.combate;

import com.example.ff1.motor.config.Configuracion;
import com.example.ff1.motor.config.Registro;

/**
 * Tipos de habilidad y estados que trae el motor. Un juego puede registrar más en los
 * mismos registros antes de cargar su contenido.
 *
 * <ul>
 * <li>{@code danio}: daño mágico al objetivo.</li>
 * <li>{@code curacion}: devuelve vida al objetivo.</li>
 * <li>{@code revivir}: levanta a un caído con {@code poder} por ciento de su vida máxima (mínimo 1);
 * solo fuera de combate.</li>
 * <li>{@code alteracion}: aplica su estado; si tiene poder, además hace daño mágico.</li>
 * <li>{@code veneno}: quita {@code venenoPorCiento} de la vida máxima (mínimo 1) al terminar cada turno.</li>
 * <li>{@code sueno}: pierde sus turnos; se despierta al recibir daño.</li>
 * <li>{@code proteccion}: recibe solo {@code proteccionPorCiento} del daño (mínimo 1).</li>
 * </ul>
 */
public final class ReglasCombate {

    private ReglasCombate() {
    }

    public static Registro<TipoHabilidad> tiposHabilidad() {
        TipoHabilidad danio = (acc, poderActor, h, obj) ->
                acc.danar(obj, acc.danioMagico(h.poder, poderActor, obj));
        return new Registro<TipoHabilidad>("tipo de habilidad")
                .registrar("danio", danio)
                .registrar("curacion", (acc, poderActor, h, obj) ->
                        obj.curar(acc.curacion(h.poder, poderActor)))
                .registrar("revivir", new TipoHabilidad() {
                    @Override
                    public int resolver(Acciones acc, int poderActor, Habilidad h, Combatiente obj) {
                        return obj.revivir(obj.vidaMaxima() * h.poder / 100);
                    }

                    @Override
                    public boolean actuaSobreCaidos() {
                        return true;
                    }
                })
                .registrar("alteracion", (acc, poderActor, h, obj) ->
                        h.poder > 0 ? danio.resolver(acc, poderActor, h, obj) : 0);
    }

    public static Registro<EfectoEstado> estados() {
        return new Registro<EfectoEstado>("estado")
                .registrar("veneno", new EfectoEstado() {
                    @Override
                    public int danioAlTerminarTurno(Combatiente c, Configuracion config) {
                        return Math.max(1, c.vidaMaxima()
                                * config.entero(ConfiguracionCombate.VENENO_POR_CIENTO) / 100);
                    }
                })
                .registrar("sueno", new EfectoEstado() {
                    @Override
                    public boolean impideActuar() {
                        return true;
                    }

                    @Override
                    public boolean seQuitaConDanio() {
                        return true;
                    }
                })
                .registrar("proteccion", new EfectoEstado() {
                    @Override
                    public int ajustarDanio(int danio, Configuracion config) {
                        return Math.max(1, danio
                                * config.entero(ConfiguracionCombate.PROTECCION_POR_CIENTO) / 100);
                    }
                });
    }
}
