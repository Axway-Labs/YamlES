package com.axway.gw.es.tools;

import com.axway.gw.es.model.type.Type;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.vordel.es.*;
import com.vordel.es.xes.PortableESPK;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings({"WeakerAccess", "ResultOfMethodCallIgnored"})
public class EntityManager {
    private final static Logger LOGGER = LoggerFactory.getLogger(EntityManager.class);

    private ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    private Map<ESPK, Entity> entities = new ConcurrentHashMap<>();
    private List<com.axway.gw.es.model.entity.Entity> mappedEntities = new ArrayList<>();
    private Set<ESPK> inlined = new HashSet<>();
    private EntityStore es;
    private Map<String, Type> types;

    public EntityManager(EntityStore es, Map<String, Type> types) {
        this.es = es;
        this.types = types;
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
            dumpAsYaml(new File(dir, ye.key), ye);
        }
    }

    private com.axway.gw.es.model.entity.Entity yamlize(Entity value, boolean allowChildren) {
        LOGGER.debug("Yamlize " + value.getPK());
        com.axway.gw.es.model.entity.Entity ye = new com.axway.gw.es.model.entity.Entity();
        ye.meta.type = value.getType().getName(); // may want to change this to the directory location of the type?
        //ye.meta.type = getTypePath(value.getType()); // may want to change this to the directory location of the type?
        // deal with pk for this entity
        ye.key = getPath(value.getPK());
        // ye.parentType = value.getParentPK() == null? null : getTypePath(getEntity(value.getParentPK()).getType());
        // children?
        ye.allowsChildren = allowChildren && isAllowsChildren(value);
        // fields
        setFields(value, ye);
        setReferences(value, ye);

        if (!ye.allowsChildren) {
            for (ESPK childPK : es.listChildren(value.getPK(), null)) {
                inlined.add(childPK);

                Entity child = getEntity(childPK);
                com.axway.gw.es.model.entity.Entity ychild = yamlize(child, false);
                ye.addChild(ychild);
                ychild.key = null;
            }
        }

        return ye;
    }

    public static final Map<String, String> TOPLEVEL = new HashMap<>();

    {
        TOPLEVEL.put("FilterCircuit", "Policies");
        TOPLEVEL.put("CircuitContainer", "Policies");
        TOPLEVEL.put("WebServiceGroup", "APIs");
        TOPLEVEL.put("WebServiceRepository", "APIs");

        TOPLEVEL.put("JSONSchemaGroup", "Resources");
        TOPLEVEL.put("ScriptGroup", "Resources");
        TOPLEVEL.put("StylesheetGroup", "Resources");
        TOPLEVEL.put("XmlSchemaGroup", "Resources");
        TOPLEVEL.put("XPathGroup", "Resources");
        TOPLEVEL.put("ResourceRepository", "Resources");

        TOPLEVEL.put("KPSRoot", "Environment Configuration");
        TOPLEVEL.put("EnvironmentalizedEntities", "Environment Configuration");
        TOPLEVEL.put("Certificates", "Environment Configuration");
        TOPLEVEL.put("PGPKeyPairs", "Environment Configuration");
        TOPLEVEL.put("NetService", "Environment Configuration");
        TOPLEVEL.put("UserStore", "Environment Configuration");

        TOPLEVEL.put("CacheManager", "Libraries");
        TOPLEVEL.put("AlertManager", "Libraries");
        TOPLEVEL.put("RegularExpressionGroup", "Libraries");
        TOPLEVEL.put("OAuth2StoresGroup", "Libraries");
        TOPLEVEL.put("CronExpressionGroup", "Libraries");
        TOPLEVEL.put("CORSGroup", "Libraries");
        TOPLEVEL.put("WAFProfileGroup", "Libraries");

        TOPLEVEL.put("AWSSettings", "External Connections");
        TOPLEVEL.put("AuthnRepositoryGroup", "External Connections");
        TOPLEVEL.put("ConnectionSetGroup", "External Connections");
        TOPLEVEL.put("AuthProfilesGroup", "External Connections");
        TOPLEVEL.put("DbConnectionGroup", "External Connections");
        TOPLEVEL.put("ICAPServerGroup", "External Connections");
        TOPLEVEL.put("JMSServiceGroup", "External Connections");
        TOPLEVEL.put("LdapDirectoryGroup", "External Connections");
        TOPLEVEL.put("ProxyServerGroup", "External Connections");
        TOPLEVEL.put("SentinelServerGroup", "External Connections");
        TOPLEVEL.put("SiteMinderConnectionSet", "External Connections");
        TOPLEVEL.put("SMTPServerGroup", "External Connections");
        TOPLEVEL.put("SyslogServerGroup", "External Connections");
        TOPLEVEL.put("RendezvousDaemonGroup", "External Connections");
        TOPLEVEL.put("UrlSetGroup", "External Connections");
        TOPLEVEL.put("XkmsConfigGroup", "External Connections");
        TOPLEVEL.put("RadiusClients", "External Connections");
        TOPLEVEL.put("SunAccessManagerSettings", "External Connections");
        TOPLEVEL.put("PassPortKeyStoreManager", "External Connections");
        TOPLEVEL.put("TivoliActionGroupGroup", "External Connections");
        TOPLEVEL.put("TivoliSettingsGroup", "External Connections");

        TOPLEVEL.put("AccessLogger", "Server Settings");
        TOPLEVEL.put("CassandraSettings", "Server Settings");
        TOPLEVEL.put("EventLog", "Server Settings");
        TOPLEVEL.put("LogManager", "Server Settings");
        TOPLEVEL.put("RealtimeMonitoring", "Server Settings");
        TOPLEVEL.put("PortalCallbackGroup", "Server Settings");
        TOPLEVEL.put("MimeTypeGroup", "Server Settings");
        TOPLEVEL.put("OPDbConfig", "Server Settings");
        TOPLEVEL.put("SystemSettings", "Server Settings");
        TOPLEVEL.put("OpenTrafficEventLog", "Server Settings");
        TOPLEVEL.put("PortalConfiguration", "Server Settings");
        TOPLEVEL.put("ZeroDowntime", "Server Settings");
    }

    private String getTopLevel(EntityType type) {
        String topLevel = TOPLEVEL.get(type.getName());
        return topLevel == null ? "System" : topLevel;
    }

    private String getTypePath(EntityType type) {
        if (type.getSuperType() == null) {
            return type.getName();
        }
        return getTypePath(type.getSuperType()) + "/" + type;
    }


    private static final Set<String> EXPANDED = new HashSet<>(Arrays.asList("KPSRoot"));
    private static final Set<String> INLINED = new HashSet<>(Arrays.asList(
            "User", // Entity/Identity
            "FilterCircuit", // Entity/RootChild
            "EnvironmentalizedEntity", // Entity
            "MetricGroupTypes", // Entity
            "MetricTypes", // Entity
            "CategoryGroup", // Entity/RootChild/NamedTopLevelGroup
            "Internationalization", // Entity/RootChild/NamedTopLevelGroup
            "HTTPRetryStatusClassGroup", // Entity/RootChild/NamedTopLevelGroup
            "HTTPStatusClassGroup", // Entity/RootChild/NamedTopLevelGroup
            "PolicyCategoryGroup", // Entity/RootChild/NamedTopLevelGroup
            "RegistryCategorizationSchemeGroup", // Entity/RootChild/NamedTopLevelGroup
            "ElementSpecifiers", // Entity/RootChild/LoadableModule/NamedLoadableModule
            "MimeTypeGroup", // Entity/RootChild/LoadableModule/NamedLoadableModule
            "NamespacesConfiguration", // Entity/RootChild/LoadableModule/NamedLoadableModule
            "TokenFinderSet", // Entity/RootChild/LoadableModule/NamedLoadableModule
            "ParserFeatureGroup" // Entity/RootChild/LoadableModule/NativeModule
    ));

    private boolean isAllowsChildren(Entity value) {
        return value.getType().allowsChildEntities() && !INLINED.contains(value.getType().getName())
                && (value.getType().extendsType("NamedLoadableModule")
                || EXPANDED.contains(value.getType().getName())
                || !value.getType().extendsType("NamedGroup")
                && !value.getType().extendsType("LoadableModule")
                && !value.getType().extendsType("Filter")
                && !value.getType().extendsType("KPSStore")
                && !value.getType().extendsType("KPSType"));
    }

    private void dumpAsYaml(File out, com.axway.gw.es.model.entity.Entity ye) {
        try {
            if (ye.allowsChildren) { // handle as directory with metadata
                out.mkdirs();
                mapper.writeValue(new File(out, "metadata-" + ye.meta.type.substring(ye.meta.type.lastIndexOf('/') + 1) + ".yaml"), ye);
            } else { // handle as file
                String filename = out.getPath() + ".yaml";
                File f = new File(filename);
                f.getParentFile().mkdirs();

                extractContent(ye, f);

                mapper.writeValue(f, ye);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to write file", e);
        }
    }

    private void extractContent(com.axway.gw.es.model.entity.Entity ye, File f) throws IOException {
        if (ye.children != null) {
            for (com.axway.gw.es.model.entity.Entity yChild : ye.children.values()) {
                extractContent(yChild, f);
            }
        }

        File dir = f.getParentFile();
        switch (ye.meta.type) {
            case "JavaScriptFilter":
                dir = new File(f.getAbsolutePath().replace(".yaml", "-Scripts"));
                extractContent(ye, dir, sanitizeFilename(ye.name) + "." + ye.fields.get("engineName"), "script", false);
                break;
            case "Script":
                extractContent(ye, dir, ye.name + "." + ye.fields.get("engineName"), "script", false);
                break;
            case "Stylesheet":
                extractContent(ye, dir, ye.fields.get("URL") + ".xsl", "contents", true);
                break;
            case "Certificate":
                extractContent(ye, dir, f.getName().replace(".yaml", ".pem"), "key", true);
                extractContent(ye, dir, f.getName().replace(".yaml", ".crt"), "content", true);
                break;
            case "ResourceBlob":
                String type = ye.fields.get("type");
                switch (type) {
                    case "schema":
                        type = "xsd";
                        break;
                }

                extractContent(ye, dir, ye.fields.get("ID") + "." + type, "content", true);
                break;
        }
    }

    private void extractContent(com.axway.gw.es.model.entity.Entity ye, File dir, String fileName, String field, boolean base64Decode) throws IOException {
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String content = ye.fields.remove(field);
        if (content == null) {
            return;
        }
        byte[] data;
        if (base64Decode) {
            data = Base64.getDecoder().decode(content.replaceAll("[\r?\n]", ""));
        } else {
            data = content.getBytes();
        }
        Files.write(dir.toPath().resolve(fileName), data);
        ye.fields.put(field, "file:" + fileName);
    }

    private void setFields(Entity value, com.axway.gw.es.model.entity.Entity ye) {
        com.vordel.es.Field[] allFields = value.getAllFields();
        List<com.vordel.es.Field> refs = value.getReferenceFields();
        for (com.vordel.es.Field f : allFields) {
            com.vordel.es.FieldType ft = f.getType();
            if (!refs.contains(f)) { // not a reference
                String fval = value.getStringValue(f.getName());
                if (!types.get(ye.meta.type).isDefaultValue(f.getName(), fval)) {
                    ye.addFval(f.getName(), fval);
                }
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
        LOGGER.debug("path to root is depth: " + path.size());
        for (Entity entity : path) {
            String name = getKeyValues(entity);
            if (builder.length() == 0) {
                builder.append(getTopLevel(entity.getType()));
            }
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
        return sanitizeFilename(b.toString());
    }

    /**
     * replace illegal characters in a filename with "_"
     * illegal characters :
     * : \ / * ? | < > "
     */
    private String sanitizeFilename(String name) {
        if (name == null)
            LOGGER.info("hello");
        return name.trim().replaceAll("[\\/:\"*?<>|]+", "_");
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
        com.axway.gw.es.model.entity.Entity ye = yamlize(e, true);
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
                LOGGER.trace("ESPK not found in current ES: " + pk);
            }
        }
        return path;
    }
}
