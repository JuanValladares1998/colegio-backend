# 📡 API Cursos Online — Módulo 5: Gestión de Evaluaciones

**Base URL:** `http://localhost:8080`
**Formato:** JSON
**Autenticación:** `Authorization: Bearer <token>`

---

## 📑 Índice

- [1. CRUD de Evaluaciones (Profesor / Admin)](#1-crud-de-evaluaciones-profesor-admin)
- [2. Gestión de Estados (Activar / Desactivar)](#2-gestion-de-estados-activar-desactivar)
- [3. Banco de Preguntas y Opciones (Profesor / Admin)](#3-banco-de-preguntas-y-opciones-profesor-admin)
- [4. Catálogos y Tipos de Pregunta](#4-catalogos-y-tipos-de-pregunta)
- [5. Rendición de Evaluación (Alumno)](#5-rendicion-de-evaluacion-alumno)
- [6. Errores y Casos de Prueba](#6-errores-y-casos-de-prueba)

---

## 1. CRUD de Evaluaciones (Profesor / Admin) — `/evaluaciones`

Este submódulo permite a los docentes y administradores gestionar las evaluaciones de los módulos de un curso (CUS-13). Toda creación y modificación está protegida por permisos de sección.

### `POST /evaluaciones`
**Acceso:** 🔒 `ROL_PROFESOR` (Debe tener acceso validado al curso del módulo) o `ROL_ADMIN`

> 💡 **Regla de Negocio:** Toda evaluación nace por defecto en estado inactivo (`estActiva: false`). No se pueden duplicar títulos de evaluaciones dentro de un mismo módulo.

#### Envía — `body: EvaluacionRequest`
| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `idModulo` | `number` | ✅ | ID del módulo al que pertenece la evaluación |
| `desTitulo` | `string` | ✅ | Título de la evaluación (Máx 150 caracteres) |
| `desInstrucciones`| `string` | ❌ | Instrucciones en texto o HTML (Máx 5000 caracteres) |
| `valPuntajeMinimo`| `number` | ✅ | Puntaje mínimo aprobatorio (ej. `60.00`). Máximo 100.00. |
| `valTiempoLimite` | `number` | ❌ | Tiempo en minutos. Si es `null`, no hay límite de tiempo. |
| `valMaxIntentos` | `number` | ✅ | Cantidad de intentos permitidos (ej. `3`). |

#### Recibe — `datos: EvaluacionResponse`
| Campo | Tipo | Descripción |
|---|---|---|
| `idEvaluacion` | `number` | ID generado de la evaluación |
| `idModulo` | `number` | ID del módulo asociado |
| `nombreModulo` | `string` | Nombre del módulo para referencia |
| `desTitulo` | `string` | Título registrado |
| `desInstrucciones`| `string` | Instrucciones guardadas |
| `valPuntajeMinimo`| `number` | Puntaje mínimo guardado |
| `valTiempoLimite` | `number` | Tiempo límite en minutos |
| `valMaxIntentos` | `number` | Número máximo de intentos |
| `estActiva` | `boolean` | Siempre `false` al crear |
| `fecCreacion` | `string` (datetime)| Fecha y hora de creación |

---

### `PUT /evaluaciones/{id}`
**Acceso:** 🔒 `ROL_PROFESOR` o `ROL_ADMIN`

> ⚠️ **Bloqueo por Estado:** Solo se pueden editar los datos de una evaluación si esta se encuentra **INACTIVA** (`estActiva = false`). Si la evaluación está activa, el sistema bloqueará la actualización para evitar cambiar reglas con alumnos rindiendo la prueba.

#### Envía — `body: EvaluacionRequest`
Mismos campos que el método `POST`. El `idModulo` debe coincidir con el módulo original (no se puede mover de módulo).

#### Recibe — `datos: EvaluacionResponse`
Devuelve el objeto actualizado con la misma estructura del `POST`.

---

### `GET /evaluaciones/modulo/{idModulo}`
**Acceso:** 🔒 `ROL_PROFESOR` o `ROL_ADMIN`

> 📊 Devuelve la lista completa de evaluaciones asociadas a un módulo específico, ordenadas alfabéticamente por título.

#### Envía
| Campo | Tipo | Ubicación | Descripción |
|---|---|---|---|
| `idModulo` | `number` | Path | ID del módulo a consultar. |

#### Recibe — `datos: EvaluacionResponse[]`
Lista de objetos `EvaluacionResponse`.

---

### `GET /evaluaciones/{id}`
**Acceso:** 🔒 `ROL_PROFESOR` o `ROL_ADMIN`

> 🔍 Devuelve el detalle individual de una evaluación específica.

#### Envía
| Campo | Tipo | Ubicación | Descripción |
|---|---|---|---|
| `id` | `number` | Path | ID de la evaluación a consultar. |

#### Recibe — `datos: EvaluacionResponse`
Objeto único `EvaluacionResponse`.

---

## 2. Gestión de Estados (Activar / Desactivar)

### `PUT /evaluaciones/{id}/activar`
**Acceso:** 🔒 `ROL_PROFESOR` o `ROL_ADMIN`

> 🟢 Cambia el estado de la evaluación a `estActiva = true`, habilitándola (teóricamente) para que los alumnos la puedan ver y resolver.

#### Envía
Solo ID en la URL, sin body.

#### Recibe
El objeto `EvaluacionResponse` completo, donde `estActiva` será `true`.

---

### `PUT /evaluaciones/{id}/desactivar`
**Acceso:** 🔒 `ROL_PROFESOR` o `ROL_ADMIN`

> 🔴 Cambia el estado a `estActiva = false`. Permite volver a editar los parámetros de la evaluación (`PUT /evaluaciones/{id}`).

#### Envía
Solo ID en la URL, sin body.

#### Recibe
El objeto `EvaluacionResponse` completo, donde `estActiva` será `false`.

---
## 3. Banco de Preguntas y Opciones (Profesor / Admin) — `/preguntas`

Gestiona las preguntas y sus posibles respuestas para una evaluación específica.

### `POST /preguntas` | `PUT /preguntas/{id}`

**Acceso:** 🔒 `ROL_PROFESOR` o `ROL_ADMIN`

> ⚠️ **Bloqueo:** No se pueden crear, editar ni eliminar preguntas si la evaluación padre está **ACTIVA** (`estActiva = true`).

#### Envía — `body: PreguntaRequest`

| Campo | Tipo | Descripción |
|---|---|---|
| `idEvaluacion` | `number` | ID de la evaluación padre |
| `idTipoPregunta` | `number` | ID del tipo (ver sección 4) |
| `desEnunciado` | `string` | Texto de la pregunta (máx. 5000 caracteres) |
| `valOrden` | `number` | Posición en el examen |
| `valPuntaje` | `number` | Peso de esta pregunta (ej: `1.00`) |
| `opciones` | `array` | Lista de `OpcionRespuestaRequest` |

#### Estructura de `opciones`

```json
{
  "desOpcion": "Texto de la respuesta",
  "estCorrecta": true,
  "valOrden": 1
}
```

---

### `DELETE /preguntas/{id}`

**Acceso:** 🔒 `ROL_PROFESOR` o `ROL_ADMIN`

Realiza una eliminación lógica de la pregunta (`estActiva = false`).

> Solo disponible si la evaluación padre está inactiva.

---

### `GET /preguntas/evaluacion/{idEvaluacion}`

**Acceso:** 🔒 `ROL_PROFESOR` o `ROL_ADMIN`

Lista todas las preguntas activas (`estActiva = true`) de una evaluación, ordenadas por `valOrden`, incluyendo sus respectivas opciones de respuesta.

---

### `GET /preguntas/{id}`

**Acceso:** 🔒 `ROL_PROFESOR` o `ROL_ADMIN`

Obtiene el detalle individual de una pregunta y sus opciones.

---

## 4. Catálogos y Tipos de Pregunta — `/tipos-pregunta`

### `GET /tipos-pregunta`

**Acceso:** 🔓 Cualquier usuario autenticado.

Devuelve los tipos de pregunta soportados por el sistema para validar la estructura de las opciones.

Esta validación se aplica dinámicamente en el backend al crear/editar.

| `codTipo` | Regla de Validación (`PreguntaInvalidaException`) |
|---|---|
| `OPCION_MULTIPLE` | Mínimo 2 opciones enviadas, exactamente 1 debe ser `estCorrecta = true`. |
| `VERDADERO_FALSO` | Exactamente 2 opciones enviadas, exactamente 1 debe ser `estCorrecta = true`. |
| `COMPLETAR_CODIGO` | Exactamente 1 opción enviada, la cual contiene la respuesta exacta (`estCorrecta = true`). |

## 5. Rendición de Evaluación (Alumno) — `/intentos`

Este submódulo es exclusivo para usuarios con `ROL_ALUMNO`. Gestiona el ciclo de vida de una rendición de examen, controlando tiempo, respuestas e idempotencia.

### `POST /intentos/evaluacion/{idEvaluacion}/iniciar`
**Acceso:** 🔒 `ROL_ALUMNO`

[cite_start]Inicia o reanuda un intento[cite: 288, 376]. **Precondiciones estrictas:**
1. [cite_start]La evaluación debe estar activa (`estActiva = true`)[cite: 289].
2. [cite_start]El alumno debe estar inscrito en el curso[cite: 291].
3. [cite_start]El alumno **debe haber completado el 100% de las lecciones obligatorias** publicadas del módulo (`countLeccionesObligatoriasPendientes` == 0)[cite: 247, 248, 292].
4. [cite_start]No haber excedido la cantidad de `valMaxIntentos` permitidos[cite: 239, 299].

> [cite_start]💡 **Reanudación:** Si el alumno ya tiene un intento en curso (`estCompletado = false`), el endpoint devuelve ese mismo intento en lugar de crear uno nuevo, siempre y cuando no haya expirado por límite de tiempo[cite: 295, 297].

### `GET /intentos/{idIntento}`
**Acceso:** 🔒 `ROL_ALUMNO`

[cite_start]Obtiene el estado actual del examen, incluyendo los `minutosRestantes` calculados dinámicamente y la lista de respuestas ya guardadas para poder retomar la evaluación donde se dejó[cite: 303, 347, 348].

### `PUT /intentos/{idIntento}/pregunta/{idPregunta}`
**Acceso:** 🔒 `ROL_ALUMNO`

[cite_start]Guarda o actualiza la respuesta a una pregunta específica[cite: 306, 378]. 
* [cite_start]Para `OPCION_MULTIPLE` o `VERDADERO_FALSO`: Se debe enviar `idOpcionElegida` y dejar el texto nulo[cite: 312, 313].
* [cite_start]Para `COMPLETAR_CODIGO`: Se debe enviar `desRespuestaTexto` y dejar el ID de opción en nulo[cite: 311].

### `POST /intentos/{idIntento}/finalizar`
**Acceso:** 🔒 `ROL_ALUMNO`

[cite_start]Cierra el intento de forma inmutable[cite: 318, 379]. [cite_start]El backend evalúa las respuestas contra las opciones correctas [cite: 334, 335][cite_start], calcula el puntaje total sobre 100 y determina si el alumno aprobó según el `valPuntajeMinimo` establecido en la evaluación[cite: 336, 338].

### `GET /intentos/{idIntento}/revision`
**Acceso:** 🔒 `ROL_ALUMNO`

[cite_start]Solo accesible si el intento está finalizado (`estCompletado = true`)[cite: 323, 381]. [cite_start]Devuelve el detalle de las preguntas, marcando claramente las opciones correctas y la elección del alumno para revisión del aprendizaje[cite: 360, 363].

### `GET /intentos/evaluacion/{idEvaluacion}/historial`
**Acceso:** 🔒 `ROL_ALUMNO`

[cite_start]Devuelve el listado de todos los intentos realizados por el alumno para una evaluación particular, ordenados del más reciente al más antiguo[cite: 320, 380].

---

## 6. Errores y Casos de Prueba

Todos los errores mantienen el estándar de la API: devuelven `exito: false`, `datos: null` y un `mensaje` descriptivo capturado por el `GlobalExceptionHandler`.

### Mensajes de error esperados
| Endpoint | Código | Excepción Lanzada | Mensaje Esperado |
|---|---|---|---|
| `POST /evaluaciones` | `404` | `ModuloNoEncontradoException` | `"El módulo con ID X no existe."` |
| `POST /evaluaciones` | `409` | `EvaluacionYaExisteException` | `"Ya existe una evaluación con el título 'X' en este módulo."` |
| `POST / PUT` | `400` | `@Valid` (Spring Validation) | `"El puntaje mínimo no puede superar 100"`, `"El título es obligatorio"`, etc. |
| `PUT /evaluaciones/{id}`| `409` | `EvaluacionActivaException` | `"La evaluación está activa. Desactívela primero para modificarla."` |
| `GET / PUT` | `404` | `EvaluacionNoEncontradaException`| `"Evaluación con ID X no encontrada."` |
| Todos | `403` | `AccesoCursoDenegadoException`| `"No tienes permiso para gestionar contenido de este curso."` (Si el profesor no está asignado al curso del módulo). |
| `POST/PUT /preguntas` | `400` | `PreguntaInvalidaException` | `"Una pregunta verdadero/falso debe tener exactamente 1 opción correcta."` (Depende de la regla del tipo) [cite: 5, 59-64] |
| `GET / PUT / DELETE` (Preguntas)| `404` | `PreguntaNoEncontradaException` | `"Pregunta con ID X no encontrada."` [cite: 1, 4] |
| `POST/PUT /preguntas` | `404` | `TipoPreguntaNoEncontradoException`| `"Tipo de pregunta con ID X no encontrado."` [cite: 2, 4, 46] |
| `POST .../iniciar` | `403` | `LeccionesPendientesException` | [cite_start]`"Debes completar las X lecciones obligatorias del módulo antes de rendir la evaluación."` [cite: 238, 243] |
| `POST .../iniciar` | `403` | `EvaluacionNoActivaException` | [cite_start]`"La evaluación no está activa para los alumnos."` [cite: 237, 243] |
| `POST .../iniciar` | `409` | `MaxIntentosAlcanzadosException` | [cite_start]`"Has alcanzado el número máximo de intentos permitidos (X)."` [cite: 239, 244] |
| `PUT/POST ...` | `409` | `IntentoCompletadoException` | [cite_start]`"El intento ya está finalizado y no admite cambios."` [cite: 236, 242] |
| `PUT .../pregunta` | `400` | `RespuestaInvalidaException` | [cite_start]`"La pregunta no pertenece a la evaluación del intento"`, `"Para esta pregunta debes enviar..."`, etc. [cite: 240, 244] |
| `GET .../revision` | `400` | `RespuestaInvalidaException` | [cite_start]`"El intento aún no está finalizado. Finalízalo primero para ver la revisión."` [cite: 323] |

### 🧪 Matriz de Pruebas (QA - Postman)
| Acción | Rol | Condición | Código HTTP | Resultado Esperado |
|---|---|---|---|---|
| **POST** crear eval | Profesor | `idModulo` pertenece a uno de sus cursos | `200 OK` | Devuelve `EvaluacionResponse` con `estActiva: false`. |
| **POST** crear eval | Profesor | `idModulo` pertenece a un curso de *otro* profesor | `403 Forbidden` | Bloqueado por `validarAccesoAlCurso()`. |
| **POST** crear eval | Profesor | Mismo `idModulo` y `desTitulo` de una prueba existente | `409 Conflict` | Error por validación de título duplicado en el módulo. |
| **PUT** actualizar | Profesor | Evaluación con `estActiva: false` | `200 OK` | Los datos de la evaluación se modifican correctamente. |
| **PUT** actualizar | Profesor | Evaluación con `estActiva: true` | `409 Conflict` | Bloqueo de seguridad para evitar mutación de pruebas en curso. |
| **PUT** activar | Admin | Cualquier evaluación | `200 OK` | `estActiva` cambia a `true`. |
| **GET** modulo/{id} | Profesor | Módulo asignado | `200 OK` | Devuelve el listado de evaluaciones del módulo. |
| **Cualquier endpoint** | Alumno | Intentar acceder a `/evaluaciones` | `403 Forbidden` | Spring Security (`@PreAuthorize`) bloquea la solicitud a nivel de controlador. |
| **POST** crear pregunta | Profesor | Enviar con Eval Activa (`estActiva: true`) | `409 Conflict` | [cite_start]Bloqueo de seguridad (`EvaluacionActivaException`) [cite: 45-46]. |
| **POST** crear pregunta | Profesor | Tipo `VERDADERO_FALSO` enviando 2 opciones correctas | `400 Bad Request` | Bloqueado por `PreguntaInvalidaException` . |
| **PUT** actualizar preg | Profesor | Enviar nuevas opciones con Eval Inactiva | `200 OK` | [cite_start]Borra opciones previas de BD y guarda las nuevas enviadas [cite: 53-54]. |
| **DELETE** eliminar preg| Profesor | ID válido y Eval Inactiva | `200 OK` | [cite_start]Queda con `estActiva: false` (eliminación lógica) [cite: 57, 81-82]. |
| **POST** iniciar | Alumno | Lecciones obligatorias sin completar | `403 Forbidden` | [cite_start]Bloqueado por `countLeccionesObligatoriasPendientes`[cite: 247, 248, 292]. |
| **POST** iniciar | Alumno | Intento activo existente (`estCompletado: false`) | `200 OK` | [cite_start]Reanuda la sesión; devuelve `IntentoEnCursoResponse` con tiempo restante[cite: 295, 297, 347, 348]. |
| **PUT** guardar resp. | Alumno | Intento superó `valTiempoLimite` en BD | `409 Conflict` | [cite_start]Auto-cierre detectado por `cerrarSiExpirado()`, rechaza guardado[cite: 328, 330]. |
| **POST** finalizar | Alumno | Intento válido | `200 OK` | [cite_start]Devuelve `valCalificacion` (0-100) y `estAprobado`[cite: 336, 337, 338]. |
| **GET** revisión | Alumno | Intento no finalizado | `400 Bad Request` | [cite_start]Rechazado hasta que finalice (`IntentoCompletadoException`)[cite: 323]. |