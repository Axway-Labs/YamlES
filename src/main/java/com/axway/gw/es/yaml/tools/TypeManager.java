package com.axway.gw.es.yaml.tools;

import com.axway.gw.es.yaml.model.type.FieldDTO;
import com.axway.gw.es.yaml.model.type.TypeDTO;
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


public class TypeManager {
    private static final Logger log = LoggerFactory.getLogger(TypeManager.class);

    public static final String TYPES_FILE = "Types.yaml";

    // // types map contains root children of the entitystore
    private final Map<String, TypeDTO> types = new LinkedHashMap<>();
    private final TypeDTO baseType;
    private final EntityStore es;

    public TypeManager(EntityStore es) {
        this.es = es;
        EntityType t = es.getBaseType();
        baseType = loadTypes(t);
    }

    public void writeTypes(File dir) throws IOException {
        boolean created = dir.mkdirs();
        if (!created) throw new IOException(dir + " was not created");
        File out = new File(dir, TYPES_FILE);
        YamlEntityStore.YAML_MAPPER.writeValue(out, baseType);
    }

    public Map<String, TypeDTO> getTypes() {
        return types;
    }

    private TypeDTO loadTypes(EntityType t) {
        TypeDTO typeDTO = loadType(t);
        for (String typename : es.getSubtypes(t.getName())) {
            EntityType st = es.getTypeForName(typename);
            TypeDTO subTypeDTO = loadTypes(st);
            typeDTO.addChild(subTypeDTO);
        }
        return typeDTO;
    }

    private TypeDTO loadType(EntityType t) {
        log.debug("Loading type: {}", t.getName());
        return getType(t.getName());
    }

    private TypeDTO getType(String typeName) {
        EntityType type = es.getTypeForName(typeName);
        TypeDTO t = createType(type);
        TypeDTO old = types.put(typeName, t);
        if (old != null) {
            throw new IllegalArgumentException("Type reuse not supported: " + old.getName());
        }
        return t;
    }

    private static TypeDTO createType(EntityType et) {
        TypeDTO t = new TypeDTO(et.getName());
        t.setAbstract(et.isAbstract());
        // fields
        Collection<String> keyFields = et.getAllDeclaredKeyFields();
        for (String name : et.getAllDeclaredFieldNames()) {
            FieldType ft = et.getFieldType(name);
            if (ft instanceof ConstantFieldType) {
                t.addConstant(name, (ConstantFieldType) ft);
            } else {
                FieldDTO f = new FieldDTO(name, ft);
                if (keyFields.contains(name))
                    f.setKeyField(true);
                t.getFields().put(name, f);
            }
        }

        // children
        Map<String, Object> components = et.getDeclaredComponentTypes();
        for (Map.Entry<String, Object> entry : components.entrySet()) {
            String name = entry.getKey();
            Object value = entry.getValue();

            t.getComponents().put(name, value.toString());
        }
        return t;
    }
}
