package com.axway.gw.es.yaml.utils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.zjsonpatch.JsonDiff;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.vordel.es.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toList;

public class ESDiff {

    private static final ObjectMapper JSON_EXPORTER = new ObjectMapper();

    static {
        JSON_EXPORTER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
//        JSON_EXPORTER.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
//        JSON_EXPORTER.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
    }

    private final EntityStore source;
    private final EntityStore target;
    private final List<Diff> diffList = new ArrayList<>();
    private final Set<ESPK> sourceESPK = new HashSet<>();
    private final Function<String, ESPK> sourceToTargetPkResolver;

    private ESDiff(EntityStore source, EntityStore target, Function<String, ESPK> sourceToTargetPkResolver) {
        this.source = source;
        this.target = target;
        this.sourceToTargetPkResolver = sourceToTargetPkResolver;
    }

    public static ESDiff diff(EntityStore source, EntityStore target, Function<String, ESPK> sourceToTargetPkResolver) {
        ESDiff diff = new ESDiff(source, target, sourceToTargetPkResolver);

        // compare root Pk
        final Entity sourceRoot = source.getEntity(source.getRootPK());
        final Entity targetRoot = target.getEntity(target.getRootPK());
        diff.registerSourceESPKAndCompare(new SourceEntity(sourceRoot), new TargetEntity(targetRoot));

        // descend the tree and compare with target
        diff.compareChildren(source.getRootPK());

        // find orphans in target
        diff.findOrphans(target.getRootPK());

        return diff;
    }

    private void compareChildren(ESPK parentPk) {
        final Collection<ESPK> sourceChildren = source.findChildren(parentPk, null, null);
        if (sourceChildren != null && !sourceChildren.isEmpty()) {
            sourceChildren.stream()
                    .map(source::getEntity)
                    .map(SourceEntity::new)
                    .forEach(srcEntity -> {
                        ESPK targetESPK = sourceToTargetPkResolver.apply(srcEntity.getESPK().toString());
                        final TargetEntity targetEntity = new TargetEntity(target.getEntity(targetESPK));
                        registerSourceESPKAndCompare(srcEntity, targetEntity);
                        compareChildren(srcEntity.getESPK());
                    });
        }
    }

    private void findOrphans(ESPK parentPk) {
        final Collection<ESPK> targetChildren = target.findChildren(parentPk, null, null);
        if (targetChildren != null && !targetChildren.isEmpty()) {
            targetChildren.stream().map(target::getEntity).map(TargetEntity::new).forEach(targetEntity -> {
                if (!sourceESPK.contains(targetEntity.getESPK())) {
                    diffList.add(compare(new SourceEntity(null), targetEntity));
                }
                findOrphans(targetEntity.getESPK());
            });
        }
    }

    private void registerSourceESPKAndCompare(SourceEntity source, TargetEntity target) {
        checkNotNull(source);
        sourceESPK.add(source.getEntity().getPK());
        final Diff diff = compare(source, target);
        if (diff != null) {
            diffList.add(diff);
        }
    }

    public int diffCount() {
        return diffList.size();
    }


    public String diffAsJsonString() throws JsonProcessingException {
        return JSON_EXPORTER.writerWithDefaultPrettyPrinter().writeValueAsString(diffList);
    }

    public void dumpDiffJson(File dumpeFile) throws IOException {
        JSON_EXPORTER.writerWithDefaultPrettyPrinter().writeValue(dumpeFile, diffList);
    }


    public List<Diff> diffList() {
        return diffList;
    }

    public Diff compare(SourceEntity sourceEntity, TargetEntity targetEntity) {
        checkNotNull(sourceEntity);
        checkNotNull(targetEntity);
        if (sourceEntity.isNull()) {
            if (targetEntity.isNull()) {
                return null;
            } else {
                // new target entity
                return new Diff(sourceEntity, targetEntity, DiffType.ADDED);
            }
        } else {
            if (targetEntity.isNull()) {
                // target is no longer there
                return new Diff(sourceEntity, targetEntity, DiffType.REMOVED);
            } else if (!Objects.equals(sourceEntity, targetEntity)) { // type are different only by class name.
                return new Diff(sourceEntity, targetEntity, DiffType.MODIFIED);
            } else {
                return null;
            }
        }
    }

