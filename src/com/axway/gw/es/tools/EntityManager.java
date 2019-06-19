package com.axway.gw.es.tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.vordel.es.ESPK;
import com.vordel.es.Entity;
import com.vordel.es.EntityStore;
import com.vordel.es.EntityStoreDelegate;
import com.vordel.es.EntityStoreException;
import com.vordel.es.xes.PortableESPK;

public class EntityManager {
	private final static Logger LOGGER = Logger.getLogger(EntityManager.class.getName());

	ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

	Map<ESPK, Entity> entities = new ConcurrentHashMap<ESPK, Entity>();
	private EntityStore es;

	public EntityManager(EntityStore es) {
		this.es = es;
		ESPK root = es.getRootPK();
		loadEntities(root);
	}

	public void writeEntities(File dir) throws IOException {
		if (!dir.exists())
	        dir.mkdirs();
	 	
		// must provide dir
		if (!dir.isDirectory())
			throw new IOException("Must provide a directory for YAML output");
		for (Map.Entry<ESPK,Entity> entry : entities.entrySet()) {  
			LOGGER.info("Dumping type " + entry.getKey());
			com.axway.gw.es.tools.Entity e = yamlize(dir, entry.getValue());
			//writeType(dir, t);
		}
	}

	private com.axway.gw.es.tools.Entity yamlize(File dir, Entity value) {
		com.axway.gw.es.tools.Entity ye = new com.axway.gw.es.tools.Entity();
		ye.type = value.getType().getName(); // may want to change this to the directory location of the type?
		// deal with pk for this entity
		File keyPath = getPath(dir, value.getPK());
		String key = createRelativePath(dir, keyPath);
		ye.key = key;
		// deal with pk for parent  entity
		File parentKeyPath = getPath(dir, value.getParentPK());
		String parentPK = createRelativePath(dir, parentKeyPath);
		ye.parent = parentPK;
		// children?
		ye.allowsChildren = value.getType().allowsChildEntities();
		// fields
		setFields(value, ye);
		setReferences(dir, value, ye);
		if (value.getType().allowsChildEntities())
			ye.allowsChildren = true;
		dumpAsYaml(keyPath, ye);
		return ye;
	}
	
	private void dumpAsYaml(File out, com.axway.gw.es.tools.Entity ye) {
		try {
			if (ye.allowsChildren) { // handle as directory with metadata
				out.mkdirs();
				mapper.writeValue(new File(out, "metadata.yaml"), ye);
			}
			else { // handle as file 
				String filename = out.getPath() + ".yaml";
				File f = new File(filename);
			//	f.mkdirs();
				mapper.writeValue(f, ye);
			}
		}
		catch (Exception x) {
			x.printStackTrace();	
		}
	}
	private void setFields(Entity value, com.axway.gw.es.tools.Entity ye) {
		com.vordel.es.Field[] allFields = value.getAllFields();
		List<com.vordel.es.Field> refs = value.getReferenceFields(); 
		for (com.vordel.es.Field f : allFields) {            
			com.vordel.es.FieldType ft = f.getType();
			if (!refs.contains(f)) { // not a reference
				EntityField field = new EntityField();
				field.name = f.getName();
				field.value = value.getStringValue(f.getName());
				ye.fields.add(field);
				ye.keyFieldValues = getKeyValues(value);
			}
		}
	}
	private static boolean isLateBoundReference(final ESPK pk) {
		if (pk instanceof PortableESPK)
		{
			final PortableESPK portableKey = (PortableESPK) pk;
			final boolean isLateBound = "_lateBoundReference".equals(portableKey.getTypeNameOfReferencedEntity());
			if (isLateBound) {
				LOGGER.info("Late bound reference to " + portableKey.terse() + " expected at run-time");
			}
			return isLateBound;
		}

		return false;
	}
	private void setReferences(File dir, Entity value, com.axway.gw.es.tools.Entity ye) {
		 List<com.vordel.es.Field> refs = value.getReferenceFields(); 
		 for (com.vordel.es.Field field : refs) {
			 ESPK ref = field.getReference(); // just deal with single at the moment
			 if (!ref.equals(EntityStore.ES_NULL_PK)) {
				 String key;
				 if (isLateBoundReference(ref)) {
					key = ref.toString();
				 }
				 else {
					key = createRelativePath(dir, getPath(dir, ref));
				 }
				 EntityField f = new EntityField();
				 f.name = field.getName();
				 f.value = key;
				 ye.fields.add(f);
			 }
		 }
	}

