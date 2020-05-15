package com.axway.gw.es.yaml;

import com.axway.gw.es.yaml.utils.ESDiff;
import com.axway.gw.es.yaml.utils.ESTestsUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;

import static com.axway.gw.es.yaml.utils.ESTestsUtil.assertDiffCount;
import static org.assertj.core.api.Assertions.assertThat;

public class ESDiffTest {

    private static final String testPackage = "/com/axway/gw/es/yaml/diff/";
    private YamlEntityStore source;
    private YamlEntityStore target;


    public void createStores(String sourcePath, String targetPath) {
        this.source = new YamlEntityStore();
        source.connect(YamlEntityStore.SCHEME + "file:" + new File(YamlEntityStore.class.getResource(testPackage + sourcePath).getPath()).getPath(), null);

        this.target = new YamlEntityStore();
        target.connect(YamlEntityStore.SCHEME + "file:" + new File(YamlEntityStore.class.getResource(testPackage + targetPath).getPath()).getPath(), null);

    }

    @Test
    public void should_find_no_diff() throws Exception {

        createStores("sample1", "sample1");

        final ESDiff diff = ESDiff.diff(source, target);

        assertDiffCount(diff, 0);
        assertThat(diff.diffAsJson()).isEqualTo("[ ]");

    }

    @Test
    public void should_find_a_diffs() throws Exception {

        createStores("sample1", "sample2");

        final ESDiff diff = ESDiff.diff(source, target);

        assertDiffCount(diff, 5);
        assertThat(diff.diffList()).extracting(ESDiff.Diff::getDiffType).contains(ESDiff.DiffType.MODIFIED, ESDiff.DiffType.ADDED, ESDiff.DiffType.REMOVED);

        // TODO check in depth


    }



}
