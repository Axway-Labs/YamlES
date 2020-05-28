package com.axway.gw.es.yaml.dto.type;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vordel.es.FieldType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class FieldDTO {

    @JsonIgnore
    private String name;
    private String type;
    private List<ValueDTO> defaultValues;
    private Object cardinality;

    public FieldDTO() {
        // for parsers
        defaultValues = new ArrayList<>();
    }

    public FieldDTO(String name, FieldType fieldType) {
        this.name = name;
        this.type = fieldType.getType();
        this.cardinality = fieldType.getCardinality();
        this.defaultValues = fieldType.getDefaultValues()
                .stream()
                .map(ValueDTO::new)
                .collect(Collectors.toList());
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

    public List<ValueDTO> getDefaultValues() {
        return defaultValues;
    }

    public FieldDTO setDefaultValue(List<ValueDTO> defaultValues) {
        this.defaultValues = defaultValues;
        return this;
    }

    public Object getCardinality() {
        return cardinality;
    }

    public FieldDTO setCardinality(Object cardinality) {
        this.cardinality = cardinality;
        return this;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FieldDTO fieldDTO = (FieldDTO) o;
        return  Objects.equals(name, fieldDTO.name) &&
                Objects.equals(type, fieldDTO.type) &&
                Objects.equals(defaultValues, fieldDTO.defaultValues) &&
                Objects.equals(cardinality, fieldDTO.cardinality);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, defaultValues, cardinality);
    }

    @Override
    public String toString() {
        return "FieldDTO{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", defaultValues='" + defaultValues + '\'' +
                ", cardinality='" + cardinality +
                '}';
    }
}
