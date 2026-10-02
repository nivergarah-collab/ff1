# Contrato de datos · motor de ff1

Describe cómo el motor recibe contenido, configuración, azar, tiempo y guardado, y el formato de cada documento. Es la base del futuro molde del motor (ver "Diseño adaptable" en `mision-mvp.md`). Se actualiza en cada hito que añada o cambie un tipo de dato.

Estado: H3 terminado (`configuracion`, `habilidades`, `combatientes`, `progresion`, `objetos`, `botin`, `mapa` y `encuentros` v1). Los demás tipos de contenido se completan en sus hitos. Cómo añadir contenido: `docs/receta-de-extension.md`.

## Formato
- **JSON** (RFC 8259), en UTF-8, leído con `motor.datos.LectorJson`, escrito en Java puro sin librerías. Funciona igual en la JVM de pruebas y en Android, y es el formato natural de una API futura.
- Claves repetidas, texto sobrante o JSON mal formado se rechazan con `ErrorDeDatos`, que indica línea y columna.
- Los accesos tipados (`Nodo.entero`, `Nodo.texto`...) fallan con la ruta del campo, por ejemplo `enemigos[2].vida: se esperaba entero y hay texto`.
- Números: sin decimales y sin exponente se leen como enteros; si no, como decimales.

## Reglas comunes de todo documento
| Campo | Tipo | Regla |
|---|---|---|
| `tipo` | texto | Obligatorio. Debe coincidir con el tipo que pide el motor. |
| `version` | entero | Obligatorio, ≥ 1 y ≤ la versión máxima que el motor sabe leer. |

- Un documento inválido se rechaza con un mensaje claro; el juego no se cierra (lo muestra y sigue o vuelve atrás).
- Identificadores (`id`): texto en minúsculas con guiones, único dentro de su tipo. El texto visible va aparte (`nombre`, `texto`), para poder traducir.
- Nombres genéricos: "combatiente", "habilidad", "objeto", nunca nombres propios del juego en el código.

## Interfaces del motor (`com.example.ff1.motor`)
| Interfaz | Para qué | Implementaciones del MVP |
|---|---|---|
| `fuentes.FuenteContenido` | `cargar(recurso, tipo, versionMaxima)` devuelve el documento validado | `FuenteContenidoJson` sobre un `LectorTexto` |
| `fuentes.LectorTexto` | Lee el texto de `<recurso>.json` | `LectorMemoria` (pruebas), `LectorArchivos` (carpeta), lector de assets de Android (pendiente, capa Android) |
| `config.ProveedorConfiguracion` | Configuración vigente, reemplazable en caliente; conserva la anterior si la nueva no es válida; avisa a oyentes | Única |
| `fuentes.Azar` | `entero(limite)` en [0, limite) | `AzarSemilla` (juego, reproducible), `AzarSecuencia` (pruebas) |
| `fuentes.Tiempo` | Milisegundos reales; solo la capa Android los convierte en ticks | `TiempoManual` (pruebas); reloj del sistema (pendiente, capa Android) |
| `fuentes.Almacen` | Guardar, cargar, comprobar y borrar texto por ranura | `AlmacenMemoria` (pruebas); archivos internos de Android (pendiente, H7) |
| `config.Registro<T>` | Extensiones por clave (habilidad, efecto, condición de victoria, escena) | Uno por tipo de extensión |

Una fuente remota (API) o un editor serían otra implementación de `LectorTexto` o `FuenteContenido`, y otro llamador de `ProveedorConfiguracion.reemplazarDesde`, sin tocar el motor.

## Organización de un paquete de contenido
Un paquete es una carpeta (en el juego, `app/src/main/assets/contenido/`) con un archivo por recurso. Cambiar de juego es cambiar el paquete.

```
contenido/
├── configuracion.json   ← tipo "configuracion" (obligatorio desde H3.5)
├── inicio.json          ← tipo "inicio": partida nueva (H3.5)
├── combatientes.json    ← héroes y enemigos (H1)
├── habilidades.json     ← (H1)
├── progresion.json      ← (H2)
├── objetos.json         ← (H2)
├── botin.json           ← (H2)
├── encuentros.json      ← (H3)
├── mapas/<id>.json      ← (H3)
└── escenas/<id>.json    ← (H4)
```

## Tipos de documento

### `configuracion` · versión 1
Parámetros de balance y ritmo ajustables en caliente. Cada módulo declara los suyos en un `EsquemaConfiguracion` (nombre, rango, valor por defecto); lo que falte toma el valor por defecto.

```json
{
  "tipo": "configuracion",
  "version": 1,
  "valores": {
    "combate.velocidadBarra": 10,
    "combate.avanceRapido": 2
  }
}
```

