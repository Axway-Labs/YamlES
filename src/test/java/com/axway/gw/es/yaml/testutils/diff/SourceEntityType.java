package com.axway.gw.es.yaml.testutils.diff;

import com.vordel.es.EntityType;

public class SourceEntityType extends EntityTypeWrapper implements Source {

    SourceEntityType(EntityType entityType) {
        super(entityType);
    }

    @Override
    public String getId() {
        if(isNull()) return null;
        return getWrapped().getName();
    }

}
