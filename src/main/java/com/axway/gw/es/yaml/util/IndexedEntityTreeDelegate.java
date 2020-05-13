package com.axway.gw.es.yaml.util;

import com.vordel.es.ESPK;
import com.vordel.es.Entity;
import com.vordel.es.provider.file.IndexedEntityTree;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

public class IndexedEntityTreeDelegate {

    private IndexedEntityTree delegate;

    public IndexedEntityTreeDelegate(IndexedEntityTree delegate) {
        this.delegate = delegate;
    }

    public void reset() {
        delegate.reset();
    }

    public void add(ESPK parentPK, Entity child) {
        delegate.add(parentPK, child);
    }

    public void remove(ESPK pk) {
        delegate.remove(pk);
    }

    public void replace(Entity oldEntity, Entity newEntity) {
        delegate.replace(oldEntity, newEntity);
    }

    public boolean exists(ESPK pk) {
        return delegate.exists(pk);
    }

    public Entity getEntity(ESPK pk) {
        return delegate.getEntity(pk);
    }

    public Collection<Entity> getChildren(ESPK parentPK) {
        try {
            @SuppressWarnings("unchecked") final Collection<Entity> result = (Collection<Entity>) getGetGetChildrenMethod().invoke(delegate, parentPK);
            return result;
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new UnsupportedOperationException("Cannot call getChildren", e);
        }
    }

    public int size() {
        return delegate.size();
    }

    private Method getGetGetChildrenMethod() {

        try {
            Method method = IndexedEntityTree.class.getDeclaredMethod("getChildren", ESPK.class);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            throw new UnsupportedOperationException("Cannot access getChildren method", e);
        }
    }

}
