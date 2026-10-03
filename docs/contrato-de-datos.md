# Contrato de datos · motor de ff1

Describe cómo el motor recibe contenido, configuración, azar, tiempo y guardado, y el formato de cada documento. Es la base del futuro molde del motor (ver "Diseño adaptable" en `mision-mvp.md`). Se actualiza en cada hito que añada o cambie un tipo de dato.

Estado: H7 terminado (`configuracion`, `habilidades`, `combatientes`, `progresion`, `objetos`, `botin`, `mapa`, `encuentros`, `inicio`, `escena`, `servicios` y `partida` v1). Cómo añadir contenido: `docs/receta-de-extension.md`.

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
├── servicios.json       ← tipo "servicios": tiendas, posadas, vecinos y jefes (H5–H6, opcional)
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
| `juego.largoNombre` | entero | 1–12 | 8 (letras como máximo al nombrar a un héroe en la partida nueva) | H8 |
| `pueblo.ventaPorCiento` | entero | 0–100 | 50 (por ciento del precio que paga la tienda por un objeto del grupo) | H5 |

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
| `golpeFuerte` | objeto | Opcional (H6), solo para enemigos: `{ "cada": N, "habilidad": "id" }`. Cada N turnos propios usa esa habilidad sobre un héroe al azar en vez de atacar. La habilidad debe existir y no costar magia. |
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

Campos opcionales (H5–H6):

| Campo | Tipo | Regla |
|---|---|---|
| `salidas` | lista | Cada una: `x`, `y` (casilla **pasable** de este mapa), `mapa` (id de otro mapa) y `destino` (`x`, `y`: casilla pasable de ese mapa; conviene que no sea una salida, para no volver al instante). Se cruza al pisarla. `requiere` (texto, opcional): id de un objeto que el grupo debe llevar; sin él la salida no cede y el grupo vuelve a la casilla anterior. Al empezar una partida se recorren todos los mapas enlazados y cualquier error (mapa inexistente, destino no pasable, objeto inexistente) se muestra en la pantalla de error. |
| `lugares` | lista | Cada uno: `x`, `y` (dentro del mapa; suele ser una casilla **no pasable**, un mostrador), `tipo` (`tienda`, `posada`, `vecino` o `jefe`) y `ref` (id del servicio en `servicios`). Se usa con Aceptar estando enfrente. |

Fuera del mapa nada es pasable. Un paso hacia una casilla no pasable no mueve al grupo, solo lo gira.

### `servicios` · versión 1
Archivo opcional `servicios.json` (`pueblo.Servicios`): lo que hay en los lugares de los mapas. Sin el archivo, un mapa no puede tener `lugares`.
```json
{ "tipo": "servicios", "version": 1,
  "tiendas": [ { "id": "tienda-de-lupe", "nombre": "Tienda de Lupe", "vendedor": "Lupe", "objetos": ["tonico-de-raiz"] } ],
  "posadas": [ { "id": "posada-de-casilda", "nombre": "Posada de Casilda", "posadero": "Casilda", "precio": 15 } ],
  "vecinos": [ { "id": "ofelia", "nombre": "Ofelia", "escena": "ofelia" } ],
  "jefes":   [ { "id": "soterrado", "nombre": "La grieta", "enemigos": ["soterrado"], "escenaPrevia": "soterrado-previa",
                 "escenaFinal": "cierre", "regreso": { "mapa": "pozaluz", "x": 6, "y": 5 } } ] }
```
| Campo | Regla |
|---|---|
| `tiendas[].objetos` | Ids de `objetos` con precio ≥ 1. Se compra al `precio` del objeto; se vende al `precio` por `pueblo.ventaPorCiento` (los objetos clave no se venden). |
| `posadas[].precio` | 0–9999 de oro por noche: vida y magia al máximo y los caídos se levantan. No cobra si nadie lo necesita. |
| `vecinos[].escena` | Id de una escena de `escenas/`; al terminar vuelve al mapa. |
| `jefes[]` | `enemigos` (ids de `combatientes`), `escenaPrevia` → combate sin huida → `escenaFinal` → el grupo reaparece en `regreso` (casilla pasable). Cada jefe se vence una sola vez por partida. |
Los ids deben ser únicos por tipo, y cada `lugar` de un mapa debe apuntar a uno que exista.

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
Partida nueva (`juego.Partida.nueva`), en `inicio.json`. Valida que el mapa exista y que sus zonas estén en `encuentros`, y recorre todos los mapas enlazados por `salidas` (y los de `regreso` de los jefes) para validarlos antes de empezar.
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
| `grupo` | lista | 1–6 héroes: `clase` (id de combatiente con `bando` `heroe`) y `nombre` (el que se propone en la pantalla de nombres de la partida nueva; el jugador puede cambiarlo). |
| `objetos` | lista | Opcional. `id` de `objetos` y `cantidad` 1–999 (limitada por `inventario.maximoPorObjeto`). |
| `oro` | entero | Opcional, 0–999999, defecto 0. |
| `introduccion` | texto | Opcional. Id de una escena de `escenas/` que se muestra al empezar la partida nueva, antes del mapa. Si no existe o no es válida, se muestra la pantalla de error. |

