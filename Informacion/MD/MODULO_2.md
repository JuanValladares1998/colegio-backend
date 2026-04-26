# 📡 Módulo Académico — Referencia para Frontend

**Base URL:** `http://localhost:8080`  
**Autenticación:** `Authorization: Bearer <token>` (requerido en todos los endpoints)

---

## 📑 Índice

- [Niveles](#-niveles---niveles)
- [Cursos](#-cursos---cursos)
- [Secciones](#-secciones---secciones)
- [Errores globales del módulo](#-errores-del-módulo)

---

## 🎓 Niveles — `/niveles`

---

### `GET /niveles`
**Acceso:** 🔒 Autenticado

#### Envía
Solo el header `Authorization`. Sin body, sin parámetros.

#### Recibe — `datos: NivelResponse[]`
| Campo | Tipo | Descripción |
|---|---|---|
| `idNivel` | `number` | ID del nivel |
| `codNivel` | `string` | Código único. Ej: `BASICO` |
| `desNombre` | `string` | Nombre del nivel. Ej: `Nivel Básico` |
| `valOrden` | `number` | Orden de visualización |
| `estActivo` | `boolean` | Si el nivel está activo |

---

### `GET /niveles/{id}`
**Acceso:** 🔒 Autenticado

#### Envía
| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `id` | `number` | ✅ | ID del nivel. Va en la URL |

#### Recibe — `datos: NivelResponse`
Mismo objeto `NivelResponse` de la tabla anterior.

---

### `POST /niveles`
**Acceso:** 🔒 Solo `ROL_ADMIN`

#### Envía
| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `codNivel` | `string` | ✅ | Código único del nivel. Ej: `BASICO` |
| `desNombre` | `string` | ✅ | Nombre del nivel. Ej: `Nivel Básico` |
| `valOrden` | `number` | ✅ | Orden de visualización. Ej: `1` |

#### Recibe — `datos: NivelResponse`
El nivel recién creado.

---

### `PUT /niveles/{id}`
**Acceso:** 🔒 Solo `ROL_ADMIN`

#### Envía
| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `id` | `number` | ✅ | ID del nivel. Va en la URL |
| `codNivel` | `string` | ✅ | Nuevo código |
| `desNombre` | `string` | ✅ | Nuevo nombre |
| `valOrden` | `number` | ✅ | Nuevo orden |

#### Recibe — `datos: NivelResponse`
El nivel actualizado.

---

## 📚 Cursos — `/cursos`

---

### `GET /cursos`
**Acceso:** 🔒 Solo `ROL_ADMIN`

#### Envía — parámetros en la URL
| Parámetro | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `page` | `number` | ❌ | Página (0-based). Default: `0` |
| `size` | `number` | ❌ | Cantidad por página. Default: `10` |
| `sort` | `string` | ❌ | Campo a ordenar. Default: `desNombre` |

#### Recibe — `datos: Page<CursoResponse>`
| Campo | Tipo | Descripción |
|---|---|---|
| `content` | `CursoResponse[]` | Lista de cursos de la página actual |
| `totalElements` | `number` | Total de cursos |
| `totalPages` | `number` | Total de páginas |
| `number` | `number` | Página actual (0-based) |
| `size` | `number` | Tamaño de página |

**`CursoResponse`:**
| Campo | Tipo | Descripción |
|---|---|---|
| `idCurso` | `number` | ID del curso |
| `idNivel` | `number` \| `null` | ID del nivel asociado |
| `desNivel` | `string` \| `null` | Nombre del nivel asociado |
| `desNombre` | `string` | Nombre del curso |
| `desDescripcion` | `string` \| `null` | Descripción del curso |
| `estPublicado` | `boolean` | Si el curso está publicado |
| `estActivo` | `boolean` | Si el curso está activo |
| `fecCreacion` | `string` (datetime) | Fecha de creación |
| `fecPublicacion` | `string` (datetime) \| `null` | Fecha de publicación. `null` si no está publicado |

---

### `GET /cursos/publicados`
**Acceso:** 🔒 Autenticado (todos los roles)

#### Envía — parámetros en la URL
| Parámetro | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `page` | `number` | ❌ | Default: `0` |
| `size` | `number` | ❌ | Default: `10` |

#### Recibe — `datos: Page<CursoResponse>`
Solo cursos con `estPublicado: true`.

---

### `GET /cursos/{id}`
**Acceso:** 🔒 Autenticado

#### Envía
| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `id` | `number` | ✅ | ID del curso. Va en la URL |

#### Recibe — `datos: CursoResponse`
Detalle completo del curso.

---

### `POST /cursos`
**Acceso:** 🔒 Solo `ROL_ADMIN`

#### Envía
| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `idNivel` | `number` | ✅ | ID del nivel al que pertenece |
| `desNombre` | `string` | ✅ | Nombre del curso |
| `desDescripcion` | `string` | ❌ | Descripción del curso |

#### Recibe — `datos: CursoResponse`
El curso recién creado con `estPublicado: false` por defecto.

---
### `GET /cursos/mis-cursos`
**Acceso:** 🔒 Solo `ROL_ALUMNO`

#### Envía — parámetros en la URL
| Parámetro | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `page` | `number` | ❌ | Página (0-based). Default: `0` |
| `size` | `number` | ❌ | Cantidad por página. Default: `10` |
| `sort` | `string` | ❌ | Campo a ordenar. Default: `desNombre` |

> 💡 **Nota:** El ID del alumno no se envía en los parámetros, se extrae automáticamente del token de autenticación del usuario.

#### Recibe — `datos: Page<CursoResponse>`
Devuelve los cursos publicados de las secciones donde el alumno autenticado está inscrito activamente. 

La respuesta utiliza exactamente la misma estructura de paginación y el objeto `CursoResponse` detallado en el endpoint `GET /cursos`.

### `PUT /cursos/{id}`
**Acceso:** 🔒 Solo `ROL_ADMIN`

#### Envía
| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `id` | `number` | ✅ | ID del curso. Va en la URL |
| `idNivel` | `number` | ✅ | ID del nivel |
| `desNombre` | `string` | ✅ | Nombre del curso |
| `desDescripcion` | `string` | ❌ | Descripción |

#### Recibe — `datos: CursoResponse`
El curso actualizado.

---

### `PUT /cursos/{id}/publicar`
**Acceso:** 🔒 Solo `ROL_ADMIN`

#### Envía
| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `id` | `number` | ✅ | ID del curso. Va en la URL. Sin body |

#### Recibe — `datos: CursoResponse`
El curso con `estPublicado: true` y `fecPublicacion` con la fecha actual.

---

### `PUT /cursos/{id}/despublicar`
**Acceso:** 🔒 Solo `ROL_ADMIN`

#### Envía
| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `id` | `number` | ✅ | ID del curso. Va en la URL. Sin body |

#### Recibe — `datos: CursoResponse`
El curso con `estPublicado: false` y `fecPublicacion: null`.

---

## 🏫 Secciones — `/secciones`

---

### `GET /secciones`
**Acceso:** 🔒 Autenticado

#### Envía — parámetros en la URL
| Parámetro | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `page` | `number` | ❌ | Default: `0` |
| `size` | `number` | ❌ | Default: `10` |
| `sort` | `string` | ❌ | Default: `desNombre` |

#### Recibe — `datos: Page<SeccionResponse>`
| Campo | Tipo | Descripción |
|---|---|---|
| `content` | `SeccionResponse[]` | Lista de secciones |
| `totalElements` | `number` | Total de secciones |
| `totalPages` | `number` | Total de páginas |
| `number` | `number` | Página actual |
| `size` | `number` | Tamaño de página |

**`SeccionResponse`:**
| Campo | Tipo | Descripción |
|---|---|---|
| `idSeccion` | `number` | ID de la sección |
| `idCurso` | `number` | ID del curso |
| `desCurso` | `string` | Nombre del curso |
| `idAnioEscolar` | `number` | ID del año escolar |
| `valAnio` | `number` | Año. Ej: `2026` |
| `desNombre` | `string` | Nombre de la sección. Ej: `Taller de Programación - A` |
| `estActivo` | `boolean` | Si la sección está activa |
| `fecCreacion` | `string` (datetime) | Fecha de creación |
| `idProfesor` | `number` \| `null` | ID del profesor asignado. `null` si no tiene |
| `desProfesor` | `string` \| `null` | Nombre completo del profesor. `null` si no tiene |

---
### `GET /secciones/{idSeccion}/alumnos`
**Acceso:** 🔒 `ROL_ADMIN` o `ROL_PROFESOR`

#### Envía
| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `idSeccion` | `number` | ✅ | ID de la sección. Va en la URL |

#### Recibe — `datos: InscripcionAlumnoResponse[]`
Lista ordenada por apellido del alumno.

**`InscripcionAlumnoResponse`:**
| Campo | Tipo | Descripción |
|---|---|---|
| `idAlumnoSeccion` | `number` | ID del registro de inscripción |
| `idUsuario` | `number` | ID del alumno |
| `desNombres` | `string` | Nombres del alumno |
| `desApellidos` | `string` | Apellidos del alumno |
| `desCorreo` | `string` | Correo del alumno |
| `idSeccion` | `number` | ID de la sección |
| `desSeccion` | `string` | Nombre de la sección |
| `estActivo` | `boolean` | Si la inscripción está activa |
| `fecInscripcion` | `string` (datetime) | Fecha de inscripción |

---

### `POST /secciones/{idSeccion}/alumnos`
**Acceso:** 🔒 Solo `ROL_ADMIN`

#### Envía
| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `idSeccion` | `number` | ✅ | ID de la sección. Va en la URL |
| `idUsuario` | `number` | ✅ | ID del usuario con rol `ROL_ALUMNO` |

#### Recibe — `datos: InscripcionAlumnoResponse`
La inscripción recién creada con `estActivo: true`.

> ⚠️ El usuario debe tener `ROL_ALUMNO`. Si ya está inscrito en esta sección o en otra del mismo año escolar devuelve `409`.

---

### `DELETE /secciones/{idSeccion}/alumnos/{idUsuario}`
**Acceso:** 🔒 Solo `ROL_ADMIN`

> ⚠️ Eliminación lógica. Pone `estActivo: false`. Si después se vuelve a inscribir al alumno, se crea una nueva fila.

#### Envía
| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `idSeccion` | `number` | ✅ | ID de la sección. Va en la URL |
| `idUsuario` | `number` | ✅ | ID del alumno. Va en la URL. Sin body |

#### Recibe — `datos: null`
Solo `exito` y `mensaje`.
---
### `GET /secciones/curso/{idCurso}`
**Acceso:** 🔒 Autenticado

#### Envía
| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `idCurso` | `number` | ✅ | ID del curso. Va en la URL |

#### Recibe — `datos: Page<SeccionResponse>`
Secciones filtradas por curso.

---

### `GET /secciones/anio/{idAnio}`
**Acceso:** 🔒 Autenticado

#### Envía
| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `idAnio` | `number` | ✅ | ID del año escolar. Va en la URL |

#### Recibe — `datos: Page<SeccionResponse>`
Secciones filtradas por año escolar.

---

### `GET /secciones/{id}`
**Acceso:** 🔒 Autenticado

#### Envía
| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `id` | `number` | ✅ | ID de la sección. Va en la URL |

#### Recibe — `datos: SeccionResponse`
Detalle completo de la sección incluyendo profesor asignado si tiene.

---

### `POST /secciones`
**Acceso:** 🔒 Solo `ROL_ADMIN`

#### Envía
| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `idCurso` | `number` | ✅ | ID del curso |
| `idAnioEscolar` | `number` | ✅ | ID del año escolar |
| `desNombre` | `string` | ✅ | Nombre de la sección. Ej: `Taller de Programación - A` |

#### Recibe — `datos: SeccionResponse`
La sección recién creada con `idProfesor: null` y `desProfesor: null`.

---

### `PUT /secciones/{id}`
**Acceso:** 🔒 Solo `ROL_ADMIN`

#### Envía
| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `id` | `number` | ✅ | ID de la sección. Va en la URL |
| `idCurso` | `number` | ✅ | ID del curso |
| `idAnioEscolar` | `number` | ✅ | ID del año escolar |
| `desNombre` | `string` | ✅ | Nombre de la sección |

#### Recibe — `datos: SeccionResponse`
La sección actualizada.

---

### `POST /secciones/{id}/profesor`
**Acceso:** 🔒 Solo `ROL_ADMIN`

#### Envía
| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `id` | `number` | ✅ | ID de la sección. Va en la URL |
| `idProfesor` | `number` | ✅ | ID del usuario con rol `ROL_PROFESOR` |

#### Recibe — `datos: SeccionResponse`
La sección con el profesor asignado en `idProfesor` y `desProfesor`.

> ⚠️ El usuario debe tener `ROL_PROFESOR`. Si ya tiene profesor asignado devuelve `409`.

---

### `DELETE /secciones/{id}/profesor`
**Acceso:** 🔒 Solo `ROL_ADMIN`

#### Envía
| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `id` | `number` | ✅ | ID de la sección. Va en la URL. Sin body |

#### Recibe — `datos: SeccionResponse`
La sección con `idProfesor: null` y `desProfesor: null`.

---

## ❌ Errores del módulo

| Código | Mensaje | Causa |
|---|---|---|
| `400` | `"El nivel es obligatorio."` | Campo vacío en request |
| `400` | `"El usuario seleccionado no tiene el rol de Profesor."` | Asignar un no-profesor |
| `400` | `"Esta sección no tiene un profesor asignado."` | Remover sin profesor |
| `404` | `"Nivel con ID X no encontrado."` | Nivel inexistente |
| `404` | `"Curso con ID X no encontrado."` | Curso inexistente |
| `404` | `"Sección con ID X no encontrada."` | Sección inexistente |
| `404` | `"Año escolar con ID X no encontrado."` | Año escolar inexistente |
| `409` | `"Ya existe un curso con el nombre 'X'."` | Nombre duplicado |
| `409` | `"Ya existe una sección con el nombre 'X' en ese curso y año escolar."` | Sección duplicada |
| `409` | `"Esta sección ya tiene un profesor asignado."` | Asignación duplicada |
| `400` | `"El usuario con ID X no tiene rol de alumno."` | Inscribir un no-alumno |
| `404` | `"No existe una inscripción activa del alumno X en la sección Y."` | Dar de baja una inscripción inexistente |
| `404` | `"Usuario con ID X no encontrado."` | Inscribir usuario inexistente |
| `409` | `"El alumno ya está inscrito activamente en esta sección."` | Inscripción duplicada en la misma sección |
| `409` | `"El alumno ya está inscrito en otra sección del mismo año escolar."` | Regla: un alumno = una sección por año |