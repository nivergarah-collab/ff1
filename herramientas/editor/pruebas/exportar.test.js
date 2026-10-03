'use strict';
// Exportar un paquete (H12): la función del editor (con almacenes en memoria) y el script real.
const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');
const { spawnSync } = require('node:child_process');
const A = require('../almacen.js');

const RAIZ = path.join(__dirname, '..', '..', '..');
const CONTENIDO = path.join(RAIZ, 'app', 'src', 'main', 'assets', 'contenido');
const SCRIPT = path.join(RAIZ, 'scripts', 'exportar-contenido.sh');
const HAY_JAVA = spawnSync('javac', ['-version']).status === 0;

test('exportarPaquete copia todos los documentos a un destino vacío', async () => {
  const origen = A.crearAlmacenMemoria({ 'a.json': '{"a":1}', 'mapas/campo.json': '{"m":2}' });
  const destino = A.crearAlmacenMemoria({});
  const r = await A.exportarPaquete(origen, destino);
  assert.equal(r.copiados, 2);
  assert.deepEqual(await destino.listar(), ['a.json', 'mapas/campo.json']);
  assert.equal(await destino.leer('mapas/campo.json'), '{"m":2}');
});

test('exportarPaquete no pisa un destino con documentos', async () => {
  const origen = A.crearAlmacenMemoria({ 'a.json': '{}' });
  const destino = A.crearAlmacenMemoria({ 'otro.json': '{"viejo":true}' });
  await assert.rejects(() => A.exportarPaquete(origen, destino), /no está vacía/);
  assert.equal(await destino.leer('otro.json'), '{"viejo":true}');
  assert.equal(await destino.existe('a.json'), false);
});

test('exportarPaquete rechaza un origen sin documentos', async () => {
  await assert.rejects(() => A.exportarPaquete(A.crearAlmacenMemoria({}), A.crearAlmacenMemoria({})), /no tiene documentos/);
});

function carpetaTemporal() {
  return fs.mkdtempSync(path.join(os.tmpdir(), 'ff1-exp-'));
}

test('el script exporta el paquete del juego y el resultado pasa la validación', { skip: !HAY_JAVA }, () => {
  const base = carpetaTemporal();
  const destino = path.join(base, 'variante');
  const r = spawnSync(SCRIPT, [CONTENIDO, destino], { encoding: 'utf8' });
  assert.equal(r.status, 0, r.stdout + r.stderr);
  assert.match(r.stdout, /EXPORTADO: \d+ documentos/);
  const v = spawnSync(path.join(RAIZ, 'scripts', 'validar-contenido.sh'), [destino], { encoding: 'utf8' });
  assert.equal(v.status, 0, v.stdout + v.stderr);
  fs.rmSync(base, { recursive: true, force: true });
});

test('el script no exporta contenido inválido ni pisa un destino ocupado', { skip: !HAY_JAVA }, () => {
  const base = carpetaTemporal();
  const malo = path.join(base, 'malo');
  fs.cpSync(CONTENIDO, malo, { recursive: true });
  const cfg = path.join(malo, 'configuracion.json');
  const doc = JSON.parse(fs.readFileSync(cfg, 'utf8'));
  doc.valores['combate.ticksPorPaso'] = 99999;
  fs.writeFileSync(cfg, JSON.stringify(doc));
  const destino = path.join(base, 'salida');
  let r = spawnSync(SCRIPT, [malo, destino], { encoding: 'utf8' });
  assert.equal(r.status, 1);
  assert.equal(fs.existsSync(destino), false, 'no debe crear nada si el contenido es inválido');

  const ocupado = path.join(base, 'ocupado');
  fs.mkdirSync(ocupado);
  fs.writeFileSync(path.join(ocupado, 'x.txt'), 'hola');
  r = spawnSync(SCRIPT, [CONTENIDO, ocupado], { encoding: 'utf8' });
  assert.equal(r.status, 1);
  assert.deepEqual(fs.readdirSync(ocupado), ['x.txt']);
  fs.rmSync(base, { recursive: true, force: true });
});

test('el script quita copias .bak al exportar y exige dos argumentos', { skip: !HAY_JAVA }, () => {
  const base = carpetaTemporal();
  const origen = path.join(base, 'origen');
  fs.cpSync(CONTENIDO, origen, { recursive: true });
  fs.writeFileSync(path.join(origen, 'configuracion.json.bak'), '{}');
  const destino = path.join(base, 'dest');
  assert.equal(spawnSync(SCRIPT, [origen, destino], { encoding: 'utf8' }).status, 0);
  assert.equal(fs.existsSync(path.join(destino, 'configuracion.json.bak')), false);
  assert.equal(spawnSync(SCRIPT, [origen], { encoding: 'utf8' }).status, 2);
  fs.rmSync(base, { recursive: true, force: true });
});

// Flujo completo de H12 sin navegador: abrir → editar un parámetro → guardar con .bak → validar → exportar → validar la copia.
test('flujo editar → guardar → validar → exportar', { skip: !HAY_JAVA }, async () => {
  const L = require('../logica.js');
  const vm = require('node:vm');
  const ctx = { window: {} };
  vm.runInNewContext(fs.readFileSync(path.join(__dirname, '..', 'esquema-configuracion.js'), 'utf8'), ctx);
  const ESQUEMA = ctx.window.ESQUEMA_CONFIGURACION;

  const base = carpetaTemporal();
  const trabajo = path.join(base, 'trabajo');
  fs.cpSync(CONTENIDO, trabajo, { recursive: true });

  // El almacén de pruebas lee y escribe en la carpeta temporal, como lo haría el navegador.
  const almacen = {
    async listar() { return fs.readdirSync(trabajo, { recursive: true }).filter((r) => r.endsWith('.json')).map((r) => r.split(path.sep).join('/')).sort(); },
    async existe(r) { return fs.existsSync(path.join(trabajo, r)); },
    async leer(r) { return fs.readFileSync(path.join(trabajo, r), 'utf8'); },
    async escribir(r, t) { fs.mkdirSync(path.dirname(path.join(trabajo, r)), { recursive: true }); fs.writeFileSync(path.join(trabajo, r), t); },
  };

  const original = JSON.parse(await almacen.leer('configuracion.json'));
  const valores = L.valoresDe(original, ESQUEMA);
  valores['combate.ticksPorPaso'] = 3;
  const doc = L.construirConfiguracion(original, valores, ESQUEMA);
  assert.deepEqual(L.validarConfiguracion(doc, ESQUEMA), []);
  await L.guardarConCopia(almacen, 'configuracion.json', L.serializar(doc));
  assert.ok(fs.existsSync(path.join(trabajo, 'configuracion.json.bak')));

  const v = spawnSync(path.join(RAIZ, 'scripts', 'validar-contenido.sh'), [trabajo], { encoding: 'utf8' });
  assert.equal(v.status, 0, v.stdout + v.stderr);

  const destino = path.join(base, 'variante');
  const e = spawnSync(SCRIPT, [trabajo, destino], { encoding: 'utf8' });
  assert.equal(e.status, 0, e.stdout + e.stderr);
  const copia = JSON.parse(fs.readFileSync(path.join(destino, 'configuracion.json'), 'utf8'));
  assert.equal(copia.valores['combate.ticksPorPaso'], 3);
  assert.equal(fs.existsSync(path.join(destino, 'configuracion.json.bak')), false);
  fs.rmSync(base, { recursive: true, force: true });
});
