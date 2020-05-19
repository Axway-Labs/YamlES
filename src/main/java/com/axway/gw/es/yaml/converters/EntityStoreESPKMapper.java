package com.axway.gw.es.yaml.converters;

import com.axway.gw.es.yaml.YamlEntityStore;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import static com.axway.gw.es.yaml.converters.EntityDTOConverter.YAML_EXTENSION;

public class EntityStoreESPKMapper<S, T> {

    public static final String HIDDEN_FILE_PREFIX = ".";
    public static final String KEY_MAPPING_FILENAME = HIDDEN_FILE_PREFIX +"federated-to-yaml-espk"+YAML_EXTENSION;
    Map<S, T> keyMapping = new LinkedHashMap<>();

    public void addKeyPair(S sourceKey, T targetKey) {
        keyMapping.put(sourceKey, targetKey);
    }

    public void writeFederatedToYamlPkMapping(String rootDir) throws IOException {
        YamlEntityStore.YAML_MAPPER.writeValue(new File(rootDir, "__federated-to-yaml-espk.yaml"), keyMapping);
    }

    public Map<S, T> readFederatedToYamlPkMapping(String rootDir) throws IOException {
        return readFederatedToYamlPkMapping(rootDir, Function.identity(), Function.identity());
    }

    public <D> Map<S, D> readFederatedToYamlPkMapping(String rootDir, Function<T, D> targetKeyTransformer) throws IOException {
        return readFederatedToYamlPkMapping(rootDir, Function.identity(), targetKeyTransformer);
    }


    public <O, D> Map<O, D> readFederatedToYamlPkMapping(String rootDir, Function<S, O> sourceTargetTransformer, Function<T, D> targetKeyTransformer) throws IOException {
        @SuppressWarnings("unchecked")
        Map<S, T> mapping = YamlEntityStore.YAML_MAPPER.readValue(new File(rootDir, KEY_MAPPING_FILENAME), LinkedHashMap.class);
        Map<O, D> result = new LinkedHashMap<>();
        mapping.forEach((k, v) -> result.put(sourceTargetTransformer.apply(k), targetKeyTransformer.apply(v)));
        return result;
    }

}
