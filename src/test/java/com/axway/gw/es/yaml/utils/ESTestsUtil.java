package com.axway.gw.es.yaml.utils;

import com.axway.gw.es.yaml.YamlEntityDTOStoreTest;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.File;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

public class ESTestsUtil {

    public static File getFileFromClasspath(String testPackage, String filename) {
        URL url = YamlEntityDTOStoreTest.class.getResource(testPackage + filename);
        assertThat(url).withFailMessage("Test file: " + filename + " doesn't exists in package: "+testPackage).isNotNull();
        File file = new File(url.getPath().replaceAll("%20", " "));
        assertThat(file).exists();
        return file;
    }

    public static void assertDiffCount(ESDiff diff, int expectedDiffCount) throws JsonProcessingException {
        assertThat(diff.diffCount())
                .withFailMessage("Should have found " + expectedDiffCount + " diff but found " + diff.diffCount() + ": \n" + diff.diffAsJson())
                .isEqualTo(expectedDiffCount);
    }

}
