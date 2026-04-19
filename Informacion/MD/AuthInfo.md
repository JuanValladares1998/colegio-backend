# API de Autenticación — Cursos Online
**Base URL:** `http://localhost:8080`
**Versión:** 1.0.0
**Formato:** JSON
**Autenticación:** Bearer Token (JWT)

---

## Índice
- [Autenticación de requests](#autenticación-de-requests)
- [Estructura de respuesta](#estructura-de-respuesta)
- [Códigos de error globales](#códigos-de-error-globales)
- [Endpoints](#endpoints)
  - [POST /auth/login](#post-authlogin)
  - [POST /auth/logout](#post-authlogout)
  - [PUT /auth/cambiar-contrasena](#put-authcambiar-contrasena)
  - [POST /auth/registrar](#post-authregistrar)
  - [PUT /auth/recuperar-credenciales/{id}](#put-authrecuperar-credencialesid)

---

## Autenticación de requests

Todos los endpoints excepto `/auth/login` requieren el token JWT en el header:
Authorization: Bearer <token>

El token se obtiene en el login y expira en **8 horas**.
Si el token expiró o fue invalidado (logout), la API devuelve `401`.

---

## Estructura de respuesta

Todos los endpoints devuelven el mismo wrapper:

```json
{
  "exito":   true,
  "mensaje": "Descripción del resultado",
  "datos":   { ... }
}
```

En caso de error, `datos` viene como `null`:

```json
{
  "exito":   false,
  "mensaje": "Correo o contraseña incorrectos.",
  "datos":   null
}
```

---

## Códigos de error globales

| Código | Significado                                      |
|--------|--------------------------------------------------|
| 400    | Campos vacíos, formato inválido o regla de negocio violada |
| 401    | No autenticado o credenciales incorrectas        |
| 403    | Autenticado pero sin permisos suficientes        |
| 404    | Recurso no encontrado                            |
| 409    | Conflicto (ej. correo duplicado)                 |
| 500    | Error interno del servidor                       |

---

## Endpoints

---

### POST /auth/login

**Descripción:** Autentica al usuario y devuelve el token JWT.
**Acceso:** Público

#### Request

```json
POST /auth/login
Content-Type: application/json

{
  "correo":     "admin@colegio.edu",
  "contrasena": "Mi$Clave2026"
}
```

#### Respuesta exitosa `200 OK`

```json
{
  "exito":   true,
  "mensaje": "Login exitoso.",
  "datos": {
    "token":       "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1...",
    "tipo":        "Bearer",
    "idUsuario":   1,
    "nombres":     "Super",
    "apellidos":   "Administrador",
    "correo":      "admin@colegio.edu",
    "rol":         "ROL_ADMIN",
    "pwdTemporal": false
  }
}
```

> ⚠️ Si `pwdTemporal` es `true`, el frontend debe redirigir al formulario de cambio de contraseña antes de permitir el acceso normal.

#### Respuestas de error

| Código | Mensaje                                      | Causa            |
|--------|----------------------------------------------|------------------|
| 400    | `"El correo es obligatorio."`                | Campo vacío      |
| 400    | `"Debe ingresar un correo válido."`          | Formato inválido |
| 401    | `"Correo o contraseña incorrectos."`         | CUS-01 E1        |
| 403    | `"Cuenta inactiva. Contacte al administrador."` | CUS-01 E2     |
| 500    | `"Error temporal del sistema. Intente más tarde."` | Error BD  |

---

### POST /auth/logout

**Descripción:** Cierra la sesión activa. El token queda inválido en la BD aunque no haya expirado.
**Acceso:** Cualquier usuario autenticado

#### Request
POST /auth/logout
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...

_(Sin body)_

#### Respuesta exitosa `200 OK`

```json
{
  "exito":   true,
  "mensaje": "Sesión cerrada correctamente.",
  "datos":   null
}
```

#### Respuestas de error

| Código | Mensaje                              | Causa                  |
|--------|--------------------------------------|------------------------|
| 401    | `"No autenticado. Por favor inicie sesión."` | Token ausente o inválido |

---

### PUT /auth/cambiar-contrasena

**Descripción:** Cambia la contraseña del usuario autenticado. Cierra todas sus sesiones activas al finalizar.
**Acceso:** Cualquier usuario autenticado

#### Request

```json
PUT /auth/cambiar-contrasena
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "contrasenaActual":    "MiClaveVieja$1",
  "nuevaContrasena":     "NuevaClave$2026",
  "confirmarContrasena": "NuevaClave$2026"
}
```

#### Política de contraseña

La `nuevaContrasena` debe cumplir:
- Mínimo **8 caracteres**
- Al menos **1 letra mayúscula**
- Al menos **1 número**
- Al menos **1 símbolo** (`!@#$%^&*` etc.)

#### Respuesta exitosa `200 OK`

```json
{
  "exito":   true,
  "mensaje": "Contraseña actualizada correctamente.",
  "datos":   null
}
```

> ⚠️ Tras este response, el frontend debe cerrar la sesión local y redirigir al login, ya que todas las sesiones activas fueron invalidadas.

#### Respuestas de error

| Código | Mensaje                                              | Causa         |
|--------|------------------------------------------------------|---------------|
| 400    | `"La contraseña actual es obligatoria."`             | Campo vacío   |
| 400    | `"La contraseña debe tener mínimo 8 caracteres."`    | CUS-03 A2     |
| 400    | `"La contraseña debe incluir mayúsculas, números y símbolos."` | CUS-03 A2 |
| 400    | `"Las contraseñas no coinciden."`                    | CUS-03 A1     |
| 400    | `"La contraseña no puede ser igual a la actual."`    | CUS-03 A3     |
| 401    | `"La contraseña actual es incorrecta."`              | CUS-03 E1     |

---

### POST /auth/registrar

**Descripción:** Crea un nuevo usuario en el sistema. Si no se provee contraseña, el sistema genera una temporal automáticamente.
**Acceso:** Solo `ROL_ADMIN`

#### Request

```json
POST /auth/registrar
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "nombres":             "Carlos Alberto",
  "apellidos":           "Quispe Mamani",
  "correo":              "carlos@colegio.edu",
  "codRol":              "ROL_ALUMNO",
  "contrasenaTemporal":  null
}
```

**Valores válidos para `codRol`:**

| Valor          | Descripción   |
|----------------|---------------|
| `ROL_ADMIN`    | Administrador |
| `ROL_PROFESOR` | Profesor      |
| `ROL_ALUMNO`   | Alumno        |

#### Respuesta exitosa `201 Created`

```json
{
  "exito":   true,
  "mensaje": "Usuario registrado correctamente. Las credenciales temporales han sido generadas.",
  "datos": {
    "idUsuario":          12,
    "nombres":            "Carlos Alberto",
    "apellidos":          "Quispe Mamani",
    "correo":             "carlos@colegio.edu",
    "rol":                "ROL_ALUMNO",
    "contrasenaTemporal": "aB3xZ9qR2"
  }
}
```

> ⚠️ El campo `contrasenaTemporal` debe ser comunicado al usuario por el admin. El frontend puede mostrarlo en un modal de confirmación.

#### Respuestas de error

| Código | Mensaje                                               | Causa           |
|--------|-------------------------------------------------------|-----------------|
| 400    | `"Los nombres son obligatorios."`                     | Campo vacío     |
| 400    | `"Formato de correo inválido."`                       | Formato inválido|
| 400    | `"El rol 'ROL_X' no existe en el sistema."`           | Rol inválido    |
| 403    | `"Acceso denegado. Requiere privilegios de administrador."` | No es admin |
| 409    | `"El correo 'x@x.com' ya está asociado a otra cuenta activa."` | CUS-06 E1 |

---

### PUT /auth/recuperar-credenciales/{id}

**Descripción:** Restablece la contraseña de un usuario generando una nueva temporal. Invalida sus sesiones activas.
**Acceso:** `ROL_ADMIN` o `ROL_PROFESOR`

#### Request
PUT /auth/recuperar-credenciales/5
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...

_(Sin body — el ID del usuario va en la URL)_

#### Respuesta exitosa `200 OK`

```json
{
  "exito":   true,
  "mensaje": "Credenciales restablecidas correctamente.",
  "datos":   null
}
```

> ⚠️ La nueva contraseña temporal se genera internamente. Si se requiere notificar al usuario, se integra con el servicio de correo (`TODO` marcado en el código).

#### Respuestas de error

| Código | Mensaje                                               | Causa               |
|--------|-------------------------------------------------------|---------------------|
| 403    | `"Acceso denegado. Requiere privilegios de administrador."` | Sin permisos  |
| 404    | `"Usuario con ID 5 no encontrado."`                   | CUS-04 A1           |

---

## Flujo recomendado para el frontend

POST /auth/login
│
├─ pwdTemporal = true  →  Redirigir a cambio de contraseña
│                              PUT /auth/cambiar-contrasena
│                              Luego redirigir al login
│
└─ pwdTemporal = false →  Guardar token en memoria/sessionStorage
Redirigir según rol:
ROL_ADMIN    → /admin/dashboard
ROL_PROFESOR → /profesor/dashboard
ROL_ALUMNO   → /alumno/dashboard
Cada request siguiente:
Header: Authorization: Bearer <token>
Si el servidor devuelve 401:
→ Limpiar token local
→ Redirigir al login
POST /auth/logout
→ Limpiar token local
→ Redirigir al login


---

## Notas de seguridad para el frontend

- **No guardar el token en `localStorage`** — preferir `sessionStorage` o memoria en contexto de React/Angular para evitar ataques XSS.
- **Nunca loguear el token** en consola en producción.
- Si `pwdTemporal` es `true`, **bloquear toda navegación** hasta que el usuario cambie su contraseña.
- El token dura **8 horas**. Si expira, limpiar sesión y redirigir al login.