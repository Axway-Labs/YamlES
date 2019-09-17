package com.axway.gw.es.yaml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import com.vordel.es.EntityStore;
import com.vordel.es.EntityStoreException;
import com.vordel.es.EntityStoreFactory;
import com.vordel.es.EntityType;
import com.vordel.es.TypeStore;
import com.vordel.es.UnknownTypeException;
import com.vordel.es.impl.StatefulStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Abstract Type store maintains a cache of type 
 * definitions, and asks that any subclasses deal with persisting the type
 * defs.
 * <br>
 * @author Jason Halpin
 * <br> 
 * Copyright(C) Axway 2014
 */
public abstract class AbstractTypeStore extends StatefulStore implements TypeStore {
    private final static Logger LOGGER = LoggerFactory.getLogger(AbstractTypeStore.class.getName());
	 

    protected abstract EntityType retrieveType(String typeName) throws EntityStoreException;
    protected abstract String[] retrieveSubtypes(String superType) throws EntityStoreException;
    protected abstract void deleteType(String typeName) throws EntityStoreException;
    protected abstract void persistType(EntityType type) throws EntityStoreException;

    private static final String DEFAULT_TYPEDEF_ENTITY = "typedocs/Entity.xml";
    private static final String DEFAULT_TYPEDEF_ROOT =    "typedocs/Root.xml";
    private static final String DEFAULT_TYPEDEF_ROOTCHILD = "typedocs/RootChild.xml";

    private EntityType baseType;
    private HashMap<String, EntityType> nameToTypeMap = new HashMap<>();
    private HashMap<String, List<String>> typeToSubtypesMap = new HashMap<>();
    private static final Collection<String> EMPTYCOLLECTION = Collections.emptySet();     

    public EntityType getBaseType() throws EntityStoreException {
        if (baseType == null)
            baseType = getTypeForName(BASE_TYPE_NAME);
        return baseType;
    }

    protected void removeFromCache(EntityType type) {
    	String typeName = type.getName();
        EntityType oldType = nameToTypeMap.remove(typeName);

        // Maintain the extensions mapping
        EntityType s = oldType.getSuperType();
        if (s != null) {
            String superType = s.getName();
            Vector<String> v = (Vector<String>)typeToSubtypesMap.get(superType);
            v.remove(typeName);
        }
    }

    public void addToCache(EntityType type) {
        if (type == null)
            throw new IllegalArgumentException("Cannot add a null type to the cache");

        if (nameToTypeMap.containsKey(type.getName()))
            return;
        
        nameToTypeMap.put(type.getName(), type);
        
        // Maintain the extensions mapping
        EntityType s = type.getSuperType();
        if (s != null) {
            String superType = s.getName();
            List<String> v = typeToSubtypesMap.get(superType);
            if (v == null) {
                v = new Vector<String>();
                typeToSubtypesMap.put(superType, v);
            }
            if (!v.contains(type.getName()))
                v.add(type.getName());
        }
    }
    
    protected void flushTypeCache() throws EntityStoreException {
        baseType = null;
        nameToTypeMap.clear();
        typeToSubtypesMap.clear();        
    }

    private EntityType getFromCache(String typeName) {
        return nameToTypeMap.get(typeName);
    }

    public EntityType getTypeForName(String name) throws EntityStoreException {
        EntityType type = getTypeViaCache(name);
        if (type == null)
            throw new UnknownTypeException(name);
        return type;
    }
    
    private EntityType getTypeViaCache(String name) {
        EntityType type = getFromCache(name);
        if (type == null) {
            type = retrieveType(name);
            if (type != null)
                addToCache(type);
        }
        return type;
    }

    public boolean hasType(String name) {
        return (getTypeViaCache(name) != null);
    }

    public Collection<String> getSubtypes(String type) throws EntityStoreException {
        List<String> v = walkSubtypes(type);
        if (v == null)
            return EMPTYCOLLECTION;
        return v;
    }

