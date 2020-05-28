package com.axway.gw.es.yaml.converters;

import com.axway.gw.es.yaml.YamlEntityStore;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import static com.axway.gw.es.yaml.YamlEntityExporter.YAML_EXTENSION;

/**
 * Read or Write kay mapping from a source to a target ES
 * @param <S> Source entity store PK type
 * @param <T> target entity store PK type
 */
public class EntityStoreESPKMapper<S, T> {

    public static final String HIDDEN_FILE_PREFIX = "_";
    public static final String KEY_MAPPING_FILENAME = HIDDEN_FILE_PREFIX + "conversion-key-mapping" + YAML_EXTENSION;
    Map<S, T> keyMapping = new LinkedHashMap<>();

    public void addPKPair(S sourceKey, T targetKey) {
        keyMapping.put(sourceKey, targetKey);
    }

    public void writePKPairs(File rootDir) throws IOException {
        YamlEntityStore.YAML_MAPPER.writeValue(new File(rootDir, KEY_MAPPING_FILENAME), keyMapping);
    }

    public void readPKPairs(String rootDir) throws IOException {
        keyMapping = readKeyPairsWithTransformation(rootDir, Function.identity(), Function.identity());
    }

    public <D> Map<S, D> readKeyPairsWithTransformation(String rootDir, Function<T, D> targetKeyTransformer) throws IOException {
        return readKeyPairsWithTransformation(rootDir, Function.identity(), targetKeyTransformer);
    }

    public <O, D> Map<O, D> readKeyPairsWithTransformation(String rootDir, Function<S, O> sourceTargetTransformer, Function<T, D> targetKeyTransformer) throws IOException {
        @SuppressWarnings("unchecked")
        Map<S, T> mapping = YamlEntityStore.YAML_MAPPER.readValue(new File(rootDir, KEY_MAPPING_FILENAME), LinkedHashMap.class);
        Map<O, D> result = new LinkedHashMap<>();
        mapping.forEach((k, v) -> result.put(sourceTargetTransformer.apply(k), targetKeyTransformer.apply(v)));
        return result;
    }

    public T getTargetKey(S sourcePk) {
        return keyMapping.get(sourcePk);
    }

    public S getSourceKey(T targetPk) {
        return keyMapping.entrySet()
                .stream()
                .filter(e->e.getValue().equals(targetPk))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }


}
