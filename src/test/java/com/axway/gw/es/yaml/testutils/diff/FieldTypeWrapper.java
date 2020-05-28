package com.axway.gw.es.yaml.testutils.diff;

import com.vordel.es.FieldType;
import com.vordel.es.Value;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class FieldTypeWrapper extends AbstractWrapper<FieldType> {

    private String type;
    private Object cardinality;
    private List<ValueWrapper> defaultValues;

    public FieldTypeWrapper(FieldType fieldType) {
        super(fieldType);
        if (!isNull()) {
            type = fieldType.getRefType() == null ? fieldType.getType() : fieldType.getRefType();
            cardinality = fieldType.getCardinality();
            List<Value> defaultValues = nullIfContainsNothing(fieldType.getDefaultValues());
            if (defaultValues != null) {
                this.defaultValues = defaultValues.stream()
                        .map(ValueWrapper::new)
                        .collect(Collectors.toList());
            }
        }
    }

    public String getType() {
        return type;
    }

    public Object getCardinality() {
        return cardinality;
    }

    public List<ValueWrapper> getDefaultValues() {
        return defaultValues;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FieldTypeWrapper that = (FieldTypeWrapper) o;
        final boolean typesEqual = Objects.equals(type, that.type);
        final boolean cardinalityEqual = Objects.equals(cardinality, that.cardinality);
        final boolean defaultValueEqual = Objects.equals(defaultValues, that.defaultValues);
        return typesEqual && cardinalityEqual && defaultValueEqual;
    }

    private List<Value> nullIfContainsNothing(List<Value> values) {
        if (values != null && (values.isEmpty() || (values.size() == 1 && (values.get(0) == null || values.get(0).isNull())))) {
            return null;
        }
        return values;
    }


    @Override
    public int hashCode() {
        return Objects.hash(type, cardinality, defaultValues);
    }
}
