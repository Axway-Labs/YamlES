package com.axway.gw.es.yaml;

import com.axway.gw.es.yaml.dto.type.TypeDTO;
import com.axway.gw.es.yaml.util.TopLevelFolders;

import java.io.File;
import java.io.IOException;

public class YamlEntityTypeImpEx {

    public static final String TYPES_DIR = "META-INF";

    private static final String TYPES_FILE = "Types.yaml";

    public void writeTypes(File baseDir, TypeDTO baseType) throws IOException {
        File typesDir = new File(baseDir, TYPES_DIR);
        TopLevelFolders.createDirectoryIfNeeded(typesDir);

        File outputFile = new File(typesDir, TYPES_FILE);
        YamlEntityStore.YAML_MAPPER.writeValue(outputFile, baseType);
    }

    public TypeDTO readTypes(File baseDir) throws IOException {
        File typesDir = new File(baseDir, TYPES_DIR);
        File inputFile = new File(typesDir, TYPES_FILE);
        return YamlEntityStore.YAML_MAPPER.readValue(inputFile, TypeDTO.class);
    }

}
