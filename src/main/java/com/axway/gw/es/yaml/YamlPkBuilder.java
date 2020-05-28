package com.axway.gw.es.yaml;

import com.axway.gw.es.yaml.util.TopLevelFolders;
import com.vordel.es.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.axway.gw.es.yaml.YamlPK.CHILD_SEPARATOR;
import static com.axway.gw.es.yaml.util.NameUtils.sanitize;
import static com.google.common.base.Preconditions.checkNotNull;

public class YamlPkBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(YamlPkBuilder.class);

    private final EntityStore entityStore;
    private final TopLevelFolders topLevelFolders;

    public YamlPkBuilder(EntityStore entityStore) {
        this.entityStore = entityStore;
        this.topLevelFolders = new TopLevelFolders();
    }

    public String buildKeyValue(ESPK childPk) {
        return buildKeyValue(getEntity(childPk));
    }

    public String buildKeyValue(Entity childEntity) {
        StringBuilder builder = new StringBuilder();

        List<Entity> path = pathToRoot(childEntity);
        LOGGER.debug("path to root is depth: {}", path.size());
        for (Entity entity : path) {
            if (builder.length() == 0) {
                builder.append(topLevelFolders.getTopLevelFolderForTypeName(entity.getType().getName()));
            }
            if (builder.length() > 0) {
                builder.append(CHILD_SEPARATOR);
            }
            String name = getKeyValues(entity);
            builder.append(name);

        }
        return builder.toString();
    }

    private List<Entity> pathToRoot(Entity childEntity) {
        List<Entity> path = new ArrayList<>();
        if (childEntity == null) return path;
        ESPK pk = childEntity.getPK();
        while (pk == null || !pk.equals(entityStore.getRootPK())) {
            try {
                Entity e = pk != null ? getEntity(pk) : childEntity;
                path.add(0, e);
                pk = e.getParentPK();
            } catch (EntityStoreException p) {
                LOGGER.warn("ESPK not found in current ES: {}", pk);
            }
        }
        return path;
    }


    public String getKeyValues(Entity entity) {

        String[] keyNames = entity.getType().getKeyFieldNames();
        if (keyNames == null) {
            throw new IllegalArgumentException("No key names for type " + entity.getType());
        }

        List<String> keyValues = new ArrayList<>();
        for (String keyName : keyNames) {
            Field keyField = entity.getField(keyName);
            if (keyField != null) {
                Value keyValue = keyField.getValues()[0];
                if (keyValue.getRef() == null) {
                    keyValues.add(keyValue.getData());
                } else {
                    keyValues.add(buildKeyValue(keyValue.getRef()));
                }
            }
        }

        return sanitize(String.join("$", keyValues));
    }


    public Entity getEntity(ESPK key) {
        final ESPK espk = EntityStoreDelegate.getEntityForKey(entityStore, key);
        return entityStore.getEntity(espk);
    }

    public boolean isAbsoluteRef(String ref) {
        return topLevelFolders.isAbsoluteRef(ref);
    }
}
