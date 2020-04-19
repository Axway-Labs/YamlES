package com.axway.gw.es.yaml;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vordel.es.Entity;

public class YamlEntityStoreTest {
	
	private final String testPackage = "/com/axway/gw/es/yaml/"; 
	
	@Test
	public void createPolicyEntityTest() throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field rootLocation = YamlEntityStore.class.getDeclaredField("rootLocation");
		rootLocation.setAccessible(true);
		
		File yamlFile = new File(YamlEntityStoreTest.class.getResource(testPackage + "APIManagerProtectionPolicy.yaml").getFile());
		YamlEntityStore es = new YamlEntityStore();
		rootLocation.set(es, yamlFile.getParentFile());
		// Entity need to load types to be able to create an Entity
		es.loadTypes();
		Entity e =  es.createEntity(yamlFile, null);
		Assertions.assertEquals(e.getField("start").getValueList().get(0).toString(), "Disable Monitoring");
	}
}
