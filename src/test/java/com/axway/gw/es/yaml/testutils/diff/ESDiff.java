package com.axway.gw.es.yaml.testutils.diff;

import com.vordel.es.ESPK;
import com.vordel.es.Entity;
import com.vordel.es.EntityStore;

import java.util.*;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

public class ESDiff extends AbstractESDiff<SourceEntity, TargetEntity> {

    private final Function<ESPK, ESPK> sourceToTargetPkResolver;
    private final Function<ESPK, ESPK> targetToSourcePkResolver;

    private ESDiff(EntityStore sourceEntityStore, EntityStore targetEntityStore, Function<ESPK, ESPK> sourceToTargetPkResolver, Function<ESPK, ESPK> targetToSourcePkResolver) {
        super(sourceEntityStore, targetEntityStore);
        this.sourceToTargetPkResolver = sourceToTargetPkResolver;
        this.targetToSourcePkResolver = targetToSourcePkResolver;
    }

    public static ESDiff diff(EntityStore sourceEntityStore, EntityStore targetEntityStore) {
        return diff(sourceEntityStore, targetEntityStore, Function.identity(), Function.identity());
    }

    public static ESDiff diff(EntityStore sourceEntityStore, EntityStore targetEntityStore, Function<ESPK, ESPK> sourceToTargetPkResolver, Function<ESPK, ESPK> targetToSourcePkResolver) {
        ESDiff diff = new ESDiff(sourceEntityStore, targetEntityStore, sourceToTargetPkResolver, targetToSourcePkResolver);

        // compare root Pk
        final Entity sourceRoot = sourceEntityStore.getEntity(sourceEntityStore.getRootPK());
        final Entity targetRoot = targetEntityStore.getEntity(targetEntityStore.getRootPK());
        diff.registerSourceAndCompare(new SourceEntity(sourceRoot), new TargetEntity(targetRoot));

        // descend the tree and compare with targetEntityStore
        diff.compareChildren(sourceEntityStore.getRootPK());

        // find orphans in targetEntityStore
        diff.findOrphans(targetEntityStore.getRootPK());

        return diff;
    }

    private void compareChildren(ESPK parentPk) {
        final Collection<ESPK> sourceChildren = sourceEntityStore.findChildren(parentPk, null, null);
        if (sourceChildren != null && !sourceChildren.isEmpty()) {
            sourceChildren.stream()
                    .map(sourceEntityStore::getEntity)
                    .map(SourceEntity::new)
                    .forEach(srcEntity -> {
                        ESPK matchingTargetESPK = sourceToTargetPkResolver.apply(srcEntity.getESPK());
                        final TargetEntity targetEntity = new TargetEntity(targetEntityStore.getEntity(matchingTargetESPK));
                        registerSourceAndCompare(srcEntity, targetEntity);
                        compareChildren(srcEntity.getESPK());
                    });
        }
    }

    private void findOrphans(ESPK parentPk) {
        final Collection<ESPK> targetChildrenPKs = targetEntityStore.findChildren(parentPk, null, null);
        if (targetChildrenPKs != null && !targetChildrenPKs.isEmpty()) {
            targetChildrenPKs.stream()
                    .map(targetEntityStore::getEntity)
                    .map(TargetEntity::new)
                    .forEach(targetEntity -> {
                        final ESPK matchingSourceESPK = targetToSourcePkResolver.apply(targetEntity.getESPK());
                        final String sourceId = matchingSourceESPK != null ? matchingSourceESPK.toString() : null;
                        if (!isSourceRegistered(sourceId)) {
                            compare(new SourceEntity(null), targetEntity);
                        }
                        findOrphans(targetEntity.getESPK());
                    });
        }
    }


}
