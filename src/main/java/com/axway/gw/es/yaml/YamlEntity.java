package com.axway.gw.es.yaml;

import com.vordel.es.Entity;
import com.vordel.es.EntityType;

public class YamlEntity extends Entity {

    public YamlEntity(EntityType type) {
        super(type);
    }

    public void setPK(YamlPK pk) {
        this.pk = pk;
    }

    public void setParentPK(YamlPK pk) {
        this.parentPK = pk;
    }

}
