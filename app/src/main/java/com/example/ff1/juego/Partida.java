package com.example.ff1.juego;

import com.example.ff1.combate.Acciones;
import com.example.ff1.combate.Bando;
import com.example.ff1.combate.CatalogoCombate;
import com.example.ff1.combate.Combate;
import com.example.ff1.combate.Combatiente;
import com.example.ff1.combate.ConfiguracionCombate;
import com.example.ff1.combate.DefinicionCombatiente;
import com.example.ff1.combate.EfectoEstado;
import com.example.ff1.combate.Habilidad;
import com.example.ff1.combate.Recompensa;
import com.example.ff1.combate.ResultadoAccion;
import com.example.ff1.combate.ReglasCombate;
import com.example.ff1.combate.TipoHabilidad;
import com.example.ff1.inventario.CatalogoObjetos;
import com.example.ff1.inventario.ConfiguracionInventario;
import com.example.ff1.inventario.DefinicionObjeto;
import com.example.ff1.inventario.Inventario;
import com.example.ff1.inventario.TablaBotin;
import com.example.ff1.motor.config.EsquemaConfiguracion;
import com.example.ff1.motor.config.ProveedorConfiguracion;
import com.example.ff1.motor.config.Registro;
import com.example.ff1.motor.datos.Documentos;
import com.example.ff1.motor.datos.ErrorDeDatos;
import com.example.ff1.motor.datos.Nodo;
import com.example.ff1.motor.fuentes.Azar;
import com.example.ff1.motor.fuentes.FuenteContenido;
import com.example.ff1.mundo.ConfiguracionMundo;
import com.example.ff1.mundo.Encuentros;
import com.example.ff1.mundo.Explorador;
import com.example.ff1.mundo.Mapa;
import com.example.ff1.mundo.TablaEncuentros;
import com.example.ff1.progresion.Heroe;
import com.example.ff1.progresion.Reparto;
import com.example.ff1.progresion.TablaProgresion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Estado de una partida y el ensamblaje de los módulos (combate, progresión, inventario y mundo)
 * a partir de un paquete de contenido. Las pantallas leen y cambian la partida; no conocen ids.
 */
public final class Partida {

    public static final String TIPO_INICIO = "inicio";
    public static final int VERSION_INICIO = 1;

    /** Lo que dejó un combate al terminar. */
    public static final class Desenlace {
        public final Combate.Estado estado;
        /** {@code null} si no hubo victoria. */
        public final Recompensa recompensa;
        /** Héroe → niveles subidos (solo los que recibieron experiencia). */
        public final Map<Heroe, Integer> niveles;
        public final Map<String, Integer> botin;

        Desenlace(Combate.Estado estado, Recompensa recompensa, Map<Heroe, Integer> niveles,
                Map<String, Integer> botin) {
            this.estado = estado;
            this.recompensa = recompensa;
            this.niveles = niveles;
            this.botin = botin;
        }
    }

    private final ProveedorConfiguracion config;
    private final Azar azar;
    private final CatalogoCombate catalogo;
    private final CatalogoObjetos objetos;
    private final TablaBotin botin;
    private final Acciones acciones;
    private final List<Heroe> grupo;
    private final Inventario inventario;
    private final Explorador explorador;
    private final Encuentros encuentros;
    private int oro;

    private Partida(ProveedorConfiguracion config, Azar azar, CatalogoCombate catalogo, CatalogoObjetos objetos,
            TablaBotin botin, Acciones acciones, List<Heroe> grupo, Inventario inventario, Explorador explorador,
            Encuentros encuentros, int oro) {
        this.config = config;
        this.azar = azar;
        this.catalogo = catalogo;
        this.objetos = objetos;
        this.botin = botin;
        this.acciones = acciones;
        this.grupo = grupo;
        this.inventario = inventario;
        this.explorador = explorador;
        this.encuentros = encuentros;
        this.oro = oro;
    }

    /** Parámetros que declaran los módulos del juego. */
    public static EsquemaConfiguracion esquema() {
        return ConfiguracionMundo.declarar(ConfiguracionInventario.declarar(
                ConfiguracionCombate.declarar(new EsquemaConfiguracion())));
    }

