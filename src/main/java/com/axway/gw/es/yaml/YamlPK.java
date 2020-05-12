package com.axway.gw.es.yaml;

import com.vordel.es.ESPK;

import java.util.Objects;

/**
 * EntityStore primary key implementation for Yaml
 */
public class YamlPK implements com.vordel.es.ESPK {

    private final String location;

    public YamlPK(String location) {
        this.location = location;
    }

    public YamlPK(ESPK parent, String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Invalid key " + parent + " " + name);
        }
        this.location = parent.toString() + "/" + name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        YamlPK yamlPK = (YamlPK) o;
        return location.equals(yamlPK.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location);
    }

    public String toString() {
        return location;
    }

    public String getLocation() {
        return location;
    }
}
