package com.axway.gw.es.yaml.tools;

import com.axway.gw.es.yaml.YamlEntityExporter;
import com.axway.gw.es.yaml.YamlEntityTypeImpEx;
import com.axway.gw.es.yaml.converters.EntityToDTOConverter;
import com.axway.gw.es.yaml.converters.EntityTypeToDTOConverter;
import com.axway.gw.es.yaml.dto.entity.EntityDTO;
import com.vordel.es.EntityStore;
import com.vordel.es.EntityStoreFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import static java.lang.System.currentTimeMillis;

public class ConvertToYamlStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConvertToYamlStore.class);

    private EntityTypeToDTOConverter entityTypeDTOConverter;
    private EntityToDTOConverter entityToDTOConverter;
    private EntityStore inputES;

    public ConvertToYamlStore(String url) {
        inputES = EntityStoreFactory.createESForURL(url);
        long start = currentTimeMillis();
        inputES.connect(url, new Properties());
        long end = currentTimeMillis();
        LOGGER.info("Loaded ES in {}ms", (end - start));

        entityTypeDTOConverter = new EntityTypeToDTOConverter(inputES);
        entityTypeDTOConverter.loadTypeAndSubtype();
        entityToDTOConverter = new EntityToDTOConverter(inputES, entityTypeDTOConverter.getTypes());
    }

    public EntityStore getInputEntityStore() {
        return inputES;
    }


    public void convert(String yamlDir) throws InterruptedException, IOException {
        convert(yamlDir, false);
    }

    public void convert(String yamlDir, boolean writeKeyMapping) throws InterruptedException, IOException {
        String whereToWriteEntities = yamlDir + "/";

        LOGGER.info("Deleting folders");
        this.delete(yamlDir);
        Thread.sleep(2000);

        final List<EntityDTO> entityDTOList = entityToDTOConverter.mapFromRoot();

        LOGGER.info("Dumping types to {}", yamlDir);
        new YamlEntityTypeImpEx().writeTypes(new File(yamlDir), entityTypeDTOConverter.getBaseType());

        LOGGER.info("Dumping entities to {}", whereToWriteEntities);
        new YamlEntityExporter(entityDTOList, writeKeyMapping).writeEntities(new File(whereToWriteEntities));

        LOGGER.info("Successfully extracted yaml files.");
    }

    private void delete(String location) {
        File dir = new File(location);
        if (dir.exists()) {
            try (Stream<Path> files = Files.walk(Paths.get(location))) {
                files.sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            } catch (IOException e) {
                LOGGER.info("Unable to delete directory {}", location);
            }
        }
    }


    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length < 2) {
            LOGGER.info("usage: federated:file:/path-to-existing-fed.xml path-to-write-yaml");
            System.exit(1);
        }
        String inputES = args[0];
        String yamlDir = args[1];
        new ConvertToYamlStore(inputES).convert(yamlDir);
    }


}
