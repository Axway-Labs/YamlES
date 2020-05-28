package com.axway.gw.es.yaml.testutils.diff;

import com.vordel.es.Entity;

class SourceEntity extends EntityWrapper implements Source {

    public SourceEntity(Entity entity) {
        super(entity);
    }

    @Override
    public String getId() {
        if (isNull()) return null;
        return getWrapped().getPK().toString();
    }
}
