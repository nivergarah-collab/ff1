'use strict';
// Pruebas de las funciones puras del editor. Sin dependencias: `node --test herramientas/editor/pruebas`.
const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');

const L = require('../logica.js');
const A = require('../almacen.js');

// El esquema del editor es un script de navegador (window.ESQUEMA_CONFIGURACION = ...).
const contexto = { window: {} };
vm.runInNewContext(fs.readFileSync(path.join(__dirname, '..', 'esquema-configuracion.js'), 'utf8'), contexto);
const ESQUEMA = contexto.window.ESQUEMA_CONFIGURACION;

const REAL = path.join(__dirname, '..', '..', '..', 'app', 'src', 'main', 'assets', 'contenido', 'configuracion.json');

test('el esquema tiene parámetros con rango, defecto y descripción', () => {
  assert.ok(ESQUEMA.parametros.length >= 17);
  for (const p of ESQUEMA.parametros) {
    assert.ok(p.minimo <= p.defecto && p.defecto <= p.maximo, p.nombre);
    assert.ok(p.descripcion.length > 10, p.nombre);
  }
});

test('analizar distingue JSON válido de inválido', () => {
  assert.deepEqual(L.analizar('{"a":1}'), { ok: true, valor: { a: 1 } });
  const mal = L.analizar('{"a":');
  assert.equal(mal.ok, false);
  assert.match(mal.error, /JSON/);
});

test('serializar usa dos espacios y termina en salto de línea', () => {
  assert.equal(L.serializar({ a: [1], b: { c: 2 } }), '{\n  "a": [\n    1\n  ],\n  "b": {\n    "c": 2\n  }\n}\n');
});

test('leerNumero acepta enteros y decimales y rechaza vacío o texto', () => {
  assert.equal(L.leerNumero('12'), 12);
  assert.equal(L.leerNumero(' 7 '), 7);
  assert.equal(L.leerNumero('1.5'), 1.5);
  assert.ok(Number.isNaN(L.leerNumero('')));
  assert.ok(Number.isNaN(L.leerNumero('abc')));
});

test('validarValor sigue las reglas del contrato', () => {
  const def = { nombre: 'combate.varianza', tipo: 'entero', minimo: 0, maximo: 50 };
  assert.equal(L.validarValor(def, 10), null);
  assert.equal(L.validarValor(def, 0), null);
  assert.equal(L.validarValor(def, 50), null);
  assert.match(L.validarValor(def, 51), /fuera del rango \[0, 50\]/);
  assert.match(L.validarValor(def, -1), /fuera del rango/);
  assert.match(L.validarValor(def, 1.5), /entero/);
  assert.match(L.validarValor(def, NaN), /número/);
});

test('valoresDe completa con los valores por defecto', () => {
  const v = L.valoresDe({ tipo: 'configuracion', version: 1, valores: { 'combate.varianza': 20 } }, ESQUEMA);
  assert.equal(v['combate.varianza'], 20);
  assert.equal(v['combate.velocidadBarra'], 10);
  assert.equal(Object.keys(v).length, ESQUEMA.parametros.length);
});

test('validarConfiguracion acepta el documento real del juego', () => {
  const doc = JSON.parse(fs.readFileSync(REAL, 'utf8'));
  assert.deepEqual(L.validarConfiguracion(doc, ESQUEMA), []);
});

test('validarConfiguracion señala cada problema con su campo', () => {
  const errores = L.validarConfiguracion({
    tipo: 'otra', version: 9,
    valores: { 'combate.varianza': 99, 'combate.inventado': 1, 'combate.huidaBase': 'mucho', 'combate.cargaLlena': 1000.5 },
  }, ESQUEMA);
  const campos = errores.map((e) => e.campo).sort();
  assert.deepEqual(campos, ['tipo', 'valores.combate.cargaLlena', 'valores.combate.huidaBase',
    'valores.combate.inventado', 'valores.combate.varianza', 'version'].sort());
  assert.match(errores.find((e) => e.campo === 'valores.combate.inventado').mensaje, /desconocido/);
});

