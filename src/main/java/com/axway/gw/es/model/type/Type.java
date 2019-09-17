package com.axway.gw.es.model.type;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.vordel.es.ConstantFieldType;
import com.vordel.es.EntityType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Type {
    private final static Logger LOGGER = Logger.getLogger(Type.class.getName());


    private String name;

    public Integer version;
    public String clazz;
    public Integer loadorder;

    @JsonIgnore
    public String apiVersion = "http://www.vordel.com/2005/06/24/entityStore";
    public String parent;
    @JsonIgnore
    public boolean isAbstract = false;
    public List<ConstantField> constants = new ArrayList<>();
    public List<Field> fields = new ArrayList<>();
    public List<Child> component = new ArrayList<>();
    public List<String> pathToRoot = new ArrayList<>();
    private List<Type> children = new ArrayList<>();

    public void addChild(Type t) {
        children.add(t);
    }

    public void addConstant() {

    }

    @JsonIgnore
    public boolean hasChild() {
        return children.size() > 0;
    }

    //@JsonIgnore
    public List<Type> getChildren() {
        return children;
    }

    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    public Type(File f) throws IOException {
        mapper.readerForUpdating(this).readValue(f);
    }

    public Type(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    @JsonIgnore
    public void setPathToRoot(EntityType et) {
        if (et == null)
            return;
        if (et.getSuperType() != null)
            pathToRoot.add(0, et.getSuperType().getName());
        setPathToRoot(et.getSuperType());
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
                f.setField(name, (ConstantFieldType) ft);
                constants.add(f);
        }

    }
}
