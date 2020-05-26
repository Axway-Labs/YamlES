package com.axway.gw.es.yaml.util;

import com.axway.gw.es.yaml.YamlPK;
import com.axway.gw.es.yaml.dto.entity.EntityDTO;

import static com.google.common.base.Preconditions.checkNotNull;

public class NameUtils {

    private NameUtils() {
        // no op
    }

    /**
     * replace illegal characters in a filename with "_"
     * illegal characters :
     * : \ / * ? | < > "
     */
    public static String sanitize(String name) {
        checkNotNull(name);
        return name.trim().replaceAll("[/:\"*?<>|]+", "_");
    }

    public static String toRelativeRef(String childRef, EntityDTO parentDTO) {
        checkNotNull(childRef);
        checkNotNull(parentDTO);
        return toRelativeRef(childRef, parentDTO.getKey());
    }

    public static String toRelativeRef(String absoluteRef, String parentKey) {
        checkNotNull(parentKey);
        checkNotNull(absoluteRef);
        return absoluteRef.replace(parentKey + YamlPK.CHILD_SEPARATOR, "");
    }


}