| Campo | Tipo | Regla |
|---|---|---|
| `valores` | objeto | Opcional. Claves con la forma `<módulo>.<parámetro>`. Una clave no declarada, un decimal en un parámetro entero o un valor fuera de rango rechazan el documento entero. |

Parámetros declarados: se listan aquí a medida que cada hito los añade.

| Parámetro | Tipo | Rango | Defecto | Hito |
|---|---|---|---|---|
| `combate.velocidadBarra` | entero | 1–100 | 10 | H1 |
| `combate.cargaLlena` | entero | 100–100000 | 1000 | H1 |
| `combate.avanceRapido` | entero | 1–8 | 2 | H1 |
| `combate.ticksPorPaso` | entero | 1–100 | 1 | H1 |
| `combate.fuerzaFisica` | entero | 1–100 | 10 | H1 |
| `combate.fuerzaMagica` | entero | 1–100 | 10 | H1 |
| `combate.varianza` | entero | 0–50 | 10 | H1 |
| `combate.huidaBase` | entero | 0–100 | 50 | H1 |
| `combate.venenoPorCiento` | entero | 1–50 | 8 | H1 |
| `combate.proteccionPorCiento` | entero | 0–100 | 50 | H1 |
| `inventario.maximoPorObjeto` | entero | 1–999 | 99 | H2 |
| `mundo.pasosMinimos` | entero | 1–999 | 15 | H3 |
| `mundo.pasosMaximos` | entero | 1–999 | 30 (si es menor que el mínimo, se usa el mínimo) | H3 |
| `juego.msMensaje` | entero | 100–5000 | 900 (milisegundos que se ve cada mensaje del combate) | H3.6 |
| `juego.rapidoAlEmpezar` | entero | 0–1 | 0 (1 = el avance rápido empieza encendido en una partida nueva) | H3.6 |

Barra de tiempo: en cada tick, cada combatiente vivo que no espera turno suma `max(1, velocidad × velocidadBarra / 10)`; con la carga llena entra en la cola de turnos. Empates en un mismo tick: primero el que más se pasó y, a igualdad, el inscrito antes.

Acciones (`combate.Acciones`; fuerzas en décimas, 10 = ×1):
- Físico: `max(1, ataque × fuerzaFisica / 10 − defensa / 2)`.
- Mágico: `max(1, poderHabilidad + poderActor × fuerzaMagica / 10 − defensa / 4)`.
- Curación: `poderHabilidad + poderActor × fuerzaMagica / 10`, sin pasar de la vida máxima.
- Daño y curación se multiplican por `(100 + r) / 100`, con `r` al azar en `[−varianza, +varianza]`.
- Objeto: su efecto se resuelve como una habilidad, sin coste de magia y con `poderActor` 0.
- Paso de animación (`Combate.avanzarPaso`): avanza `ticksPorPaso` ticks (× `avanceRapido` con el interruptor encendido), tick a tick, y se detiene en cuanto a alguien le toca actuar; mientras alguien tiene el turno, el tiempo espera. El orden de turnos es el mismo con y sin avance rápido.
- Huida: `huidaBase + 2 × (velocidad − velocidad media de los enemigos vivos)`, limitada a 5–95 %; un combate puede prohibirla (jefe).

### `habilidades` · versión 1
```json
{ "tipo": "habilidades", "version": 1, "lista": [
  { "id": "chispa", "nombre": "Chispa", "tipo": "danio", "coste": 4, "poder": 14, "objetivo": "enemigo" },
  { "id": "dardo-amargo", "nombre": "Dardo amargo", "tipo": "alteracion", "coste": 3, "poder": 4,
    "estado": { "id": "veneno", "duracion": 3 } }
] }
```
| Campo | Tipo | Regla |
|---|---|---|
| `id` | texto | Obligatorio, minúsculas con guiones, único. |
| `nombre` | texto | Obligatorio. Texto visible. |
| `tipo` | texto | Obligatorio. Clave registrada en el registro de tipos de habilidad (`ReglasCombate`): `danio` (daño mágico), `curacion` (vida al objetivo), `revivir` (levanta a un caído con `poder` por ciento de su vida máxima; solo en objetos, fuera de combate) y `alteracion` (aplica su estado y, si tiene poder, hace daño mágico). |
| `coste` | entero | 0–999, defecto 0. Magia que gasta. |
| `poder` | entero | 0–9999, defecto 0. |
| `objetivo` | texto | `enemigo` (defecto), `aliado` o `si-mismo`. |
| `estado` | objeto | Opcional: `id` (registrado en el registro de estados de `ReglasCombate`) y `duracion` (1–99 turnos, obligatorio). `veneno` quita `venenoPorCiento` de la vida máxima (mínimo 1) al terminar cada turno; `sueno` hace perder los turnos y se quita al recibir daño; `proteccion` deja pasar solo `proteccionPorCiento` del daño (mínimo 1). |

