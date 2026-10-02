package com.example.ff1.combate;

import java.util.ArrayList;
import java.util.List;

import com.example.ff1.motor.config.Configuracion;
import com.example.ff1.motor.config.ProveedorConfiguracion;
import com.example.ff1.motor.config.Registro;
import com.example.ff1.motor.fuentes.Azar;

/**
 * Acciones del combate (atacar, habilidad, objeto, huir) y cálculo de daño. Las fórmulas
 * leen la configuración vigente y toda tirada pasa por {@link Azar}. Los tipos de habilidad
 * y los estados se buscan en sus registros (ver {@link ReglasCombate}).
 *
 * <ul>
 * <li>Físico: {@code max(1, ataque × fuerzaFisica / 10 − defensa / 2)}, con varianza.</li>
 * <li>Mágico: {@code max(1, poderHabilidad + poderActor × fuerzaMagica / 10 − defensa / 4)}, con varianza.</li>
 * <li>Curación: {@code poderHabilidad + poderActor × fuerzaMagica / 10}, con varianza.</li>
 * <li>Varianza: × (100 + r) / 100, con r al azar en [−varianza, +varianza].</li>
 * <li>Huida: {@code huidaBase + 2 × (velocidad − velocidad media de los enemigos vivos)},
 * limitada a [5, 95] por ciento.</li>
 * </ul>
 */
public final class Acciones {

    private final ProveedorConfiguracion config;
    private final Azar azar;
    private final Registro<TipoHabilidad> tipos;
    private final Registro<EfectoEstado> estados;

    public Acciones(ProveedorConfiguracion config, Azar azar, Registro<TipoHabilidad> tipos,
            Registro<EfectoEstado> estados) {
        this.config = config;
        this.azar = azar;
        this.tipos = tipos;
        this.estados = estados;
    }

    public Configuracion configuracion() {
        return config.actual();
    }

    // --- Acciones -------------------------------------------------------------------------

    public ResultadoAccion atacar(Combatiente actor, Combatiente objetivo) {
        exigirRival(actor, objetivo);
        if (!objetivo.vivo()) {
            return fallo(ResultadoAccion.Accion.ATACAR, actor, objetivo, null,
                    ResultadoAccion.Fallo.OBJETIVO_CAIDO);
        }
        int hecho = danar(objetivo, danioFisico(actor, objetivo));
        return new ResultadoAccion(ResultadoAccion.Accion.ATACAR, actor, objetivo, null, hecho,
                null, ResultadoAccion.Fallo.NINGUNO);
    }

    /** Usa una habilidad: gasta su coste de magia y suma la potencia mágica del actor. */
    public ResultadoAccion usarHabilidad(Combatiente actor, Habilidad h, Combatiente objetivo) {
        return aplicar(ResultadoAccion.Accion.HABILIDAD, actor, h, objetivo, true);
    }

    /**
     * Usa el efecto de un objeto: se resuelve como una habilidad, sin coste de magia y sin
     * la potencia del actor. Quitar el objeto del inventario es tarea del llamador.
     */
    public ResultadoAccion usarObjeto(Combatiente actor, Habilidad efecto, Combatiente objetivo) {
        return aplicar(ResultadoAccion.Accion.OBJETO, actor, efecto, objetivo, false);
    }

    /** Intenta huir frente a {@code enemigos}; {@code permitida} es falso, por ejemplo, ante un jefe. */
    public ResultadoAccion huir(Combatiente actor, List<Combatiente> enemigos, boolean permitida) {
        if (!permitida) {
            return fallo(ResultadoAccion.Accion.HUIR, actor, null, null,
                    ResultadoAccion.Fallo.HUIDA_PROHIBIDA);
        }
        boolean logrado = azar.entero(100) < probabilidadHuida(actor, enemigos);
        return new ResultadoAccion(ResultadoAccion.Accion.HUIR, actor, null, null, 0, null,
                logrado ? ResultadoAccion.Fallo.NINGUNO : ResultadoAccion.Fallo.HUIDA_FALLIDA);
    }

    /** Si el combatiente puede actuar en su turno (vivo y sin un estado que lo impida). */
    public boolean puedeActuar(Combatiente c) {
        if (!c.vivo()) {
            return false;
        }
        for (String e : c.estados().keySet()) {
            if (estados.obtener(e).impideActuar()) {
                return false;
            }
        }
        return true;
    }

    /** Turno perdido por un estado: no hace nada, pero cuenta como turno. */
    public ResultadoAccion perderTurno(Combatiente actor) {
        return new ResultadoAccion(ResultadoAccion.Accion.PERDER_TURNO, actor, null, null, 0,
                null, ResultadoAccion.Fallo.NINGUNO);
    }

    /**
     * Cierra el turno de {@code c}: aplica el daño de sus estados y les descuenta un turno.
     * Devuelve el daño sufrido.
     */
    public int terminarTurno(Combatiente c) {
        Configuracion actual = config.actual();
        int total = 0;
        for (String e : new ArrayList<>(c.estados().keySet())) {
            int d = estados.obtener(e).danioAlTerminarTurno(c, actual);
            if (d > 0 && c.vivo()) {
                total += c.recibirDanio(d);
            }
        }
        c.terminarTurno();
        return total;
    }

