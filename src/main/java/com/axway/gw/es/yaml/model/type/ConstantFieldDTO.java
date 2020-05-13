package com.axway.gw.es.yaml.model.type;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vordel.es.ConstantFieldType;

public class ConstantFieldDTO {
    @JsonIgnore
    private String name;
    private String type;
    private String value;

    public ConstantFieldDTO() {
        // for parsers
    }

    public ConstantFieldDTO(String name, ConstantFieldType fieldType) {
        this.name = name;
        this.value = fieldType.getDefaultValues().get(0).getData();
        this.type = fieldType.getType();
    }

    public ConstantFieldDTO setName(String name) {
        this.name = name;
        return this;
    }

    public ConstantFieldDTO setType(String type) {
        this.type = type;
        return this;
    }

    public ConstantFieldDTO setValue(String value) {
        this.value = value;
        return this;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }
}
