package com.axway.gw.es.model.entity;

import com.axway.gw.es.model.type.Type;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Meta {

    public String type;

    @JsonIgnore
    public Type yType;

    @JsonProperty("class")
    public String _class;

    public String _version;

}
