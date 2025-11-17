package com.zentry.sigea.module_informe.presentation.api;

import com.zentry.sigea.module_informe.presentation.models.requestDTO.ActualizarInformeRequest;
import com.zentry.sigea.module_informe.presentation.models.requestDTO.CrearInformeRequest;
import com.zentry.sigea.module_informe.presentation.models.responseDTO.InformeResponse;
import com.zentry.sigea.module_informe.services.usecases.informe.CrearInformeUseCase;
import com.zentry.sigea.module_informe.services.usecases.informe.ListarInformesUseCase;
import com.zentry.sigea.module_informe.services.usecases.informe.ObtenerInformeUseCase;
import com.zentry.sigea.module_informe.services.usecases.informe.ActualizarInformeUseCase;
import com.zentry.sigea.module_informe.services.usecases.informe.EliminarInformeUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/informes")
@CrossOrigin(origins = "*")
public class InformeController {

    private final CrearInformeUseCase crearInformeUseCase;
    private final ListarInformesUseCase listarInformesUseCase;
    private final ObtenerInformeUseCase obtenerInformeUseCase;
    private final ActualizarInformeUseCase actualizarInformeUseCase;
    private final EliminarInformeUseCase eliminarInformeUseCase;

    public InformeController(
        CrearInformeUseCase crearInformeUseCase,
        ListarInformesUseCase listarInformesUseCase,
        ObtenerInformeUseCase obtenerInformeUseCase,
        ActualizarInformeUseCase actualizarInformeUseCase,
        EliminarInformeUseCase eliminarInformeUseCase
    ) {
        this.crearInformeUseCase = crearInformeUseCase;
        this.listarInformesUseCase = listarInformesUseCase;
        this.obtenerInformeUseCase = obtenerInformeUseCase;
        this.actualizarInformeUseCase = actualizarInformeUseCase;
        this.eliminarInformeUseCase = eliminarInformeUseCase;
    }

    @PostMapping
    public ResponseEntity<InformeResponse> crearInforme(@RequestBody CrearInformeRequest request) {
        try {
            InformeResponse response = crearInformeUseCase.execute(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace(); 
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<InformeResponse>> listarInformes() {
        List<InformeResponse> informes = listarInformesUseCase.execute();
        return ResponseEntity.ok(informes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InformeResponse> obtenerInforme(@PathVariable String id) {
        try {
            InformeResponse response = obtenerInformeUseCase.execute(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<InformeResponse> actualizarInforme(@PathVariable String id, @RequestBody ActualizarInformeRequest request) {
        try {
            InformeResponse response = actualizarInformeUseCase.execute(id, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarInforme(@PathVariable String id) {
        try {
            eliminarInformeUseCase.execute(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Informes API is running");
    }
}