package com.axway.gw.es.yaml.model.entity;

import com.axway.gw.es.yaml.model.type.TypeDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class MetaDTO {

    private String type;

    @JsonIgnore
    private TypeDTO typeDTO;

    @JsonProperty("class")
    private String impl;

    @JsonProperty("_version")
    private String version;

    public String getType() {
        return type;
    }

    public MetaDTO setType(String type) {
        this.type = type;
        return this;
    }

    public TypeDTO getTypeDTO() {
        return typeDTO;
    }

    public MetaDTO setTypeDTO(TypeDTO typeDTO) {
        this.typeDTO = typeDTO;
        return this;
    }

    public String getImpl() {
        return impl;
    }

    public MetaDTO setImpl(String impl) {
        this.impl = impl;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public MetaDTO setVersion(String version) {
        this.version = version;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetaDTO metaDTO = (MetaDTO) o;
        return Objects.equals(type, metaDTO.type) &&
                Objects.equals(typeDTO, metaDTO.typeDTO) &&
                Objects.equals(impl, metaDTO.impl) &&
                Objects.equals(version, metaDTO.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, typeDTO, impl, version);
    }
}