test('validarConfiguracion exige que valores sea un objeto', () => {
  const errores = L.validarConfiguracion({ tipo: 'configuracion', version: 1, valores: [1] }, ESQUEMA);
  assert.equal(errores.length, 1);
  assert.equal(errores[0].campo, 'valores');
});

test('construirConfiguracion escribe lo que cambia o ya estaba, y conserva el resto', () => {
  const original = { tipo: 'configuracion', version: 1, valores: { 'combate.ticksPorPaso': 4 }, nota: 'x' };
  const valores = L.valoresDe(original, ESQUEMA);
  valores['combate.varianza'] = 20; // cambia
  valores['combate.ticksPorPaso'] = 10; // ya estaba (aunque 10 sea el defecto de otro)
  valores['combate.huidaBase'] = 50; // igual al defecto y no estaba: no se escribe
  const doc = L.construirConfiguracion(original, valores, ESQUEMA);
  assert.deepEqual(doc.valores, { 'combate.ticksPorPaso': 10, 'combate.varianza': 20 });
  assert.equal(doc.nota, 'x');
  assert.equal(original.valores['combate.ticksPorPaso'], 4); // no muta el original
});

test('un parámetro que vuelve a su defecto sigue escrito si ya estaba en el archivo', () => {
  const original = { tipo: 'configuracion', version: 1, valores: { 'combate.varianza': 20 } };
  const valores = L.valoresDe(original, ESQUEMA);
  valores['combate.varianza'] = 10;
  assert.deepEqual(L.construirConfiguracion(original, valores, ESQUEMA).valores, { 'combate.varianza': 10 });
});

test('sin cambios, un documento sin valores sigue sin valores', () => {
  const original = { tipo: 'configuracion', version: 1 };
  const doc = L.construirConfiguracion(original, L.valoresDe(original, ESQUEMA), ESQUEMA);
  assert.deepEqual(doc, original);
});

test('guardar y volver a leer da los mismos valores y pasa la validación', () => {
  const original = JSON.parse(fs.readFileSync(REAL, 'utf8'));
  const valores = L.valoresDe(original, ESQUEMA);
  valores['mundo.pasosMinimos'] = 5;
  const texto = L.serializar(L.construirConfiguracion(original, valores, ESQUEMA));
  const leido = L.analizar(texto).valor;
  assert.deepEqual(L.validarConfiguracion(leido, ESQUEMA), []);
  assert.equal(L.valoresDe(leido, ESQUEMA)['mundo.pasosMinimos'], 5);
});

test('agruparParametros respeta el orden del esquema', () => {
  const grupos = L.agruparParametros(ESQUEMA);
  assert.equal(grupos[0].grupo, 'Combate');
  assert.deepEqual(grupos.map((g) => g.grupo), ['Combate', 'Inventario', 'Exploración', 'Interfaz', 'Pueblo']);
  assert.equal(grupos.reduce((n, g) => n + g.parametros.length, 0), ESQUEMA.parametros.length);
});

test('clasificarDocumentos reconoce el tipo de cada archivo', () => {
  const docs = L.clasificarDocumentos(['configuracion.json', 'mapas/campo.json', 'escenas/apertura.json',
    'combatientes.json', 'raro.json', 'configuracion.json.bak']);
  assert.deepEqual(docs.map((d) => d.ruta), ['configuracion.json', 'combatientes.json', 'mapas/campo.json',
    'escenas/apertura.json', 'raro.json']);
  assert.equal(docs[0].editable, true);
  assert.equal(docs[1].editable, true, 'las tablas de contenido se editan desde H11');
  assert.equal(docs[2].tipo, 'mapa');
  assert.equal(docs[2].editable, false, 'mapas y escenas siguen en solo lectura');
});

