package com.axway.gw.es.yaml.dto.entity;

import com.axway.gw.es.yaml.dto.type.ValueDTO;
import com.axway.gw.es.yaml.util.NameUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.axway.gw.es.yaml.YamlConstantFieldsNames.*;

public class EntityDTO {

    @JsonIgnore
    private String key;

    @JsonIgnore
    private String sourceKey;

    private String name;

    private final MetaDTO meta = new MetaDTO();
    private Map<String, String> fields;
    private RoutingDTO routing;
    private LoggingDTO logging;

    private Map<String, EntityDTO> children;

    @JsonIgnore
    private boolean inSeparatedFile = false;

    public void addFieldValue(String key, String value) {
        switch (key) {
            case CLASS_FIELD_NAME:
                break;
            case VERSION_FIELD_NAME:
                meta.setVersion(value);
                break;
            case NAME_FIELD_NAME:
                name = value;
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
                initFields();
                String previous = fields.put(key, value);
                if (previous != null) {
                    throw new IllegalArgumentException("Duplicate values for " + key);
                }
        }
    }


    public Map<String, String> retrieveAllFields() {

        Map<String, String> allFields = new LinkedHashMap<>();

        if (fields != null) {
            allFields.putAll(fields);
        }

        putNonNull(allFields, NAME_FIELD_NAME, name);
        putNonNull(allFields, VERSION_FIELD_NAME, getMeta().getVersion());

        if (logging != null) {
            putNonNull(allFields, LoggingDTO.FIELD_CATEGORY, logging.getCategory());
            putNonNull(allFields, LoggingDTO.FIELD_LOG_FAILURE, logging.getLogFailure());
            putNonNull(allFields, LoggingDTO.FIELD_LOG_SUCCESS, logging.getLogSuccess());
            putNonNull(allFields, LoggingDTO.FIELD_LOG_FATAL, logging.getLogFatal());
            putNonNull(allFields, LoggingDTO.FIELD_LOG_MASK, logging.getLogMask());
            putNonNull(allFields, LoggingDTO.FIELD_LOG_MASK_TYPE, logging.getLogMaskType());
            putNonNull(allFields, LoggingDTO.FIELD_ABORT_PROCESSING_ON_LOG_ERROR, logging.getAbortProcessingOnLogError());
        }

        if (routing != null) {
            putNonNull(allFields, RoutingDTO.FAILURE_NODE, routing.getFailureNode());
            putNonNull(allFields, RoutingDTO.SUCCESS_NODE, routing.getSuccessNode());
        }

        return allFields;
    }

    public void addChild(EntityDTO child) {
        initChildren();
        children.put(NameUtils.toRelativeRef(child.key, this), child);
    }

    private void putNonNull(Map<String, String> map, String name, String value) {
        if (value != null) {
            map.put(name, value);
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

    private void initChildren() {
        if (children == null) {
            children = new LinkedHashMap<>();
        }
    }

    private void initFields() {
        if (fields == null) {
            fields = new LinkedHashMap<>();
        }
    }


    public String buildKeyValue() {
        String keyDescription = meta.getTypeDTO().getKeyFields()
                .stream()
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
        final ValueDTO defaultValue = meta.getTypeDTO().getDefaultValue(fieldName);
        if (defaultValue.getRef() != null) {
            return defaultValue.getRef();
        } else {
            return defaultValue.getData();
        }
    }


    public EntityDTO setKey(String key) {
        this.key = key;
        return this;
    }

    public String getKey() {
        return key;
    }


    public String getSourceKey() {
        return sourceKey;
    }

    public EntityDTO setSourceKey(String sourceKey) {
        this.sourceKey = sourceKey;
        return this;
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

    public boolean isInSeparatedFile() {
        return inSeparatedFile;
    }

    public EntityDTO setInSeparatedFile(boolean inSeparatedFile) {
        this.inSeparatedFile = inSeparatedFile;
        return this;
    }

    public String getName() {
        return name;
    }

    public EntityDTO setName(String name) {
        this.name = name;
        return this;
    }
}




