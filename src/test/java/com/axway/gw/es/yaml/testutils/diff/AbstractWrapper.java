package com.axway.gw.es.yaml.testutils.diff;

import com.fasterxml.jackson.annotation.JsonIgnore;

abstract class AbstractWrapper<T> {

   private final T wrapped;

    protected AbstractWrapper(T wrapped) {
        this.wrapped = wrapped;
    }

    @JsonIgnore
    public T getWrapped() {
        return wrapped;
    }

    public boolean isNull() {
        return wrapped == null;
    }
}
