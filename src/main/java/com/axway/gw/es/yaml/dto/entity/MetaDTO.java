package com.axway.gw.es.yaml.dto.entity;

import com.axway.gw.es.yaml.dto.type.TypeDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

import static com.axway.gw.es.yaml.YamlConstantFieldsNames.CLASS_FIELD_NAME;
import static com.axway.gw.es.yaml.YamlConstantFieldsNames.VERSION_FIELD_NAME;

public class MetaDTO {

    private String type;

    @JsonIgnore
    private TypeDTO typeDTO;

    @JsonProperty(CLASS_FIELD_NAME)
    private String clazz;

    @JsonProperty(VERSION_FIELD_NAME)
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

    public String getClazz() {
        return clazz;
    }

    public MetaDTO setClazz(String clazz) {
        this.clazz = clazz;
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
                Objects.equals(clazz, metaDTO.clazz) &&
                Objects.equals(version, metaDTO.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, typeDTO, clazz, version);
    }
}
