-- ============================================================
--  SCRIPT DE CREACIÓN DE BASE DE DATOS
--  Sistema de Cursos Online - Plataforma Educativa
--  Versión: 1.0  |  Fecha: 2026-04-12
-- ============================================================
--
--  NOMENCLATURA DE TABLAS:
--    cat_  → Catálogos / Tablas de referencia
--    seg_  → Seguridad y autenticación
--    tra_  → Transaccionales (registran eventos del negocio)
--    rel_  → Relacionales (tablas puente muchos-a-muchos)
--    aud_  → Auditoría y logs
--
--  NOMENCLATURA DE CAMPOS:
--    id_   → Identificador (PK / FK)
--    des_  → Texto descriptivo (VARCHAR / TEXT)
--    val_  → Valor numérico
--    est_  → Estado booleano
--    fec_  → Fecha / Timestamp
--    cod_  → Código corto único
--    num_  → Número contable (intentos, orden, etc.)
--    url_  → Ruta o enlace
--    pwd_  → Contraseña (siempre hasheada)
--    tok_  → Token de sesión
-- ============================================================

-- ------------------------------------------------------------
-- CREACIÓN DE LA BASE DE DATOS
-- ------------------------------------------------------------
DROP DATABASE IF EXISTS db_cursos_online;

CREATE DATABASE db_cursos_online
    WITH ENCODING    = 'UTF8'
         LC_COLLATE  = 'es_ES.UTF-8'
         LC_CTYPE    = 'es_ES.UTF-8'
         TEMPLATE    = template0;

\c db_cursos_online;

-- ------------------------------------------------------------
-- EXTENSIONES
-- ------------------------------------------------------------
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";


-- ============================================================
-- 1. CATÁLOGOS  (cat_)
-- ============================================================

-- Roles del sistema  [CUS-01, CUS-05]
-- ROL_ADMIN | ROL_PROFESOR | ROL_ALUMNO
CREATE TABLE cat_rol (
    id_rol          SERIAL          PRIMARY KEY,
    cod_rol         VARCHAR(20)     NOT NULL UNIQUE,
    des_nombre      VARCHAR(50)     NOT NULL,
    des_descripcion TEXT,
    est_activo      BOOLEAN         NOT NULL DEFAULT TRUE,
    fec_creacion    TIMESTAMP       NOT NULL DEFAULT NOW()
);

-- Tipos de pregunta en evaluaciones  [CUS-13]
CREATE TABLE cat_tipo_pregunta (
    id_tipo_pregunta  SERIAL        PRIMARY KEY,
    cod_tipo          VARCHAR(30)   NOT NULL UNIQUE,  -- OPCION_MULTIPLE | VERDADERO_FALSO | COMPLETAR_CODIGO
    des_nombre        VARCHAR(60)   NOT NULL,
    est_activo        BOOLEAN       NOT NULL DEFAULT TRUE
);

-- Tipos de recurso adjunto a lecciones  [CUS-12]
CREATE TABLE cat_tipo_recurso (
    id_tipo_recurso  SERIAL         PRIMARY KEY,
    cod_tipo         VARCHAR(20)    NOT NULL UNIQUE,  -- ARCHIVO | ENLACE | VIDEO
    des_nombre       VARCHAR(50)    NOT NULL,
    est_activo       BOOLEAN        NOT NULL DEFAULT TRUE
);

-- Años escolares  [CUS-08]
CREATE TABLE cat_anio_escolar (
    id_anio_escolar  SERIAL         PRIMARY KEY,
    val_anio         SMALLINT       NOT NULL UNIQUE,
    des_descripcion  VARCHAR(100),
    est_activo       BOOLEAN        NOT NULL DEFAULT TRUE,
    fec_inicio       DATE,
    fec_fin          DATE
);

-- Niveles académicos  [CUS-11]
CREATE TABLE cat_nivel (
    id_nivel     SERIAL             PRIMARY KEY,
    cod_nivel    VARCHAR(20)        NOT NULL UNIQUE,
    des_nombre   VARCHAR(80)        NOT NULL,
    val_orden    SMALLINT           NOT NULL DEFAULT 1,
    est_activo   BOOLEAN            NOT NULL DEFAULT TRUE
);


-- ============================================================
-- 2. SEGURIDAD  (seg_)
-- ============================================================

-- Usuarios del sistema  [CUS-01, CUS-03, CUS-04, CUS-05, CUS-06]
CREATE TABLE seg_usuario (
    id_usuario          SERIAL        PRIMARY KEY,
    id_rol              INT           NOT NULL REFERENCES cat_rol(id_rol),
    des_nombres         VARCHAR(80)   NOT NULL,
    des_apellidos       VARCHAR(80)   NOT NULL,
    des_correo          VARCHAR(120)  NOT NULL UNIQUE,
    pwd_contrasena      VARCHAR(255)  NOT NULL,        -- Hash BCrypt (Spring Security)
    est_activo          BOOLEAN       NOT NULL DEFAULT TRUE,
    est_pwd_temporal    BOOLEAN       NOT NULL DEFAULT FALSE,  -- Contraseña temporal generada
    fec_creacion        TIMESTAMP     NOT NULL DEFAULT NOW(),
    fec_actualizacion   TIMESTAMP,
    fec_ultimo_acceso   TIMESTAMP
);

-- Sesiones activas / tokens JWT  [CUS-01, CUS-02]
CREATE TABLE seg_sesion (
    id_sesion       SERIAL      PRIMARY KEY,
    id_usuario      INT         NOT NULL REFERENCES seg_usuario(id_usuario),
    tok_jwt         TEXT        NOT NULL,
    des_ip          VARCHAR(45),
    est_activa      BOOLEAN     NOT NULL DEFAULT TRUE,
    fec_inicio      TIMESTAMP   NOT NULL DEFAULT NOW(),
    fec_expiracion  TIMESTAMP   NOT NULL,
    fec_cierre      TIMESTAMP
);


-- ============================================================
-- 3. ESTRUCTURA ACADÉMICA  (cat_ / tra_)
-- ============================================================

-- Cursos de programación disponibles  [CUS-11]
CREATE TABLE cat_curso (
    id_curso          SERIAL        PRIMARY KEY,
    id_nivel          INT           REFERENCES cat_nivel(id_nivel),
    des_nombre        VARCHAR(120)  NOT NULL,
    des_descripcion   TEXT,
    est_publicado     BOOLEAN       NOT NULL DEFAULT FALSE,
    est_activo        BOOLEAN       NOT NULL DEFAULT TRUE,
    fec_creacion      TIMESTAMP     NOT NULL DEFAULT NOW(),
    fec_publicacion   TIMESTAMP
);

-- Secciones / Grupos  [CUS-08]
-- Ej: "Taller de Programación - A | 2025"
CREATE TABLE tra_seccion (
    id_seccion        SERIAL        PRIMARY KEY,
    id_curso          INT           NOT NULL REFERENCES cat_curso(id_curso),
    id_anio_escolar   INT           NOT NULL REFERENCES cat_anio_escolar(id_anio_escolar),
    des_nombre        VARCHAR(100)  NOT NULL,
    est_activa        BOOLEAN       NOT NULL DEFAULT TRUE,
    fec_creacion      TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- Módulos de un curso  [CUS-12, CUS-13]
CREATE TABLE tra_modulo (
    id_modulo         SERIAL        PRIMARY KEY,
    id_curso          INT           NOT NULL REFERENCES cat_curso(id_curso),
    des_nombre        VARCHAR(120)  NOT NULL,
    des_descripcion   TEXT,
    val_orden         SMALLINT      NOT NULL DEFAULT 1,
    est_activo        BOOLEAN       NOT NULL DEFAULT TRUE,
    fec_creacion      TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- Lecciones dentro de un módulo  [CUS-12]
CREATE TABLE tra_leccion (
    id_leccion        SERIAL        PRIMARY KEY,
    id_modulo         INT           NOT NULL REFERENCES tra_modulo(id_modulo),
    des_nombre        VARCHAR(150)  NOT NULL,
    des_contenido     TEXT,
    val_orden         SMALLINT      NOT NULL DEFAULT 1,
    est_obligatoria   BOOLEAN       NOT NULL DEFAULT TRUE,
    est_publicada     BOOLEAN       NOT NULL DEFAULT FALSE,
    est_activa        BOOLEAN       NOT NULL DEFAULT TRUE,
    fec_creacion      TIMESTAMP     NOT NULL DEFAULT NOW(),
    fec_publicacion   TIMESTAMP
);

-- Recursos adjuntos a lecciones (archivos, videos, enlaces)  [CUS-12]
CREATE TABLE tra_recurso (
    id_recurso        SERIAL        PRIMARY KEY,
    id_leccion        INT           NOT NULL REFERENCES tra_leccion(id_leccion),
    id_tipo_recurso   INT           NOT NULL REFERENCES cat_tipo_recurso(id_tipo_recurso),
    des_nombre        VARCHAR(150)  NOT NULL,
    url_ruta          TEXT          NOT NULL,
    est_activo        BOOLEAN       NOT NULL DEFAULT TRUE,
    fec_creacion      TIMESTAMP     NOT NULL DEFAULT NOW()
);


-- ============================================================
-- 4. TABLAS RELACIONALES  (rel_)
-- ============================================================

-- Alumno inscrito en una sección  [CUS-08]
-- Restricción: un alumno solo puede estar en UNA sección por año escolar
CREATE TABLE rel_alumno_seccion (
    id_alumno_seccion  SERIAL      PRIMARY KEY,
    id_usuario         INT         NOT NULL REFERENCES seg_usuario(id_usuario),
    id_seccion         INT         NOT NULL REFERENCES tra_seccion(id_seccion),
    est_activo         BOOLEAN     NOT NULL DEFAULT TRUE,
    fec_inscripcion    TIMESTAMP   NOT NULL DEFAULT NOW(),
    UNIQUE (id_usuario, id_seccion)
);

-- Profesor asignado a una sección  [CUS-09]
CREATE TABLE rel_profesor_seccion (
    id_profesor_seccion  SERIAL    PRIMARY KEY,
    id_usuario           INT       NOT NULL REFERENCES seg_usuario(id_usuario),
    id_seccion           INT       NOT NULL REFERENCES tra_seccion(id_seccion),
    est_activo           BOOLEAN   NOT NULL DEFAULT TRUE,
    fec_asignacion       TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (id_usuario, id_seccion)
);


-- ============================================================
-- 5. EVALUACIONES  (tra_)
-- ============================================================

-- Evaluación por módulo  [CUS-13]
CREATE TABLE tra_evaluacion (
    id_evaluacion        SERIAL          PRIMARY KEY,
    id_modulo            INT             NOT NULL REFERENCES tra_modulo(id_modulo),
    des_titulo           VARCHAR(150)    NOT NULL,
    des_instrucciones    TEXT,
    val_puntaje_minimo   NUMERIC(5,2)    NOT NULL DEFAULT 60.00,
    val_tiempo_limite    SMALLINT,                              -- minutos; NULL = sin límite
    val_max_intentos     SMALLINT        NOT NULL DEFAULT 3,
    est_activa           BOOLEAN         NOT NULL DEFAULT FALSE,
    fec_creacion         TIMESTAMP       NOT NULL DEFAULT NOW()
);

-- Preguntas de una evaluación  [CUS-13]
CREATE TABLE tra_pregunta (
    id_pregunta       SERIAL            PRIMARY KEY,
    id_evaluacion     INT               NOT NULL REFERENCES tra_evaluacion(id_evaluacion),
    id_tipo_pregunta  INT               NOT NULL REFERENCES cat_tipo_pregunta(id_tipo_pregunta),
    des_enunciado     TEXT              NOT NULL,
    val_orden         SMALLINT          NOT NULL DEFAULT 1,
    val_puntaje       NUMERIC(5,2)      NOT NULL DEFAULT 1.00,
    est_activa        BOOLEAN           NOT NULL DEFAULT TRUE
);

-- Opciones de respuesta por pregunta  [CUS-13]
CREATE TABLE tra_opcion_respuesta (
    id_opcion         SERIAL            PRIMARY KEY,
    id_pregunta       INT               NOT NULL REFERENCES tra_pregunta(id_pregunta),
    des_opcion        TEXT              NOT NULL,
    est_correcta      BOOLEAN           NOT NULL DEFAULT FALSE,
    val_orden         SMALLINT          NOT NULL DEFAULT 1
);

-- Intento de evaluación realizado por un alumno  [CUS-13]
CREATE TABLE tra_intento_evaluacion (
    id_intento        SERIAL            PRIMARY KEY,
    id_evaluacion     INT               NOT NULL REFERENCES tra_evaluacion(id_evaluacion),
    id_usuario        INT               NOT NULL REFERENCES seg_usuario(id_usuario),
    val_calificacion  NUMERIC(5,2),
    num_intento       SMALLINT          NOT NULL DEFAULT 1,
    est_aprobado      BOOLEAN,
    est_completado    BOOLEAN           NOT NULL DEFAULT FALSE,
    fec_inicio        TIMESTAMP         NOT NULL DEFAULT NOW(),
    fec_fin           TIMESTAMP
);

-- Respuestas marcadas por el alumno en cada intento  [CUS-13]
CREATE TABLE tra_respuesta_alumno (
    id_respuesta_alumno  SERIAL         PRIMARY KEY,
    id_intento           INT            NOT NULL REFERENCES tra_intento_evaluacion(id_intento),
    id_pregunta          INT            NOT NULL REFERENCES tra_pregunta(id_pregunta),
    id_opcion_elegida    INT            REFERENCES tra_opcion_respuesta(id_opcion),
    des_respuesta_texto  TEXT,                                  -- para preguntas de código
    est_correcta         BOOLEAN,
    fec_respuesta        TIMESTAMP      NOT NULL DEFAULT NOW()
);


-- ============================================================
-- 6. PROGRESO DE ALUMNOS  (tra_)
-- ============================================================

-- Progreso por lección  [CUS-12, CUS-14, CUS-15, CUS-16]
CREATE TABLE tra_progreso_leccion (
    id_progreso       SERIAL            PRIMARY KEY,
    id_usuario        INT               NOT NULL REFERENCES seg_usuario(id_usuario),
    id_leccion        INT               NOT NULL REFERENCES tra_leccion(id_leccion),
    est_completada    BOOLEAN           NOT NULL DEFAULT FALSE,
    fec_inicio        TIMESTAMP         NOT NULL DEFAULT NOW(),
    fec_completado    TIMESTAMP,
    UNIQUE (id_usuario, id_leccion)
);


-- ============================================================
-- 7. AUDITORÍA  (aud_)
-- ============================================================

-- Log de acceso al sistema (exitoso y fallido)  [CUS-01, CUS-02]
CREATE TABLE aud_log_acceso (
    id_log_acceso    SERIAL             PRIMARY KEY,
    id_usuario       INT                REFERENCES seg_usuario(id_usuario),  -- NULL si falla
    des_correo       VARCHAR(120),                                            -- captura intento fallido
    des_ip           VARCHAR(45),
    cod_rol          VARCHAR(20),
    est_exitoso      BOOLEAN            NOT NULL DEFAULT FALSE,
    des_detalle      VARCHAR(255),                                            -- mensaje de error si aplica
    fec_intento      TIMESTAMP          NOT NULL DEFAULT NOW()
);

-- Log de cambios de contraseña  [CUS-03, CUS-04]
CREATE TABLE aud_log_contrasena (
    id_log_contrasena  SERIAL           PRIMARY KEY,
    id_usuario         INT              NOT NULL REFERENCES seg_usuario(id_usuario),
    des_ip             VARCHAR(45),
    est_exitoso        BOOLEAN          NOT NULL DEFAULT TRUE,
    des_detalle        VARCHAR(255),
    fec_cambio         TIMESTAMP        NOT NULL DEFAULT NOW()
);

-- Log de acciones administrativas  [CUS-04, CUS-05, CUS-06, CUS-07, CUS-08, CUS-09]
CREATE TABLE aud_log_admin (
    id_log_admin          SERIAL        PRIMARY KEY,
    id_administrador      INT           NOT NULL REFERENCES seg_usuario(id_usuario),
    id_usuario_afectado   INT           REFERENCES seg_usuario(id_usuario),
    des_accion            VARCHAR(100)  NOT NULL,   -- CREAR_USUARIO | CAMBIO_ROL | RECUPERAR_CREDENCIALES | CARGA_MASIVA | etc.
    des_detalle           TEXT,
    fec_accion            TIMESTAMP     NOT NULL DEFAULT NOW()
);


-- ============================================================
-- 8. ÍNDICES
-- ============================================================

-- Seguridad
CREATE INDEX idx_seg_usuario_correo       ON seg_usuario(des_correo);
CREATE INDEX idx_seg_usuario_rol          ON seg_usuario(id_rol);
CREATE INDEX idx_seg_sesion_usuario       ON seg_sesion(id_usuario);
CREATE INDEX idx_seg_sesion_activa        ON seg_sesion(est_activa, fec_expiracion);

-- Estructura académica
CREATE INDEX idx_cat_curso_nivel          ON cat_curso(id_nivel);
CREATE INDEX idx_tra_seccion_curso        ON tra_seccion(id_curso);
CREATE INDEX idx_tra_seccion_anio         ON tra_seccion(id_anio_escolar);
CREATE INDEX idx_tra_modulo_curso         ON tra_modulo(id_curso, val_orden);
CREATE INDEX idx_tra_leccion_modulo       ON tra_leccion(id_modulo, val_orden);

-- Relacionales
CREATE INDEX idx_rel_alumno_seccion       ON rel_alumno_seccion(id_usuario, id_seccion);
CREATE INDEX idx_rel_profesor_seccion     ON rel_profesor_seccion(id_usuario, id_seccion);

-- Evaluaciones
CREATE INDEX idx_tra_evaluacion_modulo    ON tra_evaluacion(id_modulo);
CREATE INDEX idx_tra_pregunta_eval        ON tra_pregunta(id_evaluacion, val_orden);
CREATE INDEX idx_tra_opcion_pregunta      ON tra_opcion_respuesta(id_pregunta);
CREATE INDEX idx_tra_intento_usuario      ON tra_intento_evaluacion(id_usuario);
CREATE INDEX idx_tra_intento_evaluacion   ON tra_intento_evaluacion(id_evaluacion);

-- Progreso
CREATE INDEX idx_tra_progreso_usuario     ON tra_progreso_leccion(id_usuario);
CREATE INDEX idx_tra_progreso_leccion     ON tra_progreso_leccion(id_leccion);

-- Auditoría
CREATE INDEX idx_aud_acceso_usuario       ON aud_log_acceso(id_usuario, fec_intento);
CREATE INDEX idx_aud_admin_actor          ON aud_log_admin(id_administrador, fec_accion);


-- ============================================================
-- 9. DATOS SEMILLA  (SEED)
-- ============================================================

-- Roles del sistema
INSERT INTO cat_rol (cod_rol, des_nombre, des_descripcion) VALUES
    ('ROL_ADMIN',    'Administrador', 'Control total del sistema, gestión de usuarios y configuración'),
    ('ROL_PROFESOR', 'Profesor',      'Gestión de secciones asignadas, contenido y calificaciones'),
    ('ROL_ALUMNO',   'Alumno',        'Acceso a cursos, lecciones y evaluaciones propias');

-- Tipos de pregunta
INSERT INTO cat_tipo_pregunta (cod_tipo, des_nombre) VALUES
    ('OPCION_MULTIPLE',  'Opción Múltiple'),
    ('VERDADERO_FALSO',  'Verdadero / Falso'),
    ('COMPLETAR_CODIGO', 'Completar Código');

-- Tipos de recurso
INSERT INTO cat_tipo_recurso (cod_tipo, des_nombre) VALUES
    ('ARCHIVO', 'Archivo adjunto'),
    ('ENLACE',  'Enlace externo'),
    ('VIDEO',   'Video');

-- Años escolares
INSERT INTO cat_anio_escolar (val_anio, des_descripcion, fec_inicio, fec_fin) VALUES
    (2025, 'Año escolar 2025', '2025-01-15', '2025-12-15'),
    (2026, 'Año escolar 2026', '2026-01-15', '2026-12-15');

-- Niveles académicos
INSERT INTO cat_nivel (cod_nivel, des_nombre, val_orden) VALUES
    ('BASICO',       'Básico',        1),
    ('INTERMEDIO',   'Intermedio',    2),
    ('AVANZADO',     'Avanzado',      3);

-- Usuario Administrador por defecto
-- IMPORTANTE: Reemplazar pwd_contrasena con un hash BCrypt real antes de producción
INSERT INTO seg_usuario (id_rol, des_nombres, des_apellidos, des_correo, pwd_contrasena, est_activo, est_pwd_temporal)
VALUES (
    (SELECT id_rol FROM cat_rol WHERE cod_rol = 'ROL_ADMIN'),
    'Super',
    'Administrador',
    'admin@colegio.edu',
    '$2a$12$REEMPLAZAR_CON_HASH_BCRYPT_REAL',
    TRUE,
    TRUE   -- Forzar cambio de contraseña en primer acceso
);


-- ============================================================
-- FIN DEL SCRIPT
-- ============================================================
