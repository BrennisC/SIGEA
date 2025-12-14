package com.zentry.sigea.module_sesiones.services.usecases.sesion;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.annotation.ResponseStatusExceptionResolver;
import org.springframework.http.HttpStatus;

import com.zentry.sigea.module_sesiones.core.entities.SesionDomainEntity;
import com.zentry.sigea.module_sesiones.core.repositories.ISesionRepository;
import com.zentry.sigea.module_sesiones.presentacion.models.CrearSesionRequest;
import com.zentry.sigea.module_actividad.core.entities.ActividadDomainEntity;
import com.zentry.sigea.module_actividad.core.repositories.IActividadRepository;
import com.zentry.sigea.module_actividad.services.interfaces.IActividad;

import java.time.LocalDateTime;


@Component
public class CrearSesionUseCase {

    private final ISesionRepository sesionRepository;
    private final IActividadRepository actividadRepository;
    public CrearSesionUseCase(ISesionRepository sesionRepository, IActividadRepository actividadRepository) {
        this.sesionRepository = sesionRepository;
        this.actividadRepository= actividadRepository;
    }

    /**
     * Ejecuta la creación de una sesión
     */
    public SesionDomainEntity execute(CrearSesionRequest request) {
        validateRequest(request);
        validateBusinessRules(request);

        ActividadDomainEntity actividad = actividadRepository.findById(request.getActividadId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró la actividad con ID: " + request.getActividadId()));

        if (request.getFechaSesion().toLocalDate().isBefore(actividad.getFechaInicio())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La sesión no puede ser antes de la fecha de inicio de la actividad");
        }

        SesionDomainEntity nuevaSesion = SesionDomainEntity.create(
            request.getActividadId(),
            request.getTitulo(), 
            request.getDescripcion(),
            request.getFechaSesion(),
            request.getHoraInicio(),
            request.getHoraFin(),
            request.getPonente(),
            request.getModalidad(),
            request.getLugarSesion(),
            request.getLinkVirtual(),
            request.getOrden()
        );
        return sesionRepository.save(nuevaSesion);
    }

    private void validateRequest(CrearSesionRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El request no puede ser nulo");
        }
        if (request.getActividadId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El UUID de la actividad es obligatorio");
        }
        if (request.getFechaSesion() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha de sesión es obligatoria");
        }
        if (request.getTitulo() == null || request.getTitulo().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El título es obligatorio");
        }
    }

    private void validateBusinessRules(CrearSesionRequest request) {
        if (request.getFechaSesion().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pueden crear sesiones en fechas pasadas");
        }

        LocalDateTime unAnioDelante = LocalDateTime.now().plusYears(1);
        if (request.getFechaSesion().isAfter(unAnioDelante)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pueden crear sesiones con más de 1 año de anticipación");
        }
    }
}
