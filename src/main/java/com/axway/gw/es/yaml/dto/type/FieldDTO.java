package com.axway.gw.es.yaml.dto.type;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vordel.es.FieldType;

import java.util.Objects;

public class FieldDTO {

    @JsonIgnore
    private String name;
    private String type;
    private String defaultValue;
    private Object cardinality;
    @JsonProperty("isKey")
    private boolean keyField;

    public FieldDTO() {
        // for parsers
    }

    public FieldDTO(String name, FieldType fieldType) {
        this.name = name;
        String fieldTypeName = fieldType.getType();
        if (fieldType.isSoftRefType())
            fieldTypeName = fieldTypeName.replace("^", "@");  // Soft does not make sens in yaml es, so we convert in hard ref (@)
        this.type = fieldTypeName;
        this.cardinality = fieldType.getCardinality();
        this.defaultValue = fieldType.getDefault();
        if (FieldType.BOOLEAN.equals(fieldTypeName) && fieldType.getDefault() != null) {
			defaultValue = String.valueOf(FieldType.getBooleanValue(defaultValue));
        }
    }

    public String getName() {
        return name;
    }

    public FieldDTO setName(String name) {
        this.name = name;
        return this;
    }

    public String getType() {
        return type;
    }

    public FieldDTO setType(String type) {
        this.type = type;
        return this;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public FieldDTO setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public Object getCardinality() {
        return cardinality;
    }

    public FieldDTO setCardinality(Object cardinality) {
        this.cardinality = cardinality;
        return this;
    }

    public boolean isKeyField() {
        return keyField;
    }

    public FieldDTO setKeyField(boolean isKeyFields) {
        keyField = isKeyFields;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FieldDTO fieldDTO = (FieldDTO) o;
        return keyField == fieldDTO.keyField &&
                Objects.equals(name, fieldDTO.name) &&
                Objects.equals(type, fieldDTO.type) &&
                Objects.equals(defaultValue, fieldDTO.defaultValue) &&
                Objects.equals(cardinality, fieldDTO.cardinality);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, defaultValue, cardinality, keyField);
    }

    @Override
    public String toString() {
        return "FieldDTO{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", defaultValue='" + defaultValue + '\'' +
                ", cardinality='" + cardinality + '\'' +
                ", keyField=" + keyField +
                '}';
    }
}
