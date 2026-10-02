package com.example.ff1.inventario;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.example.ff1.combate.Bando;
import com.example.ff1.combate.CatalogoCombate;
import com.example.ff1.combate.Habilidad;
import com.example.ff1.motor.config.Registro;
import com.example.ff1.motor.datos.Documentos;
import com.example.ff1.motor.datos.ErrorDeDatos;
import com.example.ff1.motor.datos.Nodo;
import com.example.ff1.motor.fuentes.FuenteContenido;

/**
 * Objetos y ranuras de equipo de un paquete de contenido, validados (documento
 * {@code "objetos"}, ver docs/contrato-de-datos.md).
 */
public final class CatalogoObjetos {

    public static final String TIPO = "objetos";
    public static final int VERSION = 1;
    private static final Pattern ID = Pattern.compile("[a-z0-9]+(-[a-z0-9]+)*");

    private final List<String> ranuras;
    private final Map<String, DefinicionObjeto> objetos;

    private CatalogoObjetos(List<String> ranuras, Map<String, DefinicionObjeto> objetos) {
        this.ranuras = Collections.unmodifiableList(ranuras);
        this.objetos = Collections.unmodifiableMap(objetos);
    }

    public static CatalogoObjetos cargar(FuenteContenido fuente, CatalogoCombate combate,
            Registro<?> tiposHabilidad, Registro<?> estados) {
        return desde(fuente.cargar(TIPO, TIPO, VERSION), combate, tiposHabilidad, estados);
    }

    public static CatalogoObjetos desde(Nodo doc, CatalogoCombate combate,
            Registro<?> tiposHabilidad, Registro<?> estados) {
        Documentos.exigir(doc, TIPO, VERSION);
        List<String> ranuras = new ArrayList<>();
        if (doc.tiene("ranuras")) {
            for (Nodo r : doc.lista("ranuras")) {
                String id = r.comoTexto();
                if (!ID.matcher(id).matches() || ranuras.contains(id)) {
                    throw new ErrorDeDatos(r.ruta() + ": ranura \"" + id + "\" inválida o repetida");
                }
                ranuras.add(id);
            }
        }
        Map<String, DefinicionObjeto> mapa = new LinkedHashMap<>();
        for (Nodo n : doc.lista("lista")) {
            String id = n.texto("id");
            if (!ID.matcher(id).matches()) {
                throw new ErrorDeDatos(n.ruta() + ".id: \"" + id + "\" debe ir en minúsculas con guiones");
            }
            if (mapa.containsKey(id)) {
                throw new ErrorDeDatos(n.ruta() + ".id: \"" + id + "\" repetido");
            }
            DefinicionObjeto.Categoria cat = categoria(n);
            int precio = Documentos.rangoO(n, "precio", 0, 999999, 0);
            Habilidad efecto = null;
            String ranura = null;
            Bonos bonos = Bonos.NINGUNO;
            List<String> clases = new ArrayList<>();
            if (cat == DefinicionObjeto.Categoria.CONSUMIBLE) {
                efecto = CatalogoCombate.leerHabilidad(n.objeto("efecto"), id, n.texto("nombre"), 0, "aliado",
                        tiposHabilidad, estados);
            } else if (cat == DefinicionObjeto.Categoria.EQUIPO) {
                ranura = n.texto("ranura");
                if (!ranuras.contains(ranura)) {
                    throw new ErrorDeDatos(n.ruta() + ".ranura: \"" + ranura + "\" no está en ranuras " + ranuras);
                }
                if (n.tiene("bonos")) {
                    bonos = Bonos.desde(n.objeto("bonos"));
                }
                if (n.tiene("clases")) {
                    for (Nodo c : n.lista("clases")) {
                        String clase = c.comoTexto();
                        if (combate.combatiente(clase).bando != Bando.HEROE) {
                            throw new ErrorDeDatos(c.ruta() + ": \"" + clase + "\" no es una clase de héroe");
                        }
                        clases.add(clase);
                    }
                }
            }
            mapa.put(id, new DefinicionObjeto(id, n.texto("nombre"), cat, precio, efecto, ranura, bonos, clases));
        }
        return new CatalogoObjetos(ranuras, mapa);
    }

    public List<String> ranuras() {
        return ranuras;
    }

    public DefinicionObjeto objeto(String id) {
        DefinicionObjeto o = objetos.get(id);
        if (o == null) {
            throw new ErrorDeDatos("objeto desconocido \"" + id + "\"");
        }
        return o;
    }

    public Map<String, DefinicionObjeto> objetos() {
        return objetos;
    }

    private static DefinicionObjeto.Categoria categoria(Nodo n) {
        String t = n.texto("categoria");
        switch (t) {
            case "consumible":
                return DefinicionObjeto.Categoria.CONSUMIBLE;
            case "equipo":
                return DefinicionObjeto.Categoria.EQUIPO;
            case "clave":
                return DefinicionObjeto.Categoria.CLAVE;
            default:
                throw new ErrorDeDatos(n.ruta() + ".categoria: \"" + t
                        + "\" desconocida; opciones: [consumible, equipo, clave]");
        }
    }
}
