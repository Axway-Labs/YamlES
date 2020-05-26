package com.axway.gw.es.yaml.converters;


import com.axway.gw.es.yaml.YamlPkBuilder;
import com.axway.gw.es.yaml.dto.entity.EntityDTO;
import com.axway.gw.es.yaml.dto.type.TypeDTO;
import com.axway.gw.es.yaml.util.NameUtils;
import com.vordel.es.*;
import com.vordel.es.impl.ConstantField;
import com.vordel.es.xes.PortableESPK;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class EntityStoreToDTOConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(EntityStoreToDTOConverter.class);

    private static final Set<String> EXPANDED_TYPES = new HashSet<>(Arrays.asList("KPSRoot"));
    private static final Set<String> INLINED_TYPES = new HashSet<>(Arrays.asList(
            "User", // Entity/Identity
            "FilterCircuit", // Entity/RootChild
            "EnvironmentalizedEntity", // Entity
            "MetricGroupTypes", // Entity
            "MetricTypes", // Entity
            "CategoryGroup", // Entity/RootChild/NamedTopLevelGroup
            "Internationalization", // Entity/RootChild/NamedTopLevelGroup
            "HTTPRetryStatusClassGroup", // Entity/RootChild/NamedTopLevelGroup
            "HTTPStatusClassGroup", // Entity/RootChild/NamedTopLevelGroup
            "PolicyCategoryGroup", // Entity/RootChild/NamedTopLevelGroup
            "RegistryCategorizationSchemeGroup", // Entity/RootChild/NamedTopLevelGroup
            "ElementSpecifiers", // Entity/RootChild/LoadableModule/NamedLoadableModule
            "MimeTypeGroup", // Entity/RootChild/LoadableModule/NamedLoadableModule
            "NamespacesConfiguration", // Entity/RootChild/LoadableModule/NamedLoadableModule
            "TokenFinderSet", // Entity/RootChild/LoadableModule/NamedLoadableModule
            "ParserFeatureGroup" // Entity/RootChild/LoadableModule/NativeModule
    ));

    private final List<EntityDTO> mappedEntities = new ArrayList<>();
    private final Set<ESPK> embeddedEntities = new HashSet<>();
    private final EntityStore sourceES;
    private final Map<String, TypeDTO> types;
    private final YamlPkBuilder keyBuilder;

    public EntityStoreToDTOConverter(EntityStore sourceES, Map<String, TypeDTO> types) {
        this.sourceES = sourceES;
        this.types = types;
        this.keyBuilder = new YamlPkBuilder(sourceES);
    }

    public List<EntityDTO> mapFromRoot() {
        ESPK root = sourceES.getRootPK();
        return mapFrom(root);
    }

    public List<EntityDTO> mapFrom(ESPK pk) {
        mapToDTO(pk);
        Collection<ESPK> children = sourceES.listChildren(pk, null);
        for (ESPK childPK : children)
            mapFrom(childPK);
        return mappedEntities;
    }

    private void mapToDTO(ESPK pk) {
        if (embeddedEntities.contains(pk)) {
            return;
        }
        Entity entity = getEntity(pk);
        EntityDTO entityDTO = mapToDTO(entity, true);
        mappedEntities.add(entityDTO);
    }

    private EntityDTO mapToDTO(Entity entity, boolean allowToBeInSeparatedFile) {

        final String entityName = entity.getType().getName();

        EntityDTO entityDTO = new EntityDTO();
        entityDTO.getMeta().setType(entityName); // may want to change this to the directory location of the type?
        entityDTO.getMeta().setTypeDTO(types.get(entityDTO.getMeta().getType()));
        // children?
        entityDTO.setInSeparatedFile(allowToBeInSeparatedFile && isShouldBeInSeparatedFile(entity));
        // deal with pk for this entity
        entityDTO.setKey(isRoot(entity) ? keyBuilder.getKeyValues(entity) : keyBuilder.buildKeyValue(entity));
        entityDTO.setSourceKey(entity.getPK().toString());
        // fields
        setFields(entity, entityDTO);
        setReferences(entity, entityDTO, allowToBeInSeparatedFile);

        if (!entityDTO.isInSeparatedFile()) {
            for (ESPK childPK : sourceES.listChildren(entity.getPK(), null)) {
                embeddedEntities.add(childPK);
                Entity child = getEntity(childPK);
                EntityDTO childEntityDTO = mapToDTO(child, false);
                entityDTO.addChild(childEntityDTO);
                childEntityDTO.setKey(null);
            }
        }

        return entityDTO;
    }

    private boolean isRoot(Entity entity) {
        return "Root".equals(entity.getType().getName());
    }

    private boolean isShouldBeInSeparatedFile(Entity entity) {
        return entity.getType().allowsChildEntities()
                && !INLINED_TYPES.contains(entity.getType().getName())
                &&
                (entity.getType().extendsType("NamedLoadableModule")
                        || EXPANDED_TYPES.contains(entity.getType().getName())
                        || !entity.getType().extendsType("NamedGroup")
                        && !entity.getType().extendsType("LoadableModule")
                        && !entity.getType().extendsType("Filter")
                        && !entity.getType().extendsType("KPSStore")
                        && !entity.getType().extendsType("KPSType")
                );
    }

    private void setFields(Entity entity, EntityDTO entityDTO) {
        Field[] allFields = entity.getAllFields();
        List<Field> refs = entity.getReferenceFields();
        for (Field field : allFields) {
            if (!refs.contains(field)) { // not a reference
                final String fieldName = field.getName();
                String fieldValue = entity.getStringValue(fieldName);
                if (field instanceof ConstantField) {
                    if (!isDefaultValue((ConstantField) field, fieldValue)) {
                        entityDTO.addFieldValue(fieldName, fieldValue);
                    }
                } else if (!types.get(entityDTO.getMeta().getType()).isDefaultValue(fieldName, fieldValue)) {
                    entityDTO.addFieldValue(fieldName, fieldValue);
                }
            }
        }
    }

    private void setReferences(Entity entity, EntityDTO entityDTO, boolean isParentDTO) {
        for (Field field : entity.getReferenceFields()) {
            ESPK fieldRef = field.getReference(); // just deal with single at the moment
            if (!fieldRef.equals(EntityStore.ES_NULL_PK)) {
                String dtoFieldRef;
                if (isLateBoundReference(fieldRef)) {
                    dtoFieldRef = fieldRef.toString();
                } else {
                    dtoFieldRef = keyBuilder.buildKeyValue(fieldRef);
                    // when the current entity makes a ref to one of his children
                    if (isParentDTO && dtoFieldRef.startsWith(entityDTO.getKey())) {
                        dtoFieldRef = NameUtils.toRelativeRef(dtoFieldRef, entityDTO);
                    } else if (!isParentDTO && entity.getParentPK() != null) {
                        final String parentRef = keyBuilder.buildKeyValue(entity.getParentPK());
                        // if a the ref is a sibling (is child of the same parent)
                        if (dtoFieldRef.startsWith(parentRef)) {
                            dtoFieldRef = NameUtils.toRelativeRef(dtoFieldRef, parentRef);
                        }
                    }
                }
                entityDTO.addFieldValue(field.getName(), dtoFieldRef);
            }
        }
    }

    private boolean isLateBoundReference(final ESPK pk) {
        if (pk instanceof PortableESPK) {
            final PortableESPK portableKey = (PortableESPK) pk;
            final boolean isLateBound = "_lateBoundReference".equals(portableKey.getTypeNameOfReferencedEntity());
            if (isLateBound) {
                LOGGER.info("Late bound reference to {}, expected at run-time", LOGGER.isInfoEnabled() ? portableKey.terse() : "");
            }
            return isLateBound;
        }

        return false;
    }

    private boolean isDefaultValue(ConstantField field, String fieldValue) {
        final String defaultValue = field.getType().getDefault();
        boolean isDefaultValue = Objects.equals(fieldValue, defaultValue);
        if (FieldType.BOOLEAN.equals(field.getType().getType())) {
            isDefaultValue = Objects.equals(FieldType.getBooleanValue(defaultValue), FieldType.getBooleanValue(fieldValue));
        }
        return isDefaultValue;
    }

    public Entity getEntity(ESPK key) {
        final ESPK espk = EntityStoreDelegate.getEntityForKey(sourceES, key);
        return sourceES.getEntity(espk);
    }


}
