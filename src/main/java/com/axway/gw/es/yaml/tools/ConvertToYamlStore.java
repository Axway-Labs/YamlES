package com.axway.gw.es.yaml.tools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Properties;

import com.vordel.es.EntityStore;
import com.vordel.es.EntityStoreFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConvertToYamlStore {

    private final static Logger LOGGER = LoggerFactory.getLogger(ConvertToYamlStore.class);

    TypeManager typeManager;
    EntityManager entityManager = null;
    EntityStore es = null;

    public ConvertToYamlStore(String url, Properties credentials) {
        es = EntityStoreFactory.createESForURL(url);
        es.connect(url, credentials);
        typeManager = new TypeManager(es);
        entityManager = new EntityManager(es, typeManager.getTypes());
    }

    private void delete(String location) {
        File dir = new File(location);
        if (dir.exists())
            try {
                Path rootPath = Paths.get(location);
                Files.walk(rootPath)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        //.peek(System.out::println)
                        .forEach(File::delete);
            } catch (IOException e) {
                LOGGER.info("Unable to delete directory " + location);
            }
    }

    public void dumpTypesAsYaml(String location) throws IOException {
        LOGGER.info("Dumping types to " + location);
        typeManager.writeTypes(new File(location + "/META-INF"));
        //typeManager.writeTypes(new File(location));
    }

    public void dumpEntitiesAsYaml(String location) throws IOException {
        LOGGER.info("Dumping entities to " + location);
        entityManager.writeEntities(new File(location));
    }

	public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length < 2) {
            LOGGER.info("usage: federated:file:/path-to-existing-fed.xml path-to-write-yaml");
            System.exit(1);
        }
		String inputES = args[0];
        String yamlDir = args[1];
		convert(inputES, yamlDir);
	}

	public static void convert(String inputES, String yamlDir) throws InterruptedException, IOException {
		String whereToWriteEntities = yamlDir + "/";
		ConvertToYamlStore converter = new ConvertToYamlStore(inputES, new Properties());
		LOGGER.info("Deleting folders");
		converter.delete(yamlDir);
		Thread.sleep(2000);
		converter.dumpTypesAsYaml(yamlDir);
		converter.dumpEntitiesAsYaml(whereToWriteEntities);
		LOGGER.info("Successfully extracted yaml files.");
	}
}