test('interpretarInforme lee la salida --json del validador', () => {
  const texto = JSON.stringify({ carpeta: 'x', ok: false, errores: 1, omitidos: 1, documentos: [
    { documento: 'a.json', estado: 'ok', detalle: null },
    { documento: 'b.json', estado: 'error', detalle: 'b.json: lista[0].x: mal' },
    { documento: 'c.json', estado: 'omitido', detalle: 'depende de b.json' }] });
  const inf = L.interpretarInforme(texto);
  assert.equal(inf.ok, false);
  assert.equal(inf.errores, 1);
  assert.equal(inf.documentos.length, 3);
  assert.equal(L.interpretarInforme('esto no es JSON').error !== undefined, true);
  assert.equal(L.interpretarInforme('{"a":1}').error !== undefined, true);
});

test('comandoValidar cita la carpeta y la salida en JSON', () => {
  assert.equal(L.comandoValidar('contenido'),
    'scripts/validar-contenido.sh "contenido" --json > informe.json');
  assert.match(L.comandoValidar('mi "carpeta"'), /mi \\"carpeta\\"/);
});

test('guardarConCopia deja la versión anterior en .bak y escribe la nueva', async () => {
  const alm = A.crearAlmacenMemoria({ 'configuracion.json': 'viejo' });
  const r = await L.guardarConCopia(alm, 'configuracion.json', 'nuevo');
  assert.equal(r.copia, 'configuracion.json.bak');
  assert.equal(await alm.leer('configuracion.json'), 'nuevo');
  assert.equal(await alm.leer('configuracion.json.bak'), 'viejo');
});

test('guardarConCopia sin archivo previo no crea .bak', async () => {
  const alm = A.crearAlmacenMemoria({});
  const r = await L.guardarConCopia(alm, 'nuevo.json', 'x');
  assert.equal(r.copia, null);
  assert.equal(await alm.existe('nuevo.json.bak'), false);
});

test('guardarConCopia no escribe el archivo si la copia falla', async () => {
  const alm = A.crearAlmacenMemoria({ 'a.json': 'viejo' });
  const escribir = alm.escribir;
  alm.escribir = async (ruta, texto) => { if (ruta.endsWith('.bak')) throw new Error('disco lleno'); return escribir(ruta, texto); };
  await assert.rejects(L.guardarConCopia(alm, 'a.json', 'nuevo'), /disco lleno/);
  assert.equal(await alm.leer('a.json'), 'viejo');
});

// ---- Almacén sobre la API de archivos del navegador (con manejadores falsos) ----

function carpetaFalsa(contenido) {
  // contenido: objeto anidado; las cadenas son archivos y los objetos, carpetas.
  const fabrica = (nodo) => ({
    kind: 'directory',
    async *entries() {
      for (const [nombre, v] of Object.entries(nodo)) yield [nombre, typeof v === 'string' ? archivo(nodo, nombre) : fabrica(v)];
    },
    async getFileHandle(nombre, op) {
      if (typeof nodo[nombre] !== 'string') {
        if (!(op && op.create)) { const e = new Error('no existe'); e.name = 'NotFoundError'; throw e; }
        nodo[nombre] = '';
      }
      return archivo(nodo, nombre);
    },
    async getDirectoryHandle(nombre, op) {
      if (typeof nodo[nombre] !== 'object') {
        if (!(op && op.create)) { const e = new Error('no existe'); e.name = 'NotFoundError'; throw e; }
        nodo[nombre] = {};
      }
      return fabrica(nodo[nombre]);
    },
  });
  const archivo = (nodo, nombre) => ({
    kind: 'file',
    async getFile() { return { async text() { return nodo[nombre]; } }; },
    async createWritable() { let t = ''; return { async write(x) { t += x; }, async close() { nodo[nombre] = t; } }; },
  });
  return fabrica(contenido);
}

