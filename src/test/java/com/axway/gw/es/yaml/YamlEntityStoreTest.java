package com.axway.gw.es.yaml;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.vordel.es.Entity;
import com.vordel.es.EntityStoreException;

public class YamlEntityStoreTest {
	
	// At this place, also the META-INF/Types.yaml is expected
	private static final String testPackage = "/com/axway/gw/es/yaml/";
	
	private static YamlEntityStore es;
	
	@BeforeAll
	static void setupEntityStoreToTest() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, EntityStoreException, IOException {
		Field rootLocation = YamlEntityStore.class.getDeclaredField("rootLocation");
		rootLocation.setAccessible(true);

		es = new YamlEntityStore();
		rootLocation.set(es, new File(YamlEntityStore.class.getResource(testPackage).getPath()));
		// Entity need to load types to be able to create an Entity
		es.loadTypes();
	}
	
	@Test
	public void loadSinglePolicy() throws IOException {
		File yamlFile = getFileFromClasspath("policies/API Manager Protection Policy.yaml");
		Entity e =  es.createEntity(yamlFile, null);
		Assertions.assertEquals(e.getField("start").getValueList().get(0).toString(), "Disable Monitoring");
	}
	
	@Test
	public void loadPolicyWithShortCuts() throws IOException {
		File yamlFile = getFileFromClasspath("policies/oauth20/Access Token Service.yaml");
		Entity e =  es.createEntity(yamlFile, null);
		Assertions.assertEquals(e.getField("start").getValueList().get(0).toString(), "Decide what grant type to use");
	}
	
	
	private File getFileFromClasspath(String filename) {
		URL url = YamlEntityStoreTest.class.getResource(testPackage + filename);
		Assertions.assertTrue(url != null, "Test file: "+filename+" doesn't exists.");
		File file = new File(url.getPath().replaceAll("%20", " "));
		Assertions.assertTrue(file.exists(), "Test file: "+file+" doesn't exists.");
		return file;
	}
}
