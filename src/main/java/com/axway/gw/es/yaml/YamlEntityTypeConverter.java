package com.axway.gw.es.yaml;

import com.axway.gw.es.yaml.dto.type.ConstantFieldDTO;
import com.axway.gw.es.yaml.dto.type.FieldDTO;
import com.axway.gw.es.yaml.dto.type.TypeDTO;
import com.vordel.es.ConstantFieldType;
import com.vordel.es.FieldType;
import com.vordel.es.Value;
import com.vordel.es.impl.ConstantField;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class YamlEntityTypeConverter {

    private YamlEntityTypeConverter() {
        // no op
    }

    public static class YamlFieldType extends FieldType {
        public YamlFieldType(String name, Object cardinality, List<Value> defaultValues) {
            super(name, cardinality, defaultValues);
        }
    }

    public static class YamlConstantFieldType extends ConstantFieldType {
        public YamlConstantFieldType(String type, Value defaultValue) {
            super(type, defaultValue);
        }
    }

    public static YamlEntityType convert(TypeDTO typeDTO) {
        YamlEntityType type = new YamlEntityType();
        type.setAbstractEntity(typeDTO.isAbstract());
        type.setName(typeDTO.getName());

        if (typeDTO.getVersion() != null) {
            ConstantField constantField = createConstantField(YamlConstantFieldsNames.VERSION_FIELD_NAME, "integer", Integer.toString(typeDTO.getVersion()));
            type.addConstantField(constantField);
        }

        if (typeDTO.getClazz() != null) {
            ConstantField constantField = createConstantField(YamlConstantFieldsNames.CLASS_FIELD_NAME, "string", typeDTO.getClazz());
            type.addConstantField(constantField);
        }
        if (typeDTO.getLoadOrder() != null) {
            ConstantField constantField = createConstantField(YamlConstantFieldsNames.LOAD_ORDER_FIELD_NAME, "integer", Integer.toString(typeDTO.getLoadOrder()));
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
            final String fieldName = entry.getKey();
            final FieldDTO fieldDTO = entry.getValue();
            FieldType fieldType = new YamlFieldType(
                    getDTOFieldType(fieldDTO),
                    fieldDTO.getCardinality(),
                    fieldDTO.getDefaultValues()
                            .stream()
                            .map(dto -> dto.getRef() != null ? new Value(new YamlPK(dto.getRef())) : new Value(dto.getData()))
                            .collect(Collectors.toList())
            );
            type.addFieldType(fieldName, fieldType);
        }

        // key fields
        typeDTO.getKeyFields().forEach(type::addKeyFieldName);

        // components
        for (Map.Entry<String, Object> entry : typeDTO.getComponents().entrySet()) {
            type.addComponentType(entry.getKey(), entry.getValue());
        }

        type.processOptionalAndDefaultedFields();

        return type;
    }

    private static String getDTOFieldType(FieldDTO fieldDTO) {
        String dtoFieldType = fieldDTO.getType();
        if (dtoFieldType.charAt(0) == FieldType.SOFT_REF_DELIMITER) {
            dtoFieldType = FieldType.REF_DELIMITER + dtoFieldType.substring(1);
        }
        return dtoFieldType;
    }

    private static ConstantField createConstantField(String name, String type, String value) {
        ConstantFieldType cft = new YamlConstantFieldType(type, new Value(value));
        return new ConstantField(cft, name);
    }

}
