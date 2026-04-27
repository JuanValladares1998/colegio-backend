# 📡 API Cursos Online — Módulo 4: Progreso del Alumno 

**Base URL:** `http://localhost:8080`
**Formato:** JSON
**Autenticación:** `Authorization: Bearer <token>`

---

## 📑 Índice

- [Módulo Progreso de Lecciones](#-módulo-progreso-de-lecciones---progreso)
- [Errores globales y Casos de Prueba](#-errores-y-casos-de-prueba)

---

## 📈 Módulo Progreso de Lecciones — `/progreso`

> Este módulo gestiona el seguimiento del avance del alumno (`TraProgresoLeccion`). Permite marcar lecciones como completadas (operación idempotente) y obtener estadísticas generales de los cursos inscritos (CUS-14).

---

### `POST /progreso/leccion/{idLeccion}/completar`
**Acceso:** 🔒 `ROL_ALUMNO` (debe tener inscripción activa en el curso de la lección)

> 💡 **Idempotencia:** Si el alumno ya había completado esta lección, el endpoint devuelve `200 OK` con los datos del progreso existente, sin duplicar registros en la base de datos.

#### Envía
| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `idLeccion` | `number` | ✅ | ID de la lección a completar. Va en la URL, sin body. |

#### Recibe — `datos: ProgresoLeccionResponse`

| Campo | Tipo | Descripción |
|---|---|---|
| `idProgreso` | `number` | ID del registro de progreso |
| `idLeccion` | `number` | ID de la lección completada |
| `desLeccion` | `string` | Nombre de la lección |
| `estCompletada` | `boolean` | Siempre `true` en respuestas exitosas |
| `fecCompletado` | `string` (datetime) | Fecha y hora en que se marcó como completada |

---

### `GET /progreso/mi-progreso`
**Acceso:** 🔒 `ROL_ALUMNO`

> 📊 Devuelve las estadísticas globales del alumno, calculadas mediante queries JPQL optimizadas, agrupando por curso y detallando por módulo.

#### Envía
Sin parámetros.

#### Recibe — `datos: ProgresoCursoResponse[]`
Lista de cursos en los que el alumno está inscrito.

**`ProgresoCursoResponse`:**
| Campo | Tipo | Descripción |
|---|---|---|
| `idCurso` | `number` | ID del curso |
| `desCurso` | `string` | Nombre del curso |
| `valTotalObligatorias` | `number` | Total de lecciones obligatorias publicadas en el curso |
| `valLeccionesCompletadas` | `number` | Cantidad de lecciones obligatorias que el alumno ha completado |
| `valPorcentaje` | `number` | Porcentaje de avance (0.0 a 100.0) |
| `fecUltimaActividad` | `string` (datetime) \| `null` | Fecha de la última lección completada. `null` si no hay actividad |
| `modulos` | `ProgresoModuloResponse[]` | Desglose del progreso por módulo |
| `calificaciones` | `array` | *(Reservado para futuras integraciones)* |

**`ProgresoModuloResponse`:**
| Campo | Tipo | Descripción |
|---|---|---|
| `idModulo` | `number` | ID del módulo |
| `desNombre` | `string` | Nombre del módulo |
| `estEstado` | `string` | Estado calculado (`PENDIENTE`, `EN_CURSO`, `COMPLETADO`) |
| `valCompletadas` | `number` | Lecciones completadas en este módulo |
| `valTotal` | `number` | Total de lecciones obligatorias en este módulo |

---

## ❌ Errores y Casos de Prueba

Todos los errores mantienen el estándar de devolver `exito: false`, `datos: null` y el `mensaje` descriptivo.

### Mensajes de error esperados

| Endpoint | Código | Mensaje / Condición |
|---|---|---|
| `POST /progreso/leccion/{id}/completar` | `403` | `"No estás inscrito en el curso al que pertenece esta lección."` |
| `POST /progreso/leccion/{id}/completar` | `404` | `"Lección con ID X no encontrada o no está publicada."` |
| Ambos | `401` | Sin token o token expirado. |
| Ambos | `403` | Rol distinto a `ROL_ALUMNO`. |

### 🧪 Matriz de Pruebas (QA - Postman)

| Acción | Condición | Código HTTP | Resultado Esperado |
|---|---|---|---|
| **POST** completar | `idLeccion` válido y publicada | `200 OK` | Devuelve DTO con `estCompletada: true` y fecha actual. |
| **POST** completar | Enviar 2 veces la misma petición | `200 OK` | Retorna el mismo `idProgreso` ambas veces. No hay error por duplicidad ni inserts extra en BD. |
| **POST** completar | Lección pertenece a curso sin inscripción | `403 Forbidden` | Bloqueado. Mensaje indicando falta de permisos/inscripción. |
| **POST** completar | Lección existe pero es un borrador | `404 Not Found` | Oculta la existencia de la lección para el alumno. |
| **GET** mi-progreso | Alumno nuevo sin actividad | `200 OK` | Cursos listados con `valPorcentaje: 0.0` y `fecUltimaActividad: null`. |
| **GET** mi-progreso | Alumno tras completar lecciones | `200 OK` | `valPorcentaje` recalculado correctamente. `estEstado` de módulos reflejando `EN_CURSO` o `COMPLETADO`. |
