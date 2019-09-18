package com.axway.gw.es.model.type;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vordel.es.ConstantFieldType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Type {

    public String name;

    public Integer version;

    @JsonProperty("class")
    public String clazz;

    public Integer loadorder;

    @JsonIgnore
    public String apiVersion = "http://www.vordel.com/2005/06/24/entityStore";

    @JsonIgnore
    public Type parent;

    @JsonIgnore
    public boolean isAbstract = false;
    public Map<String, ConstantField> constants = new LinkedHashMap<>();
    public Map<String, Field> fields = new LinkedHashMap<>();
    public Map<String, String> components = new LinkedHashMap<>();
    private List<Type> children = new ArrayList<>();

    public Type() {
    }

    public Type(String name) {
        this.name = name;
    }

    public void addChild(Type t) {
        children.add(t);
        t.parent = this;
    }

    @JsonIgnore
    public boolean hasChild() {
        return children.size() > 0;
    }

    public List<Type> getChildren() {
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
                loadorder = Integer.parseInt(ft.getDefaultValues().get(0).getData());
                return;
            default:
                ConstantField f = new ConstantField();
                f.setField(name, ft);
                constants.put(name, f);
        }
    }

    public boolean isDefaultValue(String fieldName, String fval) {
        Field field = fields.get(fieldName);
        if (field == null && parent != null) {
            return parent.isDefaultValue(fieldName, fval);
        } else if (field != null) {
            if ("boolean".equals(field.type)) {
                switch (fval) {
                    case "0" : fval = "false"; break;
                    case "1" : fval = "true"; break;
                }
            }
            String defaultValue = field.defaultValue;
            return defaultValue != null && defaultValue.equals(fval);
        } else {
            return false;
        }
    }
}
