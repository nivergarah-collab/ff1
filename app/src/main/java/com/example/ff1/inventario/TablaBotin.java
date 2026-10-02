package com.example.ff1.inventario;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.example.ff1.combate.Bando;
import com.example.ff1.combate.CatalogoCombate;
import com.example.ff1.combate.Combatiente;
import com.example.ff1.motor.datos.Documentos;
import com.example.ff1.motor.datos.ErrorDeDatos;
import com.example.ff1.motor.datos.Nodo;
import com.example.ff1.motor.fuentes.Azar;
import com.example.ff1.motor.fuentes.FuenteContenido;

/**
 * Objetos que puede soltar cada enemigo al caer (documento {@code "botin"}). Cada entrada se
 * tira por separado: sale si {@code azar.entero(100) < probabilidad}.
 */
public final class TablaBotin {

    public static final String TIPO = "botin";
    public static final int VERSION = 1;

    /** Un objeto posible del botín de un enemigo. */
    public static final class Entrada {
        public final String objeto;
        public final int probabilidad;
        public final int cantidad;

        Entrada(String objeto, int probabilidad, int cantidad) {
            this.objeto = objeto;
            this.probabilidad = probabilidad;
            this.cantidad = cantidad;
        }
    }

    private final Map<String, List<Entrada>> porEnemigo;

    private TablaBotin(Map<String, List<Entrada>> porEnemigo) {
        this.porEnemigo = Collections.unmodifiableMap(porEnemigo);
    }

    public static TablaBotin cargar(FuenteContenido fuente, CatalogoCombate combate, CatalogoObjetos objetos) {
        return desde(fuente.cargar(TIPO, TIPO, VERSION), combate, objetos);
    }

    public static TablaBotin desde(Nodo doc, CatalogoCombate combate, CatalogoObjetos objetos) {
        Documentos.exigir(doc, TIPO, VERSION);
        Map<String, List<Entrada>> mapa = new LinkedHashMap<>();
        for (Nodo n : doc.lista("lista")) {
            String enemigo = n.texto("enemigo");
            if (combate.combatiente(enemigo).bando != Bando.ENEMIGO) {
                throw new ErrorDeDatos(n.ruta() + ".enemigo: \"" + enemigo + "\" no es un enemigo");
            }
            if (mapa.containsKey(enemigo)) {
                throw new ErrorDeDatos(n.ruta() + ".enemigo: \"" + enemigo + "\" repetido");
            }
            List<Entrada> entradas = new ArrayList<>();
            for (Nodo o : n.lista("objetos")) {
                String id = o.texto("id");
                objetos.objeto(id);
                entradas.add(new Entrada(id, rango(o, "probabilidad", 1, 100, -1),
                        rango(o, "cantidad", 1, 99, 1)));
            }
            mapa.put(enemigo, Collections.unmodifiableList(entradas));
        }
        return new TablaBotin(mapa);
    }

    public List<Entrada> de(String enemigo) {
        List<Entrada> l = porEnemigo.get(enemigo);
        return l == null ? Collections.<Entrada>emptyList() : l;
    }

    /** Tira el botín de los enemigos caídos; devuelve objeto → cantidad, en orden. */
    public Map<String, Integer> tirar(List<Combatiente> enemigos, Azar azar) {
        Map<String, Integer> botin = new LinkedHashMap<>();
        for (Combatiente e : enemigos) {
            if (e.vivo()) {
                continue;
            }
            for (Entrada en : de(e.definicion().id)) {
                if (azar.entero(100) < en.probabilidad) {
                    Integer previo = botin.get(en.objeto);
                    botin.put(en.objeto, (previo == null ? 0 : previo) + en.cantidad);
                }
            }
        }
        return botin;
    }

    /** Guarda el botín en el inventario; devuelve lo que no cupo. */
    public static Map<String, Integer> recoger(Map<String, Integer> botin, Inventario inventario) {
        Map<String, Integer> sobra = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> e : botin.entrySet()) {
            int caben = inventario.agregar(e.getKey(), e.getValue());
            if (caben < e.getValue()) {
                sobra.put(e.getKey(), e.getValue() - caben);
            }
        }
        return sobra;
    }

    private static int rango(Nodo n, String clave, int min, int max, int defecto) {
        int v = defecto < 0 ? n.entero(clave) : n.enteroO(clave, defecto);
        if (v < min || v > max) {
            throw new ErrorDeDatos(n.ruta() + "." + clave + ": " + v + " fuera del rango [" + min + ", " + max + "]");
        }
        return v;
    }
}
