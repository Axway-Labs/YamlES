package com.axway.gw.es.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public class Entity {

    @JsonIgnore
    public String key;

    public Meta meta = new Meta();
    public Map<String, String> fields;
    public Routing routing;
    public Logging logging;

    public Map<String, Entity> children;

    @JsonIgnore
    public boolean allowsChildren = false;


    public void addFval(String key, String value) {
        switch (key) {
            case "class":
                // skip meta._class = value;
                break;
            case "_version":
                meta._version = value;
                break;
            case "logMaskType":
                initLogging();
                logging.logMaskType = value;
                break;
            case "logMask":
                initLogging();
                logging.logMask = value;
                break;
            case "logFatal":
                initLogging();
                logging.logFatal = value;
                break;
            case "logFailure":
                initLogging();
                logging.logFailure = value;
                break;
            case "logSuccess":
                initLogging();
                logging.logSuccess = value;
                break;
            case "abortProcessingOnLogError":
                initLogging();
                logging.abortProcessingOnLogError = value;
                break;
            case "category":
                initLogging();
                logging.category = value;
                break;
            case "successNode":
                initRouting();
                routing.successNode = value;
                break;
            case "failureNode":
                initRouting();
                routing.failureNode = value;
                break;
            default:
                if (fields == null) {
                    fields = new TreeMap<>();
                }
                String previous = fields.put(key, value);
                if (previous != null) {
                    throw new IllegalArgumentException("Duplicate values for " + key);
                }
        }
    }

    private void initRouting() {
        if (routing == null) {
            routing = new Routing();
        }
    }

    private void initLogging() {
        if (logging == null) {
            logging = new Logging();
        }
    }

    public void addChild(Entity ychild) {
        if (children == null) {
            children = new LinkedHashMap<>();
        }
        children.put(ychild.key.substring(key.length() + 1), ychild);
    }


    @JsonIgnore
    public String getKeyDescription() {
        // TODO generate something with key values if name is not the default
        String name = "[?]";
        if (fields != null) {
            name = fields.get("name");
        }
        return name;
    }



}
