'use strict';
// Acceso a los archivos de la carpeta de contenido. Un almacén tiene: listar(), existe(ruta),
// leer(ruta) y escribir(ruta, texto), con rutas relativas como "mapas/campo.json".
// `crearAlmacenCarpeta` usa la API de archivos del navegador (showDirectoryPicker);
// `crearAlmacenMemoria` es el doble de pruebas.
(function (raiz) {
  const A = {};

  A.crearAlmacenMemoria = function (archivos) {
    const datos = Object.assign({}, archivos);
    return {
      async listar() { return Object.keys(datos).filter((r) => r.endsWith('.json')).sort(); },
      async existe(ruta) { return ruta in datos; },
      async leer(ruta) {
        if (!(ruta in datos)) throw new Error(ruta + ': no existe');
        return datos[ruta];
      },
      async escribir(ruta, texto) { datos[ruta] = texto; },
    };
  };

  A.crearAlmacenCarpeta = function (raizHandle) {
    async function carpetaDe(partes, crear) {
      let dir = raizHandle;
      for (const parte of partes) dir = await dir.getDirectoryHandle(parte, { create: !!crear });
      return dir;
    }
    const partir = (ruta) => {
      const partes = ruta.split('/');
      if (partes.some((p) => p === '' || p === '.' || p === '..')) throw new Error('Ruta no válida: ' + ruta);
      return { dirs: partes.slice(0, -1), nombre: partes[partes.length - 1] };
    };
    async function recorrer(dir, prefijo, salida, profundidad) {
      for await (const [nombre, h] of dir.entries()) {
        if (nombre.startsWith('.')) continue;
        if (h.kind === 'directory' && profundidad < 2) await recorrer(h, prefijo + nombre + '/', salida, profundidad + 1);
        else if (h.kind === 'file' && nombre.endsWith('.json')) salida.push(prefijo + nombre);
      }
    }
    return {
      async listar() {
        const salida = [];
        await recorrer(raizHandle, '', salida, 0);
        return salida.sort();
      },
      async existe(ruta) {
        const { dirs, nombre } = partir(ruta);
        try {
          await (await carpetaDe(dirs, false)).getFileHandle(nombre);
          return true;
        } catch (e) {
          if (e && (e.name === 'NotFoundError' || e.name === 'TypeMismatchError')) return false;
          throw e;
        }
      },
      async leer(ruta) {
        const { dirs, nombre } = partir(ruta);
        const archivo = await (await (await carpetaDe(dirs, false)).getFileHandle(nombre)).getFile();
        return archivo.text();
      },
      async escribir(ruta, texto) {
        const { dirs, nombre } = partir(ruta);
        const h = await (await carpetaDe(dirs, true)).getFileHandle(nombre, { create: true });
        const w = await h.createWritable();
        await w.write(texto);
        await w.close();
      },
    };
  };

  if (typeof module !== 'undefined' && module.exports) module.exports = A;
  else raiz.Almacen = A;
})(typeof window !== 'undefined' ? window : globalThis);
