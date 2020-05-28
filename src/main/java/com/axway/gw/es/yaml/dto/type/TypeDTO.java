package com.axway.gw.es.yaml.dto.type;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vordel.es.ConstantFieldType;
import com.vordel.es.FieldType;

import java.util.*;

import static com.axway.gw.es.yaml.YamlConstantFieldsNames.*;

public class TypeDTO {

    private static final String API_VERSION = "http://www.vordel.com/2005/06/24/entityStore";

    private String name;

    @JsonInclude
    private Integer version;

    @JsonProperty(CLASS_FIELD_NAME)
    private String clazz;

    @JsonProperty(LOAD_ORDER_FIELD_NAME)
    @JsonInclude
    private Integer loadOrder;

    @JsonIgnore
    private TypeDTO parent;

    @JsonIgnore
    private boolean isAbstract;

    private Map<String, ConstantFieldDTO> constants = new LinkedHashMap<>();
    private Map<String, FieldDTO> fields = new LinkedHashMap<>();
    private Map<String, Object> components = new LinkedHashMap<>();
    private List<TypeDTO> children = new ArrayList<>();
    private List<String> keyFields = new ArrayList<>();

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

    public List<TypeDTO> getChildren() {
        return children;
    }

    public void addConstant(String name, ConstantFieldType ft) {
        switch (name) {
            case VERSION_FIELD_NAME:
                version = Integer.parseInt(ft.getDefault());
                return;
            case CLASS_FIELD_NAME:
                clazz = ft.getDefault();
                return;
            case LOAD_ORDER_FIELD_NAME:
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
        }
        if (field != null) {
            if (FieldType.BOOLEAN.equals(field.getType())) {
                boolean booleanFieldValue = FieldType.getBooleanValue(fieldValue);
                return field.getDefaultValues()
                        .stream()
                        .map(ValueDTO::getData)
                        .map(FieldType::getBooleanValue)
                        .anyMatch(defaultValue -> defaultValue == booleanFieldValue);
            } else {
                return isDefaultValueOrRef(fieldValue, field);
            }
        }
        return false;

    }

    private boolean isDefaultValueOrRef(String fieldValue, FieldDTO field) {
        if (isRefType(field.getType())) {
            for (ValueDTO valueDTO : field.getDefaultValues()) {
                if (Objects.equals(valueDTO.getRef(), fieldValue)) {
                    return true;
                }
            }

        } else {
            for (ValueDTO valueDTO : field.getDefaultValues()) {
                if (Objects.equals(valueDTO.getData(), fieldValue)) {
                    return true;
                }
            }
        }
        return false;
    }


    public boolean isRefType(String type) {
        return type.charAt(0) == FieldType.SOFT_REF_DELIMITER || type.charAt(0) == FieldType.REF_DELIMITER;
    }

    public boolean isKeyField(String name) {
        return keyFields.contains(name);
    }

    public List<String> getKeyFields() {
        return keyFields;
    }

    public TypeDTO setKeyFields(List<String> keyFields) {
        this.keyFields = keyFields;
        return this;
    }


    public ValueDTO getDefaultValue(String fieldName) {
        FieldDTO field = fields.get(fieldName);
        return field.getDefaultValues().stream().findFirst().orElse(null);
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

    public Map<String, Object> getComponents() {
        return components;
    }

    public TypeDTO setComponents(Map<String, Object> components) {
        this.components = components;
        return this;
    }

    public TypeDTO setChildren(List<TypeDTO> children) {
        this.children = children;
        return this;
    }
}
