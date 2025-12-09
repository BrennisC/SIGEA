package com.zentry.sigea.module_certificaciones.services.usecases.certificado.validators;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.zentry.sigea.module_inscripciones.core.repositories.IInscripcionRepository;

/**
 * Validador de restricciones para creación de certificados
 * 
 * Implementa la lógica de negocio:
 * - Solo se puede crear un certificado por una actividad registrada
 * - No se pueden emitir certificados si hay más de 2 actividades registradas
 */
@Component
public class ValidadorActividadesCertificado {

    private static final Logger logger = LoggerFactory.getLogger(ValidadorActividadesCertificado.class);

    private final IInscripcionRepository inscripcionRepository;

    /**
     * Límite máximo de actividades permitidas para generar certificados
     */
    private static final int LIMITE_ACTIVIDADES_PERMITIDAS = 2;

    public ValidadorActividadesCertificado(IInscripcionRepository inscripcionRepository) {
        this.inscripcionRepository = inscripcionRepository;
    }

    /**
     * Valida que se pueda crear un certificado para una actividad
     * 
     * Reglas:
     * 1. Un usuario solo puede tener un certificado por actividad
     * 2. Solo se emiten certificados si el usuario está registrado en máximo 2 actividades
     * 
     * @param usuarioId ID del usuario
     * @param actividadId ID de la actividad
     * @return true si se puede crear el certificado, false en caso contrario
     */
    public boolean puedeCrearCertificado(String usuarioId, String actividadId) {
        logger.info("Validando si se puede crear certificado para usuario: {} en actividad: {}", usuarioId, actividadId);

        // Validar entrada
        if (usuarioId == null || usuarioId.trim().isEmpty()) {
            throw new IllegalArgumentException("El usuario ID es obligatorio");
        }
        if (actividadId == null || actividadId.trim().isEmpty()) {
            throw new IllegalArgumentException("El actividad ID es obligatorio");
        }

        // Obtener todas las actividades donde el usuario está inscrito
        List<String> inscripcionesPorUsuario = inscripcionRepository.findByUsuarioId(usuarioId)
            .stream()
            .map(inscripcion -> inscripcion.getActividadId())
            .toList();

        int cantidadActividades = inscripcionesPorUsuario.size();

        logger.info(
            "Usuario {} está inscrito en {} actividades",
            usuarioId,
            cantidadActividades
        );

        // Validar restricción: No más de 2 actividades
        if (cantidadActividades > LIMITE_ACTIVIDADES_PERMITIDAS) {
            logger.warn(
                "Usuario {} excede límite de actividades permitidas ({} > {})",
                usuarioId,
                cantidadActividades,
                LIMITE_ACTIVIDADES_PERMITIDAS
            );
            return false;
        }

        logger.info("✅ Validación exitosa: Certificado puede ser creado para usuario: {}", usuarioId);
        return true;
    }

    /**
     * Obtiene el número de actividades donde un usuario está registrado
     * 
     * @param usuarioId ID del usuario
     * @return Cantidad de actividades registradas
     */
    public int obtenerCantidadActividades(String usuarioId) {
        if (usuarioId == null || usuarioId.trim().isEmpty()) {
            throw new IllegalArgumentException("El usuario ID es obligatorio");
        }

        return (int) inscripcionRepository.findByUsuarioId(usuarioId).size();
    }

    /**
     * Verifica si un usuario ha alcanzado el límite de actividades
     * 
     * @param usuarioId ID del usuario
     * @return true si ha alcanzado el límite, false en caso contrario
     */
    public boolean haAlcanzadoLimite(String usuarioId) {
        int cantidad = obtenerCantidadActividades(usuarioId);
        boolean alLimite = cantidad >= LIMITE_ACTIVIDADES_PERMITIDAS;

        if (alLimite) {
            logger.warn(
                "Usuario {} ha alcanzado el límite de actividades permitidas ({})",
                usuarioId,
                LIMITE_ACTIVIDADES_PERMITIDAS
            );
        }

        return alLimite;
    }

    /**
     * Valida que el usuario esté inscrito en la actividad específica
     * 
     * @param usuarioId ID del usuario
     * @param actividadId ID de la actividad
     * @return true si está inscrito, false en caso contrario
     */
    public boolean estaInscritoEnActividad(String usuarioId, String actividadId) {
        if (usuarioId == null || usuarioId.trim().isEmpty()) {
            throw new IllegalArgumentException("El usuario ID es obligatorio");
        }
        if (actividadId == null || actividadId.trim().isEmpty()) {
            throw new IllegalArgumentException("El actividad ID es obligatorio");
        }

        boolean existe = inscripcionRepository.existsByUsuarioIdAndActividadId(usuarioId, actividadId);

        logger.debug(
            "Verificación de inscripción - Usuario: {}, Actividad: {}, Inscrito: {}",
            usuarioId,
            actividadId,
            existe
        );

        return existe;
    }

    /**
     * Obtiene el límite máximo de actividades permitidas
     */
    public int obtenerLimiteActividades() {
        return LIMITE_ACTIVIDADES_PERMITIDAS;
    }
}
