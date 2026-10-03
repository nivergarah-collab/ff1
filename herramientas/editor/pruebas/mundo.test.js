'use strict';
const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');
const { spawnSync } = require('node:child_process');

const M = require('../mundo.js');
const L = require('../logica.js');

const RAIZ_REPO = path.join(__dirname, '..', '..', '..');
const CONT = path.join(RAIZ_REPO, 'app', 'src', 'main', 'assets', 'contenido');
const leer = (r) => JSON.parse(fs.readFileSync(path.join(CONT, r), 'utf8'));
const HAY_JAVA = spawnSync('javac', ['-version']).status === 0;

test('la cuadrícula de cada mapa del juego no tiene problemas y marca el inicio', () => {
  for (const n of fs.readdirSync(path.join(CONT, 'mapas'))) {
    const doc = leer('mapas/' + n);
    const c = M.cuadricula(doc);
    assert.deepEqual(c.problemas, [], n);
    assert.equal(c.alto, doc.filas.length);
    assert.equal(c.filas[0].length, c.ancho);
    assert.ok(c.marcas.some((m) => m.tipo === 'inicio' && m.x === doc.inicio.x && m.y === doc.inicio.y), n);
  }
});

test('la cuadrícula marca salidas y lugares, y avisa de símbolos fuera de la leyenda y filas desiguales', () => {
  const p = M.cuadricula(leer('mapas/pozaluz.json'));
  assert.ok(p.marcas.some((m) => m.tipo === 'lugar' && /tienda/.test(m.texto)));
  assert.ok(p.marcas.some((m) => m.tipo === 'salida'));
  const malo = M.cuadricula({ leyenda: { a: { nombre: 'a', pasable: true } }, filas: ['aaa', 'aZ'], inicio: { x: 0, y: 0 } });
  assert.equal(malo.problemas.length, 2);
  assert.match(malo.problemas.join(' '), /«Z»/);
  assert.match(malo.problemas.join(' '), /fila 2 mide 2/);
});

test('la leyenda cuenta cuántas casillas usa cada símbolo', () => {
  const l = M.leyenda({ leyenda: { a: { nombre: 'pradera', pasable: true, zona: 'llanura' }, b: { nombre: 'risco' } }, filas: ['aab', 'aaa'] });
  assert.deepEqual(l.map((x) => [x.simbolo, x.usos, x.pasable]), [['a', 5, true], ['b', 1, false]]);
  assert.equal(l[0].zona, 'llanura');
});

test('las escenas del juego pasan la validación del editor', () => {
  for (const n of fs.readdirSync(path.join(CONT, 'escenas'))) {
    assert.deepEqual(M.validarEscena(leer('escenas/' + n), n.replace(/\.json$/, '')), [], n);
  }
});

test('validarEscena detecta id distinto, texto vacío o largo, hablante largo y marcadores mal formados', () => {
  const base = () => ({ tipo: 'escena', version: 1, id: 'x', lineas: [{ texto: 'Hola {heroe:guardian}.' }] });
  assert.deepEqual(M.validarEscena(base(), 'x'), []);
  assert.ok(M.validarEscena(base(), 'otra').some((e) => e.campo === 'id'));
  const d = base();
  d.lineas = [{ texto: '' }, { texto: 'a'.repeat(301) }, { texto: 'x', quien: 'q'.repeat(41) }, { texto: 'Hola {nombre}' }, { texto: 'Hola {heroe:Mal}' }];
  const e = M.validarEscena(d, 'x');
  for (const [i, campo] of [[0, 'texto'], [1, 'texto'], [2, 'quien'], [3, 'texto'], [4, 'texto']]) {
    assert.ok(e.some((x) => x.linea === i && x.campo === campo), 'línea ' + i);
  }
  d.lineas = [];
  assert.ok(M.validarEscena(d, 'x').some((x) => x.campo === 'lineas'));
});

test('marcadores devuelve las clases usadas o null si hay llaves sueltas', () => {
  assert.deepEqual(M.marcadores('{heroe:a} y {heroe:b-c}'), ['a', 'b-c']);
  assert.deepEqual(M.marcadores('sin nada'), []);
  assert.equal(M.marcadores('hola {'), null);
});

test('editar líneas: poner, quitar hablante, agregar, mover y borrar', () => {
  const d = { lineas: [{ quien: 'A', texto: 'uno' }, { texto: 'dos' }] };
  M.ponerLinea(d, 0, 'texto', 'UNO');
  M.ponerLinea(d, 0, 'quien', '');
  assert.deepEqual(d.lineas[0], { texto: 'UNO' });
  assert.equal(M.agregarLinea(d, 0), 1);
  assert.deepEqual(d.lineas[1], { texto: '' });
  assert.equal(M.moverLinea(d, 1, 1), 2);
  assert.equal(M.moverLinea(d, 0, -1), 0);
  assert.deepEqual(d.lineas.map((l) => l.texto), ['UNO', 'dos', '']);
  assert.equal(M.borrarLinea(d, 2).texto, '');
  assert.throws(() => M.borrarLinea(d, 5));
  assert.throws(() => M.ponerLinea(d, 0, 'id', 'x'));
});

test('el motor acepta una escena editada con estas funciones y rechaza un marcador roto',
  { skip: !HAY_JAVA && 'no hay javac' }, () => {
    const tmp = fs.mkdtempSync(path.join(os.tmpdir(), 'ff1-escena-'));
    try {
      fs.cpSync(CONT, tmp, { recursive: true });
      const doc = leer('escenas/ofelia.json');
      M.ponerLinea(doc, 2, 'texto', 'Volved con agua, {heroe:guardian}, aunque sea un hilo.');
      M.agregarLinea(doc);
      M.ponerLinea(doc, 3, 'quien', 'Ofelia');
      M.ponerLinea(doc, 3, 'texto', 'Y cuidado con la maleza.');
      assert.deepEqual(M.validarEscena(doc, 'ofelia'), []);
      fs.writeFileSync(path.join(tmp, 'escenas', 'ofelia.json'), L.serializar(doc));
      const ok = spawnSync(path.join(RAIZ_REPO, 'scripts', 'validar-contenido.sh'), [tmp], { encoding: 'utf8' });
      assert.equal(ok.status, 0, ok.stdout + ok.stderr);

      M.ponerLinea(doc, 3, 'texto', 'Rota {heroe:}');
      assert.ok(M.validarEscena(doc, 'ofelia').length > 0);
      fs.writeFileSync(path.join(tmp, 'escenas', 'ofelia.json'), L.serializar(doc));
      const mal = spawnSync(path.join(RAIZ_REPO, 'scripts', 'validar-contenido.sh'), [tmp], { encoding: 'utf8' });
      assert.equal(mal.status, 1);
      assert.match(mal.stdout, /ofelia/);
    } finally {
      fs.rmSync(tmp, { recursive: true, force: true });
    }
  });
