package com.axway.gw.es.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.LinkedHashMap;
import java.util.Map;

public class Entity {

    @JsonIgnore
    public String key;
    //public String parent;
    public String name;

    public Meta meta = new Meta();
    public Map<String, String> fields;
    public Routing routing;
    public Logging logging;

    public Map<String, Entity> children;

    @JsonIgnore
    public boolean allowsChildren = false;
    @JsonIgnore
    public String keyFieldValues;


    public void addFval(String key, String value) {
        switch (key) {
            case "name":
                name = value;
                break;
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



}
