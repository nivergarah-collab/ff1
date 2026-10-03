'use strict';
// Pruebas del modelo de tablas del editor (H11). Sin dependencias: `scripts/probar-editor.sh`.
const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');
const { spawnSync } = require('node:child_process');

const T = require('../tablas.js');
const L = require('../logica.js');

const RAIZ_REPO = path.join(__dirname, '..', '..', '..');
const CONTENIDO = path.join(RAIZ_REPO, 'app', 'src', 'main', 'assets', 'contenido');
const HAY_JAVA = spawnSync('javac', ['-version']).status === 0;

const leer = (ruta) => JSON.parse(fs.readFileSync(path.join(CONTENIDO, ruta), 'utf8'));
const ids = (carpeta) => fs.readdirSync(path.join(CONTENIDO, carpeta)).filter((n) => n.endsWith('.json')).map((n) => n.replace(/\.json$/, ''));

function cargarTodo() {
  const docs = {};
  for (const n of ['combatientes', 'habilidades', 'objetos', 'botin', 'encuentros']) docs[n] = leer(n + '.json');
  const serv = leer('servicios.json');
  docs.tiendas = docs.posadas = docs.vecinos = docs.jefes = serv;
  return { docs, serv, refs: T.referencias(docs, ids('escenas'), ids('mapas')) };
}

test('obtener y poner trabajan con rutas anidadas y limpian los objetos vacíos', () => {
  const f = { id: 'a' };
  T.poner(f, 'efecto.estado.duracion', 3);
  assert.deepEqual(f, { id: 'a', efecto: { estado: { duracion: 3 } } });
  assert.equal(T.obtener(f, 'efecto.estado.duracion'), 3);
  assert.equal(T.obtener(f, 'efecto.nada.x'), undefined);
  T.poner(f, 'efecto.estado.duracion', undefined);
  assert.deepEqual(f, { id: 'a' });
  T.poner(f, 'otro.ruta', '');
  assert.deepEqual(f, { id: 'a' });
});

test('agregar, duplicar y borrar filas', () => {
  const tabla = T.TABLAS.habilidades;
  const doc = T.documentoNuevo(tabla);
  const a = T.agregar(doc, tabla);
  a.id = 'chispa'; a.nombre = 'Chispa';
  const copia = T.duplicar(doc, tabla, 0);
  assert.equal(copia.id, 'chispa-copia');
  assert.equal(copia.nombre, 'Chispa (copia)');
  assert.notEqual(copia, a);
  assert.equal(T.duplicar(doc, tabla, 0).id, 'chispa-copia-2');
  assert.deepEqual(doc.lista.map((h) => h.id), ['chispa', 'chispa-copia-2', 'chispa-copia']);
  assert.equal(T.borrar(doc, tabla, 1).id, 'chispa-copia-2');
  assert.equal(doc.lista.length, 2);
  assert.throws(() => T.borrar(doc, tabla, 9));
});

test('duplicar una entrada de botín deja el enemigo vacío para elegir otro', () => {
  const tabla = T.TABLAS.botin;
  const doc = { tipo: 'botin', version: 1, lista: [{ enemigo: 'musgoso', objetos: [{ id: 'x', probabilidad: 5 }] }] };
  const nueva = T.duplicar(doc, tabla, 0);
  assert.equal(nueva.enemigo, '');
  nueva.objetos[0].probabilidad = 99;
  assert.equal(doc.lista[0].objetos[0].probabilidad, 5, 'la copia no comparte objetos con el original');
});

test('el contenido real del juego no tiene errores en ninguna tabla', () => {
  const { docs, refs } = cargarTodo();
  for (const nombre of T.ORDEN) {
    assert.deepEqual(T.validarTabla(docs[nombre], T.TABLAS[nombre], refs), [], nombre);
  }
});

