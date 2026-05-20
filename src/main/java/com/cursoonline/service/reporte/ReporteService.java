package com.cursoonline.service.reporte;

import com.cursoonline.dto.progreso.response.DetalleProgresoAlumnoResponse;
import com.cursoonline.dto.progreso.response.FilaTableroResponse;
import com.cursoonline.dto.progreso.response.TableroSeccionResponse;
import com.cursoonline.entity.auth.AudLogAcceso;
import com.cursoonline.entity.auth.SegUsuario;
import com.cursoonline.exception.reporte.ErrorGenerandoReporteException;
import com.cursoonline.exception.reporte.SinDatosParaReporteException;
import com.cursoonline.repository.auth.AudLogAccesoRepository;
import com.cursoonline.service.progreso.ProgresoService;
import com.lowagie.text.Document;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Font;
import com.lowagie.text.Chunk;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReporteService {

    private final ProgresoService            progresoService;
    private final AudLogAccesoRepository     accesoRepository;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // ── Grupal por sección ────────────────────────────────────────────────

    public byte[] grupalSeccionExcel(Integer idSeccion, SegUsuario usuario) {
        TableroSeccionResponse t = progresoService
                .obtenerTableroSeccion(idSeccion, usuario, Pageable.unpaged());
        List<FilaTableroResponse> alumnos = t.alumnos().getContent();
        if (alumnos.isEmpty()) throw new SinDatosParaReporteException();

        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = wb.createSheet("Tablero");
            CellStyle hs = headerStyle(wb);

            sheet.createRow(0).createCell(0).setCellValue("Reporte grupal — " + t.nombreSeccion());
            Row info = sheet.createRow(1);
            info.createCell(0).setCellValue("Curso: " + t.nombreCurso());
            info.createCell(2).setCellValue("Total lecciones obligatorias: " + t.totalLeccionesObligatorias());

            Row header = sheet.createRow(3);
            String[] cols = {"#", "Apellidos", "Nombres", "Completadas", "Total", "Avance %", "Última actividad"};
            for (int i = 0; i < cols.length; i++) {
                Cell c = header.createCell(i);
                c.setCellValue(cols[i]);
                c.setCellStyle(hs);
            }

            int row = 4;
            double suma = 0;
            for (int i = 0; i < alumnos.size(); i++) {
                FilaTableroResponse a = alumnos.get(i);
                Row r = sheet.createRow(row++);
                r.createCell(0).setCellValue(i + 1);
                r.createCell(1).setCellValue(a.apellidos());
                r.createCell(2).setCellValue(a.nombres());
                r.createCell(3).setCellValue(a.leccionesCompletadas());
                r.createCell(4).setCellValue(a.totalLeccionesObligatorias());
                r.createCell(5).setCellValue(a.porcentaje());
                r.createCell(6).setCellValue(a.ultimaActividad() != null ? a.ultimaActividad().format(FMT) : "—");
                suma += a.porcentaje();
            }

            Row prom = sheet.createRow(row + 1);
            prom.createCell(0).setCellValue("Rendimiento grupal promedio:");
            prom.createCell(5).setCellValue(Math.round(suma / alumnos.size() * 100.0) / 100.0);

            for (int i = 0; i < cols.length; i++) sheet.autoSizeColumn(i);

            wb.write(baos);
            return baos.toByteArray();

        } catch (IOException e) {
            throw new ErrorGenerandoReporteException(e);
        }
    }

    public byte[] grupalSeccionPdf(Integer idSeccion, SegUsuario usuario) {
        TableroSeccionResponse t = progresoService
                .obtenerTableroSeccion(idSeccion, usuario, Pageable.unpaged());
        List<FilaTableroResponse> alumnos = t.alumnos().getContent();
        if (alumnos.isEmpty()) throw new SinDatosParaReporteException();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document();
            PdfWriter.getInstance(doc, baos);
            doc.open();

            Font fT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font fN = FontFactory.getFont(FontFactory.HELVETICA, 11);
            Font fH = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);

            doc.add(new Paragraph("Reporte grupal — " + t.nombreSeccion(), fT));
            doc.add(new Paragraph("Curso: " + t.nombreCurso(), fN));
            doc.add(new Paragraph("Total lecciones obligatorias: " + t.totalLeccionesObligatorias(), fN));
            doc.add(new Paragraph("Fecha de emisión: " + LocalDateTime.now().format(FMT), fN));
            doc.add(Chunk.NEWLINE);

            PdfPTable tbl = new PdfPTable(new float[]{1f, 3f, 3f, 1.5f, 1.5f, 1.5f, 2.5f});
            tbl.setWidthPercentage(100);
            String[] cols = {"#", "Apellidos", "Nombres", "Comp.", "Total", "Avance %", "Última actividad"};
            for (String c : cols) {
                PdfPCell cell = new PdfPCell(new Phrase(c, fH));
                cell.setBackgroundColor(new Color(60, 80, 130));
                cell.setPadding(5);
                tbl.addCell(cell);
            }

            double suma = 0;
            for (int i = 0; i < alumnos.size(); i++) {
                FilaTableroResponse a = alumnos.get(i);
                tbl.addCell(String.valueOf(i + 1));
                tbl.addCell(a.apellidos());
                tbl.addCell(a.nombres());
                tbl.addCell(String.valueOf(a.leccionesCompletadas()));
                tbl.addCell(String.valueOf(a.totalLeccionesObligatorias()));
                tbl.addCell(String.format("%.2f%%", a.porcentaje()));
                tbl.addCell(a.ultimaActividad() != null ? a.ultimaActividad().format(FMT) : "—");
                suma += a.porcentaje();
            }
            doc.add(tbl);
            doc.add(Chunk.NEWLINE);
            doc.add(new Paragraph(String.format("Rendimiento grupal promedio: %.2f%%",
                    suma / alumnos.size()), fN));

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new ErrorGenerandoReporteException(e);
        }
    }

    // ── Individual por alumno/curso ──────────────────────────────────────

    public byte[] individualAlumnoExcel(Integer idAlumno, Integer idCurso, SegUsuario usuario) {
        DetalleProgresoAlumnoResponse d = progresoService
                .obtenerProgresoAlumno(idAlumno, idCurso, usuario);

        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = wb.createSheet("Progreso");
            CellStyle hs = headerStyle(wb);

            sheet.createRow(0).createCell(0).setCellValue("Reporte individual — "
                    + d.alumno().apellidos() + ", " + d.alumno().nombres());
            int r = 1;
            sheet.createRow(r++).createCell(0).setCellValue("Correo: " + d.alumno().correo());
            sheet.createRow(r++).createCell(0).setCellValue("Curso: " + d.nombreCurso());
            sheet.createRow(r++).createCell(0).setCellValue(String.format("Avance: %.2f%%", d.porcentajeAvance()));
            sheet.createRow(r++).createCell(0).setCellValue(
                    "Lecciones completadas: " + d.leccionesCompletadas() + " / " + d.totalLeccionesObligatorias());
            r++;

            for (var modulo : d.modulos()) {
                Row mh = sheet.createRow(r++);
                Cell mc = mh.createCell(0);
                mc.setCellValue("Módulo: " + modulo.nombre());
                mc.setCellStyle(hs);

                Row colsRow = sheet.createRow(r++);
                String[] cols = {"Lección", "Obligatoria", "Completada", "Fecha completado"};
                for (int i = 0; i < cols.length; i++) {
                    Cell c = colsRow.createCell(i);
                    c.setCellValue(cols[i]);
                    c.setCellStyle(hs);
                }

                for (var lec : modulo.lecciones()) {
                    Row lr = sheet.createRow(r++);
                    lr.createCell(0).setCellValue(lec.nombre());
                    lr.createCell(1).setCellValue(Boolean.TRUE.equals(lec.obligatoria()) ? "Sí" : "No");
                    lr.createCell(2).setCellValue(Boolean.TRUE.equals(lec.completada()) ? "Sí" : "No");
                    lr.createCell(3).setCellValue(lec.fecCompletado() != null ? lec.fecCompletado().format(FMT) : "—");
                }
                r++;
            }

            for (int i = 0; i < 4; i++) sheet.autoSizeColumn(i);

            wb.write(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new ErrorGenerandoReporteException(e);
        }
    }

    public byte[] individualAlumnoPdf(Integer idAlumno, Integer idCurso, SegUsuario usuario) {
        DetalleProgresoAlumnoResponse d = progresoService
                .obtenerProgresoAlumno(idAlumno, idCurso, usuario);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document();
            PdfWriter.getInstance(doc, baos);
            doc.open();

            Font fT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font fB = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font fN = FontFactory.getFont(FontFactory.HELVETICA, 11);
            Font fH = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);

            doc.add(new Paragraph("Reporte individual de progreso", fT));
            doc.add(Chunk.NEWLINE);
            doc.add(new Paragraph("Alumno: " + d.alumno().apellidos() + ", " + d.alumno().nombres(), fN));
            doc.add(new Paragraph("Correo: " + d.alumno().correo(), fN));
            doc.add(new Paragraph("Curso: " + d.nombreCurso(), fN));
            doc.add(new Paragraph(String.format("Avance: %.2f%% (%d/%d lecciones)",
                    d.porcentajeAvance(), d.leccionesCompletadas(), d.totalLeccionesObligatorias()), fN));
            doc.add(new Paragraph("Fecha de emisión: " + LocalDateTime.now().format(FMT), fN));
            doc.add(Chunk.NEWLINE);

            for (var modulo : d.modulos()) {
                doc.add(new Paragraph(modulo.nombre(), fB));

                PdfPTable tbl = new PdfPTable(new float[]{4f, 1.5f, 1.5f, 2.5f});
                tbl.setWidthPercentage(100);
                String[] cols = {"Lección", "Obligatoria", "Completada", "Fecha"};
                for (String c : cols) {
                    PdfPCell cell = new PdfPCell(new Phrase(c, fH));
                    cell.setBackgroundColor(new Color(60, 80, 130));
                    cell.setPadding(4);
                    tbl.addCell(cell);
                }
                for (var lec : modulo.lecciones()) {
                    tbl.addCell(lec.nombre());
                    tbl.addCell(Boolean.TRUE.equals(lec.obligatoria()) ? "Sí" : "No");
                    tbl.addCell(Boolean.TRUE.equals(lec.completada()) ? "Sí" : "No");
                    tbl.addCell(lec.fecCompletado() != null ? lec.fecCompletado().format(FMT) : "—");
                }
                doc.add(tbl);
                doc.add(Chunk.NEWLINE);
            }

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new ErrorGenerandoReporteException(e);
        }
    }

    // ── Historial de accesos ─────────────────────────────────────────────

    public byte[] historialAccesosExcel(LocalDateTime desde, LocalDateTime hasta) {
        if (desde == null && hasta == null) desde = LocalDateTime.now().minusDays(30);

        List<AudLogAcceso> accesos = accesoRepository.findRangoFechas(desde, hasta);
        if (accesos.isEmpty()) throw new SinDatosParaReporteException();

        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = wb.createSheet("Accesos");
            CellStyle hs = headerStyle(wb);

            sheet.createRow(0).createCell(0).setCellValue("Historial de accesos");
            Row rango = sheet.createRow(1);
            rango.createCell(0).setCellValue("Desde: " + (desde != null ? desde.format(FMT) : "—"));
            rango.createCell(2).setCellValue("Hasta: " + (hasta != null ? hasta.format(FMT) : LocalDateTime.now().format(FMT)));

            Row header = sheet.createRow(3);
            String[] cols = {"Fecha", "Correo", "Rol", "Exitoso", "IP", "Detalle"};
            for (int i = 0; i < cols.length; i++) {
                Cell c = header.createCell(i);
                c.setCellValue(cols[i]);
                c.setCellStyle(hs);
            }

            int row = 4;
            for (AudLogAcceso a : accesos) {
                Row r = sheet.createRow(row++);
                r.createCell(0).setCellValue(a.getFecIntento() != null ? a.getFecIntento().format(FMT) : "");
                r.createCell(1).setCellValue(a.getDesCorreo() != null ? a.getDesCorreo() : "");
                r.createCell(2).setCellValue(a.getCodRol() != null ? a.getCodRol() : "");
                r.createCell(3).setCellValue(Boolean.TRUE.equals(a.getEstExitoso()) ? "Sí" : "No");
                r.createCell(4).setCellValue(a.getDesIp() != null ? a.getDesIp() : "");
                r.createCell(5).setCellValue(a.getDesDetalle() != null ? a.getDesDetalle() : "");
            }

            for (int i = 0; i < cols.length; i++) sheet.autoSizeColumn(i);

            wb.write(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new ErrorGenerandoReporteException(e);
        }
    }

    // ── Helper ───────────────────────────────────────────────────────────

    private CellStyle headerStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
}