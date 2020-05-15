package com.axway.gw.es.yaml;

import com.axway.gw.es.yaml.model.type.ConstantFieldDTO;
import com.axway.gw.es.yaml.model.type.FieldDTO;
import com.axway.gw.es.yaml.model.type.TypeDTO;
import com.vordel.es.ConstantFieldType;
import com.vordel.es.FieldType;
import com.vordel.es.Value;
import com.vordel.es.impl.ConstantField;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class EntityTypeFactory {

    private EntityTypeFactory() {
        // no op
    }

    private static class YamlFieldType extends FieldType {
        public YamlFieldType(String name, Object x, List<Value> v) {
            super(name, x, v);
        }
    }

    private static class YamlConstantFieldType extends ConstantFieldType {
        public YamlConstantFieldType(String name, Value v) {
            super(name, v);
        }
    }

    public static YamlEntityType convert(TypeDTO typeDTO) {
        YamlEntityType type = new YamlEntityType();
        type.setAbstractEntity(typeDTO.isAbstract());
        type.setName(typeDTO.getName());


        if(typeDTO.getVersion() != null) {
            ConstantField constantField = createConstantField(YamlConstantFieldsNames.VERSION, "integer", Integer.toString(typeDTO.getVersion()));
            type.addConstantField(constantField);
        }

        if(typeDTO.getClazz() != null) {
            ConstantField constantField = createConstantField(YamlConstantFieldsNames.CLASS, "string", typeDTO.getClazz());
            type.addConstantField(constantField);
        }
        if(typeDTO.getLoadOrder() != null && typeDTO.getLoadOrder() != 0) {
            ConstantField constantField = createConstantField(YamlConstantFieldsNames.LOAD_ORDER, "integer", Integer.toString(typeDTO.getLoadOrder()));
            type.addConstantField(constantField);
        }

        // constants
        for (Map.Entry<String, ConstantFieldDTO> entry : typeDTO.getConstants().entrySet()) {
            ConstantFieldDTO constant = entry.getValue();
            constant.setName(entry.getKey());
            ConstantField field = createConstantField(constant.getName(), constant.getType(), constant.getValue());
            type.addConstantField(field);
        }
        // fields
        for (Map.Entry<String, FieldDTO> entry : typeDTO.getFields().entrySet()) {
            FieldDTO field = entry.getValue();
            field.setName(entry.getKey());
            FieldType ft = new YamlFieldType(field.getType(), field.getCardinality(), new ArrayList<>());
            type.addFieldType(field.getName(), ft);
            if (field.isKeyField())
                type.addKeyFieldName(field.getName());
        }
        // components
        for (Map.Entry<String, String> entry : typeDTO.getComponents().entrySet()) {
            type.addComponentType(entry.getKey(), entry.getValue());
        }
        return type;
    }

    private static ConstantField createConstantField(String name, String type, String value) {
        ConstantFieldType cft = new YamlConstantFieldType(type, new Value(value));
        return new ConstantField(cft, name);
    }

}