test('se detectan ids repetidos, formato de id, rangos y obligatorios', () => {
  const { docs, refs } = cargarTodo();
  const doc = JSON.parse(JSON.stringify(docs.combatientes));
  doc.lista.push(Object.assign({}, doc.lista[0]));
  doc.lista[1].vida = 0;
  doc.lista[2].id = 'Mal Id';
  delete doc.lista[3].ataque;
  const e = T.validarTabla(doc, T.TABLAS.combatientes, refs);
  const tiene = (fila, campo, re) => e.some((x) => x.fila === fila && x.campo === campo && re.test(x.mensaje));
  assert.ok(tiene(doc.lista.length - 1, 'id', /Repetido/));
  assert.ok(tiene(1, 'vida', /fuera del rango \[1, 99999\]/));
  assert.ok(tiene(2, 'id', /minúsculas/));
  assert.ok(tiene(3, 'ataque', /Obligatorio/));
});

test('se avisa de ids rotos en listas y subfilas', () => {
  const { docs, refs } = cargarTodo();
  const comb = JSON.parse(JSON.stringify(docs.combatientes));
  comb.lista.find((c) => c.bando === 'heroe').habilidades = ['no-existe'];
  assert.ok(T.validarTabla(comb, T.TABLAS.combatientes, refs).some((x) => x.campo === 'habilidades[0]' && /Id roto/.test(x.mensaje)));

  const botin = JSON.parse(JSON.stringify(docs.botin));
  botin.lista[0].objetos[0].id = 'fantasma';
  botin.lista[0].enemigo = 'arcanista'; // es un héroe, no un enemigo
  const eb = T.validarTabla(botin, T.TABLAS.botin, refs);
  assert.ok(eb.some((x) => x.campo === 'objetos[0].id' && /Id roto/.test(x.mensaje)));
  assert.ok(eb.some((x) => x.campo === 'enemigo' && /Id roto/.test(x.mensaje)));

  const enc = JSON.parse(JSON.stringify(docs.encuentros));
  enc.zonas[0].grupos[0].enemigos = [];
  enc.zonas[0].grupos[1].enemigos = Array(7).fill(enc.zonas[0].grupos[1].enemigos[0]);
  const ee = T.validarTabla(enc, T.TABLAS.encuentros, refs);
  assert.ok(ee.some((x) => x.campo === 'grupos[0].enemigos' && /Obligatorio/.test(x.mensaje)));
  assert.ok(ee.some((x) => x.campo === 'grupos[1].enemigos' && /Máximo 6/.test(x.mensaje)));
});

test('reglas entre campos: estado con duración, consumible con efecto, equipo con ranura, golpe fuerte solo de enemigos', () => {
  const { docs, refs } = cargarTodo();
  const h = JSON.parse(JSON.stringify(docs.habilidades));
  h.lista[0].estado = { id: 'veneno' };
  assert.ok(T.validarTabla(h, T.TABLAS.habilidades, refs).some((x) => x.campo === 'estado.duracion'));

  const o = JSON.parse(JSON.stringify(docs.objetos));
  o.lista.push({ id: 'pocion', nombre: 'Poción', categoria: 'consumible' });
  o.lista.push({ id: 'casco', nombre: 'Casco', categoria: 'equipo' });
  const eo = T.validarTabla(o, T.TABLAS.objetos, refs);
  assert.ok(eo.some((x) => x.campo === 'efecto.tipo'));
  assert.ok(eo.some((x) => x.campo === 'ranura'));

  const c = JSON.parse(JSON.stringify(docs.combatientes));
  const heroe = c.lista.find((x) => x.bando === 'heroe');
  heroe.golpeFuerte = { cada: 3, habilidad: 'chispa' };
  assert.ok(T.validarTabla(c, T.TABLAS.combatientes, refs).some((x) => x.campo === 'golpeFuerte' && /enemigos/.test(x.mensaje)));
});

test('usosDe encuentra dónde se usa un id antes de borrarlo', () => {
  const { docs } = cargarTodo();
  const usos = T.usosDe('musgoso', 'combatientes:enemigo', docs);
  const tablas = new Set(usos.map((u) => u.tabla));
  assert.ok(tablas.has('botin'));
  assert.ok(tablas.has('encuentros'));
  assert.deepEqual(T.usosDe('id-que-nadie-usa', 'objetos', docs), []);
  assert.ok(T.usosDe('tonico-de-raiz', 'objetos', docs).some((u) => u.tabla === 'tiendas'));
});

