package com.axway.gw.es.yaml.tools;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class BatchYamlStoreConverterTestIT {

    /**
     * Main method to call test manually.
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        convert("FactoryTemplate");
        convert("FactoryTemplateSamples");
        convert("TeamDevelopmentAPI");
        convert("TeamDevelopmentSettings");
        convert("TeamDevelopmentSettingsAPIM");
    }
    
	@Test
	public void convertFactoryTemplate() throws InterruptedException, IOException {
		convert("FactoryTemplate");
	}
	
	@Test
	public void convertFactoryTemplateSamples() throws InterruptedException, IOException {
		convert("FactoryTemplateSamples");
	}
	
	@Test
	public void convertTeamDevelopmentAPI() throws InterruptedException, IOException {
		convert("TeamDevelopmentAPI");
	}
	
	@Test
	public void convertTeamDevelopmentSettings() throws InterruptedException, IOException {
		convert("TeamDevelopmentSettings");
	}
	
	@Test
	public void convertTeamDevelopmentSettingsAPIM() throws InterruptedException, IOException {
		convert("TeamDevelopmentSettingsAPIM");
	}

    private static void convert(String project) throws InterruptedException, IOException {
        String file = BatchYamlStoreConverterTestIT.class.getResource("/apiprojects/" + project + "/configs.xml").getFile();

        ConvertToYamlStore.convert("federated:file:" + file, "/tmp/yamlstores/" + project);
    }
}
