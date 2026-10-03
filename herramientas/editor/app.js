'use strict';
// Interfaz del editor: conecta el DOM con las funciones puras de logica.js y los archivos de almacen.js.
(function () {
  const L = window.Logica;
  const A = window.Almacen;
  const ESQUEMA = window.ESQUEMA_CONFIGURACION;
  const T = window.Tablas;
  const ET = window.EditorTablas;
  const VB = window.VistaBalance;
  const $ = (id) => document.getElementById(id);

  const TIPOS = {
    configuracion: 'configuración', inicio: 'partida nueva', habilidades: 'habilidades', combatientes: 'combatientes',
    progresion: 'progresión', objetos: 'objetos', botin: 'botín', encuentros: 'encuentros', servicios: 'servicios',
    mapa: 'mapa', escena: 'escena', desconocido: 'otro',
  };

  const estado = {
    almacen: null,
    carpeta: '',
    documentos: [],
    actual: null,
    config: null, // {original, textoOriginal, entradas: {nombre: texto}, errores: {nombre: msg}, problemas, sucio}
    informe: null,
    tablas: {}, // ruta -> {doc, original (texto normalizado), invalido, sucio}
  };

  const BALANCE = '__balance';

  // Documentos de tablas y las tablas que contiene cada uno.
  const TABLAS_DE = {
    'combatientes.json': ['combatientes'], 'habilidades.json': ['habilidades'], 'objetos.json': ['objetos'],
    'botin.json': ['botin'], 'encuentros.json': ['encuentros'], 'servicios.json': ['tiendas', 'posadas', 'vecinos', 'jefes'],
  };

  // ---- Utilidades de DOM ----

  function el(etiqueta, atributos, ...hijos) {
    const nodo = document.createElement(etiqueta);
    for (const [k, v] of Object.entries(atributos || {})) {
      if (k === 'texto') nodo.textContent = v;
      else if (k === 'clase') nodo.className = v;
      else if (k.startsWith('on')) nodo.addEventListener(k.slice(2), v);
      else nodo.setAttribute(k, v);
    }
    for (const h of hijos) nodo.append(h);
    return nodo;
  }

  function avisar(texto, clase) {
    const a = $('aviso');
    a.hidden = !texto;
    a.className = 'aviso' + (clase ? ' ' + clase : '');
    a.textContent = texto || '';
  }

  // ---- Configuración ----

  function cargarConfiguracion(texto) {
    const r = L.analizar(texto);
    if (!r.ok) return { invalido: r.error };
    const original = r.valor;
    const valores = L.valoresDe(original, ESQUEMA);
    const entradas = {};
    for (const p of ESQUEMA.parametros) entradas[p.nombre] = String(valores[p.nombre]);
    return { original, textoOriginal: texto, entradas, errores: {}, problemas: L.validarConfiguracion(original, ESQUEMA), sucio: false };
  }

  function valoresDelFormulario() {
    const valores = {};
    for (const p of ESQUEMA.parametros) valores[p.nombre] = L.leerNumero(estado.config.entradas[p.nombre]);
    return valores;
  }

  function revalidar() {
    const c = estado.config;
    const valores = valoresDelFormulario();
    c.errores = {};
    for (const p of ESQUEMA.parametros) {
      const m = L.validarValor(p, valores[p.nombre]);
      if (m) c.errores[p.nombre] = m;
    }
    const nuevo = L.construirConfiguracion(c.original, valores, ESQUEMA);
    c.sucio = Object.keys(c.errores).length === 0
      ? L.serializar(nuevo) !== L.serializar(c.original)
      : true;
    return { valores, nuevo };
  }

  function hayErrores() {
    return estado.config && Object.keys(estado.config.errores).length > 0;
  }

  function actualizarBarra() {
    const c = estado.config;
    const tabla = estado.tablas[estado.actual];
    $('guardar').disabled = tabla ? !tabla.sucio : !(c && c.sucio && !hayErrores());
    $('revisar').disabled = !estado.almacen;
    const sucio = algunSucio();
    $('carpeta').replaceChildren(estado.almacen ? el('strong', { texto: estado.carpeta }) : 'Ninguna carpeta abierta');
    document.title = (sucio ? '● ' : '') + 'Editor de parámetros · ff1';
    for (const b of $('documentos').querySelectorAll('[data-ruta]')) {
      const ruta = b.getAttribute('data-ruta');
      const sucioDoc = ruta === 'configuracion.json' ? !!(c && c.sucio) : !!(estado.tablas[ruta] && estado.tablas[ruta].sucio);
      b.classList.toggle('sucio', sucioDoc);
    }
    const pendiente = document.querySelector('.pendiente');
    if (pendiente) pendiente.hidden = !(tabla ? tabla.sucio : (c && c.sucio));
  }

  function dibujarConfiguracion() {
    const c = estado.config;
    const cont = $('editor');
    cont.hidden = false;
    $('bienvenida').hidden = true;
    cont.replaceChildren();
    if (c.invalido) {
      cont.append(el('h2', { texto: 'configuracion.json' }), el('p', { clase: 'aviso error', texto: c.invalido }),
        el('p', { texto: 'Corrige el archivo a mano o restáuralo desde configuracion.json.bak; luego vuelve a abrir la carpeta.' }));
      return;
    }
    cont.append(el('div', { clase: 'encabezado' },
      el('h2', { texto: 'Configuración' }),
      el('span', { clase: 'pendiente', texto: 'Cambios sin guardar', hidden: '' })));
    cont.append(el('p', { clase: 'nota', texto: 'Parámetros de balance y ritmo del juego. Lo que no cambies conserva su valor por defecto. Los rangos son los del contrato de datos.' }));
    if (c.problemas.length) {
      const lista = el('ul');
      for (const p of c.problemas) lista.append(el('li', { texto: (p.campo ? p.campo + ': ' : '') + p.mensaje }));
      cont.append(el('div', { clase: 'aviso' }, el('strong', { texto: 'El archivo abierto tiene problemas; al guardar se corrigen o se quitan:' }), lista));
    }
    for (const g of L.agruparParametros(ESQUEMA)) {
      const seccion = el('div', { clase: 'grupo' }, el('h3', { texto: g.grupo }));
      for (const p of g.parametros) seccion.append(filaParametro(p));
      cont.append(seccion);
    }
    refrescarEstadoDeFilas();
    actualizarBarra();
  }

  function filaParametro(p) {
    const id = 'p-' + p.nombre.replace(/\W/g, '-');
    const entrada = el('input', { type: 'number', id, step: p.tipo === 'entero' ? '1' : 'any', min: p.minimo, max: p.maximo, 'data-nombre': p.nombre, 'aria-describedby': id + '-e' });
    entrada.value = estado.config.entradas[p.nombre];
    entrada.addEventListener('input', () => {
      estado.config.entradas[p.nombre] = entrada.value;
      revalidar();
      refrescarEstadoDeFilas();
      actualizarBarra();
    });
    const restablecer = el('button', { type: 'button', texto: 'Restablecer', title: 'Volver al valor por defecto (' + p.defecto + ')',
      onclick: () => { entrada.value = String(p.defecto); entrada.dispatchEvent(new Event('input')); } });
    return el('div', { clase: 'parametro', 'data-nombre': p.nombre },
      el('div', {},
        el('label', { for: id }, p.etiqueta, el('span', { clase: 'clave', texto: p.nombre })),
        el('p', { clase: 'descripcion', texto: p.descripcion })),
      el('div', { clase: 'control' },
        el('div', { clase: 'fila' }, entrada, restablecer),
        el('span', { clase: 'rango', texto: 'Rango ' + p.minimo + '–' + p.maximo + ' · por defecto ' + p.defecto }),
        el('span', { clase: 'cambiado', 'data-aviso': 'cambiado' }),
        el('span', { clase: 'error', id: id + '-e', role: 'alert' })));
  }

  function refrescarEstadoDeFilas() {
    const c = estado.config;
    for (const fila of document.querySelectorAll('.parametro')) {
      const nombre = fila.getAttribute('data-nombre');
      const p = ESQUEMA.parametros.find((x) => x.nombre === nombre);
      const entrada = fila.querySelector('input');
      const msg = c.errores[nombre] || '';
      entrada.setAttribute('aria-invalid', msg ? 'true' : 'false');
      fila.querySelector('.error').textContent = msg;
      const valor = L.leerNumero(c.entradas[nombre]);
      fila.querySelector('[data-aviso=cambiado]').textContent = !msg && valor !== p.defecto ? 'Distinto del valor por defecto' : '';
    }
  }

  // ---- Tablas de contenido (H11) ----

  async function cargarTablas() {
    estado.tablas = {};
    for (const ruta of Object.keys(TABLAS_DE)) {
      if (!estado.documentos.some((d) => d.ruta === ruta)) continue;
      const r = L.analizar(await estado.almacen.leer(ruta));
      if (!r.ok) { estado.tablas[ruta] = { invalido: r.error, sucio: false }; continue; }
      estado.tablas[ruta] = { doc: r.valor, original: L.serializar(r.valor), sucio: false };
    }
  }

  function docDeTabla(nombre) {
    const ruta = T.TABLAS[nombre].archivo;
    const t = estado.tablas[ruta];
    return t && t.doc ? t.doc : undefined;
  }

  function referenciasVigentes() {
    const docs = { combatientes: docDeTabla('combatientes'), habilidades: docDeTabla('habilidades'), objetos: docDeTabla('objetos') };
    const sinExt = (carpeta) => estado.documentos.filter((d) => d.ruta.startsWith(carpeta + '/')).map((d) => d.ruta.slice(carpeta.length + 1).replace(/\.json$/, ''));
    return T.referencias(docs, sinExt('escenas'), sinExt('mapas'));
  }

  function todosLosDocs() {
    const r = {};
    for (const n of T.ORDEN) r[n] = docDeTabla(n);
    return r;
  }

  function actualizarSucioTabla(ruta) {
    const t = estado.tablas[ruta];
    t.sucio = L.serializar(t.doc) !== t.original;
  }

  function dibujarTablas(doc) {
    const t = estado.tablas[doc.ruta];
    const cont = $('editor');
    cont.hidden = false;
    $('bienvenida').hidden = true;
    cont.replaceChildren();
    if (t.invalido) {
      cont.append(el('h2', { texto: doc.ruta }), el('p', { clase: 'aviso error', texto: t.invalido }),
        el('p', { texto: 'Corrige el archivo a mano o restáuralo desde su copia .bak; luego vuelve a abrir la carpeta.' }));
      return;
    }
    const nombres = TABLAS_DE[doc.ruta];
    cont.append(el('div', { clase: 'encabezado' },
      el('h2', { texto: doc.ruta }),
      el('span', { clase: 'pendiente', texto: 'Cambios sin guardar', hidden: '' })));
    const zona = el('div', { id: 'zonaTablas' });
    cont.append(zona);
    ET.dibujar(zona, {
      archivo: doc.ruta, nombres, doc: t.doc, refs: referenciasVigentes, todos: todosLosDocs,
      onCambio: () => { actualizarSucioTabla(doc.ruta); actualizarBarra(); },
    });
    actualizarBarra();
  }

  function algunSucio() {
    return !!(estado.config && estado.config.sucio) || Object.values(estado.tablas).some((t) => t.sucio);
  }

  async function guardarTabla(ruta) {
    const t = estado.tablas[ruta];
    const errores = ET.erroresDe(TABLAS_DE[ruta], t.doc, referenciasVigentes());
    if (errores.length) {
      const e = errores[0];
      avisar('No se puede guardar: ' + errores.length + ' problema(s). Primero: ' + T.TABLAS[e.tabla].titulo + (e.fila >= 0 ? ' #' + (e.fila + 1) : '') + ', ' + (e.campo || 'documento') + ': ' + e.mensaje, 'error');
      return;
    }
    try {
      const texto = L.serializar(t.doc);
      const r = await L.guardarConCopia(estado.almacen, ruta, texto);
      t.original = texto;
      t.sucio = false;
      avisar('Guardado ' + ruta + (r.copia ? ' (la versión anterior quedó en ' + r.copia + ')' : '') + '. Revísalo con el motor para confirmar que el juego lo acepta.', 'ok');
      await seleccionar(ruta);
    } catch (e) {
      avisar('No se pudo guardar: ' + e.message, 'error');
    }
  }

  // ---- Documentos ----

  function dibujarLista() {
    const ul = $('documentos');
    ul.replaceChildren();
    if (estado.almacen && estado.documentos.some((d) => d.tipo === 'combatientes')) {
      ul.append(el('li', {}, el('button', { type: 'button', 'data-ruta': BALANCE, 'aria-current': String(estado.actual === BALANCE), onclick: () => seleccionar(BALANCE) },
        el('span', { texto: 'Vista de balance' }), el('span', { clase: 'etiqueta', texto: 'cálculo' }))));
    }
    for (const d of estado.documentos) {
      const malo = estado.informe && estado.informe.documentos.find((x) => x.documento === d.ruta && x.estado === 'error');
      const boton = el('button', { type: 'button', 'data-ruta': d.ruta, 'aria-current': String(d.ruta === estado.actual), onclick: () => seleccionar(d.ruta) },
        el('span', { texto: d.ruta, clase: malo ? 'estado-error' : '' }),
        el('span', { clase: 'etiqueta', texto: d.editable ? 'editable' : TIPOS[d.tipo] }));
      ul.append(el('li', {}, boton));
    }
  }

  async function dibujarBalance() {
    const cont = $('editor');
    cont.hidden = false;
    $('bienvenida').hidden = true;
    let progresion;
    if (estado.documentos.some((d) => d.ruta === 'progresion.json')) {
      const r = L.analizar(await estado.almacen.leer('progresion.json'));
      if (r.ok) progresion = r.valor;
    }
    const valoresVigentes = () => {
      const c = estado.config;
      if (!c || c.invalido) return {};
      const v = {};
      for (const p of ESQUEMA.parametros) { const n = L.leerNumero(c.entradas[p.nombre]); if (Number.isFinite(n)) v[p.nombre] = n; }
      return v;
    };
    VB.dibujar(cont, { docs: { combatientes: docDeTabla('combatientes'), progresion }, valores: valoresVigentes });
    actualizarBarra();
  }

  async function seleccionar(ruta) {
    estado.actual = ruta;
    dibujarLista();
    if (ruta === BALANCE) { await dibujarBalance(); return; }
    const doc = estado.documentos.find((d) => d.ruta === ruta);
    if (doc.tipo === 'configuracion') {
      if (!estado.config) estado.config = cargarConfiguracion(await estado.almacen.leer(ruta));
      if (!estado.config.invalido) revalidar();
      dibujarConfiguracion();
      return;
    }
    if (TABLAS_DE[doc.ruta] && estado.tablas[doc.ruta]) {
      dibujarTablas(doc);
      return;
    }
    await mostrarSoloLectura(doc);
    actualizarBarra();
  }

  async function mostrarSoloLectura(doc) {
    const cont = $('editor');
    cont.hidden = false;
    $('bienvenida').hidden = true;
    const texto = await estado.almacen.leer(doc.ruta);
    const r = L.analizar(texto);
    cont.replaceChildren(
      el('div', { clase: 'encabezado' }, el('h2', { texto: doc.ruta }), el('span', { clase: 'insignia', texto: 'Solo lectura · ' + TIPOS[doc.tipo] })),
      el('p', { clase: 'nota', texto: 'Este documento todavía no se edita desde aquí; se verá y se podrá modificar en las próximas versiones del editor.' }));
    if (!r.ok) cont.append(el('p', { clase: 'aviso error', texto: r.error }));
    cont.append(el('pre', { clase: 'json', texto: r.ok ? L.serializar(r.valor) : texto }));
  }

  async function abrirCarpeta() {
    let handle;
    try {
      handle = await window.showDirectoryPicker({ mode: 'readwrite' });
    } catch (e) {
      if (e && e.name === 'AbortError') return;
      avisar('No se pudo abrir la carpeta: ' + e.message, 'error');
      return;
    }
    if (algunSucio() && !window.confirm('Hay cambios sin guardar. ¿Descartarlos y abrir otra carpeta?')) return;
    try {
      const almacen = A.crearAlmacenCarpeta(handle);
      const rutas = await almacen.listar();
      const documentos = L.clasificarDocumentos(rutas);
      if (!documentos.some((d) => d.tipo === 'configuracion')) {
        avisar('En "' + handle.name + '" no hay configuracion.json: no parece una carpeta de contenido.', 'error');
        return;
      }
      Object.assign(estado, { almacen, carpeta: handle.name, documentos, config: null, informe: null, actual: null, tablas: {} });
      await cargarTablas();
      avisar('');
      await seleccionar('configuracion.json');
    } catch (e) {
      avisar('No se pudo leer la carpeta: ' + e.message, 'error');
    }
  }

  async function guardar() {
    if (estado.tablas[estado.actual]) { await guardarTabla(estado.actual); return; }
    const c = estado.config;
    const { nuevo } = revalidar();
    if (hayErrores()) { avisar('Corrige los valores marcados antes de guardar.', 'error'); return; }
    const errores = L.validarConfiguracion(nuevo, ESQUEMA);
    if (errores.length) { avisar('El documento no cumple el contrato: ' + errores[0].campo + ' ' + errores[0].mensaje, 'error'); return; }
    try {
      const texto = L.serializar(nuevo);
      const r = await L.guardarConCopia(estado.almacen, 'configuracion.json', texto);
      c.original = nuevo;
      c.textoOriginal = texto;
      c.problemas = [];
      c.sucio = false;
      avisar('Guardado configuracion.json' + (r.copia ? ' (la versión anterior quedó en ' + r.copia + ')' : '') + '. Revísalo con el motor para confirmar que el juego lo acepta.', 'ok');
      dibujarConfiguracion();
    } catch (e) {
      avisar('No se pudo guardar: ' + e.message, 'error');
    }
  }

  // ---- Revisar con el motor ----

  function abrirRevisar() {
    $('comando').textContent = L.comandoValidar(estado.carpeta);
    dibujarInforme();
    $('dialogoRevisar').showModal();
  }

  function dibujarInforme() {
    const cont = $('resultadoInforme');
    cont.replaceChildren();
    const inf = estado.informe;
    if (!inf) return;
    const resumen = inf.ok ? 'Contenido válido: el motor aceptó todos los documentos.'
      : 'Con problemas: ' + inf.errores + ' con errores, ' + inf.omitidos + ' sin comprobar.';
    cont.append(el('p', { clase: inf.ok ? 'aviso ok' : 'aviso error', texto: resumen }));
    const ul = el('ul', { clase: 'informe' });
    for (const d of inf.documentos) {
      const li = el('li', {}, el('span', { clase: d.estado, texto: d.estado.toUpperCase() + '  ' + d.documento }));
      if (d.detalle && d.estado !== 'ok') li.append(el('span', { clase: 'detalle', texto: d.detalle }));
      ul.append(li);
    }
    cont.append(ul);
  }

  async function cargarInforme(evento) {
    const archivo = evento.target.files[0];
    if (!archivo) return;
    const inf = L.interpretarInforme(await archivo.text());
    if (inf.error) {
      $('resultadoInforme').replaceChildren(el('p', { clase: 'aviso error', texto: inf.error }));
      return;
    }
    estado.informe = inf;
    dibujarInforme();
    dibujarLista();
  }

  async function copiarComando() {
    try {
      await navigator.clipboard.writeText($('comando').textContent);
      $('copiarComando').textContent = 'Copiado';
    } catch (e) {
      $('copiarComando').textContent = 'Selecciona y copia a mano';
    }
  }

  // ---- Arranque ----

  window.addEventListener('beforeunload', (e) => {
    if (algunSucio()) { e.preventDefault(); e.returnValue = ''; }
  });
  $('abrir').addEventListener('click', abrirCarpeta);
  $('guardar').addEventListener('click', guardar);
  $('revisar').addEventListener('click', abrirRevisar);
  $('copiarComando').addEventListener('click', copiarComando);
  $('archivoInforme').addEventListener('change', cargarInforme);
  if (!window.showDirectoryPicker) {
    $('sinApi').hidden = false;
    $('abrir').disabled = true;
  }
  actualizarBarra();
})();
