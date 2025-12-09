package com.zentry.sigea.module_certificaciones.services.usecases.certificado;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.zentry.sigea.module_asistencias.core.entities.AsistenciaDomainEntity;
import com.zentry.sigea.module_asistencias.core.repositories.IAsistenciaRepository;
import com.zentry.sigea.module_certificaciones.core.entities.CertificadoDomainEntity;
import com.zentry.sigea.module_certificaciones.core.entities.EstadoCertificadoDomainEntity;
import com.zentry.sigea.module_certificaciones.core.repositories.ICertificadoRepository;
import com.zentry.sigea.module_certificaciones.core.repositories.IEstadoCertificadoRepository;
import com.zentry.sigea.module_certificaciones.presentation.models.requestDTO.CrearCertificadoRequest;
import com.zentry.sigea.module_certificaciones.services.usecases.certificado.validators.ValidadorActividadesCertificado;
import com.zentry.sigea.module_inscripciones.core.entities.InscripcionDomainEntity;
import com.zentry.sigea.module_inscripciones.core.repositories.IInscripcionRepository;
import com.zentry.sigea.module_notificaciones.events.domain.CertificadoGeneradoEvent;
import com.zentry.sigea.module_notificaciones.events.domain.CertificadoGeneradoEvent.EstadoCertificado;

/**
 * Caso de uso para crear un nuevo certificado
 * 
 * RESTRICCIONES DE NEGOCIO:
 * 1. Solo se puede crear un certificado por una actividad registrada
 * 2. No se pueden emitir certificados si hay más de 2 actividades registradas
 * 3. Solo se emiten en estado EMITIDO
 */
@Component
public class CrearCertificadoUseCase {

    private static final Logger logger = LoggerFactory.getLogger(CrearCertificadoUseCase.class);

    private final ICertificadoRepository certificadoRepository;
    private final IEstadoCertificadoRepository estadoCertificadoRepository;
    private final IAsistenciaRepository asistenciaRepository;
    private final IInscripcionRepository inscripcionRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ValidadorActividadesCertificado validadorActividades;

    public CrearCertificadoUseCase(
        ICertificadoRepository certificadoRepository,
        IEstadoCertificadoRepository estadoCertificadoRepository,
        IAsistenciaRepository asistenciaRepository,
        IInscripcionRepository inscripcionRepository,
        ApplicationEventPublisher eventPublisher,
        ValidadorActividadesCertificado validadorActividades
    ) {
        this.certificadoRepository = certificadoRepository;
        this.estadoCertificadoRepository = estadoCertificadoRepository;
        this.asistenciaRepository = asistenciaRepository;
        this.inscripcionRepository = inscripcionRepository;
        this.eventPublisher = eventPublisher;
        this.validadorActividades = validadorActividades;
    }