test('el almacén de carpeta lista, lee y escribe con rutas anidadas', async () => {
  const raiz = { 'configuracion.json': 'c', 'mapas': { 'campo.json': 'm' }, 'escenas': {}, '.git': { 'x.json': '1' }, 'nota.txt': 't' };
  const alm = A.crearAlmacenCarpeta(carpetaFalsa(raiz));
  assert.deepEqual(await alm.listar(), ['configuracion.json', 'mapas/campo.json']);
  assert.equal(await alm.leer('mapas/campo.json'), 'm');
  assert.equal(await alm.existe('mapas/campo.json'), true);
  assert.equal(await alm.existe('mapas/otro.json'), false);
  await alm.escribir('mapas/otro.json', 'o');
  await alm.escribir('configuracion.json', 'c2');
  assert.equal(raiz.mapas['otro.json'], 'o');
  assert.equal(raiz['configuracion.json'], 'c2');
});

// ---- Compatibilidad con el motor: lo que guarda el editor lo acepta el validador real (Java) ----

const { spawnSync } = require('node:child_process');
const os = require('node:os');

const RAIZ_REPO = path.join(__dirname, '..', '..', '..');
const CONTENIDO = path.join(RAIZ_REPO, 'app', 'src', 'main', 'assets', 'contenido');
const HAY_JAVA = spawnSync('javac', ['-version']).status === 0;

function validarConMotor(valores) {
  const tmp = fs.mkdtempSync(path.join(os.tmpdir(), 'ff1-editor-'));
  try {
    fs.cpSync(CONTENIDO, tmp, { recursive: true });
    const original = JSON.parse(fs.readFileSync(path.join(tmp, 'configuracion.json'), 'utf8'));
    const doc = L.construirConfiguracion(original, valores, ESQUEMA);
    assert.deepEqual(L.validarConfiguracion(doc, ESQUEMA), []);
    fs.writeFileSync(path.join(tmp, 'configuracion.json'), L.serializar(doc));
    const r = spawnSync(path.join(RAIZ_REPO, 'scripts', 'validar-contenido.sh'), [tmp], { encoding: 'utf8' });
    return { codigo: r.status, salida: r.stdout + r.stderr };
  } finally {
    fs.rmSync(tmp, { recursive: true, force: true });
  }
}

test('el motor acepta la configuración con todos los mínimos, todos los máximos y un cambio normal',
  { skip: !HAY_JAVA && 'no hay javac' }, () => {
    const minimos = {}; const maximos = {}; const normal = L.valoresDe({}, ESQUEMA);
    for (const p of ESQUEMA.parametros) { minimos[p.nombre] = p.minimo; maximos[p.nombre] = p.maximo; }
    normal['combate.varianza'] = 20;
    normal['mundo.pasosMinimos'] = 5;
    for (const [nombre, valores] of [['mínimos', minimos], ['máximos', maximos], ['normal', normal]]) {
      const r = validarConMotor(valores);
      assert.equal(r.codigo, 0, nombre + ': ' + r.salida);
    }
  });

test('el motor rechaza un valor fuera de rango aunque el editor no lo hubiera impedido',
  { skip: !HAY_JAVA && 'no hay javac' }, () => {
    const tmp = fs.mkdtempSync(path.join(os.tmpdir(), 'ff1-editor-'));
    try {
      fs.cpSync(CONTENIDO, tmp, { recursive: true });
      fs.writeFileSync(path.join(tmp, 'configuracion.json'),
        L.serializar({ tipo: 'configuracion', version: 1, valores: { 'combate.varianza': 51 } }));
      const r = spawnSync(path.join(RAIZ_REPO, 'scripts', 'validar-contenido.sh'), [tmp], { encoding: 'utf8' });
      assert.equal(r.status, 1);
      assert.match(r.stdout, /configuracion\.json/);
      assert.match(r.stdout, /varianza/);
    } finally {
      fs.rmSync(tmp, { recursive: true, force: true });
    }
  });
