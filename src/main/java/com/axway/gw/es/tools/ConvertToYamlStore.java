package com.axway.gw.es.tools;

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
        entityManager = new EntityManager(es);
      }

	private void delete(String location)
	{
		File dir = new File(location);
		if (dir.exists())
			try {
				Path rootPath = Paths.get(location);
				Files.walk(rootPath)
						.sorted(Comparator.reverseOrder())
						.map(Path::toFile)
						//.peek(System.out::println)
						.forEach(File::delete);
			}
			catch (IOException e)
			{ LOGGER.info("Unable to delete directory " + location); }
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
	
	private static String ES_TO_LOAD;

	public static void main(String[] args) {
		if (args.length < 2) {
			LOGGER.info("usage: federated:file:/path-to-existing-fed.xml path-to-write-yaml");
			System.exit(1);
		}
		ES_TO_LOAD = args[0];
		String whereToWriteTypes = args[1];
		String whereToWriteEntities = args[1] + "/";
		try {
			ConvertToYamlStore converter = new ConvertToYamlStore(ES_TO_LOAD, new Properties());
			LOGGER.info("Deleting folders");
			converter.delete(args[1]);
			Thread.sleep(2000);
			converter.dumpTypesAsYaml(whereToWriteTypes);
			converter.dumpEntitiesAsYaml(whereToWriteEntities);
			LOGGER.info("Successfully extracted yaml files.");
			System.exit(0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
