package com.zentry.sigea.module_informe.presentation.api;

import com.zentry.sigea.module_informe.presentation.models.requestDTO.ActualizarInformeRequest;
import com.zentry.sigea.module_informe.presentation.models.requestDTO.CrearInformeRequest;
import com.zentry.sigea.module_informe.presentation.models.responseDTO.InformeResponse;
import com.zentry.sigea.module_informe.services.usecases.informe.CrearInformeUseCase;
import com.zentry.sigea.module_informe.services.usecases.informe.ListarInformesUseCase;
import com.zentry.sigea.module_informe.services.usecases.informe.ObtenerInformeUseCase;
import com.zentry.sigea.module_informe.services.usecases.informe.ActualizarInformeUseCase;
import com.zentry.sigea.module_informe.services.usecases.informe.EliminarInformeUseCase;
import com.zentry.sigea.module_informe.services.FileStorageService;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/v1/informes")
@CrossOrigin(origins = "*")
public class InformeController {

    private final CrearInformeUseCase crearInformeUseCase;
    private final ListarInformesUseCase listarInformesUseCase;
    private final ObtenerInformeUseCase obtenerInformeUseCase;
    private final ActualizarInformeUseCase actualizarInformeUseCase;
    private final EliminarInformeUseCase eliminarInformeUseCase;
    private final FileStorageService fileStorageService;
    private final HttpServletRequest request;

    public InformeController(
        CrearInformeUseCase crearInformeUseCase,
        ListarInformesUseCase listarInformesUseCase,
        ObtenerInformeUseCase obtenerInformeUseCase,
        ActualizarInformeUseCase actualizarInformeUseCase,
        EliminarInformeUseCase eliminarInformeUseCase,
        FileStorageService fileStorageService,
        HttpServletRequest request
    ) {
        this.crearInformeUseCase = crearInformeUseCase;
        this.listarInformesUseCase = listarInformesUseCase;
        this.obtenerInformeUseCase = obtenerInformeUseCase;
        this.actualizarInformeUseCase = actualizarInformeUseCase;
        this.eliminarInformeUseCase = eliminarInformeUseCase;
        this.fileStorageService = fileStorageService;
        this.request = request;
    }

    // CRUD de informes
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<InformeResponse> crearInforme(
        @ModelAttribute CrearInformeRequest request,
        @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        try {
            InformeResponse response = crearInformeUseCase.execute(request);

            if (files != null && !files.isEmpty()) {
                fileStorageService.guardarArchivos(response.getId(), files);
            }

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


    @GetMapping("/{informeId}/archivos/{filename:.+}")
    public ResponseEntity<Resource> descargarArchivo(
        @PathVariable String informeId,
        @PathVariable String filename
    ) throws Exception {
        Resource file = fileStorageService.cargarArchivoDeInforme(informeId, filename);
        String contentType = Files.probeContentType(file.getFile().toPath());
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .body(file);
    }
}