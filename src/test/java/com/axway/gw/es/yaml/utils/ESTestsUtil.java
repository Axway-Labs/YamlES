package com.axway.gw.es.yaml.utils;

import com.axway.gw.es.yaml.YamlEntityStoreRefsTest;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

public class ESTestsUtil {

    public static File getFileFromClasspath(String testPackage, String filename) {
        URL url = YamlEntityStoreRefsTest.class.getResource(testPackage + filename);
        assertThat(url).withFailMessage("Test file: " + filename + " doesn't exists in package: " + testPackage).isNotNull();
        File file = new File(url.getPath().replaceAll("%20", " "));
        assertThat(file).exists();
        return file;
    }

    public static void assertDiffCount(ESDiff diff, int expectedDiffCount) throws IOException {
        assertThat(diff.diffCount())
                .withFailMessage("Should have found " + expectedDiffCount + " diff but found " + diff.diffCount() + " differences:" + diff.diffAsJsonString())
                .isEqualTo(expectedDiffCount);
    }

    public static void assertDiffCount(ESDiff diff, int expectedDiffCount, String dumpFileNamePrefix) throws IOException {
        final String path = "target/diff-dump-" + dumpFileNamePrefix + ".json";
        try {
            assertThat(diff.diffCount())
                    .withFailMessage("Should have found " + expectedDiffCount + " diff but found " + diff.diffCount() + " differences can be found in " + path)
                    .isEqualTo(expectedDiffCount);
        } catch (Throwable e) {
            diff.dumpDiffJson(new File(path));
            throw e;
        }
    }

}
