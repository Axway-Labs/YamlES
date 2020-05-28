package com.axway.gw.es.yaml.testutils.diff;

import com.vordel.es.EntityStore;
import com.vordel.es.EntityType;

import static com.google.common.base.Preconditions.checkNotNull;

public class ESTypeDiff extends AbstractESDiff<SourceEntityType, TargetEntityType> {

    private ESTypeDiff(EntityStore source, EntityStore target) {
        super(source, target);
    }

    public static ESTypeDiff diff(EntityStore source, EntityStore target) {
        ESTypeDiff diff = new ESTypeDiff(source, target);

        // compare baseTypes
        final EntityType sourceBaseType = source.getBaseType();
        final EntityType targetBaseType = target.getBaseType();
        diff.registerSourceAndCompare(new SourceEntityType(sourceBaseType), new TargetEntityType(targetBaseType));

        // descend the tree and compare with target
        diff.compareChildren(sourceBaseType);

        // find orphans in target
        diff.findOrphans(targetBaseType);

        return diff;
    }

    private void compareChildren(EntityType sourceBaseType) {
        for (String subtypeName : sourceEntityStore.getSubtypes(sourceBaseType.getName())) {
            EntityType sourceType = sourceEntityStore.getTypeForName(subtypeName);
            EntityType targetType = targetEntityStore.getTypeForName(subtypeName);
            registerSourceAndCompare(new SourceEntityType(sourceType), new TargetEntityType(targetType));
            compareChildren(sourceType);
        }
    }

    private void findOrphans(EntityType targetBaseType) {
        for (String subtypeName : targetEntityStore.getSubtypes(targetBaseType.getName())) {
            final EntityType targetSubType = targetEntityStore.getTypeForName(subtypeName);
            if(!isSourceRegistered(subtypeName)) {
                // target do not exists in the source ES
                compare(new SourceEntityType(null), new TargetEntityType(targetSubType));
            }
            findOrphans(targetSubType);
        }
    }




}
