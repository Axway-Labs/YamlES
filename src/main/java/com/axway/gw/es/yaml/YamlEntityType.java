package com.axway.gw.es.yaml;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vordel.common.xml.XmlTransformerCache;
import com.vordel.es.*;
import com.vordel.es.impl.BuilderFactory;
import com.vordel.es.impl.ConstantField;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

public class YamlEntityType implements EntityType {

    // name of the type
    private String entityName;
    // This type's parent type, from which this type inherits its schema.
    private YamlEntityType superType;
    // The names of the key fields for this type.
    private final List<String> keyFieldNames = new ArrayList<>();
    // A local cache of all keys for this type. Will include any keys from the super types.
    private String[] keyNames;
    // A Map of all allowed child Entity types, by name, of an Entity of
    // this type, and the corresponding cardinality of the child count.
    private final Map<String, Object> componentTypes = new LinkedHashMap<>();
    // A Map of all field definitions for this type.
    private final Map<String, FieldType> fieldTypes = new LinkedHashMap<>();
    // A Map of names to constant fields at this level in the type hierarchy
    private final Map<String, ConstantField> constants = new LinkedHashMap<>();
    // A Set of all fields which have default values
    private final Set<String> defaultedFields = new LinkedHashSet<>();
    // A Set of all optional fields which have no default values
    private final Set<String> optionalFields = new LinkedHashSet<>();
    // If this type is abstract, then there shouldn't be any instances of
    // this specific type in the EntityStore, only of its subtypes.
    private boolean abstractEntity = false;
    // cached encoded xml typedoc
    private byte[] cachedDoc;





    @Override
    public int compareTo(EntityType other) {
        return this.entityName.compareTo(other.getName());
    }

    @Override
    public boolean allowsChildEntities() {
        YamlEntityType ancestor = this;
        boolean allowsChildren = false;
        while (ancestor != null && !allowsChildren) {
            allowsChildren = ancestor.componentTypes.size() > 0;
            ancestor = ancestor.superType;
        }
        return allowsChildren;
    }

    @Override
    public boolean allowsComponent(EntityType t) {
        EntityType ancestor = t;
        EntityType allower = null;
        while (ancestor != null && allower == null) {
            allower = getTypeForComponentType(ancestor.getName());
            ancestor = ancestor.getSuperType();
        }
        return (allower != null);
    }

    private EntityType getTypeForComponentType(String cTypeName) {
        YamlEntityType ancestor = this;
        while (ancestor != null &&
                !ancestor.componentTypes.containsKey(cTypeName))
            ancestor = ancestor.superType;
        return ancestor;
    }

    @Override
    public boolean allowsReferences() {
        for (Map.Entry<String, FieldType> entry : fieldTypes.entrySet()) {
            if (isRef(entry.getValue()))
                return true;
        }
        return false;
    }

    @Override
    public boolean allowsSoftReferences() {
        for (Map.Entry<String, FieldType> entry : fieldTypes.entrySet()) {
            if (isSoftRef(entry.getValue()))
                return true;
        }
        return false;
    }

    private boolean isRef(FieldType fieldType) {
        if (fieldType == null)
            return false;
        return fieldType.isRefType();
    }

    private boolean isSoftRef(FieldType fieldType) {
        if (fieldType == null)
            return false;
        return fieldType.isSoftRefType();

    }

    @Override
    public Field createField(String fieldName, ESPK reference) {
        return createField(fieldName, new Value[]{new Value(reference)});
    }

    @Override
    public Field createField(String fieldName, String value) {
        return createField(fieldName, new Value[]{new Value(value)});
    }

    @Override
    public Field createField(String fieldName, Value[] vals) {
        FieldType type = getFieldType(fieldName);
        if (type == null)
            throw new EntityStoreException("Unknown field: " + fieldName);
        return new Field(type, fieldName, vals);
    }

    @Override
    public boolean extendsType(String parentType) {
        EntityType t = this;
        while (t != null) {
            if (t.getName().equals(parentType))
                return true;
            t = t.getSuperType();
        }
        return false;
    }

    @Override
    @JsonIgnore
    public Collection<String> getAllConstantFieldNames() {
        Set<String> allFields = new HashSet<>();
        YamlEntityType ancestor = this;
        while (ancestor != null) {
            allFields.addAll(ancestor.constants.keySet());
            ancestor = ancestor.superType;
        }
        return allFields;
    }

