package com.axway.gw.es.tools;

import com.axway.gw.es.model.type.Type;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.vordel.es.*;
import com.vordel.es.xes.PortableESPK;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@SuppressWarnings({"WeakerAccess", "ResultOfMethodCallIgnored"})
public class EntityManager {
    private final static Logger LOGGER = Logger.getLogger(EntityManager.class.getName());

    private ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    private Map<ESPK, Entity> entities = new ConcurrentHashMap<>();
    private List<com.axway.gw.es.model.entity.Entity> mappedEntities = new ArrayList<>();
    private Set<ESPK> inlined = new HashSet<>();
    private EntityStore es;

    public EntityManager(EntityStore es) {
        this.es = es;
        this.mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        ESPK root = es.getRootPK();
        loadEntities(root);
    }

    public void writeEntities(File dir) throws IOException {
        if (!dir.exists())
            dir.mkdirs();

        // must provide dir
        if (!dir.isDirectory())
            throw new IOException("Must provide a directory for YAML output");
      /*  for (Map.Entry<ESPK, Entity> entry : entities.entrySet()) {
            if (!entry.getValue().getType().getName().equals("InternationalizationCategory")) {
                LOGGER.info("Dumping type " + entry.getKey());
                com.axway.gw.es.model.entity.Entity e = yamlize(dir, entry.getValue());
                //writeType(dir, t);
            }
        }*/

        for (com.axway.gw.es.model.entity.Entity ye : mappedEntities) {
            // deal with pk for parent  entity
            dumpAsYaml(new File(dir, sanitizeFilename(ye.key)), ye);

        }
    }

    private com.axway.gw.es.model.entity.Entity yamlize(Entity value, String parentKey) {
        LOGGER.info("Yamlize " + value.getPK());
        com.axway.gw.es.model.entity.Entity ye = new com.axway.gw.es.model.entity.Entity();
        ye.meta.type = value.getType().getName(); // may want to change this to the directory location of the type?
        // deal with pk for this entity
        ye.key = parentKey  + "/" + getKeyValues(value);
        //ye.parent = getPath(value.getParentPK()).toString();
        // children?
        ye.allowsChildren = isAllowsChildren(value);
        // fields
        setFields(value, ye);
        setReferences(value, ye);

        if (!ye.allowsChildren) {
            for (ESPK childPK : es.listChildren(value.getPK(), null)) {
                inlined.add(childPK);

                Entity child = getEntity(childPK);
                com.axway.gw.es.model.entity.Entity ychild = yamlize(child, ye.key);
                ye.addChild(ychild);
                ychild.key = null;
            }
        }

        return ye;
    }


    private static final Set<String> INLINED = new HashSet<>(Arrays.asList("FilterCircuit", "EnvironmentalizedEntity", "XPath"));

    private boolean isAllowsChildren(Entity value) {
        return value.getType().allowsChildEntities()
                && !value.getType().extendsType("NamedLoadableModule")
                && !value.getType().extendsType("NamedGroup")
                && !value.getType().extendsType("Filter")
                && !value.getType().extendsType("KPSStore")
                && !value.getType().extendsType("KPSType")
                && !INLINED.contains(value.getType().getName());
    }

