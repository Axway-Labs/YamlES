package com.axway.gw.es.yaml.it;

import com.axway.gw.es.yaml.YamlEntityStore;
import com.axway.gw.es.yaml.testutils.diff.ESTypeDiff;
import com.axway.gw.es.yaml.tools.ConvertToYamlStore;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Paths;

import static com.axway.gw.es.yaml.testutils.ESTestsUtil.assertDiffCount;

public class FedToYamlISOEntityTypeIT {

    public static final String YAML_OUTPUT_DIR = "target/yamlstores-tests/";

    @ParameterizedTest
    @ValueSource(strings = {"FactoryTemplate",
            "FactoryTemplateSamples",
            "TeamDevelopmentAPI",
            "TeamDevelopmentSettings",
            "TeamDevelopmentSettingsAPIM"})
    public void storeToYamlToStore(String project) throws InterruptedException, IOException {

        final String yamlDir = YAML_OUTPUT_DIR + project;

        final ConvertToYamlStore convertToYamlStore = new ConvertToYamlStore(getFEDFileURL(project));

        // convert => All YML Files are written as as the key mapping file
        convertToYamlStore.convert(yamlDir);

        // load all YAML filed
        final YamlEntityStore yamlEntityStore = new YamlEntityStore();
        yamlEntityStore.connect(YamlEntityStore.SCHEME + "file:" + Paths.get(yamlDir).toFile().getPath(), null);


        // compare using the key mapping to find matching elements
        ESTypeDiff diff = ESTypeDiff.diff(convertToYamlStore.getInputEntityStore(), yamlEntityStore);

        assertDiffCount(diff, 0, project+"-types");

    }

    private String getFEDFileURL(String project) {
        return "federated:file:" + FedToYamlISOEntityTypeIT.class.getResource("/apiprojects/" + project + "/configs.xml").getFile();
    }


}