### `combatientes` · versión 1
Clases de héroe y tipos de enemigo. El nombre del héroe lo elige el jugador; `nombre` es el de la clase.
```json
{ "tipo": "combatientes", "version": 1, "lista": [
  { "id": "arcanista", "nombre": "Arcanista", "bando": "heroe", "vida": 28, "magia": 24,
    "ataque": 5, "defensa": 4, "poder": 11, "velocidad": 9, "habilidades": ["chispa"] },
  { "id": "musgoso", "nombre": "Musgoso", "bando": "enemigo", "vida": 14, "ataque": 6,
    "defensa": 2, "velocidad": 6, "experiencia": 4, "oro": 3 }
] }
```
| Campo | Tipo | Regla |
|---|---|---|
| `id`, `nombre` | texto | Como en `habilidades`. |
| `bando` | texto | `heroe` o `enemigo`. |
| `vida` | entero | Obligatorio, 1–99999. |
| `magia` | entero | 0–9999, defecto 0. |
| `ataque`, `defensa` | entero | Obligatorios, 0–999. |
| `poder` | entero | 0–999, defecto 0. Potencia mágica. |
| `velocidad` | entero | Obligatorio, 1–255. Ritmo de la barra de tiempo. |
| `habilidades` | lista de texto | Opcional. Cada `id` debe existir en `habilidades`. |
| `experiencia`, `oro` | entero | 0–999999, defecto 0. Recompensa al vencerlo. |

### `progresion` · versión 1
Experiencia por nivel y crecimiento de cada clase de héroe (`progresion.TablaProgresion`).
```json
{ "tipo": "progresion", "version": 1,
  "experiencia": [0, 10, 25, 45],
  "clases": [
    { "id": "guardian", "crecimiento": { "vida": 7, "ataque": 2, "defensa": 2 } }
  ] }
```
| Campo | Tipo | Regla |
|---|---|---|
| `experiencia` | lista de enteros | Obligatoria, 1–99 valores. Experiencia acumulada para estar en cada nivel: el primero es 0 (nivel 1) y cada uno es mayor que el anterior. El nivel máximo es el largo de la lista. |
| `clases` | lista | Obligatoria. `id` de un combatiente con `bando` `heroe`, sin repetir. |
| `crecimiento` | objeto | `vida`, `magia`, `ataque`, `defensa`, `poder`, `velocidad`: enteros 0–999, defecto 0. Se suman por cada nivel sobre el 1. Una clase sin entrada no crece. |

Reglas: al subir de nivel, la vida y la magia actuales suben lo mismo que su máximo. Al vencer, cada héroe en pie recibe la experiencia completa del combate (`progresion.Reparto`).

### `objetos` · versión 1
Consumibles, equipo y objetos clave (`inventario.CatalogoObjetos`).
```json
{ "tipo": "objetos", "version": 1, "ranuras": ["arma", "armadura", "accesorio"], "lista": [
  { "id": "tonico-de-raiz", "nombre": "Tónico de raíz", "categoria": "consumible", "precio": 20,
    "efecto": { "tipo": "curacion", "poder": 30, "objetivo": "aliado" } },
  { "id": "hoja-de-ensayo", "nombre": "Hoja de ensayo", "categoria": "equipo", "precio": 50,
    "ranura": "arma", "bonos": { "ataque": 4 }, "clases": ["guardian", "rastreador"] },
  { "id": "llave-de-cantera", "nombre": "Llave de cantera", "categoria": "clave" }
] }
```
| Campo | Tipo | Regla |
|---|---|---|
| `ranuras` | lista de texto | Opcional. Ranuras de equipo del juego, minúsculas con guiones, sin repetir. |
| `id`, `nombre` | texto | Como en `habilidades`. |
| `categoria` | texto | `consumible`, `equipo` o `clave`. |
| `precio` | entero | 0–999999, defecto 0. Precio de compra (0 = fuera de tiendas). |
| `efecto` | objeto | Obligatorio en `consumible`: `tipo` (registrado), `poder` (0–9999), `objetivo` (`aliado`, defecto, `enemigo` o `si-mismo`) y `estado` opcional como en `habilidades`. Se resuelve como una habilidad sin coste ni potencia del actor. |
| `ranura` | texto | Obligatorio en `equipo`; debe estar en `ranuras`. |
| `bonos` | objeto | En `equipo`: `vida`, `magia`, `ataque`, `defensa`, `poder`, `velocidad`, enteros 0–999, defecto 0. |
| `clases` | lista de texto | En `equipo`, opcional: clases de héroe que lo pueden equipar; vacía o ausente = todas. |

Inventario: cada objeto se acumula hasta `inventario.maximoPorObjeto` unidades. Al cambiar el equipo, la vida y la magia actuales no pasan del nuevo máximo.

