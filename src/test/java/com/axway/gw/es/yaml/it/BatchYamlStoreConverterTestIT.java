package com.axway.gw.es.yaml.it;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

import com.axway.gw.es.yaml.YamlEntityStore;
import com.axway.gw.es.yaml.YamlPK;
import com.axway.gw.es.yaml.converters.EntityStoreESPKMapper;
import com.axway.gw.es.yaml.tools.ConvertToYamlStore;
import com.axway.gw.es.yaml.util.NameUtils;
import com.axway.gw.es.yaml.utils.ESDiff;
import com.vordel.es.ESPK;
import com.vordel.es.EntityStore;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.axway.gw.es.yaml.utils.ESTestsUtil.assertDiffCount;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.atomicMarkableReference;

public class BatchYamlStoreConverterTestIT {

    public static final String YAML_OUTPUT_DIR = "target/yamlstores-tests/";


    @ParameterizedTest
    @ValueSource(strings = {"FactoryTemplate",
            "FactoryTemplateSamples",
            "TeamDevelopmentAPI",
            "TeamDevelopmentSettings",
            "TeamDevelopmentSettingsAPIM"})
    @Disabled
    public void convertFactoryTemplate(String project) throws InterruptedException, IOException {

        final String yamlDir = YAML_OUTPUT_DIR + project;

        final ConvertToYamlStore convertToYamlStore = new ConvertToYamlStore(getFEDFileURL(project));

        // convert => All YML Files are written as as the key mapping file
        convertToYamlStore.convert(yamlDir, true);

        // load all YAML filed
        final YamlEntityStore yamlEntityStore = new YamlEntityStore();
        yamlEntityStore.connect(YamlEntityStore.SCHEME + "file:" + Paths.get(yamlDir).toFile().getPath(), null);

        // Read the key mapping
        final Map<String, String> keyMapping = new EntityStoreESPKMapper<String, String>().readFederatedToYamlPkMapping(yamlDir);

        // compare using the key mapping to find matching elements
        ESDiff diff = ESDiff.diff(convertToYamlStore.getInputEntityStore(), yamlEntityStore, fedKey -> {
            final String path = keyMapping.get(fedKey);
            if (path == null) return null;
            return new YamlPK(path);
        });

        assertDiffCount(diff, 0, project);

    }

    private String getFEDFileURL(String project) {
        return "federated:file:" + BatchYamlStoreConverterTestIT.class.getResource("/apiprojects/" + project + "/configs.xml").getFile();
    }


}
