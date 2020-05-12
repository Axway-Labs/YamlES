package com.axway.gw.es.yaml;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vordel.es.Entity;
import com.vordel.es.EntityStoreException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class YamlEntityDTOStoreTest {

    // At this place, also the META-INF/Types.yaml is expected
    private static final String testPackage = "/com/axway/gw/es/yaml/";

    private YamlEntityStore es;

    @BeforeEach
    void setupEntityStoreToTest() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, EntityStoreException, IOException {

        es = new YamlEntityStore();
        es.setRootLocation(new File(YamlEntityStore.class.getResource(testPackage).getPath()));
        // Entity need to load types to be able to create an Entity
        es.loadTypes();
    }

    @Test
    public void loadSinglePolicy() throws IOException {
        File yamlFile = getFileFromClasspath("policies/APIManagerProtectionPolicy.yaml");
        Entity e = es.createEntity(yamlFile, null);
        assertEquals("Disable Monitoring", e.getField("start").getValueList().get(0).toString());
    }

    @Test
    public void loadPolicyWithShortCuts() throws IOException {
        File yamlFile = getFileFromClasspath("policies/oauth20/AccessTokenService.yaml");
        Entity e = es.createEntity(yamlFile, null);
        assertEquals("Decide what grant type to use", e.getField("start").getValueList().get(0).toString());
    }

    @Test
    public void loadClientCredentials() throws IOException {
        File yamlFile = getFileFromClasspath("policies/oauth20/Client Credentials.yaml");
        Entity e = es.createEntity(yamlFile, null);
        assertEquals("Access Token using client credentials", e.getField("start").getValueList().get(0).toString());
    }


    private File getFileFromClasspath(String filename) {
        URL url = YamlEntityDTOStoreTest.class.getResource(testPackage + filename);
        assertNotNull(url, "Test file: " + filename + " doesn't exists.");
        File file = new File(url.getPath().replaceAll("%20", " "));
        Assertions.assertTrue(file.exists(), "Test file: " + file + " doesn't exists.");
        return file;
    }
}
