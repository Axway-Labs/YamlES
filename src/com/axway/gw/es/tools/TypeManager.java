package com.axway.gw.es.tools;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.vordel.es.ConstantFieldType;
import com.vordel.es.EntityStore;
import com.vordel.es.EntityType;
import com.vordel.es.FieldType;

public class TypeManager {
	private final static Logger LOGGER = Logger.getLogger(TypeManager.class.getName());

	ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

	// // types map contains root children of the entitystore
	Map<String, Type> types = new HashMap<String, Type>();
	private EntityStore es;

	public TypeManager(EntityStore es) {
		this.es = es;
		EntityType t = es.getBaseType();
		loadTypes(t);
	}

	public void writeTypes(File dir) throws IOException {
		if (!dir.exists())
	        dir.mkdirs();
	 	
		// must provide dir
		if (!dir.isDirectory())
			throw new IOException("Must provide a directory for YAML output");
		for (Map.Entry<String,Type> entry : types.entrySet()) {  
			LOGGER.info("Dumping type " + entry.getKey());
			Type t = entry.getValue();
			writeType(dir, t);
		}
	}

	private void writeType(File dir, Type t) throws JsonGenerationException, JsonMappingException, IOException {
		// if type has no children write it directory
		// if it does contain children then create a dir for the type and write metadata for the 
		// type and it's children to newly created dir
		if (!t.hasChild()) {
			File newDir = getDirForType(dir, t);
			writeType(newDir, t.getName() + ".yaml", t);
		}
		else {
			File newDir = getDirForType(dir, t);
			writeType(newDir, "metadata.yaml", t);
			// write the children 
			List<Type> children = t.getChildren();
			Iterator<Type> it = children.iterator();
	        while (it.hasNext())
	        	writeType(dir, it.next());

		}
	}
	
	File getDirForType(File dir, Type t) {
		List<String> pathFromRoot = t.pathToRoot;
		Iterator<String> it = pathFromRoot.iterator();
        while (it.hasNext()) {
        	String name = it.next();
        	File newDir = new File(dir, name);
			if (!newDir.exists())
				newDir.mkdir();
			dir = newDir;
        }
        if (t.hasChild()) { // leaf
        	File newDir = new File(dir, t.getName());
        	if (!newDir.exists())
        		newDir.mkdir();
        	return newDir;
        }
        return dir;
	}

	private void writeType(File dir, String fileName, Type t) throws JsonGenerationException, JsonMappingException, IOException {
		LOGGER.info("Dumping type " + t.getName() +  " to file " + fileName);
		File out = new File(dir, fileName);
		mapper.writeValue(out, t);
	}

	private void loadTypes(EntityType t) {
		loadType(t);
		for (String typename : es.getSubtypes(t.getName())) {
			EntityType st = es.getTypeForName(typename);
			loadTypes(st);
		}	
	}

	private void loadType(EntityType t) {
		LOGGER.info("Loading type:  " + t.getName());
		Type yamlType = getType(t.getName());
		LOGGER.info("Loaded type: " + t.getName());
	}

	public Type getType(String typeName) {
		if (types.containsKey(typeName))
			return types.get(typeName);
		EntityType type = es.getTypeForName(typeName);
		Type t = createType(type);
		types.put(typeName, t);
		return t;
	}

	private Type createType(EntityType et) {

		Type t = new Type(et.getName());
		if (et.getSuperType() != null) {
			t.parent = et.getSuperType().getName();
			Type parent = getType(et.getSuperType().getName());
			if (parent != null)
				parent.addChild(t);
			t.setPathToRoot(et);
		}
		t.isAbstract = et.isAbstract();
		// fields
		Set<String> fields = et.getAllDeclaredFieldNames();     
		Collection<String> keyFields = et.getAllDeclaredKeyFields();
		Iterator<String> it = fields.iterator();
		while (it.hasNext()) {
			String name = (String)it.next();
			FieldType ft = et.getFieldType(name);
			if (ft instanceof ConstantFieldType) {
				ConstantField f = new ConstantField();
				f.setField(name, (ConstantFieldType)ft);
				t.constants.add(f);
			}
			else {
				Field f = new Field();
				f.setField(name, ft);
				if (keyFields.contains(name))
					f.isKey = true;
				t.fields.add(f);
			}
		}

		// children
		Map<String, Object> components = et.getDeclaredComponentTypes();
		it = components.keySet().iterator();
		while (it.hasNext()) {
			String name = it.next();
			Child child = new Child();
			child.name = name;
			child.cardinality = components.get(name).toString();
			t.component.add(child);
		}
		return t;
	}
}