	private String createRelativePath(File base, File path) {
		return base.toURI().relativize(path.toURI()).getPath();
	}
	
	private File getPath(File dir, ESPK pk) {
		List<Entity> path = pathToRoot(pk);
		LOGGER.info("path to root is depth: " + path.size());
		Iterator<Entity> it = path.iterator();
		while (it.hasNext()) {
			Entity entity = it.next();
			String name = getKeyValues(entity);
			dir = new File(dir, name);
			if (!dir.exists() && entity.getType().allowsChildEntities())
				dir.mkdirs();
		}
		return dir;
	}

	private String getKeyValues(Entity e) {
		StringBuffer b = new StringBuffer();
		String[] keyNames = e.getType().getKeyFieldNames();
        if (keyNames != null) {
            for (int i = 0; i < keyNames.length; i++) {
                com.vordel.es.Field keyField = e.getField(keyNames[i]);
                if (null != keyField) {
                	String value = keyField.getValues()[0].toString();
                    b.append(sanitizeFilename(value));
                }
                if (i < keyNames.length-1)
                    b.append(" ,");
            }
        }
        return b.toString();
	}
	/**
	  * replace illegal characters in a filename with "_"
	  * illegal characters :
	  *           : \ / * ? | < > "
	  * @param name
	  * @return
	  */
	private String sanitizeFilename(String name) {
		if (name == null)
			LOGGER.info("hello");
		return name.replaceAll("[\\\\/:\"*?<>|]+", "_");
	}
	
	private void writeType(File dir, String fileName, Type t) throws JsonGenerationException, JsonMappingException, IOException {
		LOGGER.info("Dumping type " + t.getName() +  " to file " + fileName);
		File out = new File(dir, fileName);
		mapper.writeValue(out, t);
	}

	private void loadEntities(ESPK pk) {
		loadEntity(pk);
		Collection<ESPK> children = es.listChildren(pk, null);
		for (ESPK childPK : children)
			loadEntities(childPK);
	}
	
	private void loadEntity(ESPK pk) {
		Entity e = getEntity(pk);
		//LOGGER.info("Loaded entity:  " + TraceEntity.describeEntity(e));
		List<Entity> path = pathToRoot(pk);
		LOGGER.info("path to root is depth: " + path.size());
		Iterator<Entity> it = path.iterator();
		StringBuffer b = new StringBuffer();
        while (it.hasNext()) {
        	Entity entity = it.next();
        	String[] keyNames = entity.getType().getKeyFieldNames();
            Collection<String> keyFieldNames = new HashSet<String>();
            if (keyNames != null) {
                for (int i = 0; i < keyNames.length; i++) {
                    com.vordel.es.Field keyField = entity.getField(keyNames[i]);
                    if (null != keyField) {
                        keyFieldNames.add(keyField.getName());
                        b.append(keyField.getValues()[0]);
                    }
                    if (i < keyNames.length-1)
                        b.append(" ,");
                }
            }
        }        	
		LOGGER.info("path to root: " + b.toString());
	}

	public Entity getEntity(ESPK key) {
		if (entities.containsKey(key))
			return entities.get(key);
		ESPK espk = EntityStoreDelegate.getEntityForKey(es, key);
		Entity e = es.getEntity(espk);
		entities.put(key, e);
		return e;
	}
	
	public List<Entity> pathToRoot(ESPK pk) {
		List<Entity> path = new ArrayList<Entity>();
		if (pk == null)
			return path;
		while (!pk.equals(es.getRootPK())) {
			try {
				Entity e = getEntity(pk);
				path.add(0, e);
				pk = e.getParentPK();
			}
			catch (EntityStoreException p) {
				LOGGER.finer("ESPK not found in current ES: " + pk);
			}
		}
		return path;
	}
}
