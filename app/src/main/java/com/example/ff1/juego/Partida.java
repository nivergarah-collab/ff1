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
import com.example.ff1.guion.Guion;
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
import com.example.ff1.pueblo.Servicios;

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
    private final Registro<TipoHabilidad> tipos;
    private final List<Heroe> grupo;
    private final List<Heroe> orden;
    private final Inventario inventario;
    private final Explorador explorador;
    private final Encuentros encuentros;
    private final Guion introduccion;
    private final FuenteContenido fuente;
    private final TablaEncuentros tabla;
    private final Servicios servicios;
    private final Map<String, Mapa> mapas = new LinkedHashMap<>();
    private int oro;

    private Partida(FuenteContenido fuente, TablaEncuentros tabla, Servicios servicios, ProveedorConfiguracion config, Azar azar, CatalogoCombate catalogo, CatalogoObjetos objetos,
            TablaBotin botin, Acciones acciones, Registro<TipoHabilidad> tipos, List<Heroe> grupo, Inventario inventario, Explorador explorador,
            Encuentros encuentros, Guion introduccion, int oro) {
        this.fuente = fuente;
        this.tabla = tabla;
        this.servicios = servicios;
        this.config = config;
        this.azar = azar;
        this.catalogo = catalogo;
        this.objetos = objetos;
        this.botin = botin;
        this.acciones = acciones;
        this.tipos = tipos;
        this.orden = grupo;
        this.grupo = Collections.unmodifiableList(grupo);
        this.inventario = inventario;
        this.explorador = explorador;
        this.encuentros = encuentros;
        this.introduccion = introduccion;
        this.oro = oro;
    }

    /** Parámetros que declaran los módulos del juego. */
    public static EsquemaConfiguracion esquema() {
        return ConfiguracionJuego.declarar(ConfiguracionMundo.declarar(ConfiguracionInventario.declarar(
                ConfiguracionCombate.declarar(new EsquemaConfiguracion()))));
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
        Servicios servicios = Servicios.cargar(fuente, objetos);
        servicios.validarMapa(mapa);
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
        Guion introduccion = inicio.tiene("introduccion") ? Guion.cargar(fuente, inicio.texto("introduccion")) : null;
        int oro = Documentos.rangoO(inicio, "oro", 0, 999_999, 0);
        Acciones acciones = new Acciones(config, azar, tipos, estados);
        Partida p = new Partida(fuente, tabla, servicios, config, azar, catalogo, objetos, botin, acciones, tipos, grupo,
                inventario, new Explorador(mapa), new Encuentros(config, tabla, azar), introduccion, oro);
        p.mapas.put(mapa.id, mapa);
        return p;
    }

    /** Escena de texto del paquete (para los vecinos). */
    public Guion escena(String id) {
        return Guion.cargar(fuente, id);
    }

    public Servicios servicios() {
        return servicios;
    }

    /** Mapa por id, cargado una vez y validado contra encuentros y servicios. */
    public Mapa mapa(String id) {
        Mapa m = mapas.get(id);
        if (m == null) {
            m = Mapa.cargar(fuente, id);
            tabla.validarMapa(m);
            servicios.validarMapa(m);
            mapas.put(id, m);
        }
        return m;
    }

    /** Pone al grupo en (x, y) de otro mapa (al cruzar una salida) y reinicia la cuenta de encuentros. */
    public void irAMapa(String id, int x, int y) {
        explorador.colocar(mapa(id), x, y);
        encuentros.reiniciar();
    }

    /** Cruza la salida si la casilla del grupo tiene una; devuelve si cambió de mapa. */
    public boolean cruzarSalida() {
        Mapa.Salida s = explorador.mapa().salidaEn(explorador.x(), explorador.y());
        if (s == null) {
            return false;
        }
        irAMapa(s.mapa, s.destinoX, s.destinoY);
        return true;
    }

    /** Lugar (tienda, posada, vecino) justo delante del grupo, o {@code null}. */
    public Mapa.Lugar lugarDelante() {
        Explorador ex = explorador;
        int x = ex.x() + ex.mirando().dx;
        int y = ex.y() + ex.mirando().dy;
        return ex.mapa().dentro(x, y) ? ex.mapa().lugarEn(x, y) : null;
    }

    public enum Compra { HECHA, SIN_ORO, SIN_ESPACIO }

    public enum Venta { HECHA, NO_VENDIBLE, NO_LLEVA }

    public enum Descanso { HECHO, SIN_ORO, NADA_QUE_CURAR }

    /** Oro que se paga por un objeto del inventario: su precio por {@code pueblo.ventaPorCiento}. */
    public int precioVenta(String idObjeto) {
        DefinicionObjeto o = objetos.objeto(idObjeto);
        if (o.categoria == DefinicionObjeto.Categoria.CLAVE) {
            return 0;
        }
        return o.precio * config.actual().entero(ConfiguracionJuego.VENTA_POR_CIENTO) / 100;
    }

    /** Compra una unidad al precio del objeto. */
    public Compra comprar(String idObjeto) {
        DefinicionObjeto o = objetos.objeto(idObjeto);
        if (oro < o.precio) {
            return Compra.SIN_ORO;
        }
        if (inventario.espacio(idObjeto) < 1) {
            return Compra.SIN_ESPACIO;
        }
        oro -= o.precio;
        inventario.agregar(idObjeto, 1);
        return Compra.HECHA;
    }

    /** Vende una unidad del inventario; los objetos clave y los de precio 0 no se venden. */
    public Venta vender(String idObjeto) {
        if (inventario.cantidad(idObjeto) < 1) {
            return Venta.NO_LLEVA;
        }
        int precio = precioVenta(idObjeto);
        if (precio < 1) {
            return Venta.NO_VENDIBLE;
        }
        inventario.quitar(idObjeto, 1);
        oro = (int) Math.min(999_999L, (long) oro + precio);
        return Venta.HECHA;
    }

    /** Una noche en la posada: vida y magia al máximo y los caídos se levantan. Si nadie lo necesita, no cobra. */
    public Descanso descansar(Servicios.Posada posada) {
        boolean necesario = false;
        for (Heroe h : grupo) {
            if (h.vida() < h.estadisticas().vida || h.magia() < h.estadisticas().magia) {
                necesario = true;
            }
        }
        if (!necesario) {
            return Descanso.NADA_QUE_CURAR;
        }
        if (oro < posada.precio) {
            return Descanso.SIN_ORO;
        }
        oro -= posada.precio;
        for (Heroe h : grupo) {
            h.restaurar();
        }
        return Descanso.HECHO;
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

    /** Cambia de sitio a dos héroes del grupo (formación); el combate usa este orden. */
    public void intercambiarHeroes(int a, int b) {
        Collections.swap(orden, a, b);
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

    /** Escena de apertura de la partida nueva, o {@code null} si el paquete no trae una. */
    public Guion introduccion() {
        return introduccion;
    }

    /** Nombre que el jugador puso a cada héroe, por id de clase (para los marcadores de las escenas). */
    public Map<String, String> nombresPorClase() {
        Map<String, String> r = new LinkedHashMap<>();
        for (Heroe h : grupo) {
            if (!r.containsKey(h.clase().id)) {
                r.put(h.clase().id, h.nombre());
            }
        }
        return r;
    }

    public int oro() {
        return oro;
    }

    /** Si el objeto se puede usar en combate (revivir, por ejemplo, solo vale fuera de él). */
    public boolean sirveEnCombate(DefinicionObjeto o) {
        return o.efecto != null && !tipos.obtener(o.efecto.tipo).actuaSobreCaidos();
    }

    /** Resultado de usar un objeto fuera de combate. */
    public enum Uso {
        /** Hizo efecto y se gastó una unidad. */
        USADO,
        /** No habría efecto (vida llena, héroe caído...): no se gasta nada. */
        SIN_EFECTO,
        /** No es un consumible aplicable al grupo, o no quedan unidades. */
        NO_USABLE,
        /** El héroe no tiene magia suficiente. */
        SIN_MAGIA
    }

    /** Resultado de cambiar el equipo de un héroe. */
    public enum Cambio {
        HECHO,
        /** No es equipo, o la clase del héroe no lo puede llevar, o no hay nada en esa ranura. */
        NO_PUEDE,
        /** El grupo no lleva esa pieza. */
        NO_LLEVA,
        /** La pieza que se devuelve no cabe en el inventario. */
        SIN_ESPACIO
    }

    /** Pone una pieza del inventario a un héroe; la que llevaba vuelve al inventario. */
    public Cambio equipar(Heroe h, String idPieza) {
        DefinicionObjeto o = objetos.objeto(idPieza);
        if (!o.equipablePor(h.clase().id)) {
            return Cambio.NO_PUEDE;
        }
        if (inventario.cantidad(idPieza) < 1) {
            return Cambio.NO_LLEVA;
        }
        DefinicionObjeto puesta = h.equipo().get(o.ranura);
        if (puesta != null && !puesta.id.equals(idPieza) && inventario.espacio(puesta.id) < 1) {
            return Cambio.SIN_ESPACIO;
        }
        inventario.quitar(idPieza, 1);
        DefinicionObjeto anterior = h.equipar(o);
        if (anterior != null) {
            inventario.agregar(anterior.id, 1);
        }
        return Cambio.HECHO;
    }

    /** Quita la pieza de una ranura y la guarda en el inventario. */
    public Cambio quitarEquipo(Heroe h, String ranura) {
        DefinicionObjeto puesta = h.equipo().get(ranura);
        if (puesta == null) {
            return Cambio.NO_PUEDE;
        }
        if (inventario.espacio(puesta.id) < 1) {
            return Cambio.SIN_ESPACIO;
        }
        h.desequipar(ranura);
        inventario.agregar(puesta.id, 1);
        return Cambio.HECHO;
    }

    /** Piezas del inventario que {@code h} puede ponerse en {@code ranura}. */
    public List<DefinicionObjeto> piezasPara(Heroe h, String ranura) {
        List<DefinicionObjeto> r = new ArrayList<>();
        for (String id : inventario.contenido().keySet()) {
            DefinicionObjeto o = objetos.objeto(id);
            if (o.equipablePor(h.clase().id) && ranura.equals(o.ranura)) {
                r.add(o);
            }
        }
        return r;
    }

    /** Si una habilidad se puede lanzar fuera de combate: va a aliados y no aplica estados. */
    public static boolean sirveFueraDeCombate(Habilidad h) {
        return h.objetivo != Habilidad.Objetivo.ENEMIGO && h.estado == null;
    }

    /**
     * Lanza una habilidad de {@code lanzador} sobre {@code objetivo} fuera de combate, gastando
     * magia. Si no tendría efecto (vida llena, objetivo caído) no se gasta nada. Las habilidades
     * dirigidas a uno mismo ignoran el objetivo recibido.
     */
    public Uso usarHabilidad(Heroe lanzador, Habilidad h, Heroe objetivo) {
        Heroe destino = h.objetivo == Habilidad.Objetivo.SI_MISMO ? lanzador : objetivo;
        if (!sirveFueraDeCombate(h) || lanzador.vida() <= 0) {
            return Uso.NO_USABLE;
        }
        Combatiente quien = lanzador.entrarEnCombate();
        Combatiente sobre = destino == lanzador ? quien : destino.entrarEnCombate();
        ResultadoAccion r = acciones.usarHabilidad(quien, h, sobre);
        if (r.fallo == ResultadoAccion.Fallo.SIN_MAGIA) {
            return Uso.SIN_MAGIA;
        }
        if (!r.exito() || sobre.vida() == destino.vida()) {
            return Uso.SIN_EFECTO;
        }
        lanzador.salirDeCombate(quien);
        if (destino != lanzador) {
            destino.salirDeCombate(sobre);
        }
        return Uso.USADO;
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
