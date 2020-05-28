package com.axway.gw.es.yaml.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TopLevelFoldersTest {

    private TopLevelFolders topLevelFolders;

    @BeforeEach
    public void prepare() {
        topLevelFolders = new TopLevelFolders();
    }

    @Test
    public void should_load_top_level_folders() {
        assertThat(topLevelFolders.getDefaultFolder()).isNotNull();
        assertThat(topLevelFolders.getTopLevelFolderForTypeName("Root")).isEqualTo("");
        assertThat(topLevelFolders.getTopLevelFolderForTypeName("FilterCircuit")).isNotEqualTo(topLevelFolders.getDefaultFolder());
        assertThat(topLevelFolders.getTopLevelFolderForTypeName("_Foo_")).isEqualTo(topLevelFolders.getDefaultFolder());
        assertThatThrownBy(() -> topLevelFolders.getTopLevelFolderForTypeName(null)).isInstanceOfAny(Exception.class);
    }


    @Test
    public void should_assert_that_ref_is_absolute() {
        assertThat(topLevelFolders.isAbsoluteRef(topLevelFolders.getDefaultFolder() + "/Foo")).isTrue();
        assertThat(topLevelFolders.isAbsoluteRef("Policies/Foo")).isTrue();
        assertThat(topLevelFolders.isAbsoluteRef("")).isFalse();
        assertThat(topLevelFolders.isAbsoluteRef("Bar/Foo")).isFalse();
        assertThat(topLevelFolders.isAbsoluteRef("Foo")).isFalse();
        assertThatThrownBy(() -> topLevelFolders.isAbsoluteRef(null)).isInstanceOfAny(Exception.class);
    }
}
