package com.axway.gw.es.yaml.tools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.vordel.es.ESPK;
import com.vordel.es.Entity;
import com.vordel.es.EntityStore;
import com.vordel.es.EntityStoreException;
import com.vordel.es.EntityStoreFactory;
import com.vordel.es.Field;
import com.vordel.es.Value;
import com.vordel.es.util.ShorthandKeyFinder;
import com.vordel.es.xes.ExportEngine;
import com.vordel.es.xes.ExportEngine.Directives;
import com.vordel.es.xes.PortableESPK;
import com.vordel.es.xes.PortableESPKFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteAllEntities {

	private final static Logger LOGGER = LoggerFactory.getLogger(DeleteAllEntities.class);
	
	private EntityStore store;
    private List<ESPK> removedPKs = new ArrayList<ESPK>();
	
    private static final String CUT_SHORT_HAND_KEY = "/[Internationalization]/[InternationalizationCategory]";
    ShorthandKeyFinder shkf; 
    
    public DeleteAllEntities(EntityStore store) {
		this.store = store;
		shkf = new ShorthandKeyFinder(store);
	}
    
    public void clear(Collection<ESPK> rootPKs) throws EntityStoreException {
    	
    	// cut the items with ESPK as a key field
    	List<Entity> entities = shkf.getEntities(CUT_SHORT_HAND_KEY);
        if (!entities.isEmpty()) {
        	for (Entity e : entities) {
        		System.out.println("Delete entity: " + e.getPK());
        		store.deleteEntity(e.getPK());
        	}
        }
        
        for (ESPK key : rootPKs) {
        	removedPKs.clear();
        	// remove the children of the childs of root entities
        	Collection<ESPK> children = store.listChildren(key, null);
        	for (ESPK child : children) {
	        	if (shouldDelete(child)) {
			        // Get the list of all ESPKs to be removed
			        descendAndRecordPKs(child);
			        // Remove the external references
			        descendAndRemoveRefs(child);
			        // Get the portableESPKs for the deletion candidates and
			        // create the deletion directive
			        InputStream is = getDeletionDirective(child);
			        store.importData(is);  
	        	}
        	}
        }
    }
    
    private void descendAndRemoveRefs(ESPK removalPK)
    throws EntityStoreException
    {
        for (ESPK referrer : store.findReferringEntities(removalPK)) {
            boolean mod = false;
            Entity e = store.getEntity(referrer);
            for (Field f : e.getReferenceFields()) {
            	if (!e.getType().isKeyField(f)) { // don't put key refs to null 
	                List<Value> vlist = f.getValueList();
	                for (int i=0; i<vlist.size(); i++) {
	                    ESPK ref = vlist.get(i).getRef();
	                    if (removalPK.equals(ref) && !removedPKs.contains(referrer)) {
	                        mod = true;
	                        vlist.set(i, new Value(EntityStore.ES_NULL_PK));
	                    }
	                }
	                f.setValues(vlist);
            	}
            }
            if (mod)
                store.updateEntity(e);
        }
        for (ESPK childPK : store.listChildren(removalPK, null))
            descendAndRemoveRefs(childPK);
    }
    
    private void descendAndRecordPKs(ESPK pk) throws EntityStoreException {    	
        for (ESPK childPK : store.listChildren(pk, null))
            descendAndRecordPKs(childPK);
        removedPKs.add(pk);
    }
    
    private InputStream getDeletionDirective(ESPK key) throws EntityStoreException {
    	
        ExportEngine ee = new ExportEngine(store);
        Directives d = new Directives();
        PortableESPK removePPK = 
            PortableESPKFactory.newInstance().createPortableESPK(store, key);
        d.removalBranches.add(removePPK);        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Set<ESPK> pks = new HashSet<ESPK>();
        ee.exportEntitiesWithImportDirectives(bos, pks, d);
        byte[] bytes = bos.toByteArray();
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        return bis;
    }
   
    private boolean shouldDelete(ESPK pk) {
    	Entity e = store.getEntity(pk);
    	return !e.getType().getName().equalsIgnoreCase("ESConfiguration");
    }
    
	public static void main(String[] args) {

		if (args.length != 1) {
			LOGGER.info("usage: federated:file:/path-to-existing-fed.xml");
			System.exit(1);
		}
		
		String fed = args[0]; // "federated:file:/Users/dmckenna/Documents/GitHub/fed/configs.xml";
		EntityStore es = null;
		try {
			es = EntityStoreFactory.createESForURL(fed);
			es.connect(fed, new Properties());
			
			
			DeleteAllEntities del = new DeleteAllEntities(es);
			del.clear(es.listChildren(es.getRootPK(), null));
		}
		catch (Throwable exp) {
			exp.printStackTrace();
		}
	}

}
