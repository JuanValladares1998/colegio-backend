# 📡 API Cursos Online — Referencia para Frontend

**Base URL:** `http://localhost:8080`  
**Formato:** JSON  
**Autenticación:** `Authorization: Bearer <token>`

---

## 📑 Índice

- [Headers requeridos](#-headers-requeridos)
- [Wrapper de respuesta](#-wrapper-de-respuesta-apiresponse)
- [Módulo Autenticación](#-módulo-autenticación---authlogin)
- [Módulo Usuarios](#-módulo-usuarios---usuarios)
- [Errores globales](#-errores-globales)

---

## 🔒 Headers requeridos

| Endpoint | `Authorization` | `Content-Type` |
|---|---|---|
| `POST /auth/login` | ❌ No requerido | `application/json` |
| Todos los demás | ✅ `Bearer <token>` | `application/json` |
| `POST /usuarios/carga-masiva` | ✅ `Bearer <token>` | `multipart/form-data` |

---

## 📦 Wrapper de respuesta `ApiResponse<T>`

Todos los endpoints devuelven esta misma estructura:

| Campo | Tipo | Descripción |
|---|---|---|
| `exito` | `boolean` | `true` si fue exitoso |
| `mensaje` | `string` | Descripción del resultado |
| `datos` | `T` \| `null` | DTO específico del endpoint. `null` en errores o en endpoints sin datos |

---

## 🔑 Módulo Autenticación — `/auth`

---

### `POST /auth/login`
**Acceso:** Público

#### Envía
| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `correo` | `string` | ✅ | Correo electrónico válido |
| `contrasena` | `string` | ✅ | Contraseña del usuario |

#### Recibe — `datos: AuthResponse`
| Campo | Tipo | Descripción |
|---|---|---|
| `token` | `string` | Token JWT a guardar |
| `tipo` | `string` | Siempre `"Bearer"` |
| `idUsuario` | `number` | ID del usuario |
| `nombres` | `string` | Nombres del usuario |
| `apellidos` | `string` | Apellidos del usuario |
| `correo` | `string` | Correo del usuario |
| `rol` | `string` | `ROL_ADMIN` \| `ROL_PROFESOR` \| `ROL_ALUMNO` |
| `pwdTemporal` | `boolean` | Si es `true` → redirigir obligatoriamente a cambiar contraseña |

---

### `POST /auth/logout`
**Acceso:** 🔒 Autenticado

#### Envía
Solo el header `Authorization: Bearer <token>`. Sin body.

#### Recibe — `datos: null`
Solo `exito` y `mensaje`.

---

### `PUT /auth/cambiar-contrasena`
**Acceso:** 🔒 Autenticado

#### Envía
| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `contrasenaActual` | `string` | ✅ | Contraseña actual |
| `nuevaContrasena` | `string` | ✅ | Mín. 8 chars, mayúscula, número y símbolo |
| `confirmarContrasena` | `string` | ✅ | Debe ser igual a `nuevaContrasena` |

#### Recibe — `datos: null`
Solo `exito` y `mensaje`.

> ⚠️ Tras recibir `exito: true` → limpiar token local y redirigir al login.

---

### `POST /auth/registrar`
**Acceso:** 🔒 Solo `ROL_ADMIN`

#### Envía
| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `nombres` | `string` | ✅ | Nombres del usuario |
| `apellidos` | `string` | ✅ | Apellidos del usuario |
| `correo` | `string` | ✅ | Correo electrónico único |
| `codRol` | `string` | ✅ | `ROL_ADMIN` \| `ROL_PROFESOR` \| `ROL_ALUMNO` |
| `contrasenaTemporal` | `string` | ❌ | Si se omite, el sistema genera una automática |

#### Recibe — `datos: RegisterResponse`
| Campo | Tipo | Descripción |
|---|---|---|
| `idUsuario` | `number` | ID del usuario creado |
| `nombres` | `string` | Nombres |
| `apellidos` | `string` | Apellidos |
| `correo` | `string` | Correo |
| `rol` | `string` | Rol asignado |
| `contrasenaTemporal` | `string` | Contraseña generada — el admin debe comunicarla al usuario |

---

### `PUT /auth/recuperar-credenciales/{id}`
**Acceso:** 🔒 `ROL_ADMIN` o `ROL_PROFESOR`

#### Envía
| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `id` | `number` | ✅ | ID del usuario. Va en la URL, sin body |

#### Recibe — `datos: RecuperarCredencialesResponse`
| Campo | Tipo | Descripción |
|----------|-------|------|
| `correo` | `string` | Correo del usuario afectado|
| `contrasenaTemporal` | `string` | Nueva contraseña temporal generada — el admin debe comunicarla al usuario|


## 👥 Módulo Usuarios — `/usuarios`

> Todos los endpoints de este módulo requieren `ROL_ADMIN`.

---

### `GET /usuarios`
**Acceso:** 🔒 Solo `ROL_ADMIN`

#### Envía — parámetros en la URL
| Parámetro | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `page` | `number` | ❌ | Página (0-based). Default: `0` |
| `size` | `number` | ❌ | Cantidad por página. Default: `10` |
| `sort` | `string` | ❌ | Campo a ordenar. Default: `desApellidos` |

#### Recibe — `datos: Page<UsuarioResponse>`
| Campo | Tipo | Descripción |
|---|---|---|
| `content` | `UsuarioResponse[]` | Lista de usuarios de la página actual |
| `totalElements` | `number` | Total de usuarios en la BD |
| `totalPages` | `number` | Total de páginas |
| `number` | `number` | Página actual (0-based) |
| `size` | `number` | Tamaño de página solicitado |

**`UsuarioResponse`:**
| Campo | Tipo | Descripción |
|---|---|---|
| `idUsuario` | `number` | ID del usuario |
| `nombres` | `string` | Nombres |
| `apellidos` | `string` | Apellidos |
| `correo` | `string` | Correo |
| `rol` | `string` | `ROL_ADMIN` \| `ROL_PROFESOR` \| `ROL_ALUMNO` |
| `activo` | `boolean` | Si la cuenta está activa |
| `pwdTemporal` | `boolean` | Si tiene contraseña temporal pendiente |
| `fecCreacion` | `string` (datetime) | Fecha de creación |
| `fecUltimoAcceso` | `string` (datetime) \| `null` | Último acceso. `null` si nunca ingresó |

---

### `GET /usuarios/{id}`
**Acceso:** 🔒 Solo `ROL_ADMIN`

#### Envía
| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `id` | `number` | ✅ | ID del usuario. Va en la URL, sin body |

#### Recibe — `datos: UsuarioResponse`
Mismo objeto `UsuarioResponse` de la tabla anterior.

---

### `PUT /usuarios/{id}/rol`
**Acceso:** 🔒 Solo `ROL_ADMIN`

#### Envía
| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `id` | `number` | ✅ | ID del usuario. Va en la URL |
| `codRol` | `string` | ✅ | `ROL_ADMIN` \| `ROL_PROFESOR` \| `ROL_ALUMNO` |

#### Recibe — `datos: UsuarioResponse`
El usuario actualizado con el nuevo rol.

---

### `PUT /usuarios/{id}/estado`
**Acceso:** 🔒 Solo `ROL_ADMIN`

#### Envía
| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `id` | `number` | ✅ | ID del usuario. Va en la URL |
| `activo` | `boolean` | ✅ | `true` activa la cuenta \| `false` la suspende |

#### Recibe — `datos: UsuarioResponse`
El usuario actualizado con el nuevo estado.

---

### `POST /usuarios/carga-masiva`
**Acceso:** 🔒 Solo `ROL_ADMIN`

#### Envía — `multipart/form-data`
| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| `archivo` | `File` | ✅ | Archivo `.xlsx` o `.csv`. Nombre del campo: `archivo` |

Estructura requerida del archivo:

| Nombres | Apellidos | Correo |
|---|---|---|

#### Recibe — `datos: CargaMasivaResponse`
| Campo | Tipo | Descripción |
|---|---|---|
| `totalProcesados` | `number` | Total de filas leídas en el archivo |
| `exitosos` | `number` | Cantidad de alumnos registrados |
| `fallidos` | `number` | Cantidad de filas con error |
| `credenciales` | `CredencialAlumno[]` | Credenciales generadas por cada alumno registrado con éxito |
| `errores` | `ErrorFila[]` | Detalle de cada fila fallida |

**`CredencialAlumno`:**
| Campo | Tipo | Descripción |
|---|---|---|
| `correo` | `string` | Correo del alumno |
| `nombres` | `string` | Nombres del alumno |
| `apellidos` | `string` | Apellidos del alumno |
| `contrasenaTemporal` | `string` | Contraseña a comunicar al alumno. Cambio forzado en primer login |

**`ErrorFila`:**
| Campo | Tipo | Descripción |
|---|---|---|
| `fila` | `number` | Número de fila en el archivo |
| `correo` | `string` | Correo de la fila procesada |
| `motivo` | `string` | Razón del error |

> 💡 **Tip frontend:** ofrecer un botón "Descargar credenciales" tras la carga que genere un CSV con los datos para distribución masiva al alumnado.
> 
> ⚠️ **Importante:** las contraseñas viajan en texto plano en este response. Solo accesible al admin que ejecutó la carga. Asegurar HTTPS en producción.

## ❌ Errores globales

Todos los errores devuelven `exito: false`, `datos: null` y el `mensaje` descriptivo.

| Código HTTP | Cuándo ocurre |
|---|---|
| `400` | Campos vacíos, formato inválido o regla de negocio violada |
| `401` | Sin token, token expirado o credenciales incorrectas |
| `403` | Autenticado pero sin permisos para ese endpoint |
| `404` | Usuario no encontrado |
| `409` | Correo ya registrado |
| `500` | Error interno del servidor |

### Mensajes de error por endpoint

#### `POST /auth/login`
| Código | Mensaje |
|---|---|
| `400` | `"El correo es obligatorio."` |
| `400` | `"Debe ingresar un correo válido."` |
| `401` | `"Correo o contraseña incorrectos."` |
| `403` | `"Cuenta inactiva. Contacte al administrador."` |

#### `PUT /auth/cambiar-contrasena`
| Código | Mensaje |
|---|---|
| `400` | `"La contraseña debe tener mínimo 8 caracteres."` |
| `400` | `"La contraseña debe incluir mayúsculas, números y símbolos."` |
| `400` | `"Las contraseñas no coinciden."` |
| `400` | `"La contraseña no puede ser igual a la actual."` |
| `401` | `"La contraseña actual es incorrecta."` |

#### `POST /auth/registrar`
| Código | Mensaje |
|---|---|
| `400` | `"El rol 'ROL_X' no existe en el sistema."` |
| `403` | `"Acceso denegado. Requiere privilegios de administrador."` |
| `409` | `"El correo 'x@x.com' ya está asociado a otra cuenta activa."` |

#### `PUT /usuarios/{id}/rol`
| Código | Mensaje |
|---|---|
| `400` | `"Acción denegada. No puede revocar su rol de administrador porque el sistema quedaría sin gestión."` |
| `404` | `"Usuario con ID X no encontrado."` |

#### `POST /usuarios/carga-masiva`
| Código | Mensaje |
|---|---|
| `400` | `"Formato de archivo no soportado. Por favor, suba un archivo .csv o .xlsx"` |
| `400` | `"El archivo no tiene el formato correcto. Columnas requeridas: Nombres, Apellidos, Correo"` |