package com.axway.gw.es.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Meta {

    public String type;

    @JsonProperty("class")
    public String _class;

    public String _version;

}
