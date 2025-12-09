package com.zentry.sigea.module_certificaciones.presentation.models.requestDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO para solicitud de creación de certificado
 */
public class CrearCertificadoRequest {
    
    @NotNull(message = "El ID de inscripción es obligatorio")
    @Positive(message = "El ID de inscripción debe ser positivo")
    private String asistenciaId;
    
    private String observaciones;

    @NotBlank(message = "La URL del PDF no puede estar vacía")
    private String urlPdf;
    
    // Constructores
    public CrearCertificadoRequest() {}
    
    public CrearCertificadoRequest(String asistenciaId, String urlPdf) {
        this.asistenciaId = asistenciaId;
        this.urlPdf = urlPdf;   
    }
    
    // Getters y Setters
    public String getAsistenciaId() {
        return asistenciaId;
    }
    
    public void setAsistenciaId(String asistenciaId) {
        this.asistenciaId = asistenciaId;
    }
    
    public String getObservaciones() {
        return observaciones;
    }
    
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getUrlPdf() {
        return urlPdf;
    }

    public void setUrlPdf(String urlPdf) {
        this.urlPdf = urlPdf;
    }
}