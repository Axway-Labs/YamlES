package com.axway.gw.es.yaml.util;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.axway.gw.es.yaml.YamlEntityStore.YAML_MAPPER;
import static com.axway.gw.es.yaml.YamlPK.CHILD_SEPARATOR;
import static com.google.common.base.Preconditions.checkNotNull;

public final class TopLevelFolders {

    private final TopLevelFoldersDTO topLevelFoldersDTO;
    private final List<String> topLevelFoldersList = new ArrayList<>();

    public TopLevelFolders() {

        final URL resource = Thread.currentThread().getContextClassLoader().getResource("top-level-folders.yaml");
        try {
            topLevelFoldersDTO = YAML_MAPPER.readValue(resource, TopLevelFoldersDTO.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        topLevelFoldersList.add(topLevelFoldersDTO.getDefaultFolder());
        topLevelFoldersDTO.getFolders()
                .stream()
                .map(TypesCategoryDTO::getName)
                .forEach(topLevelFoldersList::add);

        topLevelFoldersDTO.getFolders().add(new TypesCategoryDTO().setName("").setTypes(Collections.singletonList("Root")));

    }

    public boolean isAbsoluteRef(String ref) {
        checkNotNull(ref);
        final int firstSlashPos = ref.indexOf(CHILD_SEPARATOR);
        if (firstSlashPos >= 0) {
            return topLevelFoldersList.contains(ref.substring(0, firstSlashPos));
        }
        return false;
    }


    public String getTopLevelFolderForTypeName(String typeName) {
        checkNotNull(typeName);
        return topLevelFoldersDTO.getFolders()
                .stream()
                .filter(typesCategoryDTO -> typesCategoryDTO.getTypes().contains(typeName))
                .map(TypesCategoryDTO::getName)
                .findFirst()
                .orElseGet(topLevelFoldersDTO::getDefaultFolder);
    }

    public String getDefaultFolder() {
        return topLevelFoldersDTO.getDefaultFolder();
    }

    public static void createDirectoryIfNeeded(File directory) throws IOException {
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Could not create directory:" + directory);
        }
    }

    static class TopLevelFoldersDTO {

        private String defaultFolder;
        private List<TypesCategoryDTO> folders;

        public String getDefaultFolder() {
            return defaultFolder;
        }

        public List<TypesCategoryDTO> getFolders() {
            return folders;
        }

        public TopLevelFoldersDTO setDefaultFolder(String defaultFolder) {
            this.defaultFolder = defaultFolder;
            return this;
        }

        public TopLevelFoldersDTO setFolders(List<TypesCategoryDTO> folders) {
            this.folders = folders;
            return this;
        }
    }

    static class TypesCategoryDTO {

        private String name;
        private List<String> types;

        public List<String> getTypes() {
            return types;
        }

        public TypesCategoryDTO setTypes(List<String> types) {
            this.types = types;
            return this;
        }

        public String getName() {
            return name;
        }

        public TypesCategoryDTO setName(String name) {
            this.name = name;
            return this;
        }
    }
}
