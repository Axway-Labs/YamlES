package com.axway.gw.es.yaml;

import com.vordel.es.ESPK;

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
    public int hashCode() {
        return location.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        YamlPK other = (YamlPK) obj;
        if (location.equals(other.location))
            return true;
        return false;
    }

    public String toString() {
        return location;
    }

    public String getPath() {
        return location;
    }
}