test('referencias separa héroes y enemigos y deja fuera lo que no está a la venta', () => {
  const { docs, refs } = cargarTodo();
  assert.ok(refs['combatientes:heroe'].includes('guardian'));
  assert.ok(!refs['combatientes:enemigo'].includes('guardian'));
  assert.ok(refs['objetos:enVenta'].includes('tonico-de-raiz'));
  assert.ok(!refs['objetos:enVenta'].includes('llave-de-cantera'));
  assert.ok(refs.escenas.includes('apertura'));
  assert.deepEqual(refs.ranuras, docs.objetos.ranuras);
});

// ---- Compatibilidad con el motor: el flujo "crear un enemigo y su encuentro" lo acepta el validador real ----

test('crear un enemigo, su botín y su encuentro desde las tablas pasa el validador del motor',
  { skip: !HAY_JAVA && 'no hay javac' }, () => {
    const tmp = fs.mkdtempSync(path.join(os.tmpdir(), 'ff1-tablas-'));
    try {
      fs.cpSync(CONTENIDO, tmp, { recursive: true });
      const { docs } = cargarTodo();
      const comb = JSON.parse(JSON.stringify(docs.combatientes));
      const botin = JSON.parse(JSON.stringify(docs.botin));
      const enc = JSON.parse(JSON.stringify(docs.encuentros));

      const e = T.agregar(comb, T.TABLAS.combatientes);
      Object.assign(e, { id: 'gusano-de-sal', nombre: 'Gusano de sal' });
      T.poner(e, 'vida', 33); T.poner(e, 'velocidad', 5);
      const b = T.agregar(botin, T.TABLAS.botin);
      b.enemigo = 'gusano-de-sal';
      b.objetos.push({ id: 'tonico-de-raiz', probabilidad: 30, cantidad: 2 });
      const zona = enc.zonas[0];
      zona.grupos.push({ enemigos: ['gusano-de-sal', 'gusano-de-sal'], peso: 2 });

      const refs = T.referencias({ combatientes: comb, habilidades: docs.habilidades, objetos: docs.objetos }, ids('escenas'), ids('mapas'));
      assert.deepEqual(T.validarTabla(comb, T.TABLAS.combatientes, refs), []);
      assert.deepEqual(T.validarTabla(botin, T.TABLAS.botin, refs), []);
      assert.deepEqual(T.validarTabla(enc, T.TABLAS.encuentros, refs), []);

      fs.writeFileSync(path.join(tmp, 'combatientes.json'), L.serializar(comb));
      fs.writeFileSync(path.join(tmp, 'botin.json'), L.serializar(botin));
      fs.writeFileSync(path.join(tmp, 'encuentros.json'), L.serializar(enc));
      const r = spawnSync(path.join(RAIZ_REPO, 'scripts', 'validar-contenido.sh'), [tmp], { encoding: 'utf8' });
      assert.equal(r.status, 0, r.stdout + r.stderr);
    } finally {
      fs.rmSync(tmp, { recursive: true, force: true });
    }
  });

test('el motor rechaza un id roto que la tabla también marca',
  { skip: !HAY_JAVA && 'no hay javac' }, () => {
    const tmp = fs.mkdtempSync(path.join(os.tmpdir(), 'ff1-tablas-'));
    try {
      fs.cpSync(CONTENIDO, tmp, { recursive: true });
      const { docs } = cargarTodo();
      const botin = JSON.parse(JSON.stringify(docs.botin));
      botin.lista[0].objetos[0].id = 'fantasma';
      const refs = T.referencias(docs, ids('escenas'), ids('mapas'));
      assert.ok(T.validarTabla(botin, T.TABLAS.botin, refs).length > 0);
      fs.writeFileSync(path.join(tmp, 'botin.json'), L.serializar(botin));
      const r = spawnSync(path.join(RAIZ_REPO, 'scripts', 'validar-contenido.sh'), [tmp], { encoding: 'utf8' });
      assert.equal(r.status, 1);
      assert.match(r.stdout, /botin\.json/);
    } finally {
      fs.rmSync(tmp, { recursive: true, force: true });
    }
  });
