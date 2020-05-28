package com.axway.gw.es.yaml.testutils.diff;

import com.vordel.es.EntityType;

import java.util.*;

class EntityTypeWrapper extends AbstractWrapper<EntityType> {

    private String name;
    private boolean typeIsAbstract;
    private String superType;
    private Map<String, FieldTypeWrapper> allDeclaredFields;
    private Map<String, FieldWrapper> allDeclaredConstantFields;
    private Map<String, Object> declaredComponentTypes;
    private Collection<String> allDeclaredKeyFields;

    EntityTypeWrapper(EntityType entityType) {
        super(entityType);
        if (!isNull()) {
            superType = entityType.getSuperType() != null ? entityType.getSuperType().getName() : null;
            name = entityType.getName();
            typeIsAbstract = entityType.isAbstract();
            allDeclaredKeyFields = getWrapped().getAllDeclaredKeyFields();
            declaredComponentTypes = getWrapped().getDeclaredComponentTypes();
            allDeclaredFields = new HashMap<>();
            entityType.getAllDeclaredFieldNames().forEach(name -> allDeclaredFields.put(name, new FieldTypeWrapper(entityType.getFieldType(name))));
            allDeclaredConstantFields = new HashMap<>();
            entityType.getAllDeclaredConstantFieldNames().forEach(name -> allDeclaredConstantFields.put(name, new FieldWrapper(entityType.getConstantField(name))));
        }
    }


    public String getSuperType() {
        return superType;
    }

    public boolean isAbstract() {
        return typeIsAbstract;
    }

    public String getName() {
        return name;
    }

    public Map<String, FieldTypeWrapper> getAllDeclaredFields() {
        return allDeclaredFields;
    }

    public Collection<String> getAllDeclaredKeyFields() {
        return allDeclaredKeyFields;
    }

    public Map<String, FieldWrapper> getAllDeclaredConstantFields() {
        return allDeclaredConstantFields;
    }

    public Map<String, Object> getDeclaredComponentTypes() {
        return declaredComponentTypes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        EntityTypeWrapper that = (EntityTypeWrapper) o;
        final boolean nameIsEqual = Objects.equals(name, that.name);
        final boolean superTypeIsEqual = Objects.equals(superType, that.superType);
        final boolean abstractIsEq = typeIsAbstract == that.typeIsAbstract;
        final boolean keyFieldsIsEqual = Objects.equals(allDeclaredKeyFields, that.allDeclaredKeyFields);
        final boolean fieldsIsEqual = Objects.equals(allDeclaredFields, that.allDeclaredFields);
        final boolean constantsIsEqual = Objects.equals(allDeclaredConstantFields, that.allDeclaredConstantFields);
        final boolean componentsIsEqual = Objects.equals(declaredComponentTypes, that.declaredComponentTypes);
        final boolean equality = abstractIsEq && fieldsIsEqual && constantsIsEqual && superTypeIsEqual && nameIsEqual && keyFieldsIsEqual && componentsIsEqual;
        return equality;
    }

    @Override
    public int hashCode() {
        return Objects.hash(allDeclaredFields, allDeclaredConstantFields, superType, typeIsAbstract, name, allDeclaredKeyFields, declaredComponentTypes);
    }
}
