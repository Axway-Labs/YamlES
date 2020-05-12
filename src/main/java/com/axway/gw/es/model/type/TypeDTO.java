package com.axway.gw.es.model.type;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vordel.es.ConstantFieldType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TypeDTO {

    private static final String apiVersion = "http://www.vordel.com/2005/06/24/entityStore";

    private String name;

    private Integer version;

    @JsonProperty("class")
    private String clazz;

    @JsonProperty("loadorder")
    private Integer loadOrder;

    @JsonIgnore
    private TypeDTO parent;

    @JsonIgnore
    private boolean isAbstract;

    private Map<String, ConstantFieldDTO> constants = new LinkedHashMap<>();
    private Map<String, FieldDTO> fields = new LinkedHashMap<>();
    private Map<String, String> components = new LinkedHashMap<>();
    private List<TypeDTO> children = new ArrayList<>();

    public TypeDTO() {
    }

    public TypeDTO(String name) {
        this.name = name;
    }

    public void addChild(TypeDTO t) {
        children.add(t);
        t.parent = this;
    }

    @JsonIgnore
    public boolean hasChild() {
        return children.size() > 0;
    }

    public List<TypeDTO> getChildren() {
        return children;
    }

    public void addConstant(String name, ConstantFieldType ft) {
        switch (name) {
            case "_version":
                version = Integer.parseInt(ft.getDefaultValues().get(0).getData());
                return;
            case "class":
                clazz = ft.getDefaultValues().get(0).getData();
                return;
            case "loadorder":
                loadOrder = Integer.parseInt(ft.getDefaultValues().get(0).getData());
                return;
            default:
                ConstantFieldDTO f = new ConstantFieldDTO(name, ft);
                constants.put(name, f);
        }
    }

    public boolean isDefaultValue(String fieldName, String fval) {
        FieldDTO field = fields.get(fieldName);
        if (field == null && parent != null) {
            return parent.isDefaultValue(fieldName, fval);
        } else if (field != null) {
            if ("boolean".equals(field.getType())) {
                switch (fval) {
                    case "0" : fval = "false"; break;
                    case "1" : fval = "true"; break;
                }
            }
            String defaultValue = field.getDefaultValue();
            return defaultValue != null && defaultValue.equals(fval);
        } else {
            return false;
        }
    }

    public String getDefaultValue(String fieldName) {
        FieldDTO field = fields.get(fieldName);
        return field.getDefaultValue();
    }

    public String getName() {
        return name;
    }

    public TypeDTO setName(String name) {
        this.name = name;
        return this;
    }

    public Integer getVersion() {
        return version;
    }

    public TypeDTO setVersion(Integer version) {
        this.version = version;
        return this;
    }

    public String getClazz() {
        return clazz;
    }

    public TypeDTO setClazz(String clazz) {
        this.clazz = clazz;
        return this;
    }

    public Integer getLoadOrder() {
        return loadOrder;
    }

    public TypeDTO setLoadOrder(Integer loadOrder) {
        this.loadOrder = loadOrder;
        return this;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public TypeDTO getParent() {
        return parent;
    }

    public TypeDTO setParent(TypeDTO parent) {
        this.parent = parent;
        return this;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public TypeDTO setAbstract(boolean anAbstract) {
        isAbstract = anAbstract;
        return this;
    }

    public Map<String, ConstantFieldDTO> getConstants() {
        return constants;
    }

    public TypeDTO setConstants(Map<String, ConstantFieldDTO> constants) {
        this.constants = constants;
        return this;
    }

    public Map<String, FieldDTO> getFields() {
        return fields;
    }

    public TypeDTO setFields(Map<String, FieldDTO> fields) {
        this.fields = fields;
        return this;
    }

    public Map<String, String> getComponents() {
        return components;
    }

    public TypeDTO setComponents(Map<String, String> components) {
        this.components = components;
        return this;
    }

    public TypeDTO setChildren(List<TypeDTO> children) {
        this.children = children;
        return this;
    }
}
