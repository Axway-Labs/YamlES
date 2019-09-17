package com.axway.gw.es.tools;

import java.io.IOException;

public class BatchYamlStoreConverter {

    public static void main(String[] args) throws IOException, InterruptedException {
        convert("FactoryTemplate");
        convert("FactoryTemplateSamples");
        convert("TeamDevelopmentAPI");
        convert("TeamDevelopmentSettings");
        convert("TeamDevelopmentSettingsAPIM");
    }

    private static void convert(String project) throws InterruptedException, IOException {
        String file = BatchYamlStoreConverter.class.getResource("/apiprojects/" + project + "/configs.xml").getFile();

        ConvertToYamlStore.convert("federated:file:" + file, "/tmp/yamlstores/" + project);
    }
}
