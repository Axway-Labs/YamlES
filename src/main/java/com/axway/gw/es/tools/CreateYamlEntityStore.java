package com.axway.gw.es.tools;

import com.axway.gw.es.yaml.YamlEntityStore;

import java.util.Properties;

public class CreateYamlEntityStore {
	
	private static final String WHERE_TO_LOAD = "yaml:file:/tmp/yamlstores/TeamDevelopmentAPI";
	//private static final String WHERE_TO_LOAD = "yaml:file:/home/jamie/hackathon-dc/fed";
	public static void main(String[] args) {
		try {
			YamlEntityStore store = new YamlEntityStore();
			store.connect(WHERE_TO_LOAD, new Properties());
		}
		catch (Throwable t) {
			t.printStackTrace();
		}
	}

}
