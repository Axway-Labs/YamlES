package com.axway.gw.es.yaml.testutils.diff;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vordel.es.EntityStore;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractESDiff<S extends Source, T extends Target> {

    static final ObjectMapper JSON_EXPORTER = new ObjectMapper();

    static {
        JSON_EXPORTER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
//        JSON_EXPORTER.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
//        JSON_EXPORTER.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
    }

    protected final EntityStore sourceEntityStore;
    protected final EntityStore targetEntityStore;
    protected final List<Diff> diffList = new ArrayList<>();
    protected final Set<String> sourceItems = new HashSet<>();

    protected AbstractESDiff(EntityStore sourceEntityStore, EntityStore targetEntityStore) {
        this.sourceEntityStore = sourceEntityStore;
        this.targetEntityStore = targetEntityStore;
    }


    public int diffCount() {
        return diffList.size();
    }


    public String diffAsJsonString() throws JsonProcessingException {
        return JSON_EXPORTER.writerWithDefaultPrettyPrinter().writeValueAsString(diffList);
    }

    public void dumpDiffJson(File dumpFile) throws IOException {
        JSON_EXPORTER.writerWithDefaultPrettyPrinter().writeValue(dumpFile, diffList);
    }


    public List<Diff> diffList() {
        return diffList;
    }

    protected final void registerSourceAndCompare(S source, T target) {
        checkNotNull(source);
        checkNotNull(target);
        sourceItems.add(source.getId());
        compare(source, target);
    }

    protected final boolean isSourceRegistered(String sourceItemId) {
        return sourceItems.contains(sourceItemId);
    }

    protected final void compare(S sourceItem, T targetItem) {
        checkNotNull(sourceItem);
        checkNotNull(targetItem);

        final Diff diff;

        if (sourceItem.isNull()) {
            if (targetItem.isNull()) {
                diff = null;
            } else {
                // new target entity
                diff = new Diff(sourceItem, targetItem, DiffType.ADDED);
            }
        } else {
            if (targetItem.isNull()) {
                // target is no longer there
                diff = new Diff(sourceItem, targetItem, DiffType.REMOVED);
            } else if (!Objects.equals(sourceItem, targetItem)) { // type are different only by class name.
                diff = new Diff(sourceItem, targetItem, DiffType.MODIFIED);
            } else {
                diff = null;
            }
        }

        if (diff != null) {
            diffList.add(diff);
        }
    }


}
