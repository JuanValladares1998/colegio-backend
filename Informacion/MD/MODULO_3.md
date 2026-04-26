# 📡 API Cursos Online — Módulo 3: Contenido Educativo

**Base URL:** `http://localhost:8080`
**Formato:** JSON
**Autenticación:** `Authorization: Bearer <token>`

---

## 📑 Índice

- [Headers requeridos](#-headers-requeridos)
- [Wrapper de respuesta](#-wrapper-de-respuesta-apiresponset)
- [Módulo Módulos](#-módulo-módulos---modulos)
- [Módulo Lecciones](#-módulo-lecciones---lecciones)
- [Módulo Tipos de Recurso](#-módulo-tipos-de-recurso---tipos-recurso)
- [Módulo Recursos](#-módulo-recursos---recursos)
- [Errores globales](#-errores-globales)

---

## 🔒 Headers requeridos

| Endpoint | `Authorization` | `Content-Type` |
|---|---|---|
| `POST /recursos/archivo` | ✅ `Bearer <token>` | `multipart/form-data` |
| `GET /recursos/{id}/descargar` | ✅ `Bearer <token>` | — (devuelve binario) |
| Todos los demás | ✅ `Bearer <token>` | `application/json` |

---

## 📦 Wrapper de respuesta `ApiResponse<T>`

Todos los endpoints (excepto descarga de archivo) devuelven esta misma estructura:

| Campo | Tipo | Descripción |
|---|---|---|
| `exito` | `boolean` | `true` si fue exitoso |
| `mensaje` | `string` | Descripción del resultado |
| `datos` | `T` \| `null` | DTO específico del endpoint. `null` en errores o en endpoints sin datos |

---

## 📚 Módulo Módulos — `/modulos`

> Un curso contiene múltiples módulos. Un módulo agrupa lecciones.

---

### `GET /modulos/curso/{idCurso}`
**Acceso:** 🔒 Autenticado

#### Envía
| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `idCurso` | `number` | ✅ | ID del curso. Va en la URL, sin body |

#### Recibe — `datos: ModuloResponse[]`
Lista ordenada por `valOrden` ascendente.

**`ModuloResponse`:**
| Campo | Tipo | Descripción |
|---|---|---|
| `idModulo` | `number` | ID del módulo |
| `idCurso` | `number` | ID del curso al que pertenece |
| `desCurso` | `string` | Nombre del curso |
| `desNombre` | `string` | Nombre del módulo |
| `desDescripcion` | `string` \| `null` | Descripción opcional |
| `valOrden` | `number` | Orden del módulo en el curso |
| `estActivo` | `boolean` | Si el módulo está activo |
| `fecCreacion` | `string` (datetime) | Fecha de creación |

---

### `GET /modulos/{id}`
**Acceso:** 🔒 Autenticado

#### Envía
| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `id` | `number` | ✅ | ID del módulo. Va en la URL, sin body |

#### Recibe — `datos: ModuloResponse`
Mismo objeto `ModuloResponse` de la tabla anterior.

---

### `POST /modulos`
**Acceso:** 🔒 `ROL_ADMIN` o `ROL_PROFESOR` (con sección asignada al curso)

#### Envía
| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `idCurso` | `number` | ✅ | ID del curso al que pertenecerá el módulo |
| `desNombre` | `string` | ✅ | Máx. 120 chars. Único dentro del curso |
| `desDescripcion` | `string` | ❌ | Descripción libre |
| `valOrden` | `number` | ✅ | Orden de visualización. Mín. `1` |

#### Recibe — `datos: ModuloResponse`
El módulo recién creado.

---

### `PUT /modulos/{id}`
**Acceso:** 🔒 `ROL_ADMIN` o `ROL_PROFESOR` (con sección asignada al curso)

#### Envía
| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `id` | `number` | ✅ | ID del módulo. Va en la URL |
| `idCurso` | `number` | ✅ | ID del curso |
| `desNombre` | `string` | ✅ | Máx. 120 chars |
| `desDescripcion` | `string` | ❌ | Descripción libre |
| `valOrden` | `number` | ✅ | Orden. Mín. `1` |

#### Recibe — `datos: ModuloResponse`
El módulo actualizado.

---

### `DELETE /modulos/{id}`
**Acceso:** 🔒 `ROL_ADMIN` o `ROL_PROFESOR` (con sección asignada al curso)

> ⚠️ Eliminación lógica. Pone `estActivo = false`. No borra de la BD.

#### Envía
| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `id` | `number` | ✅ | ID del módulo. Va en la URL, sin body |

#### Recibe — `datos: null`
Solo `exito` y `mensaje`.

---

## 📖 Módulo Lecciones — `/lecciones`

> Una lección pertenece a un módulo. Solo las lecciones publicadas son visibles para alumnos.

---

### `GET /lecciones/modulo/{idModulo}`
**Acceso:** 🔒 Autenticado (filtrado por rol)

> 🔍 **Comportamiento por rol:**
> - **Admin** y **Profesor** (con sección): ven publicadas y borradores.
> - **Alumno** (con inscripción activa): solo ve publicadas.
> - **Alumno** sin inscripción: `403`.

#### Envía
| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `idModulo` | `number` | ✅ | ID del módulo. Va en la URL, sin body |

#### Recibe — `datos: LeccionResponse[]`
Lista ordenada por `valOrden` ascendente.

**`LeccionResponse`:**
| Campo | Tipo | Descripción |
|---|---|---|
| `idLeccion` | `number` | ID de la lección |
| `idModulo` | `number` | ID del módulo padre |
| `desModulo` | `string` | Nombre del módulo padre |
| `desNombre` | `string` | Nombre de la lección |
| `desContenido` | `string` \| `null` | Contenido textual |
| `valOrden` | `number` | Orden dentro del módulo |
| `estObligatoria` | `boolean` | Si es obligatoria para completar el módulo |
| `estPublicada` | `boolean` | `true` si es visible al alumno |
| `estActiva` | `boolean` | Si la lección está activa |
| `fecCreacion` | `string` (datetime) | Fecha de creación |
| `fecPublicacion` | `string` (datetime) \| `null` | Fecha de publicación. `null` si está en borrador |

---

### `GET /lecciones/{id}`
**Acceso:** 🔒 Autenticado (mismo filtrado que listar)

> ⚠️ Si un alumno solicita una lección no publicada → `404` (no se le revela su existencia).

#### Envía
| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `id` | `number` | ✅ | ID de la lección. Va en la URL, sin body |

#### Recibe — `datos: LeccionResponse`
Mismo objeto `LeccionResponse` de la tabla anterior.

---

### `POST /lecciones`
**Acceso:** 🔒 `ROL_ADMIN` o `ROL_PROFESOR` (con sección asignada al curso)

> 📝 La lección nace como borrador (`estPublicada: false`). Para hacerla visible al alumno usar `PUT /lecciones/{id}/publicar`.

#### Envía
| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `idModulo` | `number` | ✅ | ID del módulo padre |
| `desNombre` | `string` | ✅ | Máx. 150 chars. Único dentro del módulo |
| `desContenido` | `string` | ❌ | Contenido textual de la lección |
| `valOrden` | `number` | ✅ | Orden. Mín. `1` |
| `estObligatoria` | `boolean` | ✅ | Si es obligatoria |

#### Recibe — `datos: LeccionResponse`
La lección recién creada con `estPublicada: false`.

---

### `PUT /lecciones/{id}`
**Acceso:** 🔒 `ROL_ADMIN` o `ROL_PROFESOR` (con sección asignada al curso)

#### Envía
| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `id` | `number` | ✅ | ID de la lección. Va en la URL |
| `idModulo` | `number` | ✅ | ID del módulo |
| `desNombre` | `string` | ✅ | Máx. 150 chars |
| `desContenido` | `string` | ❌ | Contenido textual |
| `valOrden` | `number` | ✅ | Orden. Mín. `1` |
| `estObligatoria` | `boolean` | ✅ | Si es obligatoria |

#### Recibe — `datos: LeccionResponse`
La lección actualizada.

---

### `PUT /lecciones/{id}/publicar`
**Acceso:** 🔒 `ROL_ADMIN` o `ROL_PROFESOR` (con sección asignada al curso)

#### Envía
| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `id` | `number` | ✅ | ID de la lección. Va en la URL, sin body |

#### Recibe — `datos: LeccionResponse`
La lección con `estPublicada: true` y `fecPublicacion` actualizada.

---

### `PUT /lecciones/{id}/despublicar`
**Acceso:** 🔒 `ROL_ADMIN` o `ROL_PROFESOR` (con sección asignada al curso)

#### Envía
| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `id` | `number` | ✅ | ID de la lección. Va en la URL, sin body |

#### Recibe — `datos: LeccionResponse`
La lección con `estPublicada: false` y `fecPublicacion: null`.

---

### `DELETE /lecciones/{id}`
**Acceso:** 🔒 `ROL_ADMIN` o `ROL_PROFESOR` (con sección asignada al curso)

> ⚠️ Eliminación lógica. Pone `estActiva = false`.

#### Envía
| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `id` | `number` | ✅ | ID de la lección. Va en la URL, sin body |

#### Recibe — `datos: null`
Solo `exito` y `mensaje`.

---

## 🏷️ Módulo Tipos de Recurso — `/tipos-recurso`

> Catálogo de solo lectura. Usar para poblar el `<select>` al crear recursos.

---

### `GET /tipos-recurso`
**Acceso:** 🔒 Autenticado

#### Envía
Sin parámetros.

#### Recibe — `datos: TipoRecursoResponse[]`

**`TipoRecursoResponse`:**
| Campo | Tipo | Descripción |
|---|---|---|
| `idTipoRecurso` | `number` | ID del tipo |
| `codTipo` | `string` | `ARCHIVO` \| `ENLACE` \| `VIDEO` |
| `desNombre` | `string` | Nombre legible |
| `estActivo` | `boolean` | Si el tipo está activo |

---

## 📎 Módulo Recursos — `/recursos`

> Una lección puede tener 0 o más recursos: archivos físicos, enlaces externos o videos.

---

### `GET /recursos/leccion/{idLeccion}`
**Acceso:** 🔒 Autenticado (filtrado por rol)

> 🔍 **Comportamiento por rol:**
> - **Admin** y **Profesor** (con sección): siempre.
> - **Alumno** (con inscripción): solo si la lección está publicada.

#### Envía
| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `idLeccion` | `number` | ✅ | ID de la lección. Va en la URL, sin body |

#### Recibe — `datos: RecursoResponse[]`

**`RecursoResponse`:**
| Campo | Tipo | Descripción |
|---|---|---|
| `idRecurso` | `number` | ID del recurso |
| `idLeccion` | `number` | ID de la lección padre |
| `idTipoRecurso` | `number` | ID del tipo |
| `codTipoRecurso` | `string` | `ARCHIVO` \| `ENLACE` \| `VIDEO` |
| `desTipoRecurso` | `string` | Nombre legible del tipo |
| `desNombre` | `string` | Nombre del recurso |
| `urlRuta` | `string` | URL externa (ENLACE/VIDEO) o ruta interna (ARCHIVO) |
| `estActivo` | `boolean` | Si el recurso está activo |
| `fecCreacion` | `string` (datetime) | Fecha de creación |

> 📌 **Cómo manejar `urlRuta` según `codTipoRecurso`:**
> - `ENLACE` → abrir directamente con `window.open(urlRuta, "_blank")`.
> - `VIDEO` → embeber con `<iframe>` o reproductor según dominio.
> - `ARCHIVO` → **NO usar `urlRuta` directamente**. Usar `GET /recursos/{id}/descargar`.

---

### `POST /recursos`
**Acceso:** 🔒 `ROL_ADMIN` o `ROL_PROFESOR` (con sección asignada al curso)

> ⚠️ **Solo para tipos ENLACE y VIDEO.** Para ARCHIVO usar `POST /recursos/archivo`.

#### Envía
| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `idLeccion` | `number` | ✅ | ID de la lección padre |
| `idTipoRecurso` | `number` | ✅ | ID del tipo (ENLACE o VIDEO) |
| `desNombre` | `string` | ✅ | Máx. 150 chars |
| `urlRuta` | `string` | ✅ | URL externa. Debe iniciar con `http://` o `https://` |

#### Recibe — `datos: RecursoResponse`
El recurso recién creado.

---

### `POST /recursos/archivo`
**Acceso:** 🔒 `ROL_ADMIN` o `ROL_PROFESOR` (con sección asignada al curso)

> 📤 Sube un archivo físico y crea un recurso de tipo `ARCHIVO` automáticamente.

#### Envía — `multipart/form-data`
| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `idLeccion` | `number` | ✅ | ID de la lección padre |
| `desNombre` | `string` | ✅ | Máx. 150 chars |
| `archivo` | `File` | ✅ | Máx. 20 MB. Extensiones permitidas (ver abajo) |

**Extensiones permitidas:**
`pdf`, `docx`, `doc`, `txt`, `pptx`, `ppt`, `xlsx`, `xls`, `csv`, `png`, `jpg`, `jpeg`, `zip`, `java`, `py`, `js`, `sql`, `html`, `css`.

#### Recibe — `datos: RecursoResponse`
El recurso recién creado con `codTipoRecurso: "ARCHIVO"` y `urlRuta` interna.

> 💡 **Tip frontend:** al usar `FormData`, **NO setear** el header `Content-Type` manualmente. El browser lo construye con el boundary correcto.

---

### `GET /recursos/{id}/descargar`
**Acceso:** 🔒 Autenticado (filtrado por rol)

> ⚠️ **Solo para recursos de tipo ARCHIVO.** No devuelve JSON, devuelve el binario.
> Para alumno: además de la inscripción, la lección debe estar publicada.

#### Envía
| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `id` | `number` | ✅ | ID del recurso. Va en la URL, sin body |

#### Recibe — Binario
**Headers de respuesta:**

| Header | Valor |
|---|---|
| `Content-Type` | `application/octet-stream` |
| `Content-Disposition` | `attachment; filename*=UTF-8''<nombre>` |
| `Content-Length` | tamaño en bytes |

**Ejemplo de descarga desde el frontend:**
```javascript
const res = await fetch(`/recursos/${idRecurso}/descargar`, {
  headers: { Authorization: `Bearer ${token}` }
});

if (!res.ok) {
  const err = await res.json();
  // ... manejar error con err.mensaje
  return;
}

const blob = await res.blob();
const url  = URL.createObjectURL(blob);
const a    = document.createElement("a");
a.href     = url;
a.download = "archivo";
a.click();
URL.revokeObjectURL(url);
```

> 📌 **Importante:** los errores (403, 404, 400) sí vienen como JSON con la estructura normal de `ApiResponse`.

---

### `DELETE /recursos/{id}`
**Acceso:** 🔒 `ROL_ADMIN` o `ROL_PROFESOR` (con sección asignada al curso)

> ⚠️ Eliminación lógica del registro. Si es de tipo `ARCHIVO`, **además se borra el archivo físico del disco**.

#### Envía
| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `id` | `number` | ✅ | ID del recurso. Va en la URL, sin body |

#### Recibe — `datos: null`
Solo `exito` y `mensaje`.

---

## ❌ Errores globales

Todos los errores devuelven `exito: false`, `datos: null` y el `mensaje` descriptivo.

| Código HTTP | Cuándo ocurre |
|---|---|
| `400` | Campos vacíos, formato inválido, regla de negocio violada o archivo inválido |
| `401` | Sin token o token expirado |
| `403` | Autenticado pero sin permisos (rol incorrecto, profesor sin sección, alumno sin inscripción) |
| `404` | Módulo, lección o recurso no encontrado |
| `409` | Nombre duplicado dentro del mismo curso o módulo |
| `413` | Archivo supera el tamaño máximo permitido (20 MB) |
| `500` | Error interno del servidor |

### Mensajes de error por endpoint

#### `POST /modulos` y `PUT /modulos/{id}`
| Código | Mensaje |
|---|---|
| `400` | `"El nombre del módulo es obligatorio."` |
| `400` | `"El nombre no puede superar 120 caracteres."` |
| `400` | `"El orden debe ser mayor o igual a 1."` |
| `403` | `"No tienes permiso para gestionar contenido de este curso."` |
| `404` | `"Módulo con ID X no encontrado."` |
| `404` | `"Curso con ID X no encontrado."` |
| `409` | `"Ya existe un módulo con el nombre 'X' en este curso."` |

#### `DELETE /modulos/{id}`
| Código | Mensaje |
|---|---|
| `403` | `"No tienes permiso para gestionar contenido de este curso."` |
| `404` | `"Módulo con ID X no encontrado."` |

#### `POST /lecciones` y `PUT /lecciones/{id}`
| Código | Mensaje |
|---|---|
| `400` | `"El nombre de la lección es obligatorio."` |
| `400` | `"El nombre no puede superar 150 caracteres."` |
| `400` | `"El orden debe ser mayor o igual a 1."` |
| `400` | `"Debe indicar si la lección es obligatoria."` |
| `403` | `"No tienes permiso para gestionar contenido de este curso."` |
| `404` | `"Módulo con ID X no encontrado."` |
| `404` | `"Lección con ID X no encontrada."` |
| `409` | `"Ya existe una lección con el nombre 'X' en este módulo."` |

#### `PUT /lecciones/{id}/publicar` y `/despublicar`
| Código | Mensaje |
|---|---|
| `403` | `"No tienes permiso para gestionar contenido de este curso."` |
| `404` | `"Lección con ID X no encontrada."` |

#### `GET /lecciones/{id}` y `GET /lecciones/modulo/{idModulo}`
| Código | Mensaje |
|---|---|
| `403` | `"No tienes permiso para gestionar contenido de este curso."` |
| `404` | `"Lección con ID X no encontrada."` (también si el alumno consulta una lección no publicada) |

#### `POST /recursos`
| Código | Mensaje |
|---|---|
| `400` | `"La URL es obligatoria para recursos de tipo ENLACE."` |
| `400` | `"La URL es obligatoria para recursos de tipo VIDEO."` |
| `400` | `"La URL debe empezar con http:// o https://."` |
| `400` | `"Para crear un recurso de tipo ARCHIVO usa el endpoint de subida de archivo."` |
| `400` | `"El nombre del recurso es obligatorio."` |
| `403` | `"No tienes permiso para gestionar contenido de este curso."` |
| `404` | `"Lección con ID X no encontrada."` |
| `404` | `"Tipo de recurso con ID X no encontrado."` |

#### `POST /recursos/archivo`
| Código | Mensaje |
|---|---|
| `400` | `"El archivo está vacío."` |
| `400` | `"El archivo no tiene nombre."` |
| `400` | `"Tipo de archivo no permitido: .X. Permitidos: [pdf, docx, ...]"` |
| `400` | `"El nombre del recurso es obligatorio."` |
| `400` | `"Error al guardar el archivo: <detalle>"` |
| `403` | `"No tienes permiso para gestionar contenido de este curso."` |
| `404` | `"Lección con ID X no encontrada."` |
| `413` | `"El archivo supera el tamaño máximo permitido."` |

#### `GET /recursos/{id}/descargar`
| Código | Mensaje |
|---|---|
| `400` | `"Solo los recursos de tipo ARCHIVO se pueden descargar. Los de tipo X se acceden por su URL."` |
| `403` | `"No tienes permiso para gestionar contenido de este curso."` |
| `404` | `"Recurso con ID X no encontrado."` (también si el alumno consulta archivo de lección no publicada) |
| `404` | `"El archivo físico no existe en el servidor: <ruta>"` |

#### `DELETE /recursos/{id}`
| Código | Mensaje |
|---|---|
| `403` | `"No tienes permiso para gestionar contenido de este curso."` |
| `404` | `"Recurso con ID X no encontrado."` |
