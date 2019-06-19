package com.axway.gw.es.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Properties;

import com.vordel.es.ESPK;
import com.vordel.es.Entity;
import com.vordel.es.EntityStore;
import com.vordel.es.EntityStoreDelegate;
import com.vordel.es.EntityStoreFactory;
import com.vordel.es.EntityType;
import com.vordel.es.Value;

public class CloneES {
	
	private static final String ES_TO_STORE = "federated:file:/C:/support/hack/configs.xml";
	private static final String ES_TO_LOAD = "yaml:file:/C:/vordel/es/";
	// private static final String ES_TO_LOAD = "yaml:file:/C:/support/shaw/YamlES/";
	
	EntityStore source;
	EntityStore destination;
	
	HashMap<ESPK, Entity> addedEntities = new HashMap<ESPK, Entity>(); 
	
	public CloneES(EntityStore source, EntityStore destination) {
		this.source = source;
		this.destination = destination;
		addedEntities.put(source.getRootPK(), 
				EntityStoreDelegate.getEntity(destination, destination.getRootPK())); 
	}
	
	private void cloneEntities(ESPK pk, ESPK parent) {
		Entity added = cloneEntity(pk, parent);
		Collection<ESPK> children = source.listChildren(pk, null);
		for (ESPK childPK : children) 
			cloneEntities(childPK, added.getPK());
	}
	
	private Entity cloneEntity(ESPK keyToClone, ESPK parent) {
		ESPK espk = EntityStoreDelegate.getEntityForKey(source, keyToClone);
		Entity e = source.getEntity(espk);
		if (addedEntities.containsKey(espk))
			return addedEntities.get(espk);
		
		Entity newEntity = e.cloneEntityFields();
		String fieldName = getKeyField(newEntity);
		String value = newEntity.getStringValue(fieldName);
		System.out.println("Processing " + value);
		Collection<ESPK> children = findNamedChildren(newEntity, parent, destination);
		ESPK pk = null;
		if (children != null && children.size() > 0)  // update
			pk = updateEntity(newEntity, children.iterator().next(), destination);
		else // add
			pk = destination.addEntity(parent, newEntity);
			
		Entity entity = destination.getEntity(pk);
		addedEntities.put(espk, entity);
		return entity;
	}
	
	private ESPK updateEntity(Entity source, ESPK pk, EntityStore es) {
		Entity dest = es.getEntity(pk);
		com.vordel.es.Field[] fields = source.getInstanceFields();
		 for (com.vordel.es.Field f : fields) 
			 dest.setField(f);
		es.updateEntity(dest);
		return dest.getPK();
	}
	
	Collection<ESPK> findNamedChildren(Entity e, ESPK parent, EntityStore es) {
		EntityType type = destination.getTypeForName(e.getType().getName());
		String[] keyFields = type.getKeyFieldNames();
		ArrayList<com.vordel.es.Field> fields = new ArrayList<com.vordel.es.Field>();
		for (String fieldName : keyFields) {
			com.vordel.es.Field field =  new com.vordel.es.Field(type.getFieldType(fieldName), fieldName);
			String value = e.getStringValue(fieldName);
	        field.addValue(new Value(value));
	        fields.add(field);
		}
		com.vordel.es.Field[] fs = fields.toArray(new com.vordel.es.Field[fields.size()]);
	  return es.findChildren(parent, fs, type);
	}
	
	String getKeyField(Entity e) {
		EntityType type = e.getType();
		String[] keyFields = type.getKeyFieldNames();
		return keyFields[0];
	}
	
	public static void main(String[] args) {
		try {
			EntityStore dest = EntityStoreFactory.createESForURL(ES_TO_STORE);
			dest.connect(ES_TO_STORE, new Properties());
			
			EntityStore source = EntityStoreFactory.createESForURL(ES_TO_LOAD);
			source.connect(ES_TO_LOAD, new Properties());
			
			CloneES fed = new CloneES(source, dest);
			fed.cloneEntities(source.getRootPK(), dest.getRootPK());			
		}
		catch (Throwable exp) {
			exp.printStackTrace();
		}
	}
}
