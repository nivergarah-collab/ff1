package com.example.ff1.inventario;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Arrays;

import org.junit.Test;

import com.example.ff1.combate.Acciones;
import com.example.ff1.combate.CatalogoCombate;
import com.example.ff1.combate.Combatiente;
import com.example.ff1.combate.ConfiguracionCombate;
import com.example.ff1.combate.ReglasCombate;
import com.example.ff1.combate.ResultadoAccion;
import com.example.ff1.motor.config.EsquemaConfiguracion;
import com.example.ff1.motor.config.ProveedorConfiguracion;
import com.example.ff1.motor.datos.ErrorDeDatos;
import com.example.ff1.motor.datos.LectorJson;
import com.example.ff1.motor.fuentes.AzarSecuencia;
import com.example.ff1.motor.fuentes.FuenteContenidoJson;
import com.example.ff1.motor.fuentes.LectorArchivos;
import com.example.ff1.progresion.Heroe;
import com.example.ff1.progresion.TablaProgresion;

public class InventarioTest {

    private static FuenteContenidoJson fuente() {
        File carpeta = new File("app/src/main/assets/contenido");
        if (!carpeta.isDirectory()) {
            carpeta = new File("src/main/assets/contenido"); // Gradle corre desde app/
        }
        return new FuenteContenidoJson(new LectorArchivos(carpeta));
    }

    private static final CatalogoCombate COMBATE = CatalogoCombate.cargar(fuente(),
            ReglasCombate.tiposHabilidad(), ReglasCombate.estados());
    private static final CatalogoObjetos OBJETOS = CatalogoObjetos.cargar(fuente(), COMBATE,
            ReglasCombate.tiposHabilidad(), ReglasCombate.estados());
    private static final TablaProgresion TABLA = TablaProgresion.cargar(fuente(), COMBATE);

    private static CatalogoObjetos catalogo(String lista) {
        return CatalogoObjetos.desde(LectorJson.leer("{\"tipo\":\"objetos\",\"version\":1,"
                + "\"ranuras\":[\"arma\"],\"lista\":" + lista + "}"), COMBATE,
                ReglasCombate.tiposHabilidad(), ReglasCombate.estados());
    }

    private static void rechaza(String lista, String fragmento) {
        try {
            catalogo(lista);
            fail("debió rechazar " + lista);
        } catch (ErrorDeDatos e) {
            assertTrue(e.getMessage(), e.getMessage().contains(fragmento));
        }
    }

    @Test
    public void elPaqueteDelJuegoCarga() {
        assertEquals(Arrays.asList("arma", "armadura", "accesorio"), OBJETOS.ranuras());
        DefinicionObjeto tonico = OBJETOS.objeto("tonico-de-raiz");
        assertEquals(DefinicionObjeto.Categoria.CONSUMIBLE, tonico.categoria);
        assertEquals("curacion", tonico.efecto.tipo);
        assertEquals(0, tonico.efecto.coste);
        assertEquals(DefinicionObjeto.Categoria.CLAVE, OBJETOS.objeto("llave-de-cantera").categoria);
    }

    @Test
    public void datosInvalidosSeRechazanConMensajeClaro() {
        rechaza("[{\"id\":\"x\",\"nombre\":\"X\",\"categoria\":\"comida\"}]", "categoria");
        rechaza("[{\"id\":\"x\",\"nombre\":\"X\",\"categoria\":\"equipo\",\"ranura\":\"casco\"}]", "no está en ranuras");
        rechaza("[{\"id\":\"x\",\"nombre\":\"X\",\"categoria\":\"consumible\",\"efecto\":{\"tipo\":\"volar\"}}]", "no registrado");
        rechaza("[{\"id\":\"x\",\"nombre\":\"X\",\"categoria\":\"equipo\",\"ranura\":\"arma\",\"clases\":[\"musgoso\"]}]", "no es una clase de héroe");
        rechaza("[{\"id\":\"x\",\"nombre\":\"X\",\"categoria\":\"clave\"},{\"id\":\"x\",\"nombre\":\"Y\",\"categoria\":\"clave\"}]", "repetido");
    }

    @Test
    public void elInventarioAcumulaHastaElMaximo() {
        Inventario inv = new Inventario(3);
        assertEquals(2, inv.agregar("tonico-de-raiz", 2));
        assertEquals(1, inv.agregar("tonico-de-raiz", 5));
        assertEquals(3, inv.cantidad("tonico-de-raiz"));
        assertFalse(inv.quitar("tonico-de-raiz", 4));
        assertTrue(inv.quitar("tonico-de-raiz", 3));
        assertTrue(inv.contenido().isEmpty());
    }

    @Test
    public void elMaximoPorObjetoVieneDeLaConfiguracion() {
        ProveedorConfiguracion config = new ProveedorConfiguracion(
                ConfiguracionInventario.declarar(new EsquemaConfiguracion()).porDefecto());
        assertEquals(99, config.actual().entero(ConfiguracionInventario.MAXIMO_POR_OBJETO));
    }

    @Test
    public void equiparSumaBonosYDevuelveLaPiezaAnterior() {
        Heroe h = new Heroe(COMBATE.combatiente("guardian"), "Ilse", TABLA);
        int ataque = h.estadisticas().ataque;
        assertNull(h.equipar(OBJETOS.objeto("hoja-de-ensayo")));
        h.equipar(OBJETOS.objeto("jubon-acolchado"));
        assertEquals(ataque + 4, h.estadisticas().ataque);
        assertEquals(ataque + 4, h.entrarEnCombate().ataque());
        assertSame(OBJETOS.objeto("hoja-de-ensayo"), h.desequipar("arma"));
        assertEquals(ataque, h.estadisticas().ataque);
    }

    @Test(expected = IllegalArgumentException.class)
    public void unaClaseNoEquipaLoQueNoLeCorresponde() {
        new Heroe(COMBATE.combatiente("arcanista"), "Oto", TABLA).equipar(OBJETOS.objeto("hoja-de-ensayo"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void unConsumibleNoSeEquipa() {
        new Heroe(COMBATE.combatiente("guardian"), "Ilse", TABLA).equipar(OBJETOS.objeto("tonico-de-raiz"));
    }

    @Test
    public void unConsumibleSeUsaEnCombateComoEfecto() {
        ProveedorConfiguracion config = new ProveedorConfiguracion(
                ConfiguracionCombate.declarar(new EsquemaConfiguracion()).porDefecto());
        config.reemplazar(config.actual().con(ConfiguracionCombate.VARIANZA, 0));
        Acciones a = new Acciones(config, new AzarSecuencia(0), ReglasCombate.tiposHabilidad(), ReglasCombate.estados());
        Combatiente c = new Heroe(COMBATE.combatiente("guardian"), "Ilse", TABLA).entrarEnCombate();
        c.recibirDanio(40);
        ResultadoAccion r = a.usarObjeto(c, OBJETOS.objeto("tonico-de-raiz").efecto, c);
        assertEquals(30, r.cantidad);
    }
}
