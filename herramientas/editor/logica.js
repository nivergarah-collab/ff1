'use strict';
// Funciones puras del editor (sin DOM): análisis, validación según docs/contrato-de-datos.md,
// construcción del documento de configuración y guardado con copia de seguridad.
// Funcionan igual en el navegador (window.Logica) y en Node (para las pruebas).
(function (raiz) {
  const L = {};

  // ---- JSON ----

  L.analizar = function (texto) {
    try {
      return { ok: true, valor: JSON.parse(texto) };
    } catch (e) {
      return { ok: false, error: 'JSON mal formado: ' + e.message };
    }
  };

  /** Mismo estilo que los archivos del juego: dos espacios y salto de línea final. */
  L.serializar = function (doc) {
    return JSON.stringify(doc, null, 2) + '\n';
  };

  L.leerNumero = function (texto) {
    const t = String(texto).trim();
    if (t === '') return NaN;
    return Number(t);
  };

  const esObjeto = (v) => v !== null && typeof v === 'object' && !Array.isArray(v);

  // ---- Configuración ----

  /** Mensaje de error de un valor para un parámetro del esquema, o null si es válido. */
  L.validarValor = function (def, valor) {
    if (typeof valor !== 'number' || !Number.isFinite(valor)) return 'Escribe un número.';
    if (def.tipo === 'entero' && !Number.isInteger(valor)) return 'Debe ser un entero.';
    if (valor < def.minimo || valor > def.maximo) {
      return 'Valor ' + valor + ' fuera del rango [' + def.minimo + ', ' + def.maximo + '].';
    }
    return null;
  };

  /** Valores de todos los parámetros: los del documento y, donde falten, el defecto. */
  L.valoresDe = function (doc, esquema) {
    const valores = {};
    for (const p of esquema.parametros) valores[p.nombre] = p.defecto;
    if (doc && esObjeto(doc.valores)) {
      for (const [k, v] of Object.entries(doc.valores)) {
        if (k in valores && typeof v === 'number') valores[k] = v;
      }
    }
    return valores;
  };

  /** Errores de un documento `configuracion` con las mismas reglas del contrato: [{campo, mensaje}]. */
  L.validarConfiguracion = function (doc, esquema) {
    const errores = [];
    if (!esObjeto(doc)) return [{ campo: '', mensaje: 'El documento debe ser un objeto.' }];
    if (doc.tipo !== 'configuracion') errores.push({ campo: 'tipo', mensaje: 'Debe ser "configuracion".' });
    if (doc.version !== 1) errores.push({ campo: 'version', mensaje: 'Versión no soportada (debe ser 1).' });
    if (doc.valores === undefined) return errores;
    if (!esObjeto(doc.valores)) {
      errores.push({ campo: 'valores', mensaje: 'Debe ser un objeto con claves <módulo>.<parámetro>.' });
      return errores;
    }
    const porNombre = {};
    for (const p of esquema.parametros) porNombre[p.nombre] = p;
    for (const [clave, valor] of Object.entries(doc.valores)) {
      const campo = 'valores.' + clave;
      const def = porNombre[clave];
      if (!def) errores.push({ campo, mensaje: 'Parámetro desconocido.' });
      else if (typeof valor !== 'number') errores.push({ campo, mensaje: 'Se esperaba un número.' });
      else {
        const m = L.validarValor(def, valor);
        if (m) errores.push({ campo, mensaje: m });
      }
    }
    return errores;
  };

  /**
   * Documento nuevo a partir del original y de los valores del formulario. Escribe los que
   * difieren del defecto y los que ya estaban en el archivo; conserva cualquier otro campo.
   */
  L.construirConfiguracion = function (original, valores, esquema) {
    const doc = JSON.parse(JSON.stringify(original || { tipo: 'configuracion', version: 1 }));
    doc.tipo = 'configuracion';
    doc.version = doc.version || 1;
    const previos = original && esObjeto(original.valores) ? original.valores : {};
    const nuevos = {};
    for (const p of esquema.parametros) {
      const v = valores[p.nombre];
      if (v !== p.defecto || p.nombre in previos) nuevos[p.nombre] = v;
    }
    if (Object.keys(nuevos).length > 0 || 'valores' in doc) doc.valores = nuevos;
    return doc;
  };

  /** [{grupo, parametros}] en el orden del esquema. */
  L.agruparParametros = function (esquema) {
    const grupos = [];
    for (const p of esquema.parametros) {
      let g = grupos.find((x) => x.grupo === p.grupo);
      if (!g) grupos.push((g = { grupo: p.grupo, parametros: [] }));
      g.parametros.push(p);
    }
    return grupos;
  };

  // ---- Documentos de la carpeta ----

  const RAIZ = ['configuracion', 'inicio', 'habilidades', 'combatientes', 'progresion', 'objetos', 'botin', 'encuentros', 'servicios'];
  const EDITABLES = new Set(['configuracion', 'combatientes', 'habilidades', 'objetos', 'botin', 'encuentros', 'servicios']);

  L.clasificarDocumentos = function (rutas) {
    const docs = [];
    for (const ruta of rutas) {
      if (!ruta.endsWith('.json')) continue;
      let tipo = 'desconocido';
      let orden = 3;
      let indice = 0;
      const nombre = ruta.replace(/\.json$/, '');
      if (RAIZ.includes(nombre)) { tipo = nombre; orden = 0; indice = RAIZ.indexOf(nombre); }
      else if (ruta.startsWith('mapas/') && !ruta.slice(6).includes('/')) { tipo = 'mapa'; orden = 1; }
      else if (ruta.startsWith('escenas/') && !ruta.slice(8).includes('/')) { tipo = 'escena'; orden = 2; }
      docs.push({ ruta, tipo, editable: EDITABLES.has(tipo), orden, indice });
    }
    docs.sort((a, b) => a.orden - b.orden || a.indice - b.indice || a.ruta.localeCompare(b.ruta));
    return docs.map(({ ruta, tipo, editable }) => ({ ruta, tipo, editable }));
  };

  // ---- Validación con el motor (scripts/validar-contenido.sh --json) ----

  L.comandoValidar = function (carpeta) {
    return 'scripts/validar-contenido.sh "' + String(carpeta).replace(/(["\\$`])/g, '\\$1') + '" --json > informe.json';
  };

  L.interpretarInforme = function (texto) {
    const r = L.analizar(texto);
    if (!r.ok) return { error: r.error };
    const v = r.valor;
    if (!esObjeto(v) || !Array.isArray(v.documentos) || typeof v.ok !== 'boolean') {
      return { error: 'No parece el informe de scripts/validar-contenido.sh --json.' };
    }
    return {
      ok: v.ok,
      errores: v.errores || 0,
      omitidos: v.omitidos || 0,
      carpeta: v.carpeta || '',
      documentos: v.documentos.map((d) => ({ documento: d.documento, estado: d.estado, detalle: d.detalle || null })),
    };
  };

  // ---- Guardado ----

  L.rutaCopia = (ruta) => ruta + '.bak';

  /**
   * Guarda `texto` en `ruta`. Si el archivo ya existía, antes deja su contenido en `<ruta>.bak`;
   * si la copia falla, no toca el archivo. Devuelve {copia} con la ruta de la copia o null.
   */
  L.guardarConCopia = async function (almacen, ruta, texto) {
    let copia = null;
    if (await almacen.existe(ruta)) {
      copia = L.rutaCopia(ruta);
      await almacen.escribir(copia, await almacen.leer(ruta));
    }
    await almacen.escribir(ruta, texto);
    return { copia };
  };

  if (typeof module !== 'undefined' && module.exports) module.exports = L;
  else raiz.Logica = L;
})(typeof window !== 'undefined' ? window : globalThis);
