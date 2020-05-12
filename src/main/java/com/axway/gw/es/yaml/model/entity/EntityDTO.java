package com.axway.gw.es.yaml.model.entity;

import com.axway.gw.es.yaml.model.type.FieldDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.axway.gw.es.yaml.YamlConstantFieldsNames.CLASS;
import static com.axway.gw.es.yaml.YamlConstantFieldsNames.VERSION;

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


    public void addFieldValue(String key, String value) {
        switch (key) {
            case CLASS:
                break;
            case VERSION:
                meta.setVersion(value);
                break;
            case LoggingDTO.FIELD_LOG_MASK_TYPE:
                initLogging();
                logging.setLogMaskType(value);
                break;
            case LoggingDTO.FIELD_LOG_MASK:
                initLogging();
                logging.setLogMask(value);
                break;
            case LoggingDTO.FIELD_LOG_FATAL:
                initLogging();
                logging.setLogFatal(value);
                break;
            case LoggingDTO.FIELD_LOG_FAILURE:
                initLogging();
                logging.setLogFailure(value);
                break;
            case LoggingDTO.FIELD_LOG_SUCCESS:
                initLogging();
                logging.setLogSuccess(value);
                break;
            case LoggingDTO.FIELD_ABORT_PROCESSING_ON_LOG_ERROR:
                initLogging();
                logging.setAbortProcessingOnLogError(value);
                break;
            case LoggingDTO.FIELD_CATEGORY:
                initLogging();
                logging.setCategory(value);
                break;
            case RoutingDTO.SUCCESS_NODE:
                initRouting();
                routing.setSuccessNode(value);
                break;
            case RoutingDTO.FAILURE_NODE:
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


