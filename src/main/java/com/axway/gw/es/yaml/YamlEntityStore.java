package com.axway.gw.es.yaml;

import com.axway.gw.es.model.entity.EntityDTO;
import com.axway.gw.es.model.type.TypeDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.vordel.es.*;
import com.vordel.es.impl.AbstractTypeStore;
import com.vordel.es.impl.EntityTypeMap;
import com.vordel.es.provider.file.IndexedEntityTree;
import com.vordel.es.util.ESPKCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

@SuppressWarnings("WeakerAccess")
public class YamlEntityStore extends AbstractTypeStore implements EntityStore {

    private static final Logger log = LoggerFactory.getLogger(YamlEntityStore.class);

    private final EntityTypeMap typeMap = new EntityTypeMap();

    public static final String SCHEME = "yaml:";

    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());

    private File rootLocation = null;
    private ESPK root;

    private Map<String, TypeDTO> types = new LinkedHashMap<>();

    private IndexedEntityTree entities = new IndexedEntityTree();

    /**
     * Connect to an entity store.
     *
     * @param url         The Url of the EntityStore's backend provider.
     * @param credentials A property list of credentials required to connect. Will usually
     *                    contain a minimum of a username and password, but may contain other
     *                    implementation specific properties, such as key locations for SSL etc.
     *                    See the specific implementations of EntityStore for details as to the
     *                    contents of these properties.
     */
    @Override
    public void connect(String url, Properties credentials) throws EntityStoreException {
        if (url == null || url.length() <= 0)
            throw new EntityStoreException("Provide URL to the directory for the yaml store");

        if (url.startsWith(SCHEME) && url.length() > SCHEME.length())
            url = url.substring(SCHEME.length());
        else
            throw new EntityStoreException("Invalid URL: " + url);
        log.info("Loading the url " + url);
        try {
            rootLocation = new File(new URL(url).getFile());
        } catch (MalformedURLException e) {
            throw new EntityStoreException("Unable to parse URL " + e.getMessage());
        }
        try {
            loadTypes();
            loadEntities();
        } catch (IOException e) {
            log.error("Error opening yaml store", e);
            throw new EntityStoreException("Error opening yaml store", e);
        }
    }

    void setRootLocation(File rootLocation) {
        this.rootLocation = rootLocation;
    }

    public void loadEntities() throws EntityStoreException {
        if (rootLocation == null)
            throw new EntityStoreException("root directory not set");
        // for the moment load everything into memory rather than ondemmand
        try {
            root = loadEntities(rootLocation, root);
        } catch (IOException e) {
            throw new EntityStoreException(e.getMessage());
        }
    }

    public ESPK loadEntities(File dir, ESPK parentPK) throws EntityStoreException, IOException {
        if (dir == null)
            throw new EntityStoreException("no directory to load entities from");
        log.info("loading files from " + dir);

        // create the parent entity using metadata.yaml
        Entity entity = createParentEntity(dir, parentPK);
        if (entity != null) {
            parentPK = entity.getPK();
        }

        if (dir.toString().contains("Certificate Store")) {
            System.out.println("YamlEntityStore.loadEntities");
        }
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.toString().endsWith("metadata.yaml") || file.toString().endsWith("META-INF")) {
                // skip over metadata.yaml
                continue;
            }
            if (file.isDirectory()) {
                loadEntities(file, parentPK);
            } else if (file.toString().endsWith(".yaml")) {
                createEntity(file, parentPK);
            }
        }

        return parentPK;
    }

    Entity createEntity(File file, ESPK parentPK) throws IOException {
        EntityDTO entityDTO = YAML_MAPPER.readValue(file, EntityDTO.class);
        File dir = file.getParentFile();

        Entity entity = createEntity(entityDTO, parentPK, dir);

        if (entityDTO.getChildren() != null) {
            for (Map.Entry<String, EntityDTO> entry : entityDTO.getChildren().entrySet()) {
                createEntity(entry.getValue(), entity.getPK(), dir);
            }
        }
        return entity;
    }

    private Entity createEntity(EntityDTO entityDTO, ESPK parentPK, File dir) throws IOException {
        entityDTO.getMeta().setTypeDTO(types.get(entityDTO.getMeta().getType()));
        Entity entity = EntityFactory.convert(entityDTO, parentPK, this, dir);
        entities.add(parentPK, entity);
        return entity;
    }

    private Entity createParentEntity(File dir, ESPK parentPK) throws IOException {
        File metadata = new File(dir, "metadata.yaml");
        if (metadata.exists()) {
            return createEntity(metadata, parentPK);
        }
        return null;
        // String name = dir.getName();
        //throw new EntityStoreException("no metadata file for the entity " + name);
    }

    public void loadTypes() throws EntityStoreException, IOException {
        if (rootLocation == null)
            throw new EntityStoreException("root directory not set");
        File root = new File(rootLocation, "META-INF/Types.yaml");
        TypeDTO typeDTO = YAML_MAPPER.readValue(root, TypeDTO.class);
        // for the moment load everything into memory rather than ondemmand
        loadType(typeDTO, null);
    }

    private YamlEntityType loadType(TypeDTO typeDTO, YamlEntityType parent) throws EntityStoreException {
        types.put(typeDTO.getName(), typeDTO);
        YamlEntityType type = YamlEntityType.convert(typeDTO);
        type.setSuperType(parent);
        addType(type);
        for (TypeDTO yChild : typeDTO.getChildren()) {
            loadType(yChild, type);
        }
        return type;
    }

    /**
     * Get the identifier for the root Entity in the Store. Always returns
     * a valid PK, or throws an IllegalStateException if the underlying provider
     * exists but is not yet initialized.
     *
     * @return The root Entity identifier
     */
    @Override
    public ESPK getRootPK() {
        return root;
    }

    /**
     * Get a particular Entity from the Store, depending on its key.
     *
     * @param pk The unique identifier for the Entity
     * @return An Entity
     * @throws EntityStoreException If the specified Entity doesn't exist
     */
    @Override
    public Entity getEntity(ESPK pk) throws EntityStoreException {
        return entities.getEntity(pk);
    }


    /**
     * Retrieve the subordinate Entities of the identified parent.
     *
     * @param pk                 The identifier of the Entities you wish to retrieve.
     * @param requiredFieldNames By specifying this, you can limit the amount of data
     *                           actually retrieved by the server.
     * @return An Entity
     */
    @Override
    public Entity getEntity(ESPK pk, String[] requiredFieldNames) throws EntityStoreException {
        return getEntity(pk);
    }

    /**
     * Add an Entity to the Store.
     *
     * @param parentPK The identifier of the candidate parent Entity to this new Entity
     * @param entity   The entity you wish to store.
     * @return The identity of the new entity in the EntityStore. The Entity itself
     * will also be updated with this identifier, so successive calls to
     * {@link Entity#getPK() } will return the new identifier
     * @throws DuplicateKeysException If an entity with the same keys already exists in the store.
     */
    @Override
    public ESPK addEntity(ESPK parentPK, Entity entity) throws EntityStoreException {
        return null;
    }

    /**
     * Replace an entity
     *
     * @param entity The candidate entity for replacement. The entity must already have
     *               been initialized via a call to getEntity, and its PK will be already
     *               set.
     */
    @Override
    public void updateEntity(Entity entity) throws EntityStoreException {
    }

    /**
     * Delete an entity from the data store.
     *
     * @param pk Identity of the Entity to be deleted
     */
    @Override
    public void deleteEntity(ESPK pk) throws EntityStoreException {

    }

    /**
     * List all children of a particular Entity.
     *
     * @param pk   The entity id of the parent whose children you wish to list
     * @param type Indicate the type of children you are interested in. "null"
     *             indicates that you wish to retrieve children of all types.
     * @return A Collection of zero or more child ESPKs
     */
    @Override
    public Collection<ESPK> listChildren(ESPK pk, EntityType type) throws EntityStoreException {
        return findChildren(pk, null, type);
    }

    /**
     * Find all children of a particular Entity.
     *
     * @param parentId  The entity id of the parent whose children you wish to get
     * @param reqFields The Fields (including their values) you wish to do the search on. The
     *                  fields can have their data and/or references set.
     * @param type      Search only for Entities of the specified type.
     * @return A Collection of zero or more child ESPKs
     * @throws EntityStoreException If the pk is invalid.
     */
    @Override
    public Collection<ESPK> findChildren(ESPK parentId, Field[] reqFields, EntityType type) {
        Collection<Entity> children = entities.getChildren(parentId);
        ESPKCollection resList = new ESPKCollection(children.size());
        for (Entity e : children) {
            if (type == null || type.isAncestorOfType(e.getType())) {
                // We are looking for specific fields to match
                if (reqFields != null) {
                    if (e.containsFieldsWithDisjunctValues(reqFields))
                        resList.addESPK(e.getPK());
                } else {
                    resList.addESPK(e.getPK());
                }
            }
        }
        return resList;
    }







    /**
     * Get a Set of ESPKs which identify all Entities in the store which
     * contain a field or fields which hold a reference to the Entity specified
     *
     * @param targetEntityPK The ESPK of the Entity to which other Entities
     *                       may refer.
     * @return A Set of ESPKs of any Entities which hold a reference to the
     * target Entity.
     * @throws EntityStoreException If the PK is unknown.
     */
    @Override
    public Collection<ESPK> findReferringEntities(ESPK targetEntityPK) throws EntityStoreException {
        return null;
    }

    /**
     * Disconnect from the EntityStore. Releases any resources which may
     * have been associated with the connection.
     */
    @Override
    public void disconnect() throws EntityStoreException {

    }

    /**
     * Connect to the EntityStore and bootstrap the store with the default
     * components required to implement a store. Any previous data in the
     * store will be wiped.
     *
     * @throws EntityStoreException if the underlying implementation cannot
     *                              perform its own initialization and relies on external administrative
     *                              steps to instantiate the schema components.
     */
    @Override
    public void initialize() throws EntityStoreException {
    }

    /**
     * Import a serialized definition of the Store.
     *
     * @param stream An input stream from which to get the entity/type
     *               definitions
     * @return Collection&lt;ESPK&gt; a list of the newly imported objects.
     */
    @Override
    public Collection<ESPK> importData(InputStream stream) throws EntityStoreException {
        return null;
    }


    /**
     * Export the contents of the entity store to the specified output stream.
     *
     * @param out        The output stream to output to.
     * @param startNodes An array of the ESPKs which indicate the starting
     *                   points of the branches to export.
     *                   Null entries and duplicates will be ignored.
     *                   A null, zero-length array or array with no usable ESPKs will
     *                   result in no Entity data being exported, but types may still be exported
     *                   if specified in the flags.
     * @param flags      A set of flags to indicate what and how to export.
     * @throws EntityStoreException if the OutputStream is null.
     */
    @Override
    public void exportContents(OutputStream out, Collection<ESPK> startNodes, int flags) throws EntityStoreException {

    }

    /**
     * Indicate to the Store that you want to start a transaction.
     *
     * @throws EntityStoreException if the underlying implementation was
     *                              unable to start a transaction
     */
    @Override
    public void startTransaction() throws EntityStoreException {
    }

    /**
     * Indicate that you wish to have all operations on the Store since
     * the last startTransaction committed to the Store, and published to
     * the Store's listeners.
     *
     * @throws EntityStoreException If a transaction hasn't been started, or
     *                              the underlying implementation was unable to commit.
     */
    @Override
    public void commit() throws EntityStoreException {

    }

    /**
     * Indicate that you wish to discard all changes to the Store since
     * the last startTransaction.
     *
     * @throws EntityStoreException If the underlying implementation was
     *                              unable to roll back your changes, or a transaction hasn't been
     *                              started
     */
    @Override
    public void rollback() throws EntityStoreException {

    }

    /**
     * Register to listen to changes to the Entities in the Store.
     *
     * @param entityPK The Entity PK on which to listen for events
     * @param listener The listener for EntityStore change events
     */
    @Override
    public void registerChangeListener(ESPK entityPK, ESChangeListener listener) throws EntityStoreException {
    }

    /**
     * Deregister from listening to changes to the Entities in the Store.
     *
     * @param entityPK The Entity PK from which we no longer listen to events
     * @param listener The listener for EntityStore change events, as initially
     *                 registered.
     */
    @Override
    public void deregisterChangeListener(ESPK entityPK, ESChangeListener listener) throws EntityStoreException {

    }

    /**
     * Decode a stringified PK as per the implementation. To encode, just use
     * ESPK.toString()
     *
     * @param stringifiedPK The string version of the PK
     * @return an ESPK as per the particular implementation
     */
    @Override
    public ESPK decodePK(String stringifiedPK) throws EntityStoreException {
        return null;
    }

    @Override
    protected void deleteType(String typeName) throws EntityStoreException {
        typeMap.removeType(typeName);
    }

    @Override
    protected void persistType(EntityType type) throws EntityStoreException {
        typeMap.addType(type);
    }

    @Override
    protected String[] retrieveSubtypes(String typeName) throws EntityStoreException {
        return typeMap.getSubtypeNames(typeName);
    }

    @Override
    protected EntityType retrieveType(String typeName) throws EntityStoreException {
        return typeMap.getTypeForName(typeName);
    }


}
