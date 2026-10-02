# Receta de extensión · ff1

Cómo añadir contenido o reglas sin tocar el núcleo del motor. Cada receta dice qué archivo cambiar, trae un ejemplo real del paquete (`app/src/main/assets/contenido/`) o del código, y qué prueba escribir. Los campos y rangos exactos están en `docs/contrato-de-datos.md`; si añades un campo o un tipo de documento, actualízalo ahí.

Regla general: el contenido va en JSON; el código solo cambia cuando hace falta una **regla** nueva, y entonces se registra (no se añaden `if` por id en el motor). Correr `scripts/probar-logica.sh <filtro>` mientras se trabaja y la suite completa al cerrar.

## 1. Un enemigo
1. Añadir una entrada con `"bando": "enemigo"` en `combatientes.json`:
   ```json
   { "id": "musgoso", "nombre": "Musgoso", "bando": "enemigo",
     "vida": 14, "ataque": 6, "defensa": 2, "velocidad": 6, "experiencia": 4, "oro": 3 }
   ```
   Con magia, añadir `"magia"`, `"poder"` y `"habilidades": ["id", ...]` (ids de `habilidades.json`).
2. Botín (opcional) en `botin.json`:
   ```json
   { "enemigo": "musgoso", "objetos": [ { "id": "tonico-de-raiz", "probabilidad": 25 } ] }
   ```
3. Hacerlo aparecer: ponerlo en un grupo de una zona de `encuentros.json`:
   ```json
   { "zona": "llanura", "grupos": [ { "enemigos": [ "musgoso", "musgoso" ], "peso": 2 } ] }
   ```
- **Prueba:** las de carga del paquete (`CatalogoCombateTest`, `BotinTest`, `EncuentrosTest.elPaqueteCubreLasZonasDelCampo`) ya fallan si un id no existe. Si el enemigo tiene algo especial, una prueba en `combate/CombateTest` que lo cree con `new Combatiente(catalogo.combatiente("id"))` y compruebe el resultado esperado de un combate con `AzarSecuencia`.

## 2. Un objeto
Entrada en `objetos.json`. Consumible (su `efecto` usa los mismos tipos que las habilidades):
```json
{ "id": "tonico-de-raiz", "nombre": "Tónico de raíz", "categoria": "consumible", "precio": 20,
  "efecto": { "tipo": "curacion", "poder": 30, "objetivo": "aliado" } }
```
Equipo: `"categoria": "equipo"`, `"ranura"` y `"bonos"`; clave: `"categoria": "clave"` (ver el contrato).
- **Prueba:** en `inventario/InventarioTest`, cargar el catálogo del paquete y comprobar que el objeto se usa o equipa como se espera (por ejemplo, cuánto cura con `Acciones.usarObjeto`, o los bonos con `Equipo`).

## 3. Una habilidad
Entrada en `habilidades.json` y su id en la lista `habilidades` del combatiente que la usa:
```json
{ "id": "dardo-amargo", "nombre": "Dardo amargo", "tipo": "alteracion", "coste": 3, "poder": 4,
  "objetivo": "enemigo", "estado": { "id": "veneno", "duracion": 3 } }
```
`tipo` y `estado.id` deben estar registrados (ver 4). En el MVP las habilidades no se aprenden por nivel: el combatiente las tiene desde el principio.
- **Prueba:** en `combate/AccionesTest`, usar la habilidad con un azar fijo y comprobar daño o curación, coste de magia y estado aplicado (hay un ejemplo con `"veneno"` hacia la línea 140).

## 4. Un tipo de habilidad o un estado
Es la única receta que toca código. Se registra en `combate/ReglasCombate.java` (o en un registro propio del juego que se pase a `CatalogoCombate.cargar` y a `Acciones`). Tipo de habilidad real:
```java
.registrar("curacion", (acc, poderActor, h, obj) ->
        obj.curar(acc.curacion(h.poder, poderActor)))
```
Estado real (implementa solo los métodos de `EfectoEstado` que necesita):
```java
.registrar("veneno", new EfectoEstado() {
    @Override
    public int danioAlTerminarTurno(Combatiente c, Configuracion config) {
        return Math.max(1, c.vidaMaxima()
                * config.entero(ConfiguracionCombate.VENENO_POR_CIENTO) / 100);
    }
})
```
Si la regla tiene un número de balance, declararlo en `ConfiguracionCombate.declarar` (nombre, rango, defecto) y documentarlo en la tabla de `configuracion` del contrato.
- **Prueba:** en `combate/AccionesTest` o `CombateTest`, una prueba del efecto (daño, turno perdido, ajuste) y otra que cambie el parámetro con `config.reemplazar(config.actual().con(...))` y vea cambiar el resultado. Que un catálogo con una clave no registrada se rechace ya lo cubre `CatalogoCombateTest`.

## 5. Un mapa
Archivo `mapas/<id>.json` (el `id` interno igual al nombre). Ejemplo real abreviado de `mapas/campo.json`:
```json
{ "tipo": "mapa", "version": 1, "id": "campo",
  "leyenda": { ".": { "nombre": "pradera", "pasable": true, "zona": "llanura" },
               "=": { "nombre": "sendero", "pasable": true },
               "^": { "nombre": "risco", "pasable": false } },
  "filas": [ "^^^^^", "^.=.^", "^^^^^" ],
  "inicio": { "x": 2, "y": 1 } }
```
Cada `zona` usada debe existir en `encuentros.json`; las casillas sin `zona` son seguras. Se carga con `Mapa.cargar(fuente, "campo")` y se recorre con `Explorador` y `Encuentros.mover`. Las salidas entre mapas aún no existen (llegan con el pueblo y la mazmorra, H5–H6); cuando se añadan, documentar el campo en el contrato.
- **Prueba:** en `mundo/MapaTest`, cargar el mapa del paquete y comprobar que el inicio es pasable; en `mundo/EncuentrosTest`, `tabla.validarMapa(mapa)`. Si el mapa tiene un camino obligatorio, una prueba que lo recorra con `Explorador.mover` y verifique la posición final.

## 6. Una escena de texto
Pendiente: el motor de escenas llega en H4. La forma prevista, coherente con el resto: documento `escena` en `escenas/<id>.json`, con una lista de líneas (quién habla y texto) que avanzan por toque, cargado con `FuenteContenido.cargar("escenas/<id>", "escena", 1)` y validado al cargar. Al implementarlo, sustituir esta sección por un ejemplo real y documentar el tipo en el contrato.
- **Prueba prevista:** cargar la escena del paquete, avanzar línea a línea y comprobar el orden y el final; y que una escena con un campo inválido se rechaza con la ruta del campo en el mensaje.

## Comprobar un paquete nuevo
Para otro juego, copiar la carpeta `contenido/` con sus propios datos y cargarla con `new FuenteContenidoJson(new LectorArchivos(carpeta))` (o `LectorMemoria` en pruebas). Ningún cargador conoce ids concretos: si los datos son válidos, el motor funciona sin cambiar código (lo verificará H8 con un segundo paquete mínimo).
