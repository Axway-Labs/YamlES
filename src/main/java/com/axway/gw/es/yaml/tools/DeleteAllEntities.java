package com.axway.gw.es.yaml.tools;

import com.vordel.es.*;
import com.vordel.es.util.ShorthandKeyFinder;
import com.vordel.es.xes.ExportEngine;
import com.vordel.es.xes.ExportEngine.Directives;
import com.vordel.es.xes.PortableESPK;
import com.vordel.es.xes.PortableESPKFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

public class DeleteAllEntities {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteAllEntities.class);

    private final EntityStore store;
    private final List<ESPK> removedPKs = new ArrayList<>();

    private static final String CUT_SHORT_HAND_KEY = "/[Internationalization]/[InternationalizationCategory]";
    private final ShorthandKeyFinder shkf;

    public DeleteAllEntities(EntityStore store) {
        this.store = store;
        this.shkf = new ShorthandKeyFinder(store);
    }

    public void clear(Collection<ESPK> rootPKs) {

        // cut the items with ESPK as a key field
        List<Entity> entities = shkf.getEntities(CUT_SHORT_HAND_KEY);
        if (!entities.isEmpty()) {
            for (Entity e : entities) {
                LOGGER.info("Deleting entity: {}", e.getPK());
                store.deleteEntity(e.getPK());
            }
        }

        for (ESPK key : rootPKs) {
            removedPKs.clear();
            // remove the children of the childs of root entities
            Collection<ESPK> children = store.listChildren(key, null);
            for (ESPK child : children) {
                if (shouldDelete(child)) {
                    // Get the list of all ESPKs to be removed
                    descendAndRecordPKs(child);
                    // Remove the external references
                    descendAndRemoveRefs(child);
                    // Get the portableESPKs for the deletion candidates and
                    // create the deletion directive
                    InputStream is = getDeletionDirective(child);
                    store.importData(is);
                }
            }
        }
    }

    private void descendAndRemoveRefs(ESPK removalPK) {
        store.findReferringEntities(removalPK)
                .forEach(referringEntity -> {
                    AtomicBoolean isModified = new AtomicBoolean(false);
                    Entity entity = store.getEntity(referringEntity);
                    entity.getReferenceFields().forEach(field -> {
                        if (!entity.getType().isKeyField(field)) { // don't put key refs to null
                            List<Value> values = field.getValueList();
                            setMatchingValuesAsNull(ref -> removalPK.equals(ref) && !removedPKs.contains(referringEntity), isModified, values);
                            field.setValues(values);
                        }
                    });
                    if (isModified.get())
                        store.updateEntity(entity);
                });
        for (ESPK childPK : store.listChildren(removalPK, null))
            descendAndRemoveRefs(childPK);
    }

    private void setMatchingValuesAsNull(Predicate<ESPK> predicate, AtomicBoolean modified, List<Value> fieldValues) {
        for (int i = 0; i < fieldValues.size(); i++) {
            ESPK ref = fieldValues.get(i).getRef();
            if (predicate.test(ref)) {
                modified.set(true);
                fieldValues.set(i, new Value(EntityStore.ES_NULL_PK));
            }
        }
    }

    private void descendAndRecordPKs(ESPK pk) {
        for (ESPK childPK : store.listChildren(pk, null))
            descendAndRecordPKs(childPK);
        removedPKs.add(pk);
    }

    private InputStream getDeletionDirective(ESPK key) {

        ExportEngine ee = new ExportEngine(store);
        Directives d = new Directives();
        PortableESPK removePPK =
                PortableESPKFactory.newInstance().createPortableESPK(store, key);
        d.removalBranches.add(removePPK);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Set<ESPK> pks = new HashSet<>();
        ee.exportEntitiesWithImportDirectives(bos, pks, d);
        return new ByteArrayInputStream(bos.toByteArray());
    }

    private boolean shouldDelete(ESPK pk) {
        Entity e = store.getEntity(pk);
        return !e.getType().getName().equalsIgnoreCase("ESConfiguration");
    }

    public static void main(String[] args) {

        if (args.length != 1) {
            LOGGER.info("usage: federated:file:/path-to-existing-fed.xml");
            System.exit(1);
        }

        String fed = args[0];
        EntityStore es = EntityStoreFactory.createESForURL(fed);
        es.connect(fed, new Properties());

        DeleteAllEntities del = new DeleteAllEntities(es);
        del.clear(es.listChildren(es.getRootPK(), null));

    }

}
