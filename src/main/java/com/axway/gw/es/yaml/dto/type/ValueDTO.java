package com.axway.gw.es.yaml.dto.type;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vordel.es.Value;

public class ValueDTO {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String data;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String ref;

    public ValueDTO() {
        // for parsers
    }

    ValueDTO(Value value) {
        this.data = value.getData();
        this.ref = value.getRef() != null ? value.getRef().toString() : null;
    }

    public String getData() {
        return data;
    }

    public ValueDTO setData(String data) {
        this.data = data;
        return this;
    }

    public String getRef() {
        return ref;
    }

    public ValueDTO setRef(String ref) {
        this.ref = ref;
        return this;
    }
}
