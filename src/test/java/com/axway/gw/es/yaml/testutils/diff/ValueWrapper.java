package com.axway.gw.es.yaml.testutils.diff;

import com.vordel.es.Value;

import java.util.Objects;

public class ValueWrapper {
    private final Value value;

    public ValueWrapper(Value value) {
        this.value = value;
    }

    public String getRef() {
        return value.getRef() != null ? value.toString() : null;
    }

    public String getData() {
        return value.getData();
    }

    public boolean isNull() {
        return value.isNull();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValueWrapper that = (ValueWrapper) o;
        return Objects.equals(this.getRef(), that.getRef()) &&
                Objects.equals(this.getData(), that.getData());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getRef(), this.getData());
    }
}



