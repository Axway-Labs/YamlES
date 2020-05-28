package com.axway.gw.es.yaml.testutils.diff;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vordel.es.ESPK;
import com.vordel.es.Entity;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

abstract class EntityWrapper extends AbstractWrapper<Entity>{

    private String entityType;
    private List<FieldWrapper> fields;

    public EntityWrapper(Entity entity) {
        super(entity);
        if (!isNull()) {
            this.entityType = entity.getType().getName();
            if (this.getWrapped().getAllFields() != null) {
                this.fields = Stream.of(entity.getAllFields()).map(FieldWrapper::new).collect(toList());
            }
        }
    }

    public String getEntityType() {
        return entityType;
    }

    @JsonIgnore
    public ESPK getESPK() {
        return getWrapped().getPK();
    }

    public List<FieldWrapper> getFields() {
        return fields;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false; // we don't compare types to ignore subtypes that bring nothing to the table
        EntityWrapper that = (EntityWrapper) o;
        boolean entityTypeEquals = Objects.equals(entityType, that.entityType);
        boolean fieldsEquals = Objects.equals(fields, that.fields);
        return entityTypeEquals && fieldsEquals;
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityType, fields);
    }


}
