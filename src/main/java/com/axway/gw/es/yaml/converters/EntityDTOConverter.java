package com.axway.gw.es.yaml.converters;


import com.axway.gw.es.yaml.YamlEntityStore;
import com.axway.gw.es.yaml.dto.entity.EntityDTO;
import com.axway.gw.es.yaml.dto.type.TypeDTO;
import com.vordel.es.*;
import com.vordel.es.impl.ConstantField;
import com.vordel.es.xes.PortableESPK;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.base.Preconditions.checkNotNull;

public class EntityDTOConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(EntityDTOConverter.class);

    public static final String POLICIES = "Policies";
    public static final String APIS = "APIs";
    public static final String RESOURCES = "Resources";
    public static final String ENVIRONMENT_CONFIGURATION = "Environment Configuration";
    public static final String LIBRARIES = "Libraries";
    public static final String EXTERNAL_CONNECTIONS = "External Connections";
    public static final String SERVER_SETTINGS = "Server Settings";


    public static final String YAML_EXTENSION = ".yaml";

    private static final Map<String, String> ENTITIES_CATEGORIES = new HashMap<>();
    public static final String DEFAULT_CATEGORY = "System";
    public static final String METADATA_FILENAME = "metadata.yaml";

    static {
        ENTITIES_CATEGORIES.put("FilterCircuit", POLICIES);
        ENTITIES_CATEGORIES.put("CircuitContainer", POLICIES);
        ENTITIES_CATEGORIES.put("WebServiceGroup", APIS);
        ENTITIES_CATEGORIES.put("WebServiceRepository", APIS);

        ENTITIES_CATEGORIES.put("JSONSchemaGroup", RESOURCES);
        ENTITIES_CATEGORIES.put("ScriptGroup", RESOURCES);
        ENTITIES_CATEGORIES.put("StylesheetGroup", RESOURCES);
        ENTITIES_CATEGORIES.put("XmlSchemaGroup", RESOURCES);
        ENTITIES_CATEGORIES.put("XPathGroup", RESOURCES);
        ENTITIES_CATEGORIES.put("ResourceRepository", RESOURCES);

        ENTITIES_CATEGORIES.put("KPSRoot", ENVIRONMENT_CONFIGURATION);
        ENTITIES_CATEGORIES.put("EnvironmentalizedEntities", ENVIRONMENT_CONFIGURATION);
        ENTITIES_CATEGORIES.put("Certificates", ENVIRONMENT_CONFIGURATION);
        ENTITIES_CATEGORIES.put("PGPKeyPairs", ENVIRONMENT_CONFIGURATION);
        ENTITIES_CATEGORIES.put("NetService", ENVIRONMENT_CONFIGURATION);
        ENTITIES_CATEGORIES.put("UserStore", ENVIRONMENT_CONFIGURATION);

        ENTITIES_CATEGORIES.put("CacheManager", LIBRARIES);
        ENTITIES_CATEGORIES.put("AlertManager", LIBRARIES);
        ENTITIES_CATEGORIES.put("RegularExpressionGroup", LIBRARIES);
        ENTITIES_CATEGORIES.put("OAuth2StoresGroup", LIBRARIES);
        ENTITIES_CATEGORIES.put("CronExpressionGroup", LIBRARIES);
        ENTITIES_CATEGORIES.put("CORSGroup", LIBRARIES);
        ENTITIES_CATEGORIES.put("WAFProfileGroup", LIBRARIES);

        ENTITIES_CATEGORIES.put("AWSSettings", EXTERNAL_CONNECTIONS);
        ENTITIES_CATEGORIES.put("AuthnRepositoryGroup", EXTERNAL_CONNECTIONS);
        ENTITIES_CATEGORIES.put("ConnectionSetGroup", EXTERNAL_CONNECTIONS);
        ENTITIES_CATEGORIES.put("AuthProfilesGroup", EXTERNAL_CONNECTIONS);
        ENTITIES_CATEGORIES.put("DbConnectionGroup", EXTERNAL_CONNECTIONS);
        ENTITIES_CATEGORIES.put("ICAPServerGroup", EXTERNAL_CONNECTIONS);
        ENTITIES_CATEGORIES.put("JMSServiceGroup", EXTERNAL_CONNECTIONS);
        ENTITIES_CATEGORIES.put("LdapDirectoryGroup", EXTERNAL_CONNECTIONS);
        ENTITIES_CATEGORIES.put("ProxyServerGroup", EXTERNAL_CONNECTIONS);
        ENTITIES_CATEGORIES.put("SentinelServerGroup", EXTERNAL_CONNECTIONS);
        ENTITIES_CATEGORIES.put("SiteMinderConnectionSet", EXTERNAL_CONNECTIONS);
        ENTITIES_CATEGORIES.put("SMTPServerGroup", EXTERNAL_CONNECTIONS);
        ENTITIES_CATEGORIES.put("SyslogServerGroup", EXTERNAL_CONNECTIONS);
        ENTITIES_CATEGORIES.put("RendezvousDaemonGroup", EXTERNAL_CONNECTIONS);
        ENTITIES_CATEGORIES.put("UrlSetGroup", EXTERNAL_CONNECTIONS);
        ENTITIES_CATEGORIES.put("XkmsConfigGroup", EXTERNAL_CONNECTIONS);
        ENTITIES_CATEGORIES.put("RadiusClients", EXTERNAL_CONNECTIONS);
        ENTITIES_CATEGORIES.put("SunAccessManagerSettings", EXTERNAL_CONNECTIONS);
        ENTITIES_CATEGORIES.put("PassPortKeyStoreManager", EXTERNAL_CONNECTIONS);
        ENTITIES_CATEGORIES.put("TivoliActionGroupGroup", EXTERNAL_CONNECTIONS);
        ENTITIES_CATEGORIES.put("TivoliSettingsGroup", EXTERNAL_CONNECTIONS);

        ENTITIES_CATEGORIES.put("AccessLogger", SERVER_SETTINGS);
        ENTITIES_CATEGORIES.put("CassandraSettings", SERVER_SETTINGS);
        ENTITIES_CATEGORIES.put("EventLog", SERVER_SETTINGS);
        ENTITIES_CATEGORIES.put("LogManager", SERVER_SETTINGS);
        ENTITIES_CATEGORIES.put("RealtimeMonitoring", SERVER_SETTINGS);
        ENTITIES_CATEGORIES.put("PortalCallbackGroup", SERVER_SETTINGS);
        ENTITIES_CATEGORIES.put("MimeTypeGroup", SERVER_SETTINGS);
        ENTITIES_CATEGORIES.put("OPDbConfig", SERVER_SETTINGS);
        ENTITIES_CATEGORIES.put("SystemSettings", SERVER_SETTINGS);
        ENTITIES_CATEGORIES.put("OpenTrafficEventLog", SERVER_SETTINGS);
        ENTITIES_CATEGORIES.put("PortalConfiguration", SERVER_SETTINGS);
        ENTITIES_CATEGORIES.put("ZeroDowntime", SERVER_SETTINGS);
    }

    private static final Set<String> EXPANDED_TYPES = new HashSet<>(Arrays.asList("KPSRoot"));
    private static final Set<String> INLINED_TYPES = new HashSet<>(Arrays.asList(
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


    private final Map<ESPK, Entity> entities = new ConcurrentHashMap<>();
    private final List<EntityDTO> mappedEntities = new ArrayList<>();
    private final Set<ESPK> inlined = new HashSet<>();
    private final EntityStore sourceES;
    private final Map<String, TypeDTO> types;

    public EntityDTOConverter(EntityStore sourceES, Map<String, TypeDTO> types) {
        this.sourceES = sourceES;
        this.types = types;
    }

    public void loadAndMapAll() {
        ESPK root = sourceES.getRootPK();
        mapAll(root);
    }

    public void mapAll(ESPK pk) {
        mapToDTO(pk);
        Collection<ESPK> children = sourceES.listChildren(pk, null);
        for (ESPK childPK : children)
            mapAll(childPK);
    }

    private void mapToDTO(ESPK pk) {
        if (inlined.contains(pk)) {
            return;
        }
        Entity entity = getEntity(pk);
        EntityDTO entityDTO = mapToDTO(entity, true);
        mappedEntities.add(entityDTO);
    }

    private EntityDTO mapToDTO(Entity entity, boolean allowChildren) {

        LOGGER.debug("Yamlize {}", entity.getPK());

        final String entityName = entity.getType().getName();

        EntityDTO entityDTO = new EntityDTO();
        entityDTO.getMeta().setType(entityName); // may want to change this to the directory location of the type?
        entityDTO.getMeta().setTypeDTO(types.get(entityDTO.getMeta().getType()));
        // deal with pk for this entity
        entityDTO.setKey(getPath(entity.getPK()));
        // children?
        entityDTO.setAllowsChildren(allowChildren && isAllowsChildren(entity));
        // fields
        setFields(entity, entityDTO);
        setReferences(entity, entityDTO);

        if (!entityDTO.isAllowsChildren()) {
            for (ESPK childPK : sourceES.listChildren(entity.getPK(), null)) {
                inlined.add(childPK);
                Entity child = getEntity(childPK);
                EntityDTO childEntityDTO = mapToDTO(child, false);
                entityDTO.addChild(childEntityDTO);
                childEntityDTO.setKey(null);
            }
        }

        return entityDTO;
    }

    private void setFields(Entity entity, EntityDTO entityDTO) {
        Field[] allFields = entity.getAllFields();
        List<Field> refs = entity.getReferenceFields();
        for (Field field : allFields) {
            if (!refs.contains(field)) { // not a reference
                final String fieldName = field.getName();
                String fieldValue = entity.getStringValue(fieldName);
                if (field instanceof ConstantField) {
                    if (!isDefaultValue((ConstantField) field, fieldValue)) {
                        entityDTO.addFieldValue(fieldName, fieldValue);
                    }
                } else if (!types.get(entityDTO.getMeta().getType()).isDefaultValue(fieldName, fieldValue)) {
                    entityDTO.addFieldValue(fieldName, fieldValue);
                }
            }
        }
    }

    private boolean isDefaultValue(ConstantField field, String fieldValue) {
        final String defaultValue = field.getType().getDefault();
        boolean isDefaultValue = Objects.equals(fieldValue, defaultValue);
        if (FieldType.BOOLEAN.equals(field.getType().getType())) {
            isDefaultValue = Objects.equals(FieldType.getBooleanValue(defaultValue), FieldType.getBooleanValue(fieldValue));
        }
        return isDefaultValue;
    }

    public Entity getEntity(ESPK key) {
        if (entities.containsKey(key))
            return entities.get(key);
        ESPK espk = EntityStoreDelegate.getEntityForKey(sourceES, key);
        Entity e = sourceES.getEntity(espk);
        entities.put(key, e);
        return e;
    }

    public List<Entity> pathToRoot(ESPK pk) {
        List<Entity> path = new ArrayList<>();
        if (pk == null)
            return path;
        while (!pk.equals(sourceES.getRootPK())) {
            try {
                Entity e = getEntity(pk);
                path.add(0, e);
                pk = e.getParentPK();
            } catch (EntityStoreException p) {
                LOGGER.trace("ESPK not found in current ES: {}", pk);
            }
        }
        return path;
    }


    private String getTopLevel(EntityType type) {
        String topLevel = ENTITIES_CATEGORIES.get(type.getName());
        return topLevel == null ? DEFAULT_CATEGORY : topLevel;
    }

    private boolean isAllowsChildren(Entity value) {
        return value.getType().allowsChildEntities()
                && !INLINED_TYPES.contains(value.getType().getName())
                &&
                (value.getType().extendsType("NamedLoadableModule")
                        || EXPANDED_TYPES.contains(value.getType().getName())
                        || !value.getType().extendsType("NamedGroup")
                        && !value.getType().extendsType("LoadableModule")
                        && !value.getType().extendsType("Filter")
                        && !value.getType().extendsType("KPSStore")
                        && !value.getType().extendsType("KPSType")
                );
    }


    private void setReferences(Entity value, EntityDTO ye) {
        List<Field> refs = value.getReferenceFields();
        for (Field field : refs) {
            ESPK ref = field.getReference(); // just deal with single at the moment
            if (!ref.equals(EntityStore.ES_NULL_PK)) {
                String key;
                if (isLateBoundReference(ref)) {
                    key = ref.toString();
                } else {
                    key = getPath(ref);
                    if (key.startsWith(ye.getKey())) {
                        key = key.substring(ye.getKey().length() + 1);
                    }
                }
                ye.addFieldValue(field.getName(), key);
            }
        }
    }

    private boolean isLateBoundReference(final ESPK pk) {
        if (pk instanceof PortableESPK) {
            final PortableESPK portableKey = (PortableESPK) pk;
            final boolean isLateBound = "_lateBoundReference".equals(portableKey.getTypeNameOfReferencedEntity());
            if (isLateBound) {
                LOGGER.info("Late bound reference to {}, expected at run-time", LOGGER.isInfoEnabled() ? portableKey.terse() : "");
            }
            return isLateBound;
        }

        return false;
    }

    private String getPath(ESPK pk) {
        StringBuilder builder = new StringBuilder();

        List<Entity> path = pathToRoot(pk);
        LOGGER.debug("path to root is depth: {}", path.size());
        for (Entity entity : path) {
            String name = getKeyValues(entity);
            if (builder.length() == 0) {
                builder.append(getTopLevel(entity.getType()));
            }
            if (builder.length() > 0) {
                builder.append('/');
            }
            builder.append(name);

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
        return sanitize(b.toString());
    }


    public void writeEntities(File dir) throws IOException {
        createDirectoryIfNeeded(dir);

        // must provide dir (this happens when a file already exists)
        if (!dir.isDirectory())
            throw new IOException("Must provide a directory for YAML output");

        for (EntityDTO ye : mappedEntities) {
            // deal with pk for parent  entity
            dumpAsYaml(new File(dir, ye.getKey()), ye);
        }
    }

    private void dumpAsYaml(File out, EntityDTO entityDTO) throws IOException {

        if (entityDTO.isAllowsChildren()) { // handle as directory with metadata
            createDirectoryIfNeeded(out);
            YamlEntityStore.YAML_MAPPER.writeValue(new File(out, METADATA_FILENAME), entityDTO);
        } else { // handle as file
            String filename = out.getPath() + YAML_EXTENSION;
            File f = new File(filename);
            createDirectoryIfNeeded(f.getParentFile());

            extractContent(entityDTO, f);

            YamlEntityStore.YAML_MAPPER.writeValue(f, entityDTO);
        }

    }

    private void extractContent(EntityDTO entityDTO, File file) throws IOException {
        if (entityDTO.getChildren() != null) {
            for (EntityDTO yChild : entityDTO.getChildren().values()) {
                extractContent(yChild, file);
            }
        }

        File dir = file.getParentFile();
        final String metaType = entityDTO.getMeta().getType();

        switch (metaType) {
            case "JavaScriptFilter":
                String fileName = file.getName().replace(YAML_EXTENSION, "-Scripts/") + sanitize(entityDTO.getKeyDescription()) + "." + entityDTO.getFieldValue("engineName");
                writeContentToFile(entityDTO, dir, fileName, "script", false);
                break;
            case "Script":
                writeContentToFile(entityDTO, dir, entityDTO.getKeyDescription() + "." + entityDTO.getFields().get("engineName"), "script", false);
                break;
            case "Stylesheet":
                writeContentToFile(entityDTO, dir, entityDTO.getFields().get("URL") + ".xsl", "contents", true);
                break;
            case "Certificate":
                writeContentToFile(entityDTO, dir, file.getName().replace(YAML_EXTENSION, ".pem"), "key", true);
                writeContentToFile(entityDTO, dir, file.getName().replace(YAML_EXTENSION, ".crt"), "content", true);
                break;
            case "ResourceBlob":
                String type = entityDTO.getFields().get("type");
                if (Objects.equals(type, "schema")) {
                    type = "xsd";
                }
                writeContentToFile(entityDTO, dir, entityDTO.getFields().get("ID") + "." + type, "content", true);
                break;
            default:
                LOGGER.debug("Nothing to extract from type {}", metaType);
        }
    }

    private void writeContentToFile(EntityDTO entityDTO, File dir, String fileName, String field, boolean base64Decode) throws IOException {
        String content = entityDTO.getFields().remove(field);
        if (content == null) {
            return;
        }

        byte[] data;
        if (base64Decode) {
            data = Base64.getDecoder().decode(content.replaceAll("[\r?\n]", ""));
        } else {
            data = content.getBytes();
        }

        Path path = dir.toPath().resolve(fileName);
        File parentDir = path.getParent().toFile();
        createDirectoryIfNeeded(parentDir);
        Files.write(path, data);

        entityDTO.getFields().put(field + "#ref" + (base64Decode ? "base64" : ""), fileName);
    }

    private static void createDirectoryIfNeeded(File directory) throws IOException {
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Could not create directory:" + directory);
        }
    }

    /**
     * replace illegal characters in a filename with "_"
     * illegal characters :
     * : \ / * ? | < > "
     */
    private String sanitize(String name) {
        checkNotNull(name);
        return name.trim().replaceAll("[\\/:\"*?<>|]+", "_");
    }


}