    private List<String> walkSubtypes(String type) throws EntityStoreException {
        for (String st : retrieveSubtypes(type))
            if (!nameToTypeMap.keySet().contains(st))
                getTypeForName(st);
        return typeToSubtypesMap.get(type);
    }

    protected List<String> getDescendantTypes(String type) throws EntityStoreException {
        List<String> v = new Vector<String>();
        v = getDescendantTypes(v, type);
        return v;
    }

    private List<String> getDescendantTypes(List<String> v, String type) throws EntityStoreException {
        List<String> subs = walkSubtypes(type);
        if (subs != null) {
            for (int i=0; i<subs.size(); i++) {
                v.add(subs.get(i));
                getDescendantTypes(v, subs.get(i));
            }
        }
        return v;
    }

    public EntityType addType(InputStream stream) throws EntityStoreException {
        EntityType t = EntityStoreFactory.getInstance().getEntityTypeFactory().create(this, stream);
        String typename = t.getName();
        if (getFromCache(typename) != null)
            throw new EntityStoreException("Type already exists - "+typename);
        addType(t);
        return t;
    }

    protected void addType(EntityType t) throws EntityStoreException {
        persistType(t);
        addToCache(t);                
    }

    public EntityType updateType(InputStream stream) throws EntityStoreException {
        EntityType t = EntityStoreFactory.getInstance().getEntityTypeFactory().create(this, stream);
        String typename = t.getName();
        EntityType existingType = null;
        if ((existingType = getFromCache(typename)) == null) {
            existingType = getTypeForName(typename);
            addToCache(existingType);            
        }
        updateType(t);
        return t;
    }
    
    protected void updateType(EntityType t) throws EntityStoreException {
        // Guaranteed to be in the cache at this stage...
        EntityType existingType  = getFromCache(t.getName());

        // Compare the super types so that there's no funny business
        EntityType oldSuper = existingType.getSuperType();
        EntityType newSuper = t.getSuperType();
        if (oldSuper != null && !newSuper.equals(oldSuper))
            LOGGER.info("Replacing type " + existingType + 
            		" which extends " + oldSuper + 
            		" with one which extends " + newSuper);        

        removeFromCache(t);
        addToCache(t);
        
        // Rebuilding the subtypes will force a rewiring to their new supertypes
        // Rebuild the extensions cache first
        walkSubtypes(t.getName());
        List<String> v = typeToSubtypesMap.get(t.getName());
        if (v != null) {
            // Avoid concurrent modification exception
            List<String> sts = new Vector<String>();
            sts.addAll(v);
            for (String subType : sts) {
                EntityType st = nameToTypeMap.get(subType);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                try {
                    st.write(bos);
                    ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
                    st = EntityStoreFactory.getInstance().getEntityTypeFactory().create(this, bis);
                    updateType(st);
                } catch (IOException e) {
                    throw new EntityStoreException("Problem reserializing subtypes", e);
                }
            }
        }
        persistType(t);
    }

    public void reset() throws EntityStoreException {
        checkState();
        flushTypeCache();
        baseType = addTypeFromResource(DEFAULT_TYPEDEF_ENTITY);
        addTypeFromResource(DEFAULT_TYPEDEF_ROOTCHILD);
        addTypeFromResource(DEFAULT_TYPEDEF_ROOT);
    }
    
    private EntityType addTypeFromResource(String resource) {
        Class<?> esClass = EntityStore.class;
        try (InputStream is = esClass.getResourceAsStream(resource)) {
            return addType(is);
        } catch (IOException ioe) {
            throw new EntityStoreException("Couldn't close input stream for resource '" + resource + "'", ioe);
        }
    }

    protected void checkState() {
        // Check that we're connected
        if (getState() == DISCONNECTED)
            throw new IllegalStateException("Cannot access store contents - Disconnected");        
    }
}