    @Override
    @JsonIgnore
    public Collection<Field> getAllConstantFields() {
        List<Field> allFields = new ArrayList<>();
        List<String> names = new ArrayList<>();

        YamlEntityType ancestor = this;
        while (ancestor != null) {
            for (Field f : ancestor.constants.values()) {
                if (!names.contains(f.getName())) {
                    names.add(f.getName());
                    allFields.add(f);
                }
            }

            ancestor = ancestor.superType;
        }
        return allFields;
    }

    @Override
    @JsonIgnore
    public Set<String> getAllDeclaredConstantFieldNames() {
        return constants.keySet();
    }

    @Override
    @JsonIgnore
    public Set<String> getAllDeclaredFieldNames() {
        return new TreeSet<>(fieldTypes.keySet());
    }

    @Override
    @JsonIgnore
    public Collection<String> getAllDeclaredKeyFields() {
        return keyFieldNames;
    }

    @Override
    @JsonIgnore
    public Collection<String> getAllDefaultedFieldNames() {
        Set<String> defFields = new HashSet<>();
        YamlEntityType ancestor = this;
        while (ancestor != null) {
            defFields.addAll(ancestor.defaultedFields);
            ancestor = ancestor.superType;
        }
        return defFields;
    }

    @Override
    @JsonIgnore
    public Collection<String> getAllFieldNames() {
        Set<String> allFields = new HashSet<>();
        YamlEntityType ancestor = this;
        while (ancestor != null) {
            allFields.addAll(ancestor.fieldTypes.keySet());
            ancestor = ancestor.superType;
        }
        return allFields;
    }

    @Override
    @JsonIgnore
    public Collection<String> getAllOptionalFieldNames() {
        Set<String> optFields = new HashSet<>();
        YamlEntityType ancestor = this;
        while (ancestor != null) {
            optFields.addAll(ancestor.optionalFields);
            ancestor = ancestor.superType;
        }
        return optFields;
    }

    @Override
    @JsonIgnore
    public DocumentBuilder getBuilder() {
        throw new EntityStoreException("this is YAML do not have a DocumentBuilder");
    }

    @Override
    @JsonIgnore
    public Field getConstantField(String fieldName) {
        YamlEntityType t = getTypeForField(fieldName, true);
        if (t != null)
            return t.constants.get(fieldName);
        return null;
    }

    private YamlEntityType getTypeForField(String fName, boolean checkConstants) {
        YamlEntityType ancestor = this;
        if (checkConstants) {
            while (ancestor != null && !ancestor.constants.containsKey(fName))
                ancestor = ancestor.superType;
        } else {
            while (ancestor != null && !ancestor.fieldTypes.containsKey(fName))
                ancestor = ancestor.superType;
        }
        return ancestor;
    }

    @Override
    @JsonIgnore
    public Map<String, Object> getDeclaredComponentTypes() {
        return new HashMap<>(componentTypes);
    }

    @Override
    public FieldType getFieldType(String name) {
        YamlEntityType ancestor = getTypeForField(name, false);
        if (ancestor != null)
            return ancestor.fieldTypes.get(name);
        return null;
    }


    private void getKeyFieldNamesRecurse(List<String> names) {
        if (superType != null)
            superType.getKeyFieldNamesRecurse(names);
        names.addAll(keyFieldNames);
    }

    @Override
    public String[] getKeyFieldNames() {
        if (keyNames == null) {
            List<String> keyNameList = new ArrayList<>();
            getKeyFieldNamesRecurse(keyNameList);
            keyNames = keyNameList.toArray(new String[0]);
        }
        return keyNames;
    }

    @Override
    public String getName() {
        return this.entityName;
    }

    @Override
    public Element getRootElement() {
        throw new EntityStoreException("this is YAML do not have a root element");
    }

    /**
     * Get the super type of this type.
     *
     * @return The EntityType which this type extends
     */
    @Override
    public EntityType getSuperType() {
        return superType;
    }

    /**
     * See if this type is abstract.
     *
     * @return true if this is an abstract type, false otherwise.
     */
    @Override
    public boolean isAbstract() {
        return abstractEntity;
    }

