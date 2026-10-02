package com.example.ff1.pueblo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.example.ff1.guion.Guion;
import com.example.ff1.inventario.CatalogoObjetos;
import com.example.ff1.inventario.DefinicionObjeto;
import com.example.ff1.motor.datos.Documentos;
import com.example.ff1.motor.datos.ErrorDeDatos;
import com.example.ff1.motor.datos.Nodo;
import com.example.ff1.motor.fuentes.FuenteContenido;
import com.example.ff1.mundo.Mapa;

/**
 * Servicios de los pueblos (documento {@code "servicios"}, recurso {@code "servicios"}): tiendas,
 * posadas y vecinos con los que se habla. Los mapas los refieren por id desde sus {@code lugares}.
 * El documento es opcional: sin él, el paquete no tiene servicios.
 */
public final class Servicios {

    public static final String TIPO = "servicios";
    public static final int VERSION = 1;

    public static final class Tienda {
        public final String id;
        public final String nombre;
        public final String vendedor;
        /** Ids de objetos que vende, en orden. */
        public final List<String> objetos;

        Tienda(String id, String nombre, String vendedor, List<String> objetos) {
            this.id = id;
            this.nombre = nombre;
            this.vendedor = vendedor;
            this.objetos = Collections.unmodifiableList(objetos);
        }
    }

    public static final class Posada {
        public final String id;
        public final String nombre;
        public final String posadero;
        /** Oro que cuesta una noche. */
        public final int precio;

        Posada(String id, String nombre, String posadero, int precio) {
            this.id = id;
            this.nombre = nombre;
            this.posadero = posadero;
            this.precio = precio;
        }
    }

    public static final class Vecino {
        public final String id;
        public final String nombre;
        /** Id de una escena de {@code escenas/}. */
        public final String escena;

        Vecino(String id, String nombre, String escena) {
            this.id = id;
            this.nombre = nombre;
            this.escena = escena;
        }
    }

    private final Map<String, Tienda> tiendas = new LinkedHashMap<>();
    private final Map<String, Posada> posadas = new LinkedHashMap<>();
    private final Map<String, Vecino> vecinos = new LinkedHashMap<>();

    private Servicios() {
    }

    /** Sin servicios (paquete sin {@code servicios.json}). */
    public static Servicios vacio() {
        return new Servicios();
    }

    /** Carga {@code servicios.json} si existe; comprueba que sus objetos y escenas existen. */
    public static Servicios cargar(FuenteContenido fuente, CatalogoObjetos objetos) {
        if (!fuente.existe(TIPO)) {
            return vacio();
        }
        Nodo doc = fuente.cargar(TIPO, TIPO, VERSION);
        Documentos.exigir(doc, TIPO, VERSION);
        Servicios s = new Servicios();
        if (doc.tiene("tiendas")) {
            for (Nodo n : doc.lista("tiendas")) {
                List<String> ids = new ArrayList<>();
                for (Nodo o : n.lista("objetos")) {
                    DefinicionObjeto def = objetos.objeto(o.comoTexto());
                    if (def.precio < 1) {
                        throw new ErrorDeDatos(o.ruta() + ": \"" + def.id + "\" no tiene precio y no se puede vender");
                    }
                    ids.add(def.id);
                }
                if (ids.isEmpty()) {
                    throw new ErrorDeDatos(n.ruta() + ".objetos: la tienda no vende nada");
                }
                s.poner(s.tiendas, n.texto("id"), new Tienda(n.texto("id"), n.texto("nombre"), n.texto("vendedor"), ids), n);
            }
        }
        if (doc.tiene("posadas")) {
            for (Nodo n : doc.lista("posadas")) {
                s.poner(s.posadas, n.texto("id"), new Posada(n.texto("id"), n.texto("nombre"), n.texto("posadero"),
                        Documentos.rango(n, "precio", 0, 9999)), n);
            }
        }
        if (doc.tiene("vecinos")) {
            for (Nodo n : doc.lista("vecinos")) {
                Guion.cargar(fuente, n.texto("escena"));
                s.poner(s.vecinos, n.texto("id"), new Vecino(n.texto("id"), n.texto("nombre"), n.texto("escena")), n);
            }
        }
        return s;
    }

    private <T> void poner(Map<String, T> mapa, String id, T valor, Nodo n) {
        if (mapa.put(id, valor) != null) {
            throw new ErrorDeDatos(n.ruta() + ".id: \"" + id + "\" está repetido");
        }
    }

    /** Comprueba que cada lugar del mapa apunta a un servicio que existe. */
    public void validarMapa(Mapa mapa) {
        for (Mapa.Lugar l : mapa.lugares()) {
            boolean ok;
            switch (l.tipo) {
                case Mapa.LUGAR_TIENDA:
                    ok = tiendas.containsKey(l.ref);
                    break;
                case Mapa.LUGAR_POSADA:
                    ok = posadas.containsKey(l.ref);
                    break;
                default:
                    ok = vecinos.containsKey(l.ref);
            }
            if (!ok) {
                throw new ErrorDeDatos("mapas/" + mapa.id + ".lugares: " + l.tipo + " \"" + l.ref
                        + "\" no está en servicios");
            }
        }
    }

    public Tienda tienda(String id) {
        return buscar(tiendas, id, "tienda");
    }

    public Posada posada(String id) {
        return buscar(posadas, id, "posada");
    }

    public Vecino vecino(String id) {
        return buscar(vecinos, id, "vecino");
    }

    private static <T> T buscar(Map<String, T> mapa, String id, String que) {
        T t = mapa.get(id);
        if (t == null) {
            throw new ErrorDeDatos("servicios: no hay " + que + " \"" + id + "\"");
        }
        return t;
    }
}
