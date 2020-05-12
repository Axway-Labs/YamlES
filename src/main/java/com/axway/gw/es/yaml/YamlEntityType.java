package com.axway.gw.es.yaml;

import com.axway.gw.es.model.type.ConstantFieldDTO;
import com.axway.gw.es.model.type.FieldDTO;
import com.axway.gw.es.model.type.TypeDTO;
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
import java.util.Map.Entry;

public class YamlEntityType implements EntityType {
    // name of the type
    private String name;
    // This type's parent type, from which this type inherits its schema.
    private YamlEntityType superType;
    // The names of the key fields for this type.
    private final List<String> keyFieldNames = new ArrayList<String>();
    // A local cache of all keys for this type. Will include any keys from the super types.
    private String[] keyNames;
    // A Map of all allowed child Entity types, by name, of an Entity of
    // this type, and the corresponding cardinality of the child count.
    private final Map<String, Object> componentTypes = new LinkedHashMap<>();
    // A Map of all field definitions for this type.
    private final Map<String, FieldType> fieldTypes = new LinkedHashMap<>();
    // A Map of names to constant fields at this level in the type hierarchy
    public Map<String, ConstantField> constants = new LinkedHashMap<>();
    // A Set of all fields which have default values
    private Set<String> defaultedFields = new LinkedHashSet<>();
    // A Set of all optional fields which have no default values
    private Set<String> optionalFields = new LinkedHashSet<>();
    // If this type is abstract, then there shouldn't be any instances of
    // this specific type in the EntityStore, only of its subtypes.
    private boolean _abstract = false;
    // cached encoded xml typedoc
    private byte[] cachedDoc;

    public static YamlEntityType convert(String name, TypeDTO t) {
        YamlEntityType type = YamlEntityType.convert(t);
        type.setName(name);
        return type;
    }

    public static class FieldTypeInner extends com.vordel.es.FieldType {
        public FieldTypeInner(String name, Object x, List<Value> v) {
            super(name, x, v);
        }
    }

    public static class ConstantFieldTypeInner extends com.vordel.es.ConstantFieldType {
        public ConstantFieldTypeInner(String name, Value v) {
            super(name, v);
        }
    }

    public static YamlEntityType convert(TypeDTO typeDTO) {
        YamlEntityType type = new YamlEntityType();
        type._abstract = typeDTO.isAbstract();
        type.name = typeDTO.getName();


        ConstantField constantField = createConstantField("_version", "integer", typeDTO.getVersion() == null ? "0" : Integer.toString(typeDTO.getVersion()));
        type.constants.put(constantField.getName(), constantField);
        constantField = createConstantField("class", "string", typeDTO.getClazz());
        type.constants.put(constantField.getName(), constantField);
        constantField = createConstantField("loadorder", "integer", typeDTO.getLoadOrder() == null ? "0" : Integer.toString(typeDTO.getLoadOrder()));
        type.constants.put(constantField.getName(), constantField);

        // constants
        for (Map.Entry<String, ConstantFieldDTO> entry : typeDTO.getConstants().entrySet()) {
            ConstantFieldDTO constant = entry.getValue();
            constant.setName(entry.getKey());
            ConstantField field = createConstantField(constant.getName(), constant.getType(), constant.getValue());
            type.constants.put(constant.getName(), field);
        }
        // fields
        for (Map.Entry<String, FieldDTO> entry : typeDTO.getFields().entrySet()) {
            FieldDTO field = entry.getValue();
            field.setName(entry.getKey());
            FieldType ft = new FieldTypeInner(field.getType(), field.getCardinality(), new ArrayList<>());
            type.fieldTypes.put(field.getName(), ft);
            if (field.isKeyField())
                type.keyFieldNames.add(field.getName());
        }
        // components
        for (Map.Entry<String, String> entry : typeDTO.getComponents().entrySet()) {
            type.componentTypes.put(entry.getKey(), entry.getValue());
        }
        return type;
    }

    private static ConstantField createConstantField(String name, String type, String value) {
        ConstantFieldType cft = new ConstantFieldTypeInner(type, new Value(value));
        return new ConstantField(cft, name);
    }


