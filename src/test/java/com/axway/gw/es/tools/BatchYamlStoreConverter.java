package com.axway.gw.es.tools;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class BatchYamlStoreConverter {

    public static void main(String[] args) throws IOException, InterruptedException {
        convert("FactoryTemplate");
        convert("FactoryTemplateSamples");
        convert("TeamDevelopmentAPI");
        convert("TeamDevelopmentSettings");
        convert("TeamDevelopmentSettingsAPIM");

        clearSimilar("FactoryTemplate");
        clearSimilar("FactoryTemplateSamples");
        clearSimilar("TeamDevelopmentSettings");
        clearSimilar("TeamDevelopmentSettingsAPIM");
    }

    private static void clearSimilar(String project) throws IOException {
        Path reference = Paths.get("/tmp/yamlstores/TeamDevelopmentAPI");
        Path base = Paths.get("/tmp/yamlstores/" + project);
        Files.walkFileTree(base, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path relativize = base.relativize(file);
                Path original = reference.resolve(relativize);
                if (!original.toFile().exists()) {
                    return FileVisitResult.CONTINUE;
                }
                HashCode originalHash = com.google.common.io.Files.asByteSource(original.toFile()).hash(Hashing.goodFastHash(128));
                HashCode newHash = com.google.common.io.Files.asByteSource(file.toFile()).hash(Hashing.goodFastHash(128));
                if (newHash.equals(originalHash)) {
                    file.toFile().delete();
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                File[] files = dir.toFile().listFiles();
                if (files == null || files.length == 0) {
                    dir.toFile().delete();
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static void convert(String project) throws InterruptedException, IOException {
        String file = BatchYamlStoreConverter.class.getResource("/apiprojects/" + project + "/configs.xml").getFile();

        ConvertToYamlStore.convert("federated:file:" + file, "/tmp/yamlstores/" + project);
    }
}