    @Override
    public boolean isAncestorOfType(EntityType candidate) {
        EntityType type = candidate;
        while (type != null) {
            if (this.equals(type))
                return true;
            type = type.getSuperType();
        }
        return false;
    }

    @Override
    public boolean isCompatible(EntityType other) {
        if (this.entityName.equals(other.getName())) {
            Collection<String> fieldNames = getAllFieldNames();
            for (String fn : fieldNames) {
                FieldType myType = getFieldType(fn);
                FieldType otherType = other.getFieldType(fn);
                if (!myType.equals(otherType))
                    return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean isConstantField(String fieldName) {
        return (getTypeForField(fieldName, true) != null);
    }

    @Override
    public boolean isDescendantOfType(String candidate) {
        EntityType type = this;
        while (type != null) {
            if (type.getName().equals(candidate))
                return true;
            type = type.getSuperType();
        }
        return false;
    }

    @Override
    public boolean isKeyField(Field field) {
        for (String fieldName : getKeyFieldNames())
            if (Objects.equals(field.getName(), fieldName))
                return true;

        return false;
    }

    @Override
    public void write(OutputStream os) throws IOException {
        if (cachedDoc == null) {
            cachedDoc = initCacheDoc();
        }
        os.write(cachedDoc);
    }

    void setName(String name) {
        this.entityName = name;
    }

    YamlEntityType setAbstractEntity(boolean abstractEntity) {
        this.abstractEntity = abstractEntity;
        return this;
    }

    void setSuperType(YamlEntityType superType) {
        this.superType = superType;
    }

    void addConstantField(ConstantField constantField) {
        this.constants.put(constantField.getName(), constantField);
    }

    void addFieldType(String fieldName, FieldType fieldType) {
        this.fieldTypes.put(fieldName, fieldType);
    }

    void addKeyFieldName(String keyFieldName) {
        this.keyFieldNames.add(keyFieldName);
    }

    void addComponentType(String key, String value) {
        this.componentTypes.put(key, value);
    }

    private byte[] initCacheDoc() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DocumentBuilder builder = BuilderFactory.getBuilder(EntityType.class);
        Document doc = builder.newDocument();

        Element entityType = doc.createElement("entityType");
        entityType.setAttribute("xmlns", "http://www.vordel.com/2005/06/24/entityStore");
        if (this.superType != null) {
            entityType.setAttribute("extends", this.getSuperType().getName());
        }
        entityType.setAttribute("name", this.getName());

        this.constants.forEach((key, value) -> {
            Element constant = doc.createElement("constant");
            constant.setAttribute("name", key);
            constant.setAttribute("type", value.getType().getType());
            constant.setAttribute("value", value.getValue().getData());
            entityType.appendChild(constant);
        });

        // Encode <field />
        this.fieldTypes.forEach((key, value) -> {
            Element field = doc.createElement("field");

            field.setAttribute("cardinality", value.getCardinality().toString());
            String defaultValue = value.getDefault();
            if (defaultValue != null) {
                field.setAttribute("default", defaultValue);
            }
            field.setAttribute("name", key);
            field.setAttribute("type", value.getType());

            entityType.appendChild(field);
        });

        this.componentTypes.forEach((key, value) -> {
            Element componentType = doc.createElement("componentType");
            String cardinality = value.toString();
            if (!cardinality.equals("?")) {
                // net set on componentType when cardinality is '?' for some reason
                componentType.setAttribute("cardinality", cardinality);
            }
            componentType.setAttribute("name", key);
            entityType.appendChild(componentType);
        });

        writeNodeAsXML(bos, entityType);

        // make sure line separator is platform independent for normalization
        return bos.toString().replace(System.getProperty("line.separator"), "\n").getBytes();
    }

    private static void writeNodeAsXML(OutputStream os, Node node) throws IOException {
        XmlTransformerCache.CachedTransformer cachedXformer = XmlTransformerCache.get();
        Transformer transformer = cachedXformer.transformer;
        transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
        try {
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.transform(new DOMSource(node), new StreamResult(os));
        } catch (javax.xml.transform.TransformerException ex) {
            throw new IOException("transformer failed during write");
        } finally {
            XmlTransformerCache.release(cachedXformer);
        }
    }
}
