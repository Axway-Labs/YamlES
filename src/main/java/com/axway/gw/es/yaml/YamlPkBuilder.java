package com.axway.gw.es.yaml;

import com.vordel.es.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.axway.gw.es.yaml.util.NameUtils.sanitize;

public class YamlPkBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(YamlPkBuilder.class);
    public static final String DEFAULT_CATEGORY = "System";
    public static final String POLICIES = "Policies";
    public static final String APIS = "APIs";
    public static final String RESOURCES = "Resources";
    public static final String ENVIRONMENT_CONFIGURATION = "Environment Configuration";
    public static final String LIBRARIES = "Libraries";
    public static final String EXTERNAL_CONNECTIONS = "External Connections";
    public static final String SERVER_SETTINGS = "Server Settings";

    private static final Map<String, String> ENTITIES_CATEGORIES = new HashMap<>();
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
    // TODO put all of the above in a file in src/main/resources

    private final EntityStore entityStore;


    public YamlPkBuilder(EntityStore entityStore) {
        this.entityStore = entityStore;
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
                builder.append(getTopLevel(entity.getType().getName()));
            }
            if (builder.length() > 0) {
                builder.append('/');
            }
            String name = getKeyValues(entity);
            builder.append(name);

        }
        return builder.toString();
    }

    private List<Entity> pathToRoot(Entity childEntity) {
        List<Entity> path = new ArrayList<>();
        if(childEntity == null) return path;
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


    private String getTopLevel(String typeName) {
        if(typeName.equals("Root")) {
            return "";
        }
        String topLevel = ENTITIES_CATEGORIES.get(typeName);
        return topLevel == null ? DEFAULT_CATEGORY : topLevel;
    }

    public String getKeyValues(Entity entity) {
        String[] keyNames = entity.getType().getKeyFieldNames();
        if (keyNames == null) {
            throw new IllegalArgumentException("No key names for type " + entity.getType());
        }

        // TODO Change for regular loop + join with '$'
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < keyNames.length; i++) {
            Field keyField = entity.getField(keyNames[i]);
            if (null != keyField) {
                Value keyValue = keyField.getValues()[0];
                if (keyValue.getRef() == null) {
                    b.append(keyValue.getData());
                } else {
                    b.append(buildKeyValue(keyValue.getRef()));
                }
            }
            if (i < keyNames.length - 1)
                b.append("$");
        }
        return sanitize(b.toString());
    }


    public Entity getEntity(ESPK key) {
        final ESPK espk = EntityStoreDelegate.getEntityForKey(entityStore, key);
        return entityStore.getEntity(espk);
    }
}
