package com.axway.gw.es.tools;

import com.axway.gw.es.model.type.Field;
import com.axway.gw.es.model.type.Type;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.vordel.es.ConstantFieldType;
import com.vordel.es.EntityStore;
import com.vordel.es.EntityType;
import com.vordel.es.FieldType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public class TypeManager {
    private final static Logger LOGGER = LoggerFactory.getLogger(TypeManager.class);

    private ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    // // types map contains root children of the entitystore
    private Map<String, Type> types = new HashMap<>();
    private Type baseType;
    private EntityStore es;

    public TypeManager(EntityStore es) {
        this.mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        this.mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        this.mapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);

        this.es = es;
        EntityType t = es.getBaseType();
        baseType = loadTypes(t);
    }

    public void writeTypes(File dir) throws IOException {
        dir.mkdirs();
        File out = new File(dir, "Types.yaml");
        mapper.writeValue(out, baseType);
    }

    public Map<String, Type> getTypes() {
        return types;
    }

    private Type loadTypes(EntityType t) {
        Type type = loadType(t);
        for (String typename : es.getSubtypes(t.getName())) {
            EntityType st = es.getTypeForName(typename);
            Type ySubType = loadTypes(st);
            type.addChild(ySubType);
        }
        return type;
    }

    private Type loadType(EntityType t) {
        LOGGER.info("Loading type:  " + t.getName());
		return getType(t.getName());
    }

    private Type getType(String typeName) {
        EntityType type = es.getTypeForName(typeName);
        Type t = createType(type);
        Type old = types.put(typeName, t);
        if (old != null) {
            throw new IllegalArgumentException("Type reuse not supported: " + old.name);
        }
        return t;
    }

    private Type createType(EntityType et) {
        Type t = new Type(et.getName());
        t.isAbstract = et.isAbstract();
        // fields
        Collection<String> keyFields = et.getAllDeclaredKeyFields();
        for (String name : et.getAllDeclaredFieldNames()) {
            FieldType ft = et.getFieldType(name);
            if (ft instanceof ConstantFieldType) {
                t.addConstant(name, (ConstantFieldType) ft);
            } else {
                Field f = new Field();
                f.setField(name, ft);
                if (keyFields.contains(name))
                    f.isKey = true;
                t.fields.put(name, f);
            }
        }

        // children
        Map<String, Object> components = et.getDeclaredComponentTypes();
        for (Map.Entry<String, Object>  entry : components.entrySet()) {
        	String name = entry.getKey();
        	Object value = entry.getValue();

            t.components.put(name, value.toString());
        }
        return t;
    }
}
