package com.axway.gw.es.yaml;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class YamlESMiscTests {
    private static final String testPackage = "/com/axway/gw/es/yaml/refs/";



    @ParameterizedTest
    @ValueSource(strings = {
            "foo/bar",
            "foo/bar/",
            "foo/bar.yaml",
            "foo/bar/metadata.yaml",
            "/tmp/my-project/foo/bar.yaml",
            "/tmp/my-project/foo/bar",
            "/tmp/my-project/foo/bar/"
    })
    public void should_work_random_yaml_file(String path) throws Exception {
        YamlEntityStore yamlEntityStore = new YamlEntityStore();
        yamlEntityStore.setRootLocation(new File("/tmp/my-project"));
        assertThat(yamlEntityStore.getYamlPkForFile(new File(path))).isEqualToComparingFieldByField(new YamlPK("foo/bar"));

        yamlEntityStore = new YamlEntityStore();
        yamlEntityStore.setRootLocation(new File("/tmp/my-project"));
        yamlEntityStore.setRootManually(new YamlPK("test"));
        assertThat(yamlEntityStore.getYamlPkForFile(new File(path))).isEqualToComparingFieldByField(new YamlPK("test/foo/bar"));
    }





}
