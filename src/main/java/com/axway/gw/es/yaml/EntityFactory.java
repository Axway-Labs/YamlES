package com.axway.gw.es.yaml;

import com.axway.gw.es.yaml.model.entity.EntityDTO;
import com.vordel.es.ESPK;
import com.vordel.es.Entity;
import com.vordel.es.EntityType;
import com.vordel.es.FieldType;
import com.vordel.es.Value;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Map;

public class EntityFactory {

    private EntityFactory() {
        // no op
    }

    private static class YamlStoreEntity extends Entity {
        public YamlStoreEntity(EntityType type) {
            super(type);
        }

        public void setPK(ESPK pk) {
            this.pk = pk;
        }

        public void setParentPK(ESPK pk) {
            this.parentPK = pk;
        }
    }

    public static Entity convert(EntityDTO entityDTO, ESPK parentPK, YamlEntityStore es, File dir) throws IOException {
        EntityType type = es.getTypeForName(entityDTO.getMeta().getType());
        YamlStoreEntity entity = new YamlStoreEntity(type);

        if (entityDTO.getFields() != null) {

            // fields
            for (Map.Entry<String, String> fieldEntry : entityDTO.getFields().entrySet()) {
                if (type.isConstantField(fieldEntry.getKey())) {
                    continue; // don't set constants
                }

                processField(dir, type, entity, fieldEntry.getKey(), fieldEntry.getValue());
            }
        }

        // pk
        ESPK pk = parentPK == null ? new YamlPK(entityDTO.getKeyDescription()) : new YamlPK(parentPK, entityDTO.getKeyDescription());
        entity.setPK(pk);

        entity.setParentPK(parentPK);
        return entity;
    }

    private static void processField(File dir, EntityType type, YamlStoreEntity entity, String rawFieldName, String fieldValue) throws IOException {
        String fieldName = StringUtils.substringBefore(rawFieldName, "#");

        FieldType ft = type.getFieldType(fieldName);
        if (ft.isRefType() || ft.isSoftRefType()) {
            entity.setReferenceField(fieldName, new YamlPK(fieldValue));
        } else {
            String content = fieldValue;
            if (rawFieldName.contains("#ref")) {
                byte[] data = Files.readAllBytes(dir.toPath().resolve(content));
                if (rawFieldName.endsWith("#refbase64")) {
                    content = Base64.getEncoder().encodeToString(data);
                } else {
                    content = new String(data);
                }
            }
            entity.setField(fieldName, new Value[]{new Value(content)});
        }
    }
}
