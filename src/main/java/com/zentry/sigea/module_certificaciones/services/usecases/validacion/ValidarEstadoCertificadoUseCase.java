package com.zentry.sigea.module_certificaciones.services.usecases.validacion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.zentry.sigea.module_certificaciones.core.entities.CertificadoDomainEntity;
import com.zentry.sigea.module_certificaciones.core.repositories.ICertificadoRepository;

/**
 * Caso de uso para validar el estado de un certificado
 * 
 * Este caso de uso SOLO observa el estado del certificado.
 * No realiza validación de asistencias ni otros estados.
 * Solo verifica si el certificado está en estado EMITIDO.
 * 
 * Si el estado NO es EMITIDO, retorna false indicando que no es válido.
 */
@Component
public class ValidarEstadoCertificadoUseCase {

    private static final Logger logger = LoggerFactory.getLogger(ValidarEstadoCertificadoUseCase.class);

    private final ICertificadoRepository certificadoRepository;

    public ValidarEstadoCertificadoUseCase(ICertificadoRepository certificadoRepository) {
        this.certificadoRepository = certificadoRepository;
    }

    /**
     * Valida solo el estado del certificado
     * 
     * @param codigoValidacion Código del certificado a validar
     * @return true si el certificado está en estado EMITIDO, false en caso contrario
     * @throws IllegalArgumentException si el certificado no existe
     */
    public boolean execute(String codigoValidacion) {
        logger.info("Validando estado del certificado con código: {}", codigoValidacion);

        // Validar entrada
        if (codigoValidacion == null || codigoValidacion.trim().isEmpty()) {
            throw new IllegalArgumentException("El código de validación es obligatorio");
        }

        // Buscar el certificado
        CertificadoDomainEntity certificado = certificadoRepository
            .findByCodigoValidacion(codigoValidacion)
            .orElseThrow(() -> {
                logger.warn("Certificado no encontrado con código: {}", codigoValidacion);
                return new IllegalArgumentException(
                    "No se encontró un certificado con código: " + codigoValidacion
                );
            });

        // Solo observar el estado del certificado
        boolean esValido = certificado.estaEmitido();

        logger.info(
            "Estado del certificado {}: {}. Es válido: {}",
            certificado.getIdCertificado(),
            certificado.getEstado() != null ? certificado.getEstado().getCodigo() : "SIN_ESTADO",
            esValido
        );

        return esValido;
    }

    /**
     * Obtiene el estado del certificado sin validar
     * Solo observa el estado actual
     * 
     * @param codigoValidacion Código del certificado
     * @return El código del estado actual del certificado
     * @throws IllegalArgumentException si el certificado no existe
     */
    public String obtenerEstadoCertificado(String codigoValidacion) {
        logger.debug("Obteniendo estado del certificado: {}", codigoValidacion);

        if (codigoValidacion == null || codigoValidacion.trim().isEmpty()) {
            throw new IllegalArgumentException("El código de validación es obligatorio");
        }

        CertificadoDomainEntity certificado = certificadoRepository
            .findByCodigoValidacion(codigoValidacion)
            .orElseThrow(() -> new IllegalArgumentException(
                "No se encontró un certificado con código: " + codigoValidacion
            ));

        String estado = certificado.getEstado() != null 
            ? certificado.getEstado().getCodigo() 
            : "SIN_ESTADO";

        logger.debug("Certificado {}: Estado = {}", certificado.getIdCertificado(), estado);

        return estado;
    }
}