    private void dumpAsYaml(File out, com.axway.gw.es.model.entity.Entity ye) {
        try {
            if (ye.allowsChildren) { // handle as directory with metadata
                out.mkdirs();
                mapper.writeValue(new File(out, "metadata.yaml"), ye);
            } else { // handle as file
                String filename = out.getPath() + ".yaml";
                File f = new File(filename);
                f.getParentFile().mkdirs();
                mapper.writeValue(f, ye);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to write file", e);
        }
    }

    private void setFields(Entity value, com.axway.gw.es.model.entity.Entity ye) {
        com.vordel.es.Field[] allFields = value.getAllFields();
        List<com.vordel.es.Field> refs = value.getReferenceFields();
        for (com.vordel.es.Field f : allFields) {
            com.vordel.es.FieldType ft = f.getType();
            if (!refs.contains(f)) { // not a reference
                ye.addFval(f.getName(), value.getStringValue(f.getName()));
                ye.keyFieldValues = getKeyValues(value);
            }
        }
    }

    private static boolean isLateBoundReference(final ESPK pk) {
        if (pk instanceof PortableESPK) {
            final PortableESPK portableKey = (PortableESPK) pk;
            final boolean isLateBound = "_lateBoundReference".equals(portableKey.getTypeNameOfReferencedEntity());
            if (isLateBound) {
                LOGGER.info("Late bound reference to " + portableKey.terse() + " expected at run-time");
            }
            return isLateBound;
        }

        return false;
    }

    private void setReferences(Entity value, com.axway.gw.es.model.entity.Entity ye) {
        File dir = new File(".");

        List<com.vordel.es.Field> refs = value.getReferenceFields();
        for (com.vordel.es.Field field : refs) {
            ESPK ref = field.getReference(); // just deal with single at the moment
            if (!ref.equals(EntityStore.ES_NULL_PK)) {
                String key;
                if (isLateBoundReference(ref)) {
                    key = ref.toString();
                } else {
                    key = getPath(ref);
                    if (key.startsWith(ye.key)) {
                        key = key.substring(ye.key.length() + 1);
                    }
                }
                ye.addFval(field.getName(), key);
            }
        }
    }

    private String createRelativePath(File base, File path) {
        return base.toURI().relativize(path.toURI()).getPath();
    }

    private String getPath(ESPK pk) {
        StringBuilder builder = new StringBuilder();

        List<Entity> path = pathToRoot(pk);
        LOGGER.info("path to root is depth: " + path.size());
        for (Entity entity : path) {
            String name = getKeyValues(entity);
            if (builder.length() > 0) {
                builder.append('/');
            }
            builder.append(name);
            //if (!dir.exists() && isAllowsChildren(entity))
            //  dir.mkdirs();
        }
        return builder.toString();
    }

    private String getKeyValues(Entity e) {
        String[] keyNames = e.getType().getKeyFieldNames();
        if (keyNames == null) {
            throw new IllegalArgumentException("No key names for type " + e.getType());
        }

        StringBuilder b = new StringBuilder();
        for (int i = 0; i < keyNames.length; i++) {
            com.vordel.es.Field keyField = e.getField(keyNames[i]);
            if (null != keyField) {
                Value keyValue = keyField.getValues()[0];
                if (keyValue.getRef() == null) {
                    b.append(keyValue.getData());
                } else {
                    b.append(getPath(keyValue.getRef()));
                }
            }
            if (i < keyNames.length - 1)
                b.append("$");
        }
        return b.toString().trim();
    }

    /**
     * replace illegal characters in a filename with "_"
     * illegal characters :
     * : \ / * ? | < > "
     */
    private String sanitizeFilename(String name) {
        if (name == null)
            LOGGER.info("hello");
        return name.trim().replaceAll("[:\"*?<>|]+", "_");
    }

    private void writeType(File dir, String fileName, Type t) throws IOException {
        LOGGER.info("Dumping type " + t.getName() + " to file " + fileName);
        File out = new File(dir, fileName);
        mapper.writeValue(out, t);
    }

    private void loadEntities(ESPK pk) {
        loadEntity(pk);
        Collection<ESPK> children = es.listChildren(pk, null);
        for (ESPK childPK : children)
            loadEntities(childPK);
    }

    private void loadEntity(ESPK pk) {
        if (inlined.contains(pk)) {
            return;
        }
        Entity e = getEntity(pk);
        com.axway.gw.es.model.entity.Entity ye = yamlize(e, getPath(e.getParentPK()));
        mappedEntities.add(ye);

        //LOGGER.info("Loaded entity:  " + TraceEntity.describeEntity(e));
        /*List<Entity> path = pathToRoot(pk);
        LOGGER.info("path to root is depth: " + path.size());
        Iterator<Entity> it = path.iterator();
        StringBuilder b = new StringBuilder();
        while (it.hasNext()) {
            Entity entity = it.next();
            String[] keyNames = entity.getType().getKeyFieldNames();
            Collection<String> keyFieldNames = new HashSet<>();
            if (keyNames != null) {
                for (int i = 0; i < keyNames.length; i++) {
                    com.vordel.es.Field keyField = entity.getField(keyNames[i]);
                    if (null != keyField) {
                        keyFieldNames.add(keyField.getName());
                        b.append(keyField.getValues()[0]);
                    }
                    if (i < keyNames.length - 1)
                        b.append(" ,");
                }
            }
        }
        LOGGER.info("path to root: " + b.toString());*/
    }

    public Entity getEntity(ESPK key) {
        if (entities.containsKey(key))
            return entities.get(key);
        ESPK espk = EntityStoreDelegate.getEntityForKey(es, key);
        Entity e = es.getEntity(espk);
        entities.put(key, e);
        return e;
    }

    public List<Entity> pathToRoot(ESPK pk) {
        List<Entity> path = new ArrayList<>();
        if (pk == null)
            return path;
        while (!pk.equals(es.getRootPK())) {
            try {
                Entity e = getEntity(pk);
                path.add(0, e);
                pk = e.getParentPK();
            } catch (EntityStoreException p) {
                LOGGER.finer("ESPK not found in current ES: " + pk);
            }
        }
        return path;
    }
}
