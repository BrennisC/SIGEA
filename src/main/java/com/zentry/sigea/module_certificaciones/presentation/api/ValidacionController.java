package com.zentry.sigea.module_certificaciones.presentation.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zentry.sigea.module_certificaciones.presentation.models.requestDTO.ValidarCertificadoRequest;
import com.zentry.sigea.module_certificaciones.presentation.models.responseDTO.ValidacionResponse;
import com.zentry.sigea.module_certificaciones.services.ValidacionService;


@RestController
@RequestMapping("/api/validaciones")
@CrossOrigin(origins = "*")
public class ValidacionController {

    private final ValidacionService validacionService;

    public ValidacionController(ValidacionService validacionService) {
        this.validacionService = validacionService;
    }


    @PostMapping("/validar")
    public ResponseEntity<ValidacionResponse> validarCertificado(
            @RequestBody ValidarCertificadoRequest request) {
        ValidacionResponse response = validacionService.validarCertificado(request);
        return ResponseEntity.ok(response);
    }    


    @GetMapping("/obtener-certificado")
    public List<ResponseEntity<ValidacionResponse>> obtenerCertificado(@RequestParam String param) {
        List<ValidacionResponse> responses = validacionService.obtenerValidacionesCertificado(param);
        return responses.stream()
                .map(ResponseEntity::ok)
                .toList();
                
    }
    
    /**
     * Endpoint para validar SOLO el estado del certificado
     * No realiza validación de asistencias ni otros procesos
     * 
     * @param codigoValidacion Código del certificado
     * @return true si el certificado está en estado EMITIDO, false en caso contrario
     */
    @GetMapping("/validar-estado")
    public ResponseEntity<Boolean> validarEstadoCertificado(@RequestParam String codigoValidacion) {
        boolean esValido = validacionService.validarEstadoCertificado(codigoValidacion);
        return ResponseEntity.ok(esValido);
    }
    
    /**
     * Endpoint para obtener el estado actual del certificado
     * 
     * @param codigoValidacion Código del certificado
     * @return El código del estado actual (ej: "EMITIDO", "REVOCADO", "SUSPENDIDO")
     */
    @GetMapping("/obtener-estado")
    public ResponseEntity<String> obtenerEstadoCertificado(@RequestParam String codigoValidacion) {
        String estado = validacionService.obtenerEstadoCertificado(codigoValidacion);
        return ResponseEntity.ok(estado);
    }

}