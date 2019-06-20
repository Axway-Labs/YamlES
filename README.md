# YAML Generator

## Configuration

Adding ES Core jar file to your local Maven repository.  Run the following command.
```java
    mvn install:install-file \
    -Dfile=<location to es-core-7.x.x-x.jar> \
    -DgroupId=es-core \
    -DartifactId=es-core \
    -Dversion=7.x \
    -Dpackaging=jar \
    -DgeneratePom=true
```

Adding API Gateway Common to your local Maven repository.  Run the following command.
```java
    mvn install:install-file \
    -Dfile=<location to apigw-common-7.x.x-x.jar> \
    -DgroupId=apigw-common \
    -DartifactId=apigw-common \
    -Dversion=7.x \
    -Dpackaging=jar \
    -DgeneratePom=true
```

In the pom.xml file, update the version number of the following dependencies based on what was installed above.
```java
    <!-- Local dependency to es-core-7.x.x-x.jar -->
    <dependency>
        <groupId>es-core</groupId>
        <artifactId>es-core</artifactId>
        <version>7.7</version>
    </dependency>

    <!-- Local dependency to apigw-common-7.x.x-x.jar -->
    <dependency>
        <groupId>apigw-common</groupId>
        <artifactId>apigw-common</artifactId>
        <version>7.7</version>
    </dependency>
```

## Build

```java
    mvn package
```

## Test

TODO

### Convert .fed to yaml

```java
    java -cp YamlES-0.0.1-SNAPSHOT-jar-with-dependencies.jar com.axway.gw.es.tools.ConvertToYamlStore federated:file:<fed_file_directory>/configs.xml <yaml_output_directory>
```

### Merge yaml to .fed

```java
    java -cp YamlES-0.0.1-SNAPSHOT-jar-with-dependencies.jar com.axway.gw.es.tools.CloneES federated:file:<fed_file_directory>/configs.xml <yaml_output_directory>
```
