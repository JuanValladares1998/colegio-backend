package com.cursoonline.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import com.cursoonline.dto.common.ApiResponse;
import com.cursoonline.exception.academico.*;
import com.cursoonline.exception.auth.*;
import com.cursoonline.exception.usuario.CorreoDuplicadoException;
import com.cursoonline.exception.usuario.RolNoEncontradoException;
import com.cursoonline.exception.usuario.UsuarioNoEncontradoException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidacion(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(" | "));
        return ResponseEntity.badRequest().body(ApiResponse.error(mensaje));
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ApiResponse<Void>> handleCredenciales(CredencialesInvalidasException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(UsuarioInactivoException.class)
    public ResponseEntity<ApiResponse<Void>> handleInactivo(UsuarioInactivoException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(CorreoDuplicadoException.class)
    public ResponseEntity<ApiResponse<Void>> handleCorreoDuplicado(CorreoDuplicadoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler({
        ContrasenaActualIncorrectaException.class,
        ContrasenaIgualException.class,
        ContrasenaNoCoincideException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleContrasena(RuntimeException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(RolNoEncontradoException.class)
    public ResponseEntity<ApiResponse<Void>> handleRol(RolNoEncontradoException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(UsuarioNoEncontradoException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(UsuarioNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAcceso(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Acceso denegado. Requiere privilegios de administrador."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenerico(Exception ex) {
        log.error("Error inesperado: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error temporal del sistema. Intente más tarde."));
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler({
    NivelNoEncontradoException.class,
    CursoNoEncontradoException.class,
    SeccionNoEncontradaException.class,
    AnioEscolarNoEncontradoException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleAcademicoNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(CursoYaExisteException.class)
    public ResponseEntity<ApiResponse<Void>> handleCursoExiste(CursoYaExisteException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(SeccionYaExisteException.class)
    public ResponseEntity<ApiResponse<Void>> handleSeccionExiste(SeccionYaExisteException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(ProfesorYaAsignadoException.class)
    public ResponseEntity<ApiResponse<Void>> handleProfesorAsignado(ProfesorYaAsignadoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(UsuarioNoEsProfesorException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoEsProfesor(UsuarioNoEsProfesorException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }
    @ExceptionHandler(ModuloNoEncontradoException.class)
    public ResponseEntity<ApiResponse<Void>> handleModuloNotFound(ModuloNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(ModuloYaExisteException.class)
    public ResponseEntity<ApiResponse<Void>> handleModuloExiste(ModuloYaExisteException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(AccesoCursoDenegadoException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccesoCursoDenegado(AccesoCursoDenegadoException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(ex.getMessage()));
    }
    @ExceptionHandler(LeccionNoEncontradaException.class)
    public ResponseEntity<ApiResponse<Void>> handleLeccionNotFound(LeccionNoEncontradaException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(LeccionYaExisteException.class)
    public ResponseEntity<ApiResponse<Void>> handleLeccionExiste(LeccionYaExisteException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(ex.getMessage()));
    }
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ApiResponse<Void>> handleRecursoNotFound(RecursoNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(TipoRecursoNoEncontradoException.class)
    public ResponseEntity<ApiResponse<Void>> handleTipoRecursoNotFound(TipoRecursoNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(RecursoInvalidoException.class)
    public ResponseEntity<ApiResponse<Void>> handleRecursoInvalido(RecursoInvalidoException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(ArchivoInvalidoException.class)
    public ResponseEntity<ApiResponse<Void>> handleArchivoInvalido(ArchivoInvalidoException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(ArchivoNoEncontradoException.class)
    public ResponseEntity<ApiResponse<Void>> handleArchivoNoEncontrado(ArchivoNoEncontradoException ex) {
        log.warn("Archivo físico no encontrado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(ApiResponse.error("El archivo supera el tamaño máximo permitido."));
    }

    @ExceptionHandler(AlumnoYaInscritoException.class)
    public ResponseEntity<ApiResponse<Void>> handleAlumnoYaInscrito(AlumnoYaInscritoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(InscripcionNoEncontradaException.class)
    public ResponseEntity<ApiResponse<Void>> handleInscripcionNotFound(InscripcionNoEncontradaException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(UsuarioNoEsAlumnoException.class)
    public ResponseEntity<ApiResponse<Void>> handleUsuarioNoEsAlumno(UsuarioNoEsAlumnoException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(ex.getMessage()));
    }

    
}