package com.axway.gw.es.yaml.model.type;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vordel.es.ConstantFieldType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.axway.gw.es.yaml.YamlConstantFieldsNames.*;

public class TypeDTO {

    private static final String API_VERSION = "http://www.vordel.com/2005/06/24/entityStore";

    private String name;

    private Integer version;

    @JsonProperty(CLASS)
    private String clazz;

    @JsonProperty(LOAD_ORDER)
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
        // for parsers
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
        return !children.isEmpty();
    }

    public List<TypeDTO> getChildren() {
        return children;
    }

    public void addConstant(String name, ConstantFieldType ft) {
        switch (name) {
            case VERSION:
                version = Integer.parseInt(ft.getDefault());
                return;
            case CLASS:
                clazz = ft.getDefault();
                return;
            case LOAD_ORDER:
                loadOrder = Integer.parseInt(ft.getDefault());
                return;
            default:
                ConstantFieldDTO f = new ConstantFieldDTO(name, ft);
                constants.put(name, f);
        }
    }

    public boolean isDefaultValue(String fieldName, String fieldValue) {
        FieldDTO field = fields.get(fieldName);
        if (field == null && parent != null) {
            return parent.isDefaultValue(fieldName, fieldValue);
        } else if (field != null) {
            if ("boolean".equals(field.getType())) {
                switch (fieldValue) {
                    case "0" : fieldValue = "false"; break;
                    case "1" : fieldValue = "true"; break;
                    default:
                }
            }
            String defaultValue = field.getDefaultValue();
            return defaultValue != null && defaultValue.equals(fieldValue);
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

    @JsonIgnore
    public String getApiVersion() {
        return API_VERSION;
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
