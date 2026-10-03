'use strict';
// Modelo declarativo de las tablas de contenido (H11) y funciones puras para editarlas, sin DOM:
// rutas con punto (`efecto.poder`), filas nuevas, duplicar, borrar, referencias por id y validación
// según docs/contrato-de-datos.md. Funciona igual en el navegador (window.Tablas) y en Node.
(function (raiz) {
  const T = {};

  const ID = /^[a-z0-9]+(-[a-z0-9]+)*$/;
  const ESTADOS = ['veneno', 'sueno', 'proteccion'];
  const TIPOS_EFECTO = ['danio', 'curacion', 'revivir', 'alteracion'];
  const OBJETIVOS = ['enemigo', 'aliado', 'si-mismo'];
  const STATS = ['vida', 'magia', 'ataque', 'defensa', 'poder', 'velocidad'];

  const col = (ruta, titulo, tipo, extra) => Object.assign({ ruta, titulo, tipo }, extra || {});
  const entero = (ruta, titulo, min, max, extra) => col(ruta, titulo, 'entero', Object.assign({ min, max }, extra || {}));
  const idCol = col('id', 'Id', 'id', { obligatorio: true, ayuda: 'Minúsculas, números y guiones; único.' });
  const nombreCol = col('nombre', 'Nombre', 'texto', { obligatorio: true });
  const estadoCols = (pref) => [
    col(pref + 'id', 'Estado', 'opcion', { opciones: ESTADOS, ayuda: 'Estado que aplica (opcional).' }),
    entero(pref + 'duracion', 'Turnos', 1, 99, { ayuda: 'Obligatorio si hay estado.' }),
  ];

  /** Las tablas: archivo, nombre de la lista dentro del documento, columna clave y columnas. */
  T.TABLAS = {
    habilidades: {
      titulo: 'Habilidades', archivo: 'habilidades.json', tipo: 'habilidades', lista: 'lista', clave: 'id', singular: 'habilidad',
      columnas: [
        idCol, nombreCol,
        col('tipo', 'Tipo', 'opcion', { obligatorio: true, opciones: TIPOS_EFECTO }),
        entero('coste', 'Coste (PM)', 0, 999, { defecto: 0 }),
        entero('poder', 'Poder', 0, 9999, { defecto: 0 }),
        col('objetivo', 'Objetivo', 'opcion', { opciones: OBJETIVOS, defecto: 'enemigo' }),
      ].concat(estadoCols('estado.')),
      plantilla: () => ({ id: '', nombre: '', tipo: 'danio', coste: 0, poder: 10, objetivo: 'enemigo' }),
    },
    combatientes: {
      titulo: 'Combatientes', archivo: 'combatientes.json', tipo: 'combatientes', lista: 'lista', clave: 'id', singular: 'combatiente',
      columnas: [
        idCol, nombreCol,
        col('bando', 'Bando', 'opcion', { obligatorio: true, opciones: ['heroe', 'enemigo'] }),
        entero('vida', 'Vida', 1, 99999, { obligatorio: true }),
        entero('magia', 'Magia', 0, 9999, { defecto: 0 }),
        entero('ataque', 'Ataque', 0, 999, { obligatorio: true }),
        entero('defensa', 'Defensa', 0, 999, { obligatorio: true }),
        entero('poder', 'Poder', 0, 999, { defecto: 0 }),
        entero('velocidad', 'Velocidad', 1, 255, { obligatorio: true }),
        entero('experiencia', 'Experiencia', 0, 999999, { defecto: 0, ayuda: 'Al vencerlo (enemigos).' }),
        entero('oro', 'Oro', 0, 999999, { defecto: 0, ayuda: 'Al vencerlo (enemigos).' }),
        col('habilidades', 'Habilidades', 'refs', { ref: 'habilidades' }),
        entero('golpeFuerte.cada', 'Golpe fuerte cada', 1, 99, { ayuda: 'Solo enemigos: cada N turnos propios.' }),
        col('golpeFuerte.habilidad', 'Golpe fuerte: habilidad', 'ref', { ref: 'habilidades:sinCoste' }),
      ],
      plantilla: () => ({ id: '', nombre: '', bando: 'enemigo', vida: 20, ataque: 5, defensa: 2, velocidad: 6, experiencia: 4, oro: 3 }),
    },
    objetos: {
      titulo: 'Objetos', archivo: 'objetos.json', tipo: 'objetos', lista: 'lista', clave: 'id', singular: 'objeto',
      columnas: [
        idCol, nombreCol,
        col('categoria', 'Categoría', 'opcion', { obligatorio: true, opciones: ['consumible', 'equipo', 'clave'] }),
        entero('precio', 'Precio', 0, 999999, { defecto: 0, ayuda: '0 = fuera de tiendas.' }),
        col('efecto.tipo', 'Efecto', 'opcion', { opciones: TIPOS_EFECTO, ayuda: 'Obligatorio en consumibles.' }),
        entero('efecto.poder', 'Poder del efecto', 0, 9999),
        col('efecto.objetivo', 'Objetivo', 'opcion', { opciones: OBJETIVOS, defecto: 'aliado' }),
      ].concat(estadoCols('efecto.estado.')).concat([
        col('ranura', 'Ranura', 'ref', { ref: 'ranuras', ayuda: 'Obligatoria en equipo.' }),
      ]).concat(STATS.map((s) => entero('bonos.' + s, 'Bono: ' + s, 0, 999)))
        .concat([col('clases', 'Clases', 'refs', { ref: 'combatientes:heroe', ayuda: 'Vacío = todas.' })]),
      plantilla: () => ({ id: '', nombre: '', categoria: 'consumible', precio: 10, efecto: { tipo: 'curacion', poder: 20, objetivo: 'aliado' } }),
    },
    botin: {
      titulo: 'Botín', archivo: 'botin.json', tipo: 'botin', lista: 'lista', clave: 'enemigo', singular: 'entrada de botín',
      columnas: [
        col('enemigo', 'Enemigo', 'ref', { ref: 'combatientes:enemigo', obligatorio: true }),
        col('objetos', 'Objetos que suelta', 'subfilas', {
          columnas: [
            col('id', 'Objeto', 'ref', { ref: 'objetos', obligatorio: true }),
            entero('probabilidad', 'Probabilidad (%)', 1, 100, { obligatorio: true }),
            entero('cantidad', 'Cantidad', 1, 99, { defecto: 1 }),
          ],
          plantilla: () => ({ id: '', probabilidad: 25 }),
        }),
      ],
      plantilla: () => ({ enemigo: '', objetos: [] }),
    },
    encuentros: {
      titulo: 'Encuentros', archivo: 'encuentros.json', tipo: 'encuentros', lista: 'zonas', clave: 'zona', singular: 'zona',
      columnas: [
        col('zona', 'Zona', 'id', { obligatorio: true, ayuda: 'La misma que usa la leyenda de un mapa.' }),
        col('grupos', 'Grupos de enemigos', 'subfilas', {
          minimo: 1,
          columnas: [
            col('enemigos', 'Enemigos (1–6)', 'refs', { ref: 'combatientes:enemigo', obligatorio: true, min: 1, max: 6, repetibles: true }),
            entero('peso', 'Peso', 1, 100, { defecto: 1 }),
          ],
          plantilla: () => ({ enemigos: [], peso: 1 }),
        }),
      ],
      plantilla: () => ({ zona: '', grupos: [{ enemigos: [], peso: 1 }] }),
    },
    tiendas: {
      titulo: 'Tiendas', archivo: 'servicios.json', tipo: 'servicios', lista: 'tiendas', clave: 'id', singular: 'tienda', seccion: true,
      columnas: [idCol, nombreCol, col('vendedor', 'Vendedor', 'texto'),
        col('objetos', 'Objetos a la venta', 'refs', { ref: 'objetos:enVenta', ayuda: 'Solo objetos con precio mayor que 0.' })],
      plantilla: () => ({ id: '', nombre: '', vendedor: '', objetos: [] }),
    },
    posadas: {
      titulo: 'Posadas', archivo: 'servicios.json', tipo: 'servicios', lista: 'posadas', clave: 'id', singular: 'posada', seccion: true,
      columnas: [idCol, nombreCol, col('posadero', 'Posadero', 'texto'), entero('precio', 'Precio por noche', 0, 9999, { obligatorio: true })],
      plantilla: () => ({ id: '', nombre: '', posadero: '', precio: 10 }),
    },
    vecinos: {
      titulo: 'Vecinos', archivo: 'servicios.json', tipo: 'servicios', lista: 'vecinos', clave: 'id', singular: 'vecino', seccion: true,
      columnas: [idCol, nombreCol, col('escena', 'Escena', 'ref', { ref: 'escenas', obligatorio: true })],
      plantilla: () => ({ id: '', nombre: '', escena: '' }),
    },
    jefes: {
      titulo: 'Jefes', archivo: 'servicios.json', tipo: 'servicios', lista: 'jefes', clave: 'id', singular: 'jefe', seccion: true,
      columnas: [idCol, nombreCol,
        col('enemigos', 'Enemigos', 'refs', { ref: 'combatientes:enemigo', obligatorio: true, min: 1, max: 6, repetibles: true }),
        col('escenaPrevia', 'Escena previa', 'ref', { ref: 'escenas' }),
        col('escenaFinal', 'Escena final', 'ref', { ref: 'escenas' }),
        col('regreso.mapa', 'Regreso: mapa', 'ref', { ref: 'mapas', obligatorio: true }),
        entero('regreso.x', 'Regreso: x', 0, 999, { obligatorio: true }),
        entero('regreso.y', 'Regreso: y', 0, 999, { obligatorio: true })],
      plantilla: () => ({ id: '', nombre: '', enemigos: [], regreso: { mapa: '', x: 0, y: 0 } }),
    },
  };
  T.ORDEN = ['combatientes', 'habilidades', 'objetos', 'botin', 'encuentros', 'tiendas', 'posadas', 'vecinos', 'jefes'];

  // ---- Rutas ----

  const esObjeto = (v) => v !== null && typeof v === 'object' && !Array.isArray(v);

  T.obtener = function (fila, ruta) {
    let v = fila;
    for (const p of ruta.split('.')) {
      if (!esObjeto(v) || !(p in v)) return undefined;
      v = v[p];
    }
    return v;
  };

  /** Escribe un valor en la ruta; con `undefined` (o '') quita la clave y los objetos que queden vacíos. */
  T.poner = function (fila, ruta, valor) {
    const partes = ruta.split('.');
    const quitar = valor === undefined || valor === '';
    const pila = [fila];
    for (let i = 0; i < partes.length - 1; i++) {
      let sig = pila[pila.length - 1][partes[i]];
      if (!esObjeto(sig)) {
        if (quitar) return fila;
        sig = pila[pila.length - 1][partes[i]] = {};
      }
      pila.push(sig);
    }
    const ultimo = partes[partes.length - 1];
    if (quitar) delete pila[pila.length - 1][ultimo];
    else pila[pila.length - 1][ultimo] = valor;
    for (let i = pila.length - 1; i > 0; i--) {
      if (Object.keys(pila[i]).length === 0) delete pila[i - 1][partes[i - 1]];
    }
    return fila;
  };

  // ---- Edición de filas ----

  const copia = (v) => JSON.parse(JSON.stringify(v));

  T.filas = function (doc, tabla) {
    if (!Array.isArray(doc[tabla.lista])) doc[tabla.lista] = [];
    return doc[tabla.lista];
  };

  /** Documento vacío para una tabla cuyo archivo no existe. */
  T.documentoNuevo = function (tabla) {
    const doc = { tipo: tabla.tipo, version: 1 };
    doc[tabla.lista] = [];
    return doc;
  };

  T.agregar = function (doc, tabla) {
    const fila = tabla.plantilla();
    T.filas(doc, tabla).push(fila);
    return fila;
  };

  /** Id libre a partir de otro: "x" → "x-copia" → "x-copia-2"... */
  T.idLibre = function (base, usados) {
    let candidato = base + '-copia';
    for (let n = 2; usados.has(candidato); n++) candidato = base + '-copia-' + n;
    return candidato;
  };

  /**
   * Duplica la fila `i` justo debajo. Si la clave es un id, la copia recibe un id libre; si es una
   * referencia (botín) o una zona, la copia queda con la clave vacía para que se elija otra.
   */
  T.duplicar = function (doc, tabla, i) {
    const filas = T.filas(doc, tabla);
    if (i < 0 || i >= filas.length) throw new Error('Fila inexistente: ' + i);
    const nueva = copia(filas[i]);
    const claveCol = tabla.columnas.find((c) => c.ruta === tabla.clave);
    if (claveCol.tipo === 'id' && claveCol.ruta === 'id') {
      nueva.id = T.idLibre(String(filas[i].id || 'nuevo'), new Set(filas.map((f) => f.id)));
      if (typeof nueva.nombre === 'string' && nueva.nombre) nueva.nombre = nueva.nombre + ' (copia)';
    } else {
      nueva[tabla.clave] = '';
    }
    filas.splice(i + 1, 0, nueva);
    return nueva;
  };

  T.borrar = function (doc, tabla, i) {
    const filas = T.filas(doc, tabla);
    if (i < 0 || i >= filas.length) throw new Error('Fila inexistente: ' + i);
    return filas.splice(i, 1)[0];
  };

  // ---- Referencias ----

  /**
   * Conjuntos de ids para las listas desplegables. `docs`: { combatientes, habilidades, objetos } (documentos
   * ya leídos o ausentes), `escenas` y `mapas`: listas de ids (nombres de archivo sin extensión).
   */
  T.referencias = function (docs, escenas, mapas) {
    const lista = (d) => (d && Array.isArray(d.lista) ? d.lista.filter(esObjeto) : []);
    const ids = (xs) => xs.map((x) => x.id).filter((x) => typeof x === 'string' && x !== '');
    const comb = lista(docs.combatientes);
    const habs = lista(docs.habilidades);
    const objs = lista(docs.objetos);
    return {
      habilidades: ids(habs),
      'habilidades:sinCoste': ids(habs.filter((h) => !h.coste)),
      combatientes: ids(comb),
      'combatientes:heroe': ids(comb.filter((c) => c.bando === 'heroe')),
      'combatientes:enemigo': ids(comb.filter((c) => c.bando === 'enemigo')),
      objetos: ids(objs),
      'objetos:enVenta': ids(objs.filter((o) => o.precio > 0)),
      ranuras: docs.objetos && Array.isArray(docs.objetos.ranuras) ? docs.objetos.ranuras.filter((r) => typeof r === 'string') : [],
      escenas: (escenas || []).slice(),
      mapas: (mapas || []).slice(),
    };
  };

  // ---- Validación ----

  const vacio = (v) => v === undefined || v === null || v === '' || (Array.isArray(v) && v.length === 0);

  function validarValor(c, v, refs) {
    if (c.tipo === 'texto') return typeof v === 'string' ? null : 'Se esperaba texto.';
    if (c.tipo === 'id') {
      if (typeof v !== 'string') return 'Se esperaba texto.';
      return ID.test(v) ? null : 'Usa minúsculas, números y guiones (por ejemplo "mi-id").';
    }
    if (c.tipo === 'entero') {
      if (typeof v !== 'number' || !Number.isFinite(v)) return 'Escribe un número.';
      if (!Number.isInteger(v)) return 'Debe ser un entero.';
      if (v < c.min || v > c.max) return 'Valor ' + v + ' fuera del rango [' + c.min + ', ' + c.max + '].';
      return null;
    }
    if (c.tipo === 'opcion') return c.opciones.includes(v) ? null : 'Elige una opción de la lista.';
    if (c.tipo === 'ref') {
      if (typeof v !== 'string') return 'Se esperaba un id.';
      return (refs[c.ref] || []).includes(v) ? null : 'Id roto: "' + v + '" no existe en ' + c.ref.split(':')[0] + '.';
    }
    return null;
  }

  function validarFila(fila, columnas, refs, prefijo, errores, fn) {
    if (!esObjeto(fila)) { errores.push({ campo: prefijo, mensaje: 'La fila debe ser un objeto.' }); return; }
    for (const c of columnas) {
      const campo = prefijo + c.ruta;
      const v = T.obtener(fila, c.ruta);
      if (c.tipo === 'subfilas') {
        const subs = v === undefined ? [] : v;
        if (!Array.isArray(subs)) { errores.push({ campo, mensaje: 'Debe ser una lista.' }); continue; }
        if (c.minimo && subs.length < c.minimo) errores.push({ campo, mensaje: 'Necesita al menos ' + c.minimo + '.' });
        subs.forEach((s, k) => validarFila(s, c.columnas, refs, campo + '[' + k + '].', errores, fn));
        continue;
      }
      if (vacio(v)) {
        if (c.obligatorio) errores.push({ campo, mensaje: 'Obligatorio.' });
        continue;
      }
      if (c.tipo === 'refs') {
        if (!Array.isArray(v)) { errores.push({ campo, mensaje: 'Debe ser una lista de ids.' }); continue; }
        if (c.min && v.length < c.min) errores.push({ campo, mensaje: 'Necesita al menos ' + c.min + '.' });
        if (c.max && v.length > c.max) errores.push({ campo, mensaje: 'Máximo ' + c.max + '.' });
        if (!c.repetibles && new Set(v).size !== v.length) errores.push({ campo, mensaje: 'Hay ids repetidos.' });
        v.forEach((id, k) => {
          const lista = refs[c.ref] || [];
          if (typeof id !== 'string' || !lista.includes(id)) {
            errores.push({ campo: campo + '[' + k + ']', mensaje: 'Id roto: "' + id + '" no existe en ' + c.ref.split(':')[0] + '.' });
          }
        });
        continue;
      }
      const m = validarValor(c, v, refs);
      if (m) errores.push({ campo, mensaje: m });
    }
    if (fn) fn(fila, errores, prefijo);
  }

  /** Reglas entre campos de cada tabla (el motor repite todas; aquí se avisa antes de guardar). */
  const REGLAS = {
    habilidades(f, e, p) {
      if (f.estado && f.estado.id && !f.estado.duracion) e.push({ campo: p + 'estado.duracion', mensaje: 'Un estado necesita su duración.' });
      if (f.estado && !f.estado.id && f.estado.duracion) e.push({ campo: p + 'estado.id', mensaje: 'La duración necesita un estado.' });
    },
    combatientes(f, e, p) {
      if (f.golpeFuerte && f.bando !== 'enemigo') e.push({ campo: p + 'golpeFuerte', mensaje: 'El golpe fuerte es solo de enemigos.' });
      const g = f.golpeFuerte;
      if (g && (!g.cada || !g.habilidad)) e.push({ campo: p + 'golpeFuerte', mensaje: 'Necesita "cada" y la habilidad.' });
    },
    objetos(f, e, p) {
      if (f.categoria === 'consumible' && !(f.efecto && f.efecto.tipo)) e.push({ campo: p + 'efecto.tipo', mensaje: 'Un consumible necesita su efecto.' });
      if (f.categoria === 'equipo' && !f.ranura) e.push({ campo: p + 'ranura', mensaje: 'Un equipo necesita su ranura.' });
      if (f.categoria !== 'equipo' && (f.ranura || f.bonos)) e.push({ campo: p + 'ranura', mensaje: 'Ranura y bonos son solo de equipo.' });
      if (f.categoria !== 'consumible' && f.efecto) e.push({ campo: p + 'efecto', mensaje: 'El efecto es solo de consumibles.' });
    },
  };

  /** Errores de una tabla: [{fila, campo, mensaje}] (`fila` = índice, -1 si es de la tabla entera). */
  T.validarTabla = function (doc, tabla, refs) {
    const salida = [];
    if (!esObjeto(doc)) return [{ fila: -1, campo: '', mensaje: 'El documento debe ser un objeto.' }];
    if (doc.tipo !== tabla.tipo) salida.push({ fila: -1, campo: 'tipo', mensaje: 'Debe ser "' + tabla.tipo + '".' });
    if (doc.version !== 1) salida.push({ fila: -1, campo: 'version', mensaje: 'Versión no soportada (debe ser 1).' });
    const filas = doc[tabla.lista];
    if (!Array.isArray(filas)) return salida.concat([{ fila: -1, campo: tabla.lista, mensaje: 'Debe ser una lista.' }]);
    const vistos = new Map();
    filas.forEach((fila, i) => {
      const errores = [];
      validarFila(fila, tabla.columnas, refs, '', errores, REGLAS[tabla.seccion ? '' : tabla.tipo]);
      for (const e of errores) salida.push({ fila: i, campo: e.campo, mensaje: e.mensaje });
      const k = esObjeto(fila) ? fila[tabla.clave] : undefined;
      if (typeof k === 'string' && k !== '') {
        if (vistos.has(k)) salida.push({ fila: i, campo: tabla.clave, mensaje: 'Repetido: ya existe en la fila ' + (vistos.get(k) + 1) + '.' });
        else vistos.set(k, i);
      }
    });
    return salida;
  };

  /** Dónde se usa un id: [{tabla, fila, campo}] en todas las tablas cargadas (para avisar antes de borrar). */
  T.usosDe = function (id, ref, docsPorTabla) {
    const base = ref.split(':')[0];
    const usos = [];
    const buscar = (fila, columnas, tablaNombre, i, pref) => {
      for (const c of columnas) {
        const v = T.obtener(fila, c.ruta);
        if (c.tipo === 'subfilas' && Array.isArray(v)) v.forEach((s, k) => buscar(s, c.columnas, tablaNombre, i, pref + c.ruta + '[' + k + '].'));
        else if (c.tipo === 'ref' && c.ref.split(':')[0] === base && v === id) usos.push({ tabla: tablaNombre, fila: i, campo: pref + c.ruta });
        else if (c.tipo === 'refs' && c.ref.split(':')[0] === base && Array.isArray(v) && v.includes(id)) usos.push({ tabla: tablaNombre, fila: i, campo: pref + c.ruta });
      }
    };
    for (const nombre of T.ORDEN) {
      const doc = docsPorTabla[nombre];
      const tabla = T.TABLAS[nombre];
      if (!doc || !Array.isArray(doc[tabla.lista])) continue;
      doc[tabla.lista].forEach((fila, i) => { if (esObjeto(fila)) buscar(fila, tabla.columnas, nombre, i, ''); });
    }
    return usos;
  };

  T.ID = ID;

  if (typeof module !== 'undefined' && module.exports) module.exports = T;
  else raiz.Tablas = T;
})(typeof window !== 'undefined' ? window : globalThis);
