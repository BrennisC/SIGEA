package com.zentry.sigea.module_certificaciones.services;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zentry.sigea.module_certificaciones.core.entities.ValidacionDomainEntity;
import com.zentry.sigea.module_certificaciones.presentation.models.requestDTO.ValidarCertificadoRequest;
import com.zentry.sigea.module_certificaciones.presentation.models.responseDTO.ValidacionResponse;
import com.zentry.sigea.module_certificaciones.services.interfaces.IValidacionService;
import com.zentry.sigea.module_certificaciones.services.usecases.validacion.ObtenerValidacionesCertificadoUseCase;
import com.zentry.sigea.module_certificaciones.services.usecases.validacion.ValidarCertificadoUseCase;
import com.zentry.sigea.module_certificaciones.services.usecases.validacion.ValidarEstadoCertificadoUseCase;

@Service
@Transactional
public class ValidacionService implements IValidacionService {
    
    private static final Logger log = LoggerFactory.getLogger(ValidacionService.class);
    
    // Use Cases
    private final ValidarCertificadoUseCase validarCertificadoUseCase;
    private final ObtenerValidacionesCertificadoUseCase obtenerValidacionesCertificadoUseCase;
    private final ValidarEstadoCertificadoUseCase validarEstadoCertificadoUseCase;
    
    public ValidacionService(
        ValidarCertificadoUseCase validarCertificadoUseCase,
        ObtenerValidacionesCertificadoUseCase obtenerValidacionesCertificadoUseCase,
        ValidarEstadoCertificadoUseCase validarEstadoCertificadoUseCase
    ) {
        this.validarCertificadoUseCase = validarCertificadoUseCase;
        this.obtenerValidacionesCertificadoUseCase = obtenerValidacionesCertificadoUseCase;
        this.validarEstadoCertificadoUseCase = validarEstadoCertificadoUseCase;
    }
    
    /**
     * Implementación de métodos de IValidacionService
     * @param request Datos para validar el certificado
     * @return Resultado de la validación
     * 
     * IMPORTANTE: Solo valida si el certificado está en estado EMITIDO
     */
    @Override
    public ValidacionResponse validarCertificado(ValidarCertificadoRequest request) {
        log.info("Validando certificado: {} con tipo: {}", 
                request.getCodigoValidacion(), request.getTipoValidador());
        
        try {
            ValidacionDomainEntity validacionCreada = validarCertificadoUseCase.execute(request);
            
            log.info("Validación creada exitosamente con ID: {}", validacionCreada.getTipoValidador());
            
            return convertirAValidacionResponse(validacionCreada);
            
        } catch (Exception e) {
            log.error("Error al validar certificado {}: {}", 
                     request.getCodigoValidacion(), e.getMessage());
            throw e;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ValidacionResponse> obtenerValidacionesCertificado(String codigoValidacion) {
        log.debug("Obteniendo validaciones para certificado: {}", codigoValidacion);
        
        List<ValidacionDomainEntity> validaciones = obtenerValidacionesCertificadoUseCase.execute(codigoValidacion);
        
        return validaciones.stream()
            .map(this::convertirAValidacionResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ValidacionResponse> obtenerValidacionesPorTipo(String tipoValidador) {
        log.debug("Obteniendo validaciones por tipo: {}", tipoValidador);
        
        // TODO: Crear caso de uso específico para buscar por tipo de validador
        // Por ahora implementamos lógica directa
        
        return List.of(); // Placeholder
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ValidacionResponse> obtenerValidacionesPorResultado(String resultado) {
        log.debug("Obteniendo validaciones por resultado: {}", resultado);
        
        // TODO: Crear caso de uso específico para buscar por resultado
        // Por ahora implementamos lógica directa
        
        return List.of(); // Placeholder
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean validarEstadoCertificado(String codigoValidacion) {
        log.info("Validando ESTADO del certificado: {}", codigoValidacion);
        
        try {
            boolean esValido = validarEstadoCertificadoUseCase.execute(codigoValidacion);
            
            log.info(
                "Validación de estado completada. Certificado {}: {}",
                codigoValidacion,
                esValido ? "VÁLIDO (EMITIDO)" : "NO VÁLIDO (NO EMITIDO)"
            );
            
            return esValido;
            
        } catch (Exception e) {
            log.error("Error al validar estado del certificado {}: {}", 
                     codigoValidacion, e.getMessage());
            throw e;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public String obtenerEstadoCertificado(String codigoValidacion) {
        log.debug("Obteniendo estado del certificado: {}", codigoValidacion);
        
        try {
            String estado = validarEstadoCertificadoUseCase.obtenerEstadoCertificado(codigoValidacion);
            
            log.debug("Estado del certificado {}: {}", codigoValidacion, estado);
            
            return estado;
            
        } catch (Exception e) {
            log.error("Error al obtener estado del certificado {}: {}", 
                     codigoValidacion, e.getMessage());
            throw e;
        }
    }
    
    // Métodos auxiliares de conversión
    
    /**
     * Convierte una entidad de dominio de validación a DTO de respuesta
     */
    private ValidacionResponse convertirAValidacionResponse(ValidacionDomainEntity validacion) {
        ValidacionResponse response = new ValidacionResponse();
        // Generar un ID temporal basado en certificado y tipo de validador
        // En una implementación real, esto vendría de la base de datos
        if (validacion.getCertificado() != null && validacion.getTipoValidador() != null) {
            response.setIdValidacion(validacion.getCertificado() + "_" + validacion.getTipoValidador());
        }
        
        response.setCodigoValidacion(validacion.getCertificado());
        response.setTipoValidador(validacion.getTipoValidador());
        response.setFechaValidacion(validacion.getFechaValidacion());
        response.setResultado(validacion.getResultado());
        response.setDetalle(validacion.getDetalle());

        
        return response;
    }
}