    // --- Cálculo de daño (también lo usan los tipos de habilidad) --------------------------

    public int danioFisico(Combatiente atacante, Combatiente defensor) {
        Configuracion c = config.actual();
        int base = atacante.ataque() * c.entero(ConfiguracionCombate.FUERZA_FISICA) / 10
                - defensor.defensa() / 2;
        return Math.max(1, variar(Math.max(1, base), c));
    }

    public int danioMagico(int poderHabilidad, int poderActor, Combatiente defensor) {
        Configuracion c = config.actual();
        int base = poderHabilidad + poderActor * c.entero(ConfiguracionCombate.FUERZA_MAGICA) / 10
                - defensor.defensa() / 4;
        return Math.max(1, variar(Math.max(1, base), c));
    }

    public int curacion(int poderHabilidad, int poderActor) {
        Configuracion c = config.actual();
        return variar(poderHabilidad + poderActor * c.entero(ConfiguracionCombate.FUERZA_MAGICA) / 10, c);
    }

    /** Resta daño aplicando los estados del objetivo; devuelve la vida realmente quitada. */
    public int danar(Combatiente objetivo, int cantidad) {
        Configuracion actual = config.actual();
        int ajustado = cantidad;
        for (String e : objetivo.estados().keySet()) {
            ajustado = estados.obtener(e).ajustarDanio(ajustado, actual);
        }
        int hecho = objetivo.recibirDanio(Math.max(0, ajustado));
        if (hecho > 0) {
            for (String e : new ArrayList<>(objetivo.estados().keySet())) {
                if (estados.obtener(e).seQuitaConDanio()) {
                    objetivo.quitarEstado(e);
                }
            }
        }
        return hecho;
    }

    public int probabilidadHuida(Combatiente actor, List<Combatiente> enemigos) {
        int suma = 0;
        int vivos = 0;
        for (Combatiente e : enemigos) {
            if (e.vivo()) {
                suma += e.velocidad();
                vivos++;
            }
        }
        int media = vivos == 0 ? actor.velocidad() : suma / vivos;
        int p = config.actual().entero(ConfiguracionCombate.HUIDA_BASE) + 2 * (actor.velocidad() - media);
        return Math.max(5, Math.min(95, p));
    }

    // --- Interno --------------------------------------------------------------------------

    private ResultadoAccion aplicar(ResultadoAccion.Accion accion, Combatiente actor, Habilidad h,
            Combatiente objetivo, boolean esHabilidad) {
        exigirObjetivo(actor, h, objetivo);
        TipoHabilidad tipo = tipos.obtener(h.tipo);
        if (!objetivo.vivo()) {
            return fallo(accion, actor, objetivo, h, ResultadoAccion.Fallo.OBJETIVO_CAIDO);
        }
        if (esHabilidad && !actor.gastarMagia(h.coste)) {
            return fallo(accion, actor, objetivo, h, ResultadoAccion.Fallo.SIN_MAGIA);
        }
        int cantidad = tipo.resolver(this, esHabilidad ? actor.poder() : 0, h, objetivo);
        String estado = null;
        if (h.estado != null && objetivo.vivo()) {
            estados.obtener(h.estado);
            objetivo.aplicarEstado(h.estado, h.duracionEstado);
            estado = h.estado;
        }
        return new ResultadoAccion(accion, actor, objetivo, h, cantidad, estado,
                ResultadoAccion.Fallo.NINGUNO);
    }

    private int variar(int base, Configuracion c) {
        int v = c.entero(ConfiguracionCombate.VARIANZA);
        if (v == 0) {
            return base;
        }
        int r = azar.entero(2 * v + 1) - v;
        return base * (100 + r) / 100;
    }

    private static void exigirRival(Combatiente actor, Combatiente objetivo) {
        if (actor.bando() == objetivo.bando()) {
            throw new IllegalArgumentException(actor + " no puede atacar a un aliado: " + objetivo);
        }
    }

    private static void exigirObjetivo(Combatiente actor, Habilidad h, Combatiente objetivo) {
        boolean valido;
        switch (h.objetivo) {
            case ENEMIGO:
                valido = actor.bando() != objetivo.bando();
                break;
            case ALIADO:
                valido = actor.bando() == objetivo.bando();
                break;
            default:
                valido = actor == objetivo;
        }
        if (!valido) {
            throw new IllegalArgumentException(h.id + " no se puede usar sobre " + objetivo);
        }
    }

    private static ResultadoAccion fallo(ResultadoAccion.Accion accion, Combatiente actor,
            Combatiente objetivo, Habilidad h, ResultadoAccion.Fallo fallo) {
        return new ResultadoAccion(accion, actor, objetivo, h, 0, null, fallo);
    }
}