    @Override
    public int compareTo(EntityType other) {
        return name.compareTo(other.getName());
    }

    @Override
    public boolean allowsChildEntities() {
        YamlEntityType ancestor = this;
        boolean allowsChildren = false;
        while (ancestor != null && allowsChildren == false) {
            if (ancestor.componentTypes != null)
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
    public Field createField(String fieldName, ESPK reference) throws EntityStoreException {
        return createField(fieldName, new Value[]{new Value(reference)});
    }

    @Override
    public Field createField(String fieldName, String value) throws EntityStoreException {
        return createField(fieldName, new Value[]{new Value(value)});
    }

    @Override
    public Field createField(String fieldName, Value[] vals) throws EntityStoreException {
        FieldType type = getFieldType(fieldName);
        if (type == null)
            throw new EntityStoreException("Unknown field: " + fieldName);
        Field f = new Field(type, fieldName, vals);
        return f;
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
        Set<String> allFields = new HashSet<String>();
        YamlEntityType ancestor = this;
        while (ancestor != null) {
            if (ancestor.constants != null)
                allFields.addAll(ancestor.constants.keySet());
            ancestor = ancestor.superType;
        }
        return allFields;
    }

    @Override
    @JsonIgnore
    public Collection<Field> getAllConstantFields() {
        List<Field> allFields = new ArrayList<Field>();
        List<String> names = new ArrayList<String>();

        YamlEntityType ancestor = this;
        while (ancestor != null) {
            Map<String, ConstantField> cons = ancestor.constants;
            if (cons != null) {
                for (Field f : cons.values()) {
                    if (!names.contains(f.getName())) {
                        names.add(f.getName());
                        allFields.add(f);
                    }
                }
            }
            ancestor = ancestor.superType;
        }
        return allFields;
    }

    @Override
    @JsonIgnore
    public Set<String> getAllDeclaredConstantFieldNames() {
        if (constants != null)
            return constants.keySet();
        return Collections.emptySet();
    }

    @Override
    @JsonIgnore
    public Set<String> getAllDeclaredFieldNames() {
        Set<String> fields = new java.util.TreeSet<String>(fieldTypes.keySet());
        return fields;
    }

    @Override
    @JsonIgnore
    public Collection<String> getAllDeclaredKeyFields() {
        return keyFieldNames;
    }

    @Override
    @JsonIgnore
    public Collection<String> getAllDefaultedFieldNames() {
        Set<String> defFields = new HashSet<String>();
        YamlEntityType ancestor = this;
        while (ancestor != null) {
            if (ancestor.defaultedFields != null)
                defFields.addAll(ancestor.defaultedFields);
            ancestor = ancestor.superType;
        }
        return defFields;
    }

    @Override
    @JsonIgnore
    public Collection<String> getAllFieldNames() {
        Set<String> allFields = new HashSet<String>();
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
        Set<String> optFields = new HashSet<String>();
        YamlEntityType ancestor = this;
        while (ancestor != null) {
            if (ancestor.optionalFields != null)
                optFields.addAll(ancestor.optionalFields);
            ancestor = ancestor.superType;
        }
        return optFields;
    }

    @Override
    @JsonIgnore
    public DocumentBuilder getBuilder() throws EntityStoreException {
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
            while (ancestor != null &&
                    (ancestor.constants == null ||
                            !ancestor.constants.containsKey(fName)))
                ancestor = ancestor.superType;
        } else {
            while (ancestor != null &&
                    !ancestor.fieldTypes.containsKey(fName))
                ancestor = ancestor.superType;
        }
        return ancestor;
    }

    @Override
    @JsonIgnore
    public Map<String, Object> getDeclaredComponentTypes() {
        return new HashMap<String, Object>(componentTypes);
    }

    @Override
    public FieldType getFieldType(String name) {
        YamlEntityType ancestor = getTypeForField(name, false);
        if (ancestor != null)
            return ancestor.fieldTypes.get(name);
        return null;
    }

    public void setName(String name) {
        this.name = name;
    }

    private void getKeyFieldNamesRecurse(List<String> names) {
        if (superType != null)
            superType.getKeyFieldNamesRecurse(names);
        names.addAll(keyFieldNames);
    }

    @Override
    public String[] getKeyFieldNames() {
        if (keyNames == null) {
            List<String> keyNameList = new ArrayList<String>();
            getKeyFieldNamesRecurse(keyNameList);
            keyNames = keyNameList.toArray(new String[keyNameList.size()]);
        }
        return keyNames;
    }

    @Override
    public String getName() {
        return name;
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

    public void setSuperType(YamlEntityType superType) {
        this.superType = superType;
    }

    /**
     * See if this type is abstract.
     *
     * @return true if this is an abstract type, false otherwise.
     */
    @Override
    public boolean isAbstract() {
        return _abstract;
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
        if (name.equals(other.getName())) {
            Collection<String> fieldNames = getAllFieldNames();
            for (String fn : fieldNames) {
                FieldType myType = getFieldType(fn);
                FieldType otherType = other.getFieldType(fn);
                if (otherType == null || !myType.equals(otherType))
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
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DocumentBuilder builder = BuilderFactory.getBuilder(EntityType.class);
            Document doc = builder.newDocument();

            Element entityType = doc.createElement("entityType");
            entityType.setAttribute("xmlns", "http://www.vordel.com/2005/06/24/entityStore");
            if (this.superType != null) {
                entityType.setAttribute("extends", this.getSuperType().getName());
            }
            entityType.setAttribute("name", this.getName());

            Set<Entry<String, ConstantField>> constantEntries = this.constants.entrySet();
            Iterator<Entry<String, ConstantField>> it3 = constantEntries.iterator();
            for (; it3.hasNext(); ) {
                Entry<String, ConstantField> next = it3.next();
                ConstantField value = next.getValue();

                Element constant = doc.createElement("constant");
                constant.setAttribute("name", next.getKey());
                constant.setAttribute("type", value.getType().getType());
                constant.setAttribute("value", value.getValue().getData());
                entityType.appendChild(constant);
            }

            // Encode <field />
            Set<Entry<String, FieldType>> fieldEntries = this.fieldTypes.entrySet();
            Iterator<Entry<String, FieldType>> it = fieldEntries.iterator();
            for (; it.hasNext(); ) {
                Entry<String, FieldType> next = it.next();
                FieldType value = next.getValue();
                Element field = doc.createElement("field");

                field.setAttribute("cardinality", value.getCardinality().toString());
                String defaultValue = value.getDefault();
                if (defaultValue != null) {
                    field.setAttribute("default", defaultValue);
                }
                field.setAttribute("name", next.getKey());
                field.setAttribute("type", value.getType());

                entityType.appendChild(field);
            }

            Set<Entry<String, Object>> componentEntries = this.componentTypes.entrySet();
            Iterator<Entry<String, Object>> it2 = componentEntries.iterator();
            for (; it2.hasNext(); ) {
                Entry<String, Object> next = it2.next();
                Object value = next.getValue();

                Element componentType = doc.createElement("componentType");
                String cardinality = value.toString();
                if (cardinality != "?") {
                    // net set on componentType when cardinality is '?' for some reason
                    componentType.setAttribute("cardinality", cardinality);
                }
                componentType.setAttribute("name", next.getKey());
                entityType.appendChild(componentType);
            }

            Node type = (Node) entityType;

            writeNodeAsXML(bos, type);

            if (this.getName().equals("LdapDirectory")) {
                type = null;
            }

            // make sure line separator is platform independent for normalization
            cachedDoc = bos.toString().replace(System.getProperty("line.separator"), "\n").getBytes();
        }
        os.write(cachedDoc);
    }

    private static void writeNodeAsXML(OutputStream os, Node node) throws IOException {
        XmlTransformerCache.CachedTransformer cachedXformer = XmlTransformerCache.get();
        Transformer transformer = cachedXformer.transformer;
        transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
        try {
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.transform(new DOMSource(node), new StreamResult(os));
        } catch (javax.xml.transform.TransformerException ex) {
            IOException ioe = new IOException("transformer failed during write");
            ioe.initCause(ex);
            throw ioe;
        } finally {
            XmlTransformerCache.release(cachedXformer);
        }
    }
}
