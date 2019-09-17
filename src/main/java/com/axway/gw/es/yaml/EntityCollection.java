package com.axway.gw.es.yaml;

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.vordel.es.Entity;

public class EntityCollection 
extends AbstractCollection<Entity>
{
    private int size;
    private Entity[] entities;

    private class EntityIterator
    implements Iterator<Entity>
    {
        int index = 0;
        EntityCollection c;
        
        EntityIterator(EntityCollection c) {
            this.c = c;
        }
        
        @Override
        public boolean hasNext() {
            return c.size != index;
        }

        @Override
        public Entity next() {
            if (c.size == index)
                throw new NoSuchElementException();
            return c.entities[index++];
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
    }
    
    EntityCollection(int maxSize) {
        entities = new Entity[maxSize];        
    }
    
    @Override
    public Iterator<Entity> iterator() {
        return new EntityIterator(this);
    }

    @Override
    public int size() {
        return size;
    }
    
    void addEntity(Entity entity) {
        entities[size] = entity;
        size++;
    }
}