package com.axway.gw.es.yaml.it;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

import com.axway.gw.es.yaml.YamlEntityStore;
import com.axway.gw.es.yaml.YamlPK;
import com.axway.gw.es.yaml.converters.ConvertToYamlStore;
import com.axway.gw.es.yaml.converters.EntityDTOConverter;
import com.axway.gw.es.yaml.utils.ESDiff;
import com.vordel.es.EntityStore;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.axway.gw.es.yaml.utils.ESTestsUtil.assertDiffCount;
import static org.assertj.core.api.Assertions.assertThat;

public class BatchYamlStoreConverterTestIT {

    public static final String YAML_OUTPUT_DIR = "target/yamlstores-tests/";


    @ParameterizedTest
    @ValueSource(strings = {"FactoryTemplate",
            "FactoryTemplateSamples",
            "TeamDevelopmentAPI",
            "TeamDevelopmentSettings",
            "TeamDevelopmentSettingsAPIM"})
    public void convertFactoryTemplate(String project) throws InterruptedException, IOException {

        final ConvertToYamlStore convertToYamlStore = new ConvertToYamlStore("federated:file:" + BatchYamlStoreConverterTestIT.class.getResource("/apiprojects/" + project + "/configs.xml").getFile());
        convertToYamlStore.convert(YAML_OUTPUT_DIR + project);
       //  convertToYamlStore.writeFederatedToYamlPkMapping(YAML_OUTPUT_DIR + project);

        final EntityStore sourceES = convertToYamlStore.getInputEntityStore();

        final YamlEntityStore yamlEntityStore = new YamlEntityStore();
        yamlEntityStore.connect(YamlEntityStore.SCHEME + "file:" + Paths.get(YAML_OUTPUT_DIR + project).toFile().getPath(), null);

        // final Map<String, YamlPK> federatedToYamlPk = EntityDTOConverter.readFederatedToYamlPkMapping(YAML_OUTPUT_DIR + project);

        ESDiff diff = ESDiff.diff(sourceES, yamlEntityStore, YamlPK::new);

        assertDiffCount(diff,0, project);

    }


}