    public static class Diff {

        final SourceEntity sourceEntity;
        final TargetEntity targetEntity;
        final DiffType diffType;

        private Diff(SourceEntity sourceEntity, TargetEntity targetEntity, DiffType diffType) {
            this.sourceEntity = sourceEntity;
            this.targetEntity = targetEntity;
            this.diffType = diffType;
        }

        public DiffType getDiffType() {
            return diffType;
        }

        public SourceEntity getSourceEntity() {
            return sourceEntity;
        }

        public TargetEntity getTargetEntity() {
            return targetEntity;
        }

        public JsonNode getDiff() {
            if (diffType == DiffType.MODIFIED) {
                final JsonNode sourceNode = JSON_EXPORTER.valueToTree(getSourceEntity());
                final JsonNode targetNode = JSON_EXPORTER.valueToTree(getTargetEntity());
                return JsonDiff.asJson(sourceNode, targetNode);
            }
            return null;
        }
    }

    public enum DiffType {
        ADDED, REMOVED, MODIFIED
    }

    public static abstract class EntityWrapper {

        private final Entity entity;
        private String entityType;
        private List<FieldWrapper> fields;

        public EntityWrapper(Entity entity) {
            this.entity = entity;
            if (!isNull()) {
                this.entityType = entity.getType().getName();
                if (this.entity.getAllFields() != null) {
                    this.fields = Stream.of(entity.getAllFields()).map(FieldWrapper::new).collect(toList());
                }
            }
        }

        @JsonIgnore
        public Entity getEntity() {
            return entity;
        }

        public boolean isNull() {
            return entity == null;
        }

        public String getEntityType() {
            return entityType;
        }

        @JsonIgnore
        public ESPK getESPK() {
            return entity.getPK();
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

    public static class FieldWrapper {
        private final Field field;
        private final List<Value> values;

        FieldWrapper(Field field) {
            this.field = field;
            this.values = field.getValueList();
        }

        public String getName() {
            return field.getName();
        }

        public String getType() {
            return field.getType().getType();
        }

        public String getTypeCardinality() {
            return Objects.toString(field.getType().getCardinality());
        }

        public boolean isRefType() {
            return field.isRefType();
        }

        public boolean isSoftRefType() {
            return field.getType().isSoftRefType();
        }

        public List<Value> getTypeDefaultValues() {
            return field.getType().getDefaultValues();
        }

        public String getReference() {
            if (isRefType()) {
                return field.getReference().toString();
            } else {
                return null;
            }
        }

        public List<String> getRefs() {
            return field.getRefs()
                    .stream()
                    .filter(Objects::nonNull)
                    .map(Objects::toString)
                    .collect(toList());
        }

        public List<ValueWrapper> getValues() {
            return values.stream()
                    .map(ValueWrapper::new)
                    .collect(toList());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FieldWrapper that = (FieldWrapper) o;
            boolean fieldEquals = field.equals(that.field);
            boolean valueEquals = Objects.equals(values, that.values);
            return fieldEquals && valueEquals;
        }

        @Override
        public int hashCode() {
            return Objects.hash(field, values);
        }

    }

    public static class ValueWrapper {
        private final Value value;

        public ValueWrapper(Value value) {
            this.value = value;
        }

        public String getRef() {
            return value.getRef() != null ? value.toString() : null;
        }

        public String getData() {
            return value.getData();
        }

        public boolean isNull() {
            return value.isNull();
        }
    }

    private static class TargetEntity extends EntityWrapper {
        public TargetEntity(Entity entity) {
            super(entity);
        }
    }

    private static class SourceEntity extends EntityWrapper {
        public SourceEntity(Entity entity) {
            super(entity);
        }
    }

}
