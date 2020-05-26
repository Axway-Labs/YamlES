package com.axway.gw.es.yaml.converters;

import com.axway.gw.es.yaml.dto.type.FieldDTO;
import com.axway.gw.es.yaml.dto.type.TypeDTO;
import com.vordel.es.ConstantFieldType;
import com.vordel.es.EntityStore;
import com.vordel.es.EntityType;
import com.vordel.es.FieldType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;


public class EntityTypeDTOConverter {
    private static final Logger log = LoggerFactory.getLogger(EntityTypeDTOConverter.class);


    // // types map contains root children of the entity store
    private final Map<String, TypeDTO> types = new LinkedHashMap<>();
    private TypeDTO baseType;
    private final EntityStore sourceES;

    public EntityTypeDTOConverter(EntityStore sourceES) {
        this.sourceES = sourceES;
    }

    public  TypeDTO loadTypeAndSubtype() {
        this.baseType = loadTypeAndSubtype(sourceES.getBaseType());
        return getBaseType();
    }

    public Map<String, TypeDTO> getTypes() {
        return types;
    }

    public TypeDTO getBaseType() {
        return baseType;
    }

    private TypeDTO loadTypeAndSubtype(EntityType baseType) {
        TypeDTO typeDTO = mapToDTO(baseType);
        for (String subtypeName : sourceES.getSubtypes(baseType.getName())) {
            EntityType subtype = sourceES.getTypeForName(subtypeName);
            TypeDTO subTypeDTO = loadTypeAndSubtype(subtype);
            typeDTO.addChild(subTypeDTO);
        }
        return typeDTO;
    }

    private TypeDTO mapToDTO(EntityType entityType) {

        final String typeName = entityType.getName();
        log.debug("Loading type: {}", typeName);

        TypeDTO typeDTO = new TypeDTO(entityType.getName());
        typeDTO.setAbstract(entityType.isAbstract());
        // fields
        Collection<String> keyFields = entityType.getAllDeclaredKeyFields();
        for (String name : entityType.getAllDeclaredFieldNames()) {
            FieldType fieldType = entityType.getFieldType(name);
            if (fieldType instanceof ConstantFieldType) {
                typeDTO.addConstant(name, (ConstantFieldType) fieldType);
            } else {
                FieldDTO fieldDTO = new FieldDTO(name, fieldType);
                if (keyFields.contains(name))
                    fieldDTO.setKeyField(true);
                typeDTO.getFields().put(name, fieldDTO);
            }
        }

        // children
        Map<String, Object> components = entityType.getDeclaredComponentTypes();
        for (Map.Entry<String, Object> entry : components.entrySet()) {
            String name = entry.getKey();
            Object value = entry.getValue();
            typeDTO.getComponents().put(name, value.toString());
        }

        TypeDTO old = types.put(typeName, typeDTO);
        if (old != null) {
            throw new IllegalArgumentException("Type reuse not supported: " + old.getName());
        }
        return typeDTO;
    }


}
