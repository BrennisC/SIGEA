package com.zentry.sigea.module_certificaciones.services.interfaces;

import java.util.List;

import com.zentry.sigea.module_certificaciones.presentation.models.requestDTO.ValidarCertificadoRequest;
import com.zentry.sigea.module_certificaciones.presentation.models.responseDTO.ValidacionResponse;

/**
 * Interfaz del servicio de validaciones
 * Define los contratos para la gestión de validaciones de certificados
 */
public interface IValidacionService {
    
    /**
     * Valida un certificado con un tipo de validador específico
     * IMPORTANTE: Solo valida si el certificado está en estado EMITIDO
     */
    ValidacionResponse validarCertificado(ValidarCertificadoRequest request);
    
    /**
     * Obtiene todas las validaciones de un certificado
     */
    List<ValidacionResponse> obtenerValidacionesCertificado(String codigoValidacion);
    
    /**
     * Obtiene validaciones por tipo de validador
     */
    List<ValidacionResponse> obtenerValidacionesPorTipo(String tipoValidador);
    
    /**
     * Obtiene validaciones por resultado
     */
    List<ValidacionResponse> obtenerValidacionesPorResultado(String resultado);
    
    /**
     * Valida SOLO el estado del certificado sin procesar otras validaciones
     * 
     * Este método observa únicamente el estado actual del certificado.
     * - Si el estado es EMITIDO: retorna true (válido)
     * - Si el estado NO es EMITIDO: retorna false (no válido)
     * 
     * No realiza validaciones de asistencias ni otros procesos.
     * 
     * @param codigoValidacion Código del certificado
     * @return true si el certificado está en estado EMITIDO, false en caso contrario
     */
    boolean validarEstadoCertificado(String codigoValidacion);
    
    /**
     * Obtiene el estado actual del certificado
     * 
     * @param codigoValidacion Código del certificado
     * @return El código del estado actual (ej: "EMITIDO", "REVOCADO", "SUSPENDIDO")
     */
    String obtenerEstadoCertificado(String codigoValidacion);
}