    /** Partida nueva según {@code inicio.json}; lanza {@link ErrorDeDatos} si el paquete no es válido. */
    public static Partida nueva(FuenteContenido fuente, Azar azar) {
        ProveedorConfiguracion config = new ProveedorConfiguracion(esquema().porDefecto());
        config.reemplazarDesde(fuente.cargar("configuracion", EsquemaConfiguracion.TIPO, EsquemaConfiguracion.VERSION));
        Registro<TipoHabilidad> tipos = ReglasCombate.tiposHabilidad();
        Registro<EfectoEstado> estados = ReglasCombate.estados();
        CatalogoCombate catalogo = CatalogoCombate.cargar(fuente, tipos, estados);
        CatalogoObjetos objetos = CatalogoObjetos.cargar(fuente, catalogo, tipos, estados);
        TablaProgresion progresion = TablaProgresion.cargar(fuente, catalogo);
        TablaBotin botin = TablaBotin.cargar(fuente, catalogo, objetos);
        TablaEncuentros tabla = TablaEncuentros.cargar(fuente, catalogo);

        Nodo inicio = fuente.cargar(TIPO_INICIO, TIPO_INICIO, VERSION_INICIO);
        Mapa mapa = Mapa.cargar(fuente, inicio.texto("mapa"));
        tabla.validarMapa(mapa);
        List<Nodo> ng = inicio.lista("grupo");
        Documentos.dentro(inicio, "grupo", ng.size(), 1, 6);
        List<Heroe> grupo = new ArrayList<>();
        for (Nodo n : ng) {
            DefinicionCombatiente clase = catalogo.combatiente(n.texto("clase"));
            if (clase.bando != Bando.HEROE) {
                throw new ErrorDeDatos(n.ruta() + ".clase: \"" + clase.id + "\" no es un héroe");
            }
            grupo.add(new Heroe(clase, n.texto("nombre"), progresion));
        }
        Inventario inventario = new Inventario(config.actual().entero(ConfiguracionInventario.MAXIMO_POR_OBJETO));
        if (inicio.tiene("objetos")) {
            for (Nodo n : inicio.lista("objetos")) {
                objetos.objeto(n.texto("id"));
                inventario.agregar(n.texto("id"), Documentos.rango(n, "cantidad", 1, 999));
            }
        }
        int oro = Documentos.rangoO(inicio, "oro", 0, 999_999, 0);
        Acciones acciones = new Acciones(config, azar, tipos, estados);
        return new Partida(config, azar, catalogo, objetos, botin, acciones, Collections.unmodifiableList(grupo),
                inventario, new Explorador(mapa), new Encuentros(config, tabla, azar), oro);
    }

    public ProveedorConfiguracion config() {
        return config;
    }

    public CatalogoObjetos objetos() {
        return objetos;
    }

    public List<Heroe> grupo() {
        return grupo;
    }

    public Inventario inventario() {
        return inventario;
    }

    public Explorador explorador() {
        return explorador;
    }

    public Encuentros encuentros() {
        return encuentros;
    }

    public CatalogoCombate catalogo() {
        return catalogo;
    }

    public int oro() {
        return oro;
    }

    /** Resultado de usar un objeto fuera de combate. */
    public enum Uso {
        /** Hizo efecto y se gastó una unidad. */
        USADO,
        /** No habría efecto (vida llena, héroe caído...): no se gasta nada. */
        SIN_EFECTO,
        /** No es un consumible aplicable al grupo, o no quedan unidades. */
        NO_USABLE
    }

    /**
     * Usa un consumible sobre un héroe fuera de combate, con las mismas reglas que en combate
     * (el efecto del objeto es el de {@code objetos.json}). Solo sirven los efectos dirigidos a
     * aliados o a uno mismo; si no cambia nada, no se gasta la unidad.
     */
    public Uso usarObjeto(String id, Heroe objetivo) {
        DefinicionObjeto o = objetos.objeto(id);
        if (o.categoria != DefinicionObjeto.Categoria.CONSUMIBLE || o.efecto == null
                || o.efecto.objetivo == Habilidad.Objetivo.ENEMIGO || inventario.cantidad(id) < 1) {
            return Uso.NO_USABLE;
        }
        Combatiente c = objetivo.entrarEnCombate();
        ResultadoAccion r = acciones.usarObjeto(c, o.efecto, c);
        if (!r.exito() || (c.vida() == objetivo.vida() && c.estados().isEmpty())) {
            return Uso.SIN_EFECTO;
        }
        objetivo.salirDeCombate(c);
        inventario.quitar(id, 1);
        return Uso.USADO;
    }

    /** Combate contra los enemigos de un encuentro; los héroes entran con su vida y magia actuales. */
    public Combate empezarCombate(List<String> enemigos) {
        List<Combatiente> heroes = new ArrayList<>();
        for (Heroe h : grupo) {
            heroes.add(h.entrarEnCombate());
        }
        return new Combate(config, acciones, azar, heroes, Encuentros.crearEnemigos(enemigos, catalogo), true);
    }

    /** Guarda en los héroes cómo terminaron y, si hubo victoria, reparte experiencia, oro y botín. */
    public Desenlace terminarCombate(Combate combate) {
        if (!combate.terminado()) {
            throw new IllegalStateException("el combate no terminó");
        }
        for (int i = 0; i < grupo.size(); i++) {
            grupo.get(i).salirDeCombate(combate.heroes().get(i));
        }
        if (combate.estado() != Combate.Estado.VICTORIA) {
            return new Desenlace(combate.estado(), null, Collections.<Heroe, Integer>emptyMap(),
                    Collections.<String, Integer>emptyMap());
        }
        Recompensa r = combate.recompensa();
        Map<Heroe, Integer> niveles = Reparto.experiencia(grupo, r);
        oro = (int) Math.min(999_999L, (long) oro + r.oro);
        Map<String, Integer> tirado = botin.tirar(combate.enemigos(), azar);
        TablaBotin.recoger(tirado, inventario);
        return new Desenlace(combate.estado(), r, niveles, new LinkedHashMap<>(tirado));
    }
}
