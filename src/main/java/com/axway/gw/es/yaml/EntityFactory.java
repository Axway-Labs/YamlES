package com.axway.gw.es.yaml;

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

    private static class MyEntity extends Entity {
        public MyEntity(EntityType type) {
            super(type);
        }

        public void setPK(ESPK pk) {
            this.pk = pk;
        }

        public void setParentPK(ESPK pk) {
            this.parentPK = pk;
        }
    }

    public static Entity convert(com.axway.gw.es.model.entity.Entity yEntity, ESPK parentPK, YamlEntityStore es, File dir) throws IOException {
        EntityType type = es.getTypeForName(yEntity.meta.type);
        MyEntity entity = new MyEntity(type);

        if (yEntity.fields != null) {

            // fields
            for (Map.Entry<String, String> field : yEntity.fields.entrySet()) {
                if (type.isConstantField(field.getKey())) {
                    continue; // don't set constants
                }

                String fieldName = StringUtils.substringBefore(field.getKey(), "#");

                FieldType ft = type.getFieldType(fieldName);
                if (ft.isRefType() || ft.isSoftRefType()) {
                    entity.setReferenceField(fieldName, new YamlPK(field.getValue()));
                } else {
                    String content = field.getValue();
                    if (field.getKey().contains("#ref")) {
                        byte[] data = Files.readAllBytes(dir.toPath().resolve(content));
                        if (field.getKey().endsWith("#refbase64")) {
                            content = Base64.getEncoder().encodeToString(data);
                        } else {
                            content = new String(data);
                        }
                    }
                    entity.setField(fieldName, new Value[]{new Value(content)});
                }
            }
        }

        // pk
        ESPK pk = parentPK == null ? new YamlPK(yEntity.getKeyDescription()) : new YamlPK(parentPK, yEntity.getKeyDescription());
        entity.setPK(pk);
        // parent pk
        //ESPK parentPK = new YamlPK(yEntity.parent);
        entity.setParentPK(parentPK);
        return entity;
    }
}
