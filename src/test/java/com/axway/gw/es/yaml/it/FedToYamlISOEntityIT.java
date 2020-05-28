package com.axway.gw.es.yaml.it;

import java.io.IOException;
import java.nio.file.Paths;

import com.axway.gw.es.yaml.YamlEntityStore;
import com.axway.gw.es.yaml.YamlPK;
import com.axway.gw.es.yaml.converters.EntityStoreESPKMapper;
import com.axway.gw.es.yaml.tools.ConvertToYamlStore;
import com.axway.gw.es.yaml.testutils.diff.ESDiff;
import com.vordel.es.fed.FSPK;
import com.vordel.es.impl.ESLongPK;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.axway.gw.es.yaml.testutils.ESTestsUtil.assertDiffCount;
import static org.assertj.core.api.Assertions.assertThat;

public class FedToYamlISOEntityIT {

    public static final String YAML_OUTPUT_DIR = "target/yamlstores-tests/";
    public static final String RESOLVED_TO_NULL = "__resolved_to_null__:-1";


    @ParameterizedTest
    @ValueSource(strings = {"FactoryTemplate",
            "FactoryTemplateSamples",
            "TeamDevelopmentAPI",
            "TeamDevelopmentSettings",
            "TeamDevelopmentSettingsAPIM"})
    @Disabled
    public void storeToYamlToStore(String project) throws InterruptedException, IOException {

        final String yamlDir = YAML_OUTPUT_DIR + project;

        final ConvertToYamlStore convertToYamlStore = new ConvertToYamlStore(getFEDFileURL(project));

        // convert => All YML Files are written as as the key mapping file
        convertToYamlStore.convert(yamlDir, true);

        // load all YAML filed
        final YamlEntityStore yamlEntityStore = new YamlEntityStore();
        yamlEntityStore.connect(YamlEntityStore.SCHEME + "file:" + Paths.get(yamlDir).toFile().getPath(), null);

        // Read the key mapping
        final EntityStoreESPKMapper<String, String> espkMapper = new EntityStoreESPKMapper<>();
        espkMapper.readPKPairs(yamlDir);


        // compare using the key mapping to find matching elements
        ESDiff diff = ESDiff.diff(convertToYamlStore.getInputEntityStore(), yamlEntityStore, fedKey -> {
            String path = espkMapper.getTargetKey(fedKey.toString());
            if (path == null) {
                path = RESOLVED_TO_NULL;
            }
            return new YamlPK(path);
        }, yamlPk -> {
            String fedKey = espkMapper.getSourceKey(yamlPk.toString());
            if (fedKey == null) {
                fedKey = RESOLVED_TO_NULL;
            }
            final int realPkPosition = fedKey.lastIndexOf(':');
            return new FSPK(fedKey.substring(0, realPkPosition), new ESLongPK(fedKey.substring(realPkPosition+1)));
        });

        assertDiffCount(diff, 0, project);

    }

    private String getFEDFileURL(String project) {
        return "federated:file:" + FedToYamlISOEntityIT.class.getResource("/apiprojects/" + project + "/configs.xml").getFile();
    }


}
