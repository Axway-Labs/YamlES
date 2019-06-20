#YAML Generator

##Configuration

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

##Build

##Test

###Convert .fed to yaml

###Convert yaml to .fed