    /**
     * Ejecuta la creación de certificado
     * 
     * Flujo:
     * 1. Validar que no exista ya un certificado para esta asistencia
     * 2. Obtener la asistencia y su inscripción asociada
     * 3. Validar que el usuario cumpla con los requisitos de actividades (máx 2)
     * 4. Crear el certificado en estado EMITIDO
     * 5. Publicar evento de notificación
     */
    public CertificadoDomainEntity execute(CrearCertificadoRequest request, MultipartFile archivoPdf) {
        logger.info("Iniciando creación de certificado para asistencia: {}", request.getAsistenciaId());

        if (!archivoPdf.getContentType().equals("application/pdf")) {
            throw new IllegalArgumentException("El archivo debe ser un PDF");
        }


        if (archivoPdf != null && !archivoPdf.isEmpty()) {
            logger.info("archi recibido: {}", archivoPdf.getOriginalFilename());

            // Ejemplo: guardar archivo PDF en sistema de archivos local
            Path path = Paths.get("uploads/" + archivoPdf.getOriginalFilename());
            try {
                Files.copy(archivoPdf.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException("Error al guardar archi");
            }
        }
        
        // Validar que no exista ya un certificado para esta inscripción
        String asistenciaId = request.getAsistenciaId();
        if (certificadoRepository.existsByAsistenciaId(asistenciaId)) {
            logger.warn("Ya existe un certificado para la asistencia: {}", asistenciaId);
            throw new IllegalArgumentException(
                "Ya existe un certificado para esta asistencia"
            );
        }
        
        // Obtener la asistencia
        AsistenciaDomainEntity asistencia = asistenciaRepository.findById(asistenciaId)

            .orElseThrow(() -> {
                logger.error("Asistencia no encontrada: {}", asistenciaId);
                return new IllegalArgumentException("Asistencia no encontrada: " + asistenciaId);
            });
        
        // Obtener la inscripción asociada
        InscripcionDomainEntity inscripcion = inscripcionRepository.findById(asistencia.getInscripcionId())
            .orElseThrow(() -> {
                logger.error("Inscripción no encontrada: {}", asistencia.getInscripcionId());
                return new IllegalArgumentException("Inscripción no encontrada: " + asistencia.getInscripcionId());
            });
        
        String usuarioId = inscripcion.getUsuarioId();
        String actividadId = inscripcion.getActividadId();
        
        logger.info("Datos extraídos - Usuario: {}, Actividad: {}", usuarioId, actividadId);
        
        // VALIDACIÓN CRÍTICA: Verificar restricción de máximo 2 actividades
        logger.info("🔍 Validando restricción de actividades para usuario: {}", usuarioId);
        
        if (!validadorActividades.puedeCrearCertificado(usuarioId, actividadId)) {
            int cantidadActividades = validadorActividades.obtenerCantidadActividades(usuarioId);
            int limite = validadorActividades.obtenerLimiteActividades();
            
            logger.error(
                "❌ Certificado NO puede ser emitido. Usuario: {} tiene {} actividades registradas. " +
                "Límite permitido: {} actividades",
                usuarioId,
                cantidadActividades,
                limite
            );
            
            throw new IllegalStateException(
                String.format(
                    "No se puede emitir certificado. El usuario tiene %d actividades registradas " +
                    "y solo se permiten %d. Límite excedido.",
                    cantidadActividades,
                    limite
                )
            );
        }
        
        logger.info("✅ Validación exitosa: Usuario cumple con los requisitos de actividades");
        
        // Obtener estado EMITIDO por defecto
        EstadoCertificadoDomainEntity estadoEmitido = estadoCertificadoRepository
            .findByCodigo("EMITIDO")
            .orElseThrow(() -> {
                logger.error("Estado EMITIDO no encontrado en el sistema");
                return new IllegalStateException("Estado EMITIDO no encontrado en el sistema");
            });
        
        // Generar código de validación único
        String codigoValidacion = generarCodigoValidacion();
        
        // Asegurar que el código sea único
        while (certificadoRepository.existsByCodigoValidacion(codigoValidacion)) {
            codigoValidacion = generarCodigoValidacion();
        }
        
        logger.debug("Código de validación generado: {}", codigoValidacion);
        
        // Crear la entidad usando el factory method del dominio
        CertificadoDomainEntity nuevoCertificado = CertificadoDomainEntity.create(
            asistenciaId,
            codigoValidacion,
            estadoEmitido
        );
        
        // Guardar usando el repositorio
        CertificadoDomainEntity certificadoGuardado = certificadoRepository.save(nuevoCertificado);
        logger.info("✅ Certificado guardado exitosamente. ID: {}", certificadoGuardado.getIdCertificado());
        
        // Publicar evento de notificación
        try {
            logger.info("📧 Publicando evento CertificadoGeneradoEvent para certificado: {}", certificadoGuardado.getIdCertificado());
            
            eventPublisher.publishEvent(new CertificadoGeneradoEvent(
                usuarioId,                                      // ID del usuario desde inscripción
                certificadoGuardado.getIdCertificado(),        // ID del certificado
                actividadId,                                    // ID de la actividad desde inscripción
                "Actividad Completada",                        // Título
                certificadoGuardado.getCodigoValidacion(),     // Código de validación
                certificadoGuardado.getFechaEmision(),         // Fecha de emisión
                EstadoCertificado.EMITIDO,                     // Estado del certificado
                certificadoGuardado.getUrlPdf(),               // URL del PDF (puede ser null)
                asistenciaId,                                  // ID de asistencia
                LocalDateTime.now()                             // Fecha de generación
            ));
            
            logger.info("✅ Evento CertificadoGeneradoEvent publicado exitosamente");
            
        } catch (Exception e) {
            logger.error("❌ Error publicando evento de notificación para certificado {}: {}", 
                certificadoGuardado.getIdCertificado(), e.getMessage(), e);
            // No lanzamos la excepción para que el certificado se cree aunque falle la notificación
        }
        
        return certificadoGuardado;
    }

    /**
     * Genera un código de validación único
     */
    private String generarCodigoValidacion() {
        return "CERT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
