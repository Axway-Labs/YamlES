package com.axway.gw.es.yaml;

import com.axway.gw.es.yaml.converters.EntityStoreESPKMapper;
import com.axway.gw.es.yaml.dto.entity.EntityDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static com.axway.gw.es.yaml.util.NameUtils.sanitize;
import static com.axway.gw.es.yaml.util.TopLevelFolders.createDirectoryIfNeeded;

public class YamlEntityExporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(YamlEntityExporter.class);

    public static final String YAML_EXTENSION = ".yaml";
    public static final String METADATA_FILENAME = "metadata" + YAML_EXTENSION;

    private final List<EntityDTO> mappedEntities;
    private final boolean saveKeyMapping;
    private EntityStoreESPKMapper<String, String> entityStoreESPKMapper;

    public YamlEntityExporter(List<EntityDTO> entityDTOList) {
        this(entityDTOList, false);
    }

    public YamlEntityExporter(List<EntityDTO> entityDTOList, boolean saveKeyMapping) {
        this.mappedEntities = entityDTOList;
        this.saveKeyMapping = saveKeyMapping;
        entityStoreESPKMapper = new EntityStoreESPKMapper<>();
    }

    public void writeEntities(File rootDir) throws IOException {

        createDirectoryIfNeeded(rootDir);

        // must provide rootDir (this happens when a file already exists)
        if (!rootDir.isDirectory())
            throw new IOException("Must provide a directory for YAML output");

        for (EntityDTO entityDTO : mappedEntities) {
            // deal with pk for parent  entity
            if ("Root".equals(entityDTO.getMeta().getType())) {
                // No path for root
                dumpAsYaml(rootDir, "", entityDTO);
            } else {
                dumpAsYaml(rootDir, entityDTO.getKey(), entityDTO);
            }
        }

        if (saveKeyMapping) {
            entityStoreESPKMapper.writePKPairs(rootDir);
        }

    }

    private void dumpAsYaml(File rootDir, String path, EntityDTO entityDTO) throws IOException {
        final File output = new File(rootDir, path);

        if (entityDTO.isInSeparatedFile()) { // handle as directory with metadata
            createDirectoryIfNeeded(output);
            YamlEntityStore.YAML_MAPPER.writeValue(new File(output, METADATA_FILENAME), entityDTO);
        } else { // handle as file
            File f = new File(output.getPath() + YAML_EXTENSION);
            createDirectoryIfNeeded(f.getParentFile());
            extractContent(entityDTO, f);
            YamlEntityStore.YAML_MAPPER.writeValue(f, entityDTO);
        }

        if (saveKeyMapping) {
            entityStoreESPKMapper.addPKPair(entityDTO.getSourceKey(), entityDTO.getKey());
        }

    }


    private void extractContent(EntityDTO entityDTO, File file) throws IOException {
        if (entityDTO.getChildren() != null) {
            for (EntityDTO child : entityDTO.getChildren().values()) {
                extractContent(child, file);
            }
        }

        File dir = file.getParentFile();
        final String metaType = entityDTO.getMeta().getType();

        switch (metaType) {
            case "JavaScriptFilter":
                String fileName = file.getName().replace(YAML_EXTENSION, "-Scripts/") + sanitize(entityDTO.buildKeyValue()) + "." + entityDTO.getFieldValue("engineName");
                writeContentToFile(entityDTO, dir, fileName, "script", false);
                break;
            case "Script":
                writeContentToFile(entityDTO, dir, entityDTO.buildKeyValue() + "." + entityDTO.getFields().get("engineName"), "script", false);
                break;
            case "Stylesheet":
                writeContentToFile(entityDTO, dir, entityDTO.getFields().get("URL") + ".xsl", "contents", true);
                break;
            case "Certificate":
                writeContentToFile(entityDTO, dir, file.getName().replace(YAML_EXTENSION, ".pem"), "key", true);
                writeContentToFile(entityDTO, dir, file.getName().replace(YAML_EXTENSION, ".crt"), "content", true);
                break;
            case "ResourceBlob":
                String type = entityDTO.getFields().get("type");
                if (Objects.equals(type, "schema")) {
                    type = "xsd";
                }
                writeContentToFile(entityDTO, dir, entityDTO.getFields().get("ID") + "." + type, "content", true);
                break;
            default:
                LOGGER.debug("Nothing to extract from type {}", metaType);
        }
    }

    private void writeContentToFile(EntityDTO entityDTO, File dir, String fileName, String field, boolean base64Decode) throws IOException {
        String content = entityDTO.getFields().remove(field);
        if (content == null) {
            return;
        }

        byte[] data;
        if (base64Decode) {
            data = Base64.getDecoder().decode(content.replaceAll("[\r?\n]", ""));
        } else {
            data = content.getBytes();
        }

        Path path = dir.toPath().resolve(fileName);
        File parentDir = path.getParent().toFile();
        createDirectoryIfNeeded(parentDir);
        Files.write(path, data);

        entityDTO.getFields().put(field + "#ref" + (base64Decode ? "base64" : ""), fileName);
    }

}