### `partida` · versión 1
Guardado (`Partida.guardar` / `Partida.cargar`, formato en `juego.Guardado`), un documento JSON en una ranura del `Almacen` (el juego usa la ranura `partida1`; en Android, `AlmacenArchivos` sobre `getFilesDir()`). Se carga sobre una partida nueva del mismo paquete: lo que el guardado no dice queda como en `inicio.json`. Si algo no vale, se rechaza con la ruta del campo y el juego muestra la pantalla de error sin cambiar nada.
```json
{ "tipo": "partida", "version": 1, "mapa": "pozaluz", "x": 6, "y": 5, "oro": 50, "jefes": [],
  "grupo": [ { "clase": "guardian", "nombre": "Bruna", "experiencia": 0, "vida": 48, "magia": 0,
               "equipo": { "arma": "hoja-de-ensayo" } } ],
  "inventario": { "tonico-de-raiz": 3 },
  "ajustes": { "combate.ticksPorPaso": 4, "juego.msMensaje": 900, "juego.rapidoAlEmpezar": 0 } }
```
| Campo | Regla |
|---|---|
| `mapa`, `x`, `y` | Mapa existente y casilla pasable. |
| `grupo` | 1–6 héroes **en el orden de la formación**; `clase` de bando héroe; `experiencia` ≥ 0 (de ella sale el nivel); `vida` y `magia` entre 0 y el máximo con el equipo puesto; `equipo`: ranura → id de una pieza que esa clase pueda llevar en esa ranura. |
| `inventario` | Id de objeto existente → cantidad 1–999. |
| `jefes` | Ids de `servicios.jefes` ya vencidos. |
| `ajustes` | Los parámetros que el menú Ajustes cambia; deben estar dentro de su rango. |
La semilla del azar no se guarda: cada sesión empieza con una nueva.

### `escena` · versión 1
Escena de texto (`guion.Guion`), en `escenas/<id>.json`; el `id` interno debe coincidir con el nombre del archivo. Se muestra con `PantallaEscena`, que avanza una línea por cada Aceptar.
```json
{ "tipo": "escena", "version": 1, "id": "apertura",
  "lineas": [ { "texto": "Amanece en Pozaluz." },
              { "quien": "{heroe:herbolaria}", "texto": "Las ovejas ya no beben." } ] }
```
| Campo | Tipo | Regla |
|---|---|---|
| `id` | texto | Obligatorio; igual al nombre del archivo sin extensión. |
| `lineas` | lista | 1–300 líneas. |
| `lineas[].texto` | texto | Obligatorio, 1–300 caracteres (se corta en líneas de 34 columnas al dibujar). |
| `lineas[].quien` | texto | Opcional, hasta 40 caracteres; vacío o ausente = narrador. |

Marcadores: en `texto` y `quien`, `{heroe:<clase>}` se sustituye por el nombre que el jugador puso al héroe de esa clase (si no hay uno, se muestra el id de la clase). Cualquier otra llave se rechaza al cargar.

### Tipos de contenido de juego
Documentados arriba: `servicios` (tiendas, posadas, vecinos y jefes).

### Tipos de lugar
Los `tipo` de `mapa.lugares` están en `Mapa.TIPOS_LUGAR`; cada uno tiene su apartado en `servicios` (`Servicios`, por tipo) y su acción en el registro de `PantallaExploracion.lugares()`. Un tipo nuevo se añade en esos tres sitios (ver `receta-de-extension.md`, 5b).

### Textos de la interfaz
Los textos fijos de menús y mensajes (por ejemplo "No te alcanza el oro.") siguen en el código (`juego.Mensajes` y cada `Pantalla*`); los nombres de personas, lugares, objetos y escenas vienen siempre del paquete. Pasarlos a un documento propio quedó como deuda aceptada del MVP (ver `plan.md`).
