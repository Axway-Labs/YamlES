package com.axway.gw.es.yaml.converters;

import com.axway.gw.es.yaml.dto.type.FieldDTO;
import com.axway.gw.es.yaml.dto.type.TypeDTO;
import com.vordel.es.ConstantFieldType;
import com.vordel.es.EntityStore;
import com.vordel.es.EntityType;
import com.vordel.es.FieldType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import com.axway.gw.es.yaml.YamlEntityStore;


public class EntityTypeDTOConverter {
    private static final Logger log = LoggerFactory.getLogger(EntityTypeDTOConverter.class);

    public static final String TYPES_FILE = "Types.yaml";

    // // types map contains root children of the entitystore
    private final Map<String, TypeDTO> types = new LinkedHashMap<>();
    private final TypeDTO baseType;
    private final EntityStore sourceES;

    public EntityTypeDTOConverter(EntityStore sourceES) {
        this.sourceES = sourceES;
        this.baseType = loadTypes(sourceES.getBaseType());
    }

    public Map<String, TypeDTO> getTypes() {
        return types;
    }

    private TypeDTO loadTypes(EntityType entityType) {
        TypeDTO typeDTO = loadType(entityType);
        for (String subtypeName : sourceES.getSubtypes(entityType.getName())) {
            EntityType subtype = sourceES.getTypeForName(subtypeName);
            TypeDTO subTypeDTO = loadTypes(subtype);
            typeDTO.addChild(subTypeDTO);
        }
        return typeDTO;
    }

    private TypeDTO loadType(EntityType t) {
        log.debug("Loading type: {}", t.getName());
        return typeNameToDTO(t.getName());
    }

    private TypeDTO typeNameToDTO(String typeName) {
        EntityType type = sourceES.getTypeForName(typeName);
        TypeDTO t = typeToDTO(type);
        TypeDTO old = types.put(typeName, t);
        if (old != null) {
            throw new IllegalArgumentException("Type reuse not supported: " + old.getName());
        }
        return t;
    }

    public static TypeDTO typeToDTO(EntityType entityType) {
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
        return typeDTO;
    }

    public void writeTypes(File dir) throws IOException {
        boolean created = dir.mkdirs();
        if (!created) throw new IOException(dir + " was not created");
        File out = new File(dir, TYPES_FILE);
        YamlEntityStore.YAML_MAPPER.writeValue(out, baseType);
    }

}
