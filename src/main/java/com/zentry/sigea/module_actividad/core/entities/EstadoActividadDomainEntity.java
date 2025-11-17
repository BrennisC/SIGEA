package com.zentry.sigea.module_actividad.core.entities;

public class EstadoActividadDomainEntity {
    private String codigo;
    private String etiqueta;

    public static EstadoActividadDomainEntity create(String codigo, String etiqueta) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código no puede ser nulo o vacío");
        }
        if (etiqueta == null || etiqueta.isBlank()) {
            throw new IllegalArgumentException("La etiqueta no puede ser nula o vacía");
        }
        EstadoActividadDomainEntity estadoActividadDomainEntity = new EstadoActividadDomainEntity();
        estadoActividadDomainEntity.setCodigo(codigo);
        estadoActividadDomainEntity.setEtiqueta(etiqueta);
        return estadoActividadDomainEntity;
    }

    public String getCodigo() {
        return codigo;
    }
    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
    public void setEtiqueta(String etiqueta) {
        this.etiqueta = etiqueta;
    }

}
