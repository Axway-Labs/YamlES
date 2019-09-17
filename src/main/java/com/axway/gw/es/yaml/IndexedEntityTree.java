package com.axway.gw.es.yaml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.vordel.es.ESPK;
import com.vordel.es.Entity;

public class IndexedEntityTree {
    
    public static final List<Entity> EMPTY_LIST = 
        new ArrayList<Entity>(0);
    
    class EntityNode {
        Entity entity;
        EntityNode parent;
        List<EntityNode> children;
        
        public EntityNode(Entity e) {
            this.entity = e;
        }
        
        public void addChild(EntityNode e) {
            if (children == null)
                children = new LinkedList<EntityNode>();
            children.add(e);
            e.parent = EntityNode.this;
        }
        
        public void removeChild(EntityNode e) {
            children.remove(e);
            e.parent = null;
        }
        
        public List<EntityNode> getChildren() {
            return children;
        }
    }

    // A Collection of Entities
    private HashMap<ESPK, EntityNode> keyToEntityNodeMap;
    
    private EntityNode root;
    
    public IndexedEntityTree() {
        keyToEntityNodeMap = new HashMap<ESPK, EntityNode>();
    }
    
    public void reset() {
        keyToEntityNodeMap.clear();
        root = null;
    }

    public void add(ESPK parentPK, Entity child) {
        EntityNode n = new EntityNode(child);        
        
        //if (parentPK == null) {
         //   if (keyToEntityNodeMap.size() > 0 || root != null)
         //       throw new IllegalStateException(
         //               "Tried to add root to an already populated tree");
        if (keyToEntityNodeMap.size() == 0 || root == null)
        	root = n;
        else {
            EntityNode p = keyToEntityNodeMap.get(parentPK);
            p.addChild(n);
        }
        
        keyToEntityNodeMap.put(child.getPK(), n);
    }
    
    public void remove(ESPK pk) {
        EntityNode n = keyToEntityNodeMap.remove(pk);
        // Can't remove the root node, so n.parent never null
        n.parent.removeChild(n);
    }
    
    public void replace(Entity oldEntity, Entity newEntity) {
        EntityNode n = keyToEntityNodeMap.get(oldEntity.getPK());
        n.entity = newEntity.cloneEntity();
    }
    
    public boolean exists(ESPK pk) {
        return keyToEntityNodeMap.containsKey(pk);
    }
    
    public Entity getEntity(ESPK pk) {
        EntityNode node = keyToEntityNodeMap.get(pk);
        return node == null ? null : node.entity;
    }
    
    Collection<Entity> getChildren(ESPK parentPK) {
        EntityNode n = keyToEntityNodeMap.get(parentPK);
        if (n != null) {
            List<IndexedEntityTree.EntityNode> children = n.getChildren();
            if (children != null) {
                EntityCollection entities = new EntityCollection(children.size());
                for (EntityNode node : children)
                    entities.addEntity(node.entity);
                return entities;
            }
        }
        return EMPTY_LIST;
    }
    
    public Collection<EntityNode> getEntityNodes() {
        return keyToEntityNodeMap.values();
    }
    
    public int size() {
        return keyToEntityNodeMap.size();
    }
}
