package com.axway.gw.es.yaml.converters;

import com.axway.gw.es.yaml.YamlEntityStoreFactory;
import com.vordel.es.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Properties;

public class CloneES {

    private static final Logger LOGGER = LoggerFactory.getLogger(CloneES.class.getName());


    EntityStore source;
    EntityStore destination;

    HashMap<ESPK, Entity> addedEntities = new HashMap<>();

    public CloneES(EntityStore source, EntityStore destination) {
        this.source = source;
        this.destination = destination;
        addedEntities.put(source.getRootPK(),
                EntityStoreDelegate.getEntity(destination, destination.getRootPK()));
    }

    private void cloneEntities(ESPK pk, ESPK parent) {
        Entity added = cloneEntity(pk, parent);
        Collection<ESPK> children = source.listChildren(pk, null);
        for (ESPK childPK : children)
            cloneEntities(childPK, added.getPK());
    }

    private Entity cloneEntity(ESPK keyToClone, ESPK parent) {
        ESPK espk = EntityStoreDelegate.getEntityForKey(source, keyToClone);
        Entity e = source.getEntity(espk);
        if (addedEntities.containsKey(espk))
            return addedEntities.get(espk);

        Entity newEntity = e.cloneEntityFields();
        String fieldName = getKeyField(newEntity);
        String value = newEntity.getStringValue(fieldName);
        LOGGER.debug("Processing field name={} value {}", fieldName, value);

        Collection<ESPK> children = findNamedChildren(newEntity, parent, destination);
        ESPK pk = null;
        if (children != null && !children.isEmpty())  // update
            pk = updateEntity(newEntity, children.iterator().next(), destination);
        else // add
            pk = destination.addEntity(parent, newEntity);

        Entity entity = destination.getEntity(pk);
        addedEntities.put(espk, entity);
        return entity;
    }

    private ESPK updateEntity(Entity source, ESPK pk, EntityStore es) {
        Entity dest = es.getEntity(pk);
        com.vordel.es.Field[] fields = source.getInstanceFields();
        for (com.vordel.es.Field f : fields)
            dest.setField(f);
        es.updateEntity(dest);
        return dest.getPK();
    }

    Collection<ESPK> findNamedChildren(Entity e, ESPK parent, EntityStore es) {
        EntityType type = destination.getTypeForName(e.getType().getName());
        String[] keyFields = type.getKeyFieldNames();
        ArrayList<Field> fields = new ArrayList<>();
        for (String fieldName : keyFields) {
            Field field = new Field(type.getFieldType(fieldName), fieldName);
            if (field.isRefType()) {
                ESPK ref = e.getReferenceValue(fieldName);
                field.addValue(new Value(ref));
            } else {
                String value = e.getStringValue(fieldName);
                field.addValue(new Value(value));
            }
            fields.add(field);
        }
        com.vordel.es.Field[] fs = fields.toArray(new Field[0]);
        return es.findChildren(parent, fs, type);
    }

    String getKeyField(Entity e) {
        EntityType type = e.getType();
        String[] keyFields = type.getKeyFieldNames();
        return keyFields[0];
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            LOGGER.info("Usage: federated:file:/path-to-existing-fed.xml path-to-write-yaml");
            System.exit(1);
        }
        String entityStoreExported = args[0];
        String entityStoreImport = args[1];

        EntityStore dest = YamlEntityStoreFactory.createESForURL(entityStoreExported);
        dest.connect(entityStoreExported, new Properties());

        EntityStore source = YamlEntityStoreFactory.createESForURL(entityStoreImport);
        source.connect(entityStoreImport, new Properties());

        CloneES fed = new CloneES(source, dest);
        fed.cloneEntities(source.getRootPK(), dest.getRootPK());

    }
}
