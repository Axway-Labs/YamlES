package com.axway.gw.es.yaml.testutils.diff;

import com.vordel.es.Entity;

class TargetEntity extends EntityWrapper implements Target {

    public TargetEntity(Entity entity) {
        super(entity);
    }

    @Override
    public String getId() {
        if(isNull()) return null;
        return getWrapped().getPK().toString();
    }
}
