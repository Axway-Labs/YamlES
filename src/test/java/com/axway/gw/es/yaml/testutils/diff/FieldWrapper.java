package com.axway.gw.es.yaml.testutils.diff;

import com.vordel.es.Field;
import com.vordel.es.Value;

import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

class FieldWrapper extends AbstractWrapper<Field> {

    private final Field field;
    private final List<Value> values;

    FieldWrapper(Field field) {
        super(field);
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
