# 📡 API Cursos Online — Módulo 6: Gestión de Reportes

**Base URL:** `http://localhost:8080`
**Formato:** Binario (`application/pdf`, `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`)
**Autenticación:** `Authorization: Bearer <token>`

---

## 📑 Índice

- [1. Reportes de Progreso (Síncronos)](#1-reportes-de-progreso-sincronos)
- [2. Auditoría de Accesos](#2-auditoria-de-accesos)
- [3. Errores y Casos de Prueba](#3-errores-y-casos-de-prueba)

---

> [cite_start]💡 **Decisiones de Diseño (Handoff):** La generación de reportes es síncrona (descarga directa)[cite: 392]. Los documentos se regeneran bajo demanda y no se persisten en base de datos. [cite_start]No incluyen logo institucional en esta versión inicial[cite: 392].

## 1. Reportes de Progreso (Síncronos) — `/reportes`

Permite a Profesores y Administradores exportar el rendimiento de los alumnos. [cite_start]El acceso para profesores está protegido y validado contra las secciones que tienen asignadas [cite: 393-395].

### `GET /reportes/seccion/{idSeccion}/grupal/excel`
### `GET /reportes/seccion/{idSeccion}/grupal/pdf`
**Acceso:** 🔒 `ROL_ADMIN` o `ROL_PROFESOR` (con acceso a la sección).

> [cite_start]📊 Genera un tablero general con la lista de alumnos inscritos en la sección, mostrando cuántas lecciones han completado, su porcentaje de avance y su última fecha de actividad, promediando el rendimiento grupal al final[cite: 414, 420].

#### Envía
| Campo | Tipo | Ubicación | Descripción |
|---|---|---|---|
| `idSeccion` | `number` | Path | ID de la sección a consultar. |

#### Recibe
[cite_start]Un archivo binario directo con cabecera `Content-Disposition: attachment` [cite: 486-487].

---

### `GET /reportes/alumno/{idAlumno}/curso/{idCurso}/individual/excel`
### `GET /reportes/alumno/{idAlumno}/curso/{idCurso}/individual/pdf`
**Acceso:** 🔒 `ROL_ADMIN` o `ROL_PROFESOR` (con acceso a la sección del alumno).

> [cite_start]🔍 Genera un reporte detallado del desempeño de un solo alumno en un curso específico, desglosando módulo por módulo y lección por lección su estado de completitud [cite: 436, 442-445].

#### Envía
| Campo | Tipo | Ubicación | Descripción |
|---|---|---|---|
| `idAlumno` | `number` | Path | ID del alumno. |
| `idCurso` | `number` | Path | ID del curso. |

#### Recibe
[cite_start]Un archivo binario directo con cabecera `Content-Disposition: attachment` [cite: 486-487].

---

## 2. Auditoría de Accesos

### `GET /reportes/accesos/excel`
**Acceso:** 🔒 `ROL_ADMIN` (Exclusivo).

> 🛡️ Exporta en formato Excel el historial de inicios de sesión (exitosos y fallidos), direcciones IP y roles. [cite_start]Si no se envían fechas, por defecto evalúa los últimos 30 días [cite: 463-464].

#### Envía
| Campo | Tipo | Ubicación | Descripción |
|---|---|---|---|
| `fechaDesde` | `string` (ISO) | Query | [cite_start]Fecha inicio (Opcional, ej: `2026-01-01T00:00:00`)[cite: 485]. |
| `fechaHasta` | `string` (ISO) | Query | [cite_start]Fecha fin (Opcional, ej: `2026-04-30T23:59:59`)[cite: 485]. |

#### Recibe
[cite_start]Un archivo `.xlsx` binario directo [cite: 485-486].

---

## 3. Errores y Casos de Prueba

Todos los errores mantienen el estándar de la API: devuelven JSON con `exito: false`, `datos: null` y un `mensaje` descriptivo capturado por el `GlobalExceptionHandler`.

### Mensajes de error esperados
| Código | Excepción Lanzada | Mensaje Esperado / Condición |
|---|---|---|
| `404` | `SinDatosParaReporteException` | [cite_start]`"No se encontraron registros para los criterios seleccionados..."`[cite: 396]. |
| `500` | `ErrorGenerandoReporteException` | [cite_start]`"Hubo un problema al generar el documento..."` (Captura fallos de I/O o de librería)[cite: 397]. |
| `403` | `AccesoCursoDenegadoException` | [cite_start]Si el profesor intenta generar el reporte de una sección ajena[cite: 395]. |

### 🧪 Matriz de Pruebas (QA)
| Acción | Rol | Condición | HTTP | Resultado Esperado |
|---|---|---|---|---|
| **GET** grupal excel | Profesor | Sección con alumnos matriculados | `200` | [cite_start]Descarga archivo `.xlsx` con tabla y promedio[cite: 489]. |
| **GET** grupal pdf | Admin | Cualquier sección válida | `200` | [cite_start]Descarga archivo `.pdf` gracias al parche de validación de Admin [cite: 393-394, 489]. |
| **GET** grupal excel | Profesor | Sección SIN alumnos | `404` | [cite_start]Lanza `SinDatosParaReporteException`[cite: 411, 489]. |
| **GET** individual pdf | Profesor | Alumno no inscrito en su curso | `403` | [cite_start]Lanza `AccesoCursoDenegadoException`[cite: 489]. |
| **GET** accesos | Admin | Sin parámetros de fecha | `200` | [cite_start]Archivo Excel con datos de los últimos 30 días[cite: 463, 489]. |
| **GET** accesos | Profesor | Intentar acceder a este endpoint | `403` | [cite_start]Bloqueado por `@PreAuthorize("hasAuthority('ROL_ADMIN')")`[cite: 485, 489]. |

> [cite_start]📌 **Deuda Técnica:** La auditoría de "Reportes Generados" (CUS-17) y el envío automático por correo (CUS-17 A2) quedan diferidos para futuras iteraciones [cite: 489-491].