package com.cursoonline.service.academico;

import com.cursoonline.exception.academico.ArchivoInvalidoException;
import com.cursoonline.exception.academico.ArchivoNoEncontradoException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${app.upload.max-size-mb}")
    private long maxSizeMb;

    private static final Set<String> EXTENSIONES_PERMITIDAS = Set.of(
            "pdf", "docx", "doc", "txt",
            "pptx", "ppt",
            "xlsx", "xls", "csv",
            "png", "jpg", "jpeg",
            "zip",
            "java", "py", "js", "sql", "html", "css"
    );

    private Path raizUploads;

    @PostConstruct
    public void init() {
        try {
            this.raizUploads = Paths.get(uploadDir, "recursos").toAbsolutePath().normalize();
            Files.createDirectories(this.raizUploads);
            log.info("Directorio de uploads inicializado en: {}", this.raizUploads);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo inicializar el directorio de uploads", e);
        }
    }

    /**
     * Guarda el archivo en disco bajo recursos/yyyy/MM/<uuid>_<nombreSaneado>.<ext>
     * y devuelve la ruta RELATIVA (la que se almacena en url_ruta).
     */
    public String guardar(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new ArchivoInvalidoException("El archivo está vacío.");
        }

        // Validar tamaño
        long maxBytes = maxSizeMb * 1024L * 1024L;
        if (archivo.getSize() > maxBytes) {
            throw new ArchivoInvalidoException(
                "El archivo supera el tamaño máximo permitido de " + maxSizeMb + " MB.");
        }

        // Validar nombre y extensión
        String nombreOriginal = archivo.getOriginalFilename();
        if (nombreOriginal == null || nombreOriginal.isBlank()) {
            throw new ArchivoInvalidoException("El archivo no tiene nombre.");
        }

        String extension = obtenerExtension(nombreOriginal);
        if (!EXTENSIONES_PERMITIDAS.contains(extension)) {
            throw new ArchivoInvalidoException(
                "Tipo de archivo no permitido: ." + extension +
                ". Permitidos: " + EXTENSIONES_PERMITIDAS);
        }

        // Construir nombre y ruta destino
        String nombreSaneado  = sanearNombre(nombreOriginal);
        String nombreFinal    = UUID.randomUUID() + "_" + nombreSaneado;

        LocalDate hoy = LocalDate.now();
        Path carpetaMes = raizUploads
                .resolve(String.valueOf(hoy.getYear()))
                .resolve(String.format("%02d", hoy.getMonthValue()));

        try {
            Files.createDirectories(carpetaMes);
            Path destino = carpetaMes.resolve(nombreFinal).normalize();

            // Defensa contra path traversal
            if (!destino.startsWith(raizUploads)) {
                throw new ArchivoInvalidoException("Ruta de destino inválida.");
            }

            archivo.transferTo(destino);

            // Devolver ruta relativa: recursos/2026/04/<uuid>_<nombre>.<ext>
            String rutaRelativa = raizUploads.getParent().relativize(destino)
                    .toString().replace("\\", "/");
            log.info("Archivo guardado → {}", rutaRelativa);
            return rutaRelativa;

        } catch (IOException e) {
            throw new ArchivoInvalidoException("Error al guardar el archivo: " + e.getMessage());
        }
    }

    /**
     * Resuelve la ruta absoluta de un archivo a partir de su ruta relativa
     * almacenada en BD. Lanza ArchivoNoEncontradoException si no existe.
     */
    public Path resolverRuta(String rutaRelativa) {
        Path destino = Paths.get(uploadDir).toAbsolutePath()
                .resolve(rutaRelativa).normalize();

        // Defensa contra path traversal en lectura
        Path baseAbsoluta = Paths.get(uploadDir).toAbsolutePath().normalize();
        if (!destino.startsWith(baseAbsoluta)) {
            throw new ArchivoNoEncontradoException(rutaRelativa);
        }

        if (!Files.exists(destino) || !Files.isRegularFile(destino)) {
            throw new ArchivoNoEncontradoException(rutaRelativa);
        }
        return destino;
    }

    /**
     * Elimina físicamente un archivo. Si no existe, no falla (idempotente).
     */
    public void eliminar(String rutaRelativa) {
        try {
            Path destino = Paths.get(uploadDir).toAbsolutePath()
                    .resolve(rutaRelativa).normalize();

            Path baseAbsoluta = Paths.get(uploadDir).toAbsolutePath().normalize();
            if (!destino.startsWith(baseAbsoluta)) return;

            Files.deleteIfExists(destino);
            log.info("Archivo eliminado del disco → {}", rutaRelativa);
        } catch (IOException e) {
            log.warn("No se pudo eliminar el archivo {}: {}", rutaRelativa, e.getMessage());
        }
    }

    // ── Helpers privados ──────────────────────────────────────────────────────

    private String obtenerExtension(String nombre) {
        int idx = nombre.lastIndexOf('.');
        if (idx < 0 || idx == nombre.length() - 1) return "";
        return nombre.substring(idx + 1).toLowerCase();
    }

    /**
     * Reemplaza espacios y caracteres no seguros por guiones.
     * Mantiene letras, números, puntos, guiones y guiones bajos.
     */
    private String sanearNombre(String nombre) {
        String soloNombre = Paths.get(nombre).getFileName().toString();
        return soloNombre.replaceAll("[^a-zA-Z0-9._-]", "-");
    }
}