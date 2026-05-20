# API — Endpoints Adicionales

Documentación para el frontend. Cubre los 7 endpoints nuevos agregados al backend de Cursos Online.

**Base URL:** `http://localhost:8080`
**Autenticación:** todos requieren header `Authorization: Bearer <jwt>` (excepto donde se indique).

## Wrapper de respuesta

Toda respuesta del backend usa este formato:

```json
{
  "exito": true,
  "mensaje": "Operación exitosa.",
  "datos": { /* contenido específico del endpoint */ }
}
```

En errores: `exito = false`, `datos = null` y `mensaje` describe el problema.

## Códigos HTTP comunes

| Código | Significado |
|---|---|
| 200 | OK |
| 400 | Error de validación (`mensaje` lo describe) |
| 401 | Token ausente o inválido |
| 403 | Token válido pero rol insuficiente |
| 404 | Recurso no encontrado |
| 409 | Conflicto (ej: correo duplicado) |
| 500 | Error inesperado del servidor |

---

## 1. Listar usuarios con filtros

```
GET /usuarios
```

**Rol requerido:** `ROL_ADMIN`

### Query params

| Param | Tipo | Default | Descripción |
|---|---|---|---|
| `rol` | string | — | `ROL_ADMIN` \| `ROL_PROFESOR` \| `ROL_ALUMNO` |
| `busqueda` | string | — | Busca en nombres, apellidos o correo (case-insensitive) |
| `estActivo` | boolean | — | `true` o `false` |
| `sinSeccion` | boolean | `false` | Si `true`, **solo aplica para `rol=ROL_ALUMNO`** y filtra alumnos sin inscripción activa |
| `page` | int | `0` | Página (0-based) |
| `size` | int | `20` | Tamaño de página |
| `sort` | string | — | Ej: `desApellidos,asc` |

### Ejemplos

- **Todos los profesores:** `GET /usuarios?rol=ROL_PROFESOR`
- **Alumnos sin sección:** `GET /usuarios?rol=ROL_ALUMNO&sinSeccion=true`
- **Buscar por nombre:** `GET /usuarios?busqueda=juan&estActivo=true`

### Response 200

```json
{
  "exito": true,
  "mensaje": "Usuarios obtenidos.",
  "datos": {
    "content": [
      {
        "idUsuario": 12,
        "desNombres": "Ana",
        "desApellidos": "Pérez",
        "desCorreo": "ana.perez@colegio.edu",
        "codRol": "ROL_ALUMNO",
        "estActivo": true,
        "estPwdTemporal": false,
        "fecUltimoAcceso": "2026-04-20T08:30:00"
      }
    ],
    "totalElements": 28,
    "totalPages": 2,
    "number": 0,
    "size": 20
  }
}
```

---

## 2. Obtener mi propia info

```
GET /usuarios/me
```

**Rol requerido:** cualquier autenticado.

Devuelve los datos del usuario asociado al token. Útil al cargar la app para saber quién está conectado.

### Response 200

```json
{
  "exito": true,
  "mensaje": "Mi información.",
  "datos": {
    "idUsuario": 5,
    "desNombres": "Gabriel",
    "desApellidos": "García",
    "desCorreo": "gabriel@colegio.edu",
    "codRol": "ROL_PROFESOR",
    "estActivo": true,
    "estPwdTemporal": false,
    "fecUltimoAcceso": "2026-04-26T14:20:00"
  }
}
```

---

## 3. Actualizar nombre y correo de un usuario

```
PUT /usuarios/{id}/datos
```

**Rol requerido:** `ROL_ADMIN`

### Path params

| Param | Descripción |
|---|---|
| `id` | ID del usuario a actualizar |

### Request body

```json
{
  "desNombres": "Juan Carlos",
  "desApellidos": "Pérez García",
  "desCorreo": "juan.perez@colegio.edu"
}
```

### Validaciones

- `desNombres`: obligatorio, máx 80 caracteres
- `desApellidos`: obligatorio, máx 80 caracteres
- `desCorreo`: obligatorio, formato email, máx 120 caracteres, único entre los demás usuarios

### Response 200

```json
{
  "exito": true,
  "mensaje": "Datos actualizados.",
  "datos": {
    "idUsuario": 12,
    "desNombres": "Juan Carlos",
    "desApellidos": "Pérez García",
    "desCorreo": "juan.perez@colegio.edu",
    "codRol": "ROL_ALUMNO",
    "estActivo": true
  }
}
```

### Errores

- `400` — validación fallida
- `404` — usuario no existe
- `409` — el correo ya está en otro usuario

---

## 4. Setear contraseña de un usuario (admin)

```
PUT /usuarios/{id}/contrasena
```

**Rol requerido:** `ROL_ADMIN`

Setea una contraseña específica para un usuario. La marca como temporal (`est_pwd_temporal = true`), forzando al usuario a cambiarla en su próximo login.

Diferente de `PUT /auth/recuperar-credenciales/{id}`, que genera una contraseña aleatoria.

### Request body

```json
{
  "pwdNueva": "NuevaPass123"
}
```

### Validaciones

- `pwdNueva`: obligatorio, entre 8 y 100 caracteres

### Response 200

```json
{
  "exito": true,
  "mensaje": "Contraseña actualizada.",
  "datos": null
}
```

### Errores

- `400` — contraseña muy corta o vacía
- `404` — usuario no existe

---

## 5. Resumen del año escolar

```
GET /anios-escolares/{idAnio}/resumen
```

**Rol requerido:** `ROL_ADMIN` o `ROL_PROFESOR`

Indicadores agregados del año: total de secciones, cursos, alumnos inscritos y profesores asignados. Útil para un dashboard de inicio.

### Response 200

```json
{
  "exito": true,
  "mensaje": "Resumen obtenido.",
  "datos": {
    "idAnioEscolar": 2,
    "valAnio": 2026,
    "desDescripcion": "Año escolar 2026",
    "fecInicio": "2026-01-15",
    "fecFin": "2026-12-15",
    "estActivo": true,
    "totalSecciones": 12,
    "totalCursos": 5,
    "totalAlumnosInscritos": 240,
    "totalProfesoresAsignados": 8
  }
}
```

### Errores

- `404` — año escolar no existe

---

## 6. Secciones sin profesor asignado

```
GET /secciones/sin-profesor
```

**Rol requerido:** `ROL_ADMIN`

Secciones activas que **no tienen** ningún profesor asignado activo. Útil para alertar al admin sobre asignaciones pendientes.

### Response 200

```json
{
  "exito": true,
  "mensaje": "Secciones sin profesor obtenidas.",
  "datos": [
    {
      "idSeccion": 7,
      "idCurso": 3,
      "nombreCurso": "Programación I",
      "idAnioEscolar": 2,
      "valAnio": 2026,
      "desNombre": "Programación I - B",
      "estActiva": true,
      "fecCreacion": "2026-02-01T10:00:00"
    }
  ]
}
```

> El shape exacto de `SeccionResponse` puede variar — está reusando el DTO existente del M2.

---

## 7. Asignaciones profesor-sección con filtros

```
GET /profesor-seccion
```

**Rol requerido:** `ROL_ADMIN` o `ROL_PROFESOR`

Listado de asignaciones activas con filtros combinables.

### Query params (todos opcionales)

| Param | Tipo | Descripción |
|---|---|---|
| `idProfesor` | int | Filtra por un profesor |
| `idSeccion` | int | Filtra por una sección |
| `idCurso` | int | Filtra por curso |
| `idAnio` | int | Filtra por año escolar |

### Ejemplos

- **Todas las del profesor 3:** `GET /profesor-seccion?idProfesor=3`
- **Del curso 1 en el año 2:** `GET /profesor-seccion?idCurso=1&idAnio=2`
- **Sin filtros (todas):** `GET /profesor-seccion`

### Response 200

```json
{
  "exito": true,
  "mensaje": "Asignaciones obtenidas.",
  "datos": [
    {
      "idProfesorSeccion": 14,
      "idProfesor": 3,
      "nombresProfesor": "Gabriel",
      "apellidosProfesor": "García",
      "correoProfesor": "gabriel@colegio.edu",
      "idSeccion": 5,
      "nombreSeccion": "Programación I - A",
      "idCurso": 1,
      "nombreCurso": "Programación I",
      "idAnioEscolar": 2,
      "valAnio": 2026,
      "fecAsignacion": "2026-02-15T09:00:00"
    }
  ]
}
```

---

## Notas para el frontend

1. **Paginación:** los endpoints que devuelven `Page<...>` siguen el formato estándar de Spring Data (`content`, `totalElements`, `totalPages`, `number`, `size`). Pueden ordenarse con `sort=campo,asc|desc`.
2. **Filtros combinables:** los query params son AND. Omitir un param = no filtrar por ese criterio.
3. **`/usuarios/me`** es el primer endpoint a llamar tras el login para saber qué UI mostrar (admin / profesor / alumno).
4. **Manejo de errores:** siempre revisar `exito`. En `false`, mostrar `mensaje` al usuario.
5. **Token expirado:** si recibes 401 en cualquier endpoint, redirige al login.
