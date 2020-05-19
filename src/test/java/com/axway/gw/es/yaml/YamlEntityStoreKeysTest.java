package com.axway.gw.es.yaml;

import com.vordel.es.ESPK;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class YamlEntityStoreKeysTest {
    // At this place, also the META-INF/Types.yaml is expected
    private static final String testPackage = "/com/axway/gw/es/yaml/keys/";

    private YamlEntityStore yamlEntityStore;

    @Test
    public void should_find_all_entity_none_should_be_null() throws Exception {
        yamlEntityStore = new YamlEntityStore();
        yamlEntityStore.setRootLocation(new File(YamlEntityStore.class.getResource(testPackage).getPath()));
        // Entity need to load types to be able to create an Entity
        yamlEntityStore.loadTypes();
        yamlEntityStore.loadEntities();

        assertThat(yamlEntityStore.getRootPK().toString()).isEqualTo("Test Components");
        // none is null
        assertThat(yamlEntityStore.listChildren(yamlEntityStore.getRootPK(), null))
                .extracting(yamlEntityStore::getEntity)
                .doesNotContainNull();


        assertThat(yamlEntityStore.getEntity(new YamlPK("Test Components"))).isNotNull();
        assertThat(yamlEntityStore.getEntity(new YamlPK("Test Components")).getType().getName()).isEqualTo("Root");
        assertThat(yamlEntityStore.getEntity(new YamlPK("Test Components")).get("name")).isEqualTo("Test Components");
        assertThat(yamlEntityStore.getEntity(new YamlPK(yamlEntityStore.getRootPK(), "Test Category"))).isNull();
        assertThat(yamlEntityStore.getEntity(new YamlPK(yamlEntityStore.getRootPK(), "Test Category/Test"))).isNotNull();
        assertThat(yamlEntityStore.getEntity(new YamlPK(yamlEntityStore.getRootPK(), "Test Category/Test")).getType().getName()).isEqualTo("CircuitContainer");
        assertThat(yamlEntityStore.getEntity(new YamlPK(yamlEntityStore.getRootPK(), "Test Category/Test")).get("name")).isEqualTo("Test");
        assertThat(yamlEntityStore.getEntity(new YamlPK(yamlEntityStore.getRootPK(), "Test Category/Test/Default Fault Handler"))).isNotNull();
        assertThat(yamlEntityStore.getEntity(new YamlPK(yamlEntityStore.getRootPK(), "Test Category/Test/Default Fault Handler")).getType().getName()).isEqualTo("FilterCircuit");
        assertThat(yamlEntityStore.getEntity(new YamlPK(yamlEntityStore.getRootPK(), "Test Category/Test/Default Fault Handler")).get("name")).isEqualTo("Default Fault Handler");
        assertThat(yamlEntityStore.getEntity(new YamlPK(yamlEntityStore.getRootPK(), "Test Category/Test/Default Fault Handler")).get("start").toString()).isEqualTo("Generic Error");
        assertThat(yamlEntityStore.getEntity(new YamlPK(yamlEntityStore.getRootPK(), "Test Category/Test/Default Fault Handler/Generic Error"))).isNotNull();
        assertThat(yamlEntityStore.getEntity(new YamlPK(yamlEntityStore.getRootPK(), "Test Category/Test/Default Fault Handler/Generic Error")).getType().getName()).isEqualTo("GenericFaultFilter");
        assertThat(yamlEntityStore.getEntity(new YamlPK(yamlEntityStore.getRootPK(), "Test Category/Test/Default Fault Handler/Generic Error")).get("name")).isEqualTo("Generic Error");
        assertThat(yamlEntityStore.getEntity(new YamlPK(yamlEntityStore.getRootPK(), "Test Category/Test/lookup$test"))).isNotNull();
        assertThat(yamlEntityStore.getEntity(new YamlPK(yamlEntityStore.getRootPK(), "Test Category/Test/lookup$test")).get("name")).isEqualTo("lookup");
        assertThat(yamlEntityStore.getEntity(new YamlPK(yamlEntityStore.getRootPK(), "Test Category/Test/lookup$test")).get("namespace")).isEqualTo("test");

    }


}
