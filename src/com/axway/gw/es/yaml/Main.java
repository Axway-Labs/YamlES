package com.axway.gw.es.yaml;

import java.util.Properties;

import com.vordel.es.EntityStoreFactory;

public class Main {
	
	private static final String WHERE_TO_LOAD = "yaml:file:/C:/vordel/es";
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
