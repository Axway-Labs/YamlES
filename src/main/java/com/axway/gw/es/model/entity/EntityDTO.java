package com.axway.gw.es.model.entity;

import com.axway.gw.es.model.type.FieldDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class EntityDTO {

    @JsonIgnore
    private String key;

    private final MetaDTO meta = new MetaDTO();
    private Map<String, String> fields;
    private RoutingDTO routing;
    private LoggingDTO logging;

    private Map<String, EntityDTO> children;

    @JsonIgnore
    private boolean allowsChildren = false;


    public void addFval(String key, String value) {
        switch (key) {
            case "class":
                // skip meta._class = value;
                break;
            case "_version":
                meta.setVersion(value);
                break;
            case "logMaskType":
                initLogging();
                logging.setLogMaskType(value);
                break;
            case "logMask":
                initLogging();
                logging.setLogMask(value);
                break;
            case "logFatal":
                initLogging();
                logging.setLogFatal(value);
                break;
            case "logFailure":
                initLogging();
                logging.setLogFailure(value);
                break;
            case "logSuccess":
                initLogging();
                logging.setLogSuccess(value);
                break;
            case "abortProcessingOnLogError":
                initLogging();
                logging.setAbortProcessingOnLogError(value);
                break;
            case "category":
                initLogging();
                logging.setCategory(value);
                break;
            case "successNode":
                initRouting();
                routing.setSuccessNode(value);
                break;
            case "failureNode":
                initRouting();
                routing.setFailureNode(value);
                break;
            default:
                if (fields == null) {
                    fields = new LinkedHashMap<>();
                }
                String previous = fields.put(key, value);
                if (previous != null) {
                    throw new IllegalArgumentException("Duplicate values for " + key);
                }
        }
    }

    private void initRouting() {
        if (routing == null) {
            routing = new RoutingDTO();
        }
    }

    private void initLogging() {
        if (logging == null) {
            logging = new LoggingDTO();
        }
    }

    public void addChild(EntityDTO child) {
        if (children == null) {
            children = new LinkedHashMap<>();
        }
        children.put(child.key.substring(key.length() + 1), child);
    }


    @JsonIgnore
    public String getKeyDescription() {
        String keyDescription = meta.getTypeDTO().getFields().values()
                .stream()
                .filter(FieldDTO::isKeyField)
                .map(keyField -> getFieldValue(keyField.getName()))
                .collect(Collectors.joining("$"));
        return keyDescription.isEmpty() ? meta.getType() : keyDescription;
    }

    @JsonIgnore
    public String getFieldValue(String fieldName) {
        if (fields != null) {
            String value = fields.get(fieldName);
            if (value != null) {
                return value;
            }
        }
        return meta.getTypeDTO().getDefaultValue(fieldName);
    }

    public EntityDTO setKey(String key) {
        this.key = key;
        return this;
    }

    public String getKey() {
        return key;
    }

    public MetaDTO getMeta() {
        return meta;
    }

    public Map<String, String> getFields() {
        return fields;
    }

    public EntityDTO setFields(Map<String, String> fields) {
        this.fields = fields;
        return this;
    }

    public RoutingDTO getRouting() {
        return routing;
    }

    public EntityDTO setRouting(RoutingDTO routing) {
        this.routing = routing;
        return this;
    }

    public LoggingDTO getLogging() {
        return logging;
    }

    public EntityDTO setLogging(LoggingDTO logging) {
        this.logging = logging;
        return this;
    }

    public Map<String, EntityDTO> getChildren() {
        return children;
    }

    public EntityDTO setChildren(Map<String, EntityDTO> children) {
        this.children = children;
        return this;
    }

    public boolean isAllowsChildren() {
        return allowsChildren;
    }

    public EntityDTO setAllowsChildren(boolean allowsChildren) {
        this.allowsChildren = allowsChildren;
        return this;
    }
}