### `botin` · versión 1
Objetos que suelta cada enemigo al caer (`inventario.TablaBotin`).
```json
{ "tipo": "botin", "version": 1, "lista": [
  { "enemigo": "lagarto-de-cantera", "objetos": [
    { "id": "tonico-de-raiz", "probabilidad": 40, "cantidad": 2 } ] }
] }
```
| Campo | Tipo | Regla |
|---|---|---|
| `enemigo` | texto | `id` de un combatiente con `bando` `enemigo`, sin repetir. |
| `objetos[].id` | texto | Debe existir en `objetos`. |
| `objetos[].probabilidad` | entero | Obligatorio, 1–100 (por ciento). Cada entrada se tira por separado. |
| `objetos[].cantidad` | entero | 1–99, defecto 1. |

Lo que no cabe en el inventario se informa como sobrante y no se guarda.

### `mapa` · versión 1
Mapa por casillas (`mundo.Mapa`), un archivo por mapa en `mapas/<id>.json`. El grupo se mueve con `mundo.Explorador`.
```json
{ "tipo": "mapa", "version": 1, "id": "campo",
  "leyenda": { ".": { "nombre": "pradera", "pasable": true },
               "^": { "nombre": "risco", "pasable": false } },
  "filas": [ "^^^^", "^..^", "^^^^" ],
  "inicio": { "x": 1, "y": 1 } }
```
| Campo | Tipo | Regla |
|---|---|---|
| `id` | texto | Igual al nombre del archivo. |
| `leyenda` | objeto | Clave de un solo carácter → `nombre` (texto), `pasable` (booleano), `zona` (texto, opcional: zona de `encuentros`; sin zona no hay encuentros en esa casilla) y `color` (texto `#RRGGBB`, opcional desde H3.5: color de dibujo; sin él, la presentación usa uno genérico y muestra el símbolo). |
| `filas` | lista de textos | Al menos una; todas con el mismo ancho; cada carácter debe estar en la leyenda. La fila 0 es la de arriba. |
| `inicio` | objeto `x`, `y` | Casilla pasable dentro del mapa donde aparece el grupo. |

Fuera del mapa nada es pasable. Un paso hacia una casilla no pasable no mueve al grupo, solo lo gira.

### `encuentros` · versión 1
Grupos de enemigos por zona (`mundo.TablaEncuentros`), en `encuentros.json`. `mundo.Encuentros` lleva una cuenta atrás tirada entre `mundo.pasosMinimos` y `mundo.pasosMaximos`; solo baja con los pasos sobre casillas con `zona`, y al llegar a cero elige un grupo de esa zona por peso y vuelve a tirar.
```json
{ "tipo": "encuentros", "version": 1, "zonas": [
  { "zona": "llanura", "grupos": [
    { "enemigos": [ "musgoso" ], "peso": 3 },
    { "enemigos": [ "musgoso", "musgoso" ], "peso": 2 } ] }
] }
```
| Campo | Tipo | Regla |
|---|---|---|
| `zona` | texto | Sin repetir. Toda zona usada por un mapa debe estar aquí (`validarMapa`). |
| `grupos` | lista | Al menos uno. |
| `grupos[].enemigos` | lista de textos | 1–6 ids de combatientes con `bando` `enemigo`; se pueden repetir. |
| `grupos[].peso` | entero | 1–100, defecto 1. Probabilidad relativa dentro de la zona. |

### `inicio` · versión 1
Partida nueva (`juego.Partida.nueva`), en `inicio.json`. Valida que el mapa exista y que sus zonas estén en `encuentros`.
```json
{ "tipo": "inicio", "version": 1, "titulo": "Crónica de la Cantera", "mapa": "campo",
  "grupo": [ { "clase": "guardian", "nombre": "Bruna" } ],
  "objetos": [ { "id": "tonico-de-raiz", "cantidad": 3 } ],
  "oro": 50 }
```
| Campo | Tipo | Regla |
|---|---|---|
| `titulo` | texto | Opcional. Se muestra en la pantalla de título. |
| `mapa` | texto | Id de un archivo de `mapas/`. El grupo aparece en su `inicio`. |
| `grupo` | lista | 1–6 héroes: `clase` (id de combatiente con `bando` `heroe`) y `nombre`. |
| `objetos` | lista | Opcional. `id` de `objetos` y `cantidad` 1–999 (limitada por `inventario.maximoPorObjeto`). |
| `oro` | entero | Opcional, 0–999999, defecto 0. |

### Guardado de partida
Pendiente (H7). Será un documento JSON con `tipo` `"partida"` y `version`, escrito con `EscritorJson` y guardado en un `Almacen`. Incluirá la semilla del azar.

### Tipos de contenido de juego
Pendientes: `escena` (H4), `tienda` (H5). Cada uno se documenta aquí con su versión, sus campos y un ejemplo al implementarlo.
