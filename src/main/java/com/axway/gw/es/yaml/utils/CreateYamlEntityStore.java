package com.axway.gw.es.yaml.utils;

import com.axway.gw.es.yaml.YamlEntityStore;

import java.util.Properties;

public class CreateYamlEntityStore {


    private static final String WHERE_TO_LOAD = "yaml:file:/tmp/yamlstores/FactoryTemplateSamples";

    public static void main(String[] args) {

        YamlEntityStore store = new YamlEntityStore();
        store.connect(WHERE_TO_LOAD, new Properties());

    }

}
