package com.axway.gw.es.yaml.util;

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

    public static String toInlinedRef(String key, EntityDTO entityDTO) {
        checkNotNull(key);
        checkNotNull(entityDTO);
        return key.substring(entityDTO.getKey().length() + 1);
    }


}
