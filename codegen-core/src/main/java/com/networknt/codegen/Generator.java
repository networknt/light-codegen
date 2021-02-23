package com.networknt.codegen;

import java.io.File;
import static java.io.File.separator;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fizzed.rocker.runtime.ArrayOfByteArraysOutput;
import com.fizzed.rocker.runtime.DefaultRockerModel;

import org.apache.commons.io.IOUtils;

/**
 * This is the interface that every generator needs to implement. There are three methods
 * that is used to generate project, get the name of the framework for the generator and
 * get the config schema for the generator in order to find out how many options are supported
 * by the generator.
 *
 * @author Steve Hu
 */
public interface Generator {
    // Jackson ObjectMapper with YAML support.
    public static ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    public static ObjectMapper jsonMapper = new ObjectMapper();
    // config schema cache
    Map<String, byte []> schemaMap = new HashMap<>();

    /**
     * Generate the project based on the input parameters. For most frameworks, the model can be converted to JSON, but
     * the GraphQL is using a schema file that can only be passed in as a string. This is why, the model is an Object.
     *
     * @param targetPath The output directory of the generated project
     * @param model The optional model data that trigger the generation, i.e. swagger specification, graphql IDL etc.
     * @param config A json object that controls how the generator behaves.
     * @throws IOException throws IOException
     */
    void generate(String targetPath, Object model, JsonNode config) throws IOException;

    /**
     * Get generator name
     *
     * @return The generator name and it is used in the -f option in the command line.
     */
    String getFramework();

    /**
     * Get the config schema for the generator.
     *
     * @return ByteBuffer of config schema for the generator. Used by codegen-web API.
     * @throws IOException IO exception
     */
    default ByteBuffer getConfigSchema() throws IOException {
        byte [] schemaBuf = schemaMap.get(getFramework());
        if(schemaBuf == null) {
            try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(getFramework() + ".json")) {
                schemaBuf = IOUtils.toByteArray(is);
                schemaMap.put(getFramework(), schemaBuf);
            }
        }
        return ByteBuffer.wrap(schemaBuf);
    }

    /**
     * This is a default method used by all generators to transfer a template into a generated
     * file into the right location.
     *
     * @param folder The output folder of the project
     * @param path  Current file path in the output folder
     * @param filename Current filename in the output folder
     * @param rockerModel The rocker template class compiled from the template for this file.
     * @throws IOException throws IOException
     */
    default void transfer(String folder, String path, String filename, DefaultRockerModel rockerModel) throws IOException {
        String absPath = folder + (path.isEmpty()? "" : separator + path);
        if(Files.notExists(Paths.get(absPath))) {
            Files.createDirectories(Paths.get(absPath));
        }
        try(FileOutputStream fos = new FileOutputStream(absPath + separator + filename);
            ReadableByteChannel rbc = rockerModel.render(ArrayOfByteArraysOutput.FACTORY).asReadableByteChannel()) {
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        }
    }

    /**
     * This is a default method used by all generators to transfer a static file into the right location.
     *
     * @param srcPath The source path and name relative from src/main/resources
     * @param destFolder The destination project folder
     * @param destPath The destination path relative from the generated project folder
     * @param destName The destination filename
     * @throws IOException throws IOException
     */
    default void transfer(String srcPath, String destFolder, String destPath, String destName) throws IOException {
        String absPath = destFolder + (destPath.isEmpty()? "" : separator + destPath);
        if(Files.notExists(Paths.get(absPath))) {
            Files.createDirectories(Paths.get(absPath));
        }
        try(InputStream ins = Generator.class.getResourceAsStream(srcPath); FileOutputStream fos = new FileOutputStream(absPath + separator + destName)) {
            IOUtils.copy(ins, fos);
        }
    }

    /**
     * This is a default method to update the path with executable permission for owner, group and other
     *
     * @param path The target file with path
     * @throws IOException IOException
     */
    default void setExecutable(String path) throws IOException {
        File file = new File(path);
        if(file.exists()) {
            file.setExecutable(true, false);
        }
    }

    /**
     * Transfer the mvnw and wrapper to the target project folder from generator resources
     *
     * @param targetPath the target folder the maven commands and wrapper that need to write to
     * @throws IOException IOException
     */
    default void transferMaven(final String targetPath) throws IOException {
        transfer("/maven/mvnw", targetPath, "", "mvnw");
        setExecutable(Paths.get(targetPath, "mvnw").toString());
        transfer("/maven/mvnw.cmd", targetPath, "", "mvnw.cmd");
        transfer("/maven/mvn/wrapper/maven-wrapper.jar", targetPath, ".mvn/wrapper", "maven-wrapper.jar");
        transfer("/maven/mvn/wrapper/maven-wrapper.properties", targetPath, ".mvn/wrapper", "maven-wrapper.properties");
        transfer("/maven/mvn/wrapper/MavenWrapperDownloader.java", targetPath, ".mvn/wrapper", "MavenWrapperDownloader.java");
    }

    /**
     * Transfer the gradlew and wrapper to the target project folder from generator resources
     *
     * @param targetPath the target folder the gradlew commands and wrapper that need to write to
     * @throws IOException IOException
     */
    default void transferGradle(final String targetPath) throws IOException {
        transfer("/gradle/gradlew", targetPath, "", "gradlew");
        setExecutable(Paths.get(targetPath, "gradlew").toString());
        transfer("/gradle/gradlew.bat", targetPath, "", "gradlew.bat");
        transfer("/gradle/gradle/wrapper/gradle-wrapper.jar", targetPath, "gradle/wrapper", "gradle-wrapper.jar");
        transfer("/gradle/gradle/wrapper/gradle-wrapper.properties", targetPath, "gradle/wrapper", "gradle-wrapper.properties");
    }

    /**
     * This is a default method to check if a handler or handler test exists or not before making overwrite decision.
     *
     * @param folder The output folder of the project
     * @param path  Current file path in the output folder
     * @param filename Current filename in the output folder
     * @throws IOException throws IOException
     * @return boolean if the path exists or not
     */
    default boolean checkExist(String folder, String path, String filename) throws IOException {
        String absPath = folder + (path.isEmpty()? "" : separator + path) + separator + filename;
        return Files.exists(Paths.get(absPath));
    }

    static void copyFile(final InputStream is, final java.nio.file.Path folder) throws IOException {
        java.nio.file.Path parent = folder.getParent();
        if (!Files.isDirectory(parent)) {
            Files.createDirectories(parent);
        }

        Files.copy(is, folder, StandardCopyOption.REPLACE_EXISTING);
    }

    default String getRootPackage(JsonNode config, String defaultValue) {
        String rootPackage = defaultValue == null ? "com.networknt.app" : defaultValue;
        JsonNode jsonNode = config.get("rootPackage");
        if(jsonNode == null) {
            ((ObjectNode)config).put("rootPackage", rootPackage);
        } else {
            rootPackage = jsonNode.textValue();
        }
        return rootPackage;
    }

    default String getModelPackage(JsonNode config, String defaultValue) {
        String rootPackage = getRootPackage(config, null);
        String modelPackage = defaultValue == null ? rootPackage + ".model" : defaultValue;
        JsonNode jsonNode = config.get("modelPackage");
        if(jsonNode == null) {
            ((ObjectNode)config).put("modelPackage", modelPackage);
        } else {
            modelPackage = jsonNode.textValue();
        }
        return modelPackage;
    }

    default String getHandlerPackage(JsonNode config, String defaultValue) {
        String rootPackage = getRootPackage(config, null);
        String handlerPackage = defaultValue == null ? rootPackage + ".handler" : defaultValue;
        JsonNode jsonNode = config.get("handlerPackage");
        if(jsonNode == null) {
            ((ObjectNode)config).put("handlerPackage", handlerPackage);
        } else {
            handlerPackage = jsonNode.textValue();
        }
        return handlerPackage;
    }

    default boolean isOverwriteHandler(JsonNode config, Boolean defaultValue) {
        boolean overwriteHandler = defaultValue == null ? true : defaultValue;
        JsonNode jsonNode = config.get("overwriteHandler");
        if(jsonNode == null) {
            ((ObjectNode)config).put("overwriteHandler", overwriteHandler);
        } else {
            overwriteHandler = jsonNode.booleanValue();
        }
        return overwriteHandler;
    }

    default boolean isOverwriteHandlerTest(JsonNode config, Boolean defaultValue) {
        boolean overwriteHandlerTest = defaultValue == null ? true : defaultValue;
        JsonNode jsonNode = config.get("overwriteHandlerTest");
        if(jsonNode == null) {
            ((ObjectNode)config).put("overwriteHandlerTest", overwriteHandlerTest);
        } else {
            overwriteHandlerTest = jsonNode.booleanValue();
        }
        return overwriteHandlerTest;
    }

    default boolean isOverwriteModel(JsonNode config, Boolean defaultValue) {
        boolean overwriteModel = defaultValue == null ? true : defaultValue;
        JsonNode jsonNode = config.get("overwriteModel");
        if(jsonNode == null) {
            ((ObjectNode)config).put("overwriteModel", overwriteModel);
        } else {
            overwriteModel = jsonNode.booleanValue();
        }
        return overwriteModel;
    }

    default boolean isUseLightProxy(JsonNode config, Boolean defaultValue) {
        boolean useLightProxy = defaultValue == null ? false : defaultValue;
        JsonNode jsonNode = config.get("useLightProxy");
        if(jsonNode == null) {
            ((ObjectNode)config).put("useLightProxy", useLightProxy);
        } else {
            useLightProxy = jsonNode.booleanValue();
        }
        return useLightProxy;
    }

    default boolean isGenerateModelOnly(JsonNode config, Boolean defaultValue) {
        boolean generateModelOnly = defaultValue == null ? false : defaultValue;
        JsonNode jsonNode = config.get("generateModelOnly");
        if(jsonNode == null) {
            ((ObjectNode)config).put("generateModelOnly", generateModelOnly);
        } else {
            generateModelOnly = jsonNode.booleanValue();
        }
        return generateModelOnly;
    }

    default boolean isEnableHttp(JsonNode config, Boolean defaultValue) {
        boolean enableHttp = defaultValue == null ? false : defaultValue;
        JsonNode jsonNode = config.get("enableHttp");
        if(jsonNode == null) {
            ((ObjectNode)config).put("enableHttp", enableHttp);
        } else {
            enableHttp = jsonNode.booleanValue();
        }
        return enableHttp;
    }

    default String getHttpPort(JsonNode config, String defaultValue) {
        String httpPort = defaultValue == null ? "8080" : defaultValue;
        JsonNode jsonNode = config.get("httpPort");
        if(jsonNode == null) {
            ((ObjectNode)config).put("httpPort", httpPort);
        } else {
            httpPort = jsonNode.asText();
        }
        return httpPort;
    }

    default boolean isEnableHttps(JsonNode config, Boolean defaultValue) {
        boolean enableHttps = defaultValue == null ? true : defaultValue;
        JsonNode jsonNode = config.get("enableHttps");
        if(jsonNode == null) {
            ((ObjectNode)config).put("enableHttps", enableHttps);
        } else {
            enableHttps = jsonNode.booleanValue();
        }
        return enableHttps;
    }

    default boolean isEnableHttp2(JsonNode config, Boolean defaultValue) {
        boolean enableHttp2 = defaultValue == null ? true : defaultValue;
        JsonNode jsonNode = config.get("enableHttp2");
        if(jsonNode == null) {
            ((ObjectNode)config).put("enableHttp2", enableHttp2);
        } else {
            enableHttp2 = jsonNode.booleanValue();
        }
        return enableHttp2;
    }

    default String getHttpsPort(JsonNode config, String defaultValue) {
        String httpsPort = defaultValue == null ? "8443" : defaultValue;
        JsonNode jsonNode = config.get("httpsPort");
        if(jsonNode == null) {
            ((ObjectNode)config).put("httpsPort", httpsPort);
        } else {
            httpsPort = jsonNode.asText();
        }
        return httpsPort;
    }

    default boolean isEnableRegistry(JsonNode config, Boolean defaultValue) {
        boolean enableRegistry = defaultValue == null ? false : defaultValue;
        JsonNode jsonNode = config.get("enableRegistry");
        if(jsonNode == null) {
            ((ObjectNode)config).put("enableRegistry", enableRegistry);
        } else {
            enableRegistry = jsonNode.booleanValue();
        }
        return enableRegistry;
    }

    default boolean isEclipseIDE(JsonNode config, Boolean defaultValue) {
        boolean eclipseIDE = defaultValue == null ? false : defaultValue;
        JsonNode jsonNode = config.get("eclipseIDE");
        if(jsonNode == null) {
            ((ObjectNode)config).put("eclipseIDE", eclipseIDE);
        } else {
            eclipseIDE = jsonNode.booleanValue();
        }
        return eclipseIDE;
    }

    default boolean isSupportClient(JsonNode config, Boolean defaultValue) {
        boolean supportClient = defaultValue == null ? false : defaultValue;
        JsonNode jsonNode = config.get("supportClient");
        if(jsonNode == null) {
            ((ObjectNode)config).put("supportClient", supportClient);
        } else {
            supportClient = jsonNode.booleanValue();
        }
        return supportClient;
    }

    default boolean isPrometheusMetrics(JsonNode config, Boolean defaultValue) {
        boolean prometheusMetrics = defaultValue == null ? false : defaultValue;
        JsonNode jsonNode = config.get("prometheusMetrics");
        if(jsonNode == null) {
            ((ObjectNode)config).put("prometheusMetrics", prometheusMetrics);
        } else {
            prometheusMetrics = jsonNode.booleanValue();
        }
        return prometheusMetrics;
    }

    default String getDockerOrganization(JsonNode config, String defaultValue) {
        String dockerOrganization = defaultValue == null ? "networknt" : defaultValue;
        JsonNode jsonNode = config.get("dockerOrganization");
        if(jsonNode == null) {
            ((ObjectNode)config).put("dockerOrganization", dockerOrganization);
        } else {
            dockerOrganization = jsonNode.textValue();
        }
        return dockerOrganization;
    }

    default String getVersion(JsonNode config, String defaultValue) {
        String version = defaultValue == null ? "1.0.0" : defaultValue;
        JsonNode jsonNode = config.get("version");
        if(jsonNode == null) {
            ((ObjectNode)config).put("version", version);
        } else {
            version = jsonNode.textValue();
        }
        return version;
    }

    default String getGroupId(JsonNode config, String defaultValue) {
        String groupId = defaultValue == null ? "com.networknt" : defaultValue;
        JsonNode jsonNode = config.get("groupId");
        if(jsonNode == null) {
            ((ObjectNode)config).put("groupId", groupId);
        } else {
            groupId = jsonNode.textValue();
        }
        return groupId;
    }

    default String getArtifactId(JsonNode config, String defaultValue) {
        String artifactId = defaultValue == null ? "app" : defaultValue;
        JsonNode jsonNode = config.get("artifactId");
        if(jsonNode == null) {
            ((ObjectNode)config).put("artifactId", artifactId);
        } else {
            artifactId = jsonNode.textValue();
        }
        return artifactId;
    }

    default boolean isSpecChangeCodeReGenOnly(JsonNode config, Boolean defaultValue) {
        boolean specChangeCodeReGenOnly = defaultValue == null ? false : defaultValue;
        JsonNode jsonNode = config.get("specChangeCodeReGenOnly");
        if(jsonNode == null) {
            ((ObjectNode)config).put("specChangeCodeReGenOnly", specChangeCodeReGenOnly);
        } else {
            specChangeCodeReGenOnly = jsonNode.booleanValue();
        }
        return specChangeCodeReGenOnly;
    }

    default boolean isEnableParamDescription(JsonNode config, Boolean defaultValue) {
        boolean enableParamDescription = defaultValue == null ? false : defaultValue;
        JsonNode jsonNode = config.get("enableParamDescription");
        if(jsonNode == null) {
            ((ObjectNode)config).put("enableParamDescription", enableParamDescription);
        } else {
            enableParamDescription = jsonNode.booleanValue();
        }
        return enableParamDescription;
    }

    default boolean isSkipPomFile(JsonNode config, Boolean defaultValue) {
        boolean skipPomFile = defaultValue == null ? false : defaultValue;
        JsonNode jsonNode = config.get("skipPomFile");
        if(jsonNode == null) {
            ((ObjectNode)config).put("skipPomFile", skipPomFile);
        } else {
            skipPomFile = jsonNode.booleanValue();
        }
        return skipPomFile;
    }

    default boolean isKafkaProducer(JsonNode config, Boolean defaultValue) {
        boolean kafkaProducer = defaultValue == null ? false : defaultValue;
        JsonNode jsonNode = config.get("kafkaProducer");
        if(jsonNode == null) {
            ((ObjectNode)config).put("kafkaProducer", kafkaProducer);
        } else {
            kafkaProducer = jsonNode.booleanValue();
        }
        return kafkaProducer;
    }

    default boolean isKafkaConsumer(JsonNode config, Boolean defaultValue) {
        boolean kafkaConsumer = defaultValue == null ? false : defaultValue;
        JsonNode jsonNode = config.get("kafkaConsumer");
        if(jsonNode == null) {
            ((ObjectNode)config).put("kafkaConsumer", kafkaConsumer);
        } else {
            kafkaConsumer = jsonNode.booleanValue();
        }
        return kafkaConsumer;
    }

    default boolean isSupportAvro(JsonNode config, Boolean defaultValue) {
        boolean supportAvro = defaultValue == null ? false : defaultValue;
        JsonNode jsonNode = config.get("supportAvro");
        if(jsonNode == null) {
            ((ObjectNode)config).put("supportAvro", supportAvro);
        } else {
            supportAvro = jsonNode.booleanValue();
        }
        return supportAvro;
    }

    default String getKafkaTopic(JsonNode config, String defaultValue) {
        String kafkaTopic = defaultValue == null ? "event" : defaultValue;
        JsonNode jsonNode = config.get("kafkaTopic");
        if(jsonNode == null) {
            ((ObjectNode)config).put("kafkaTopic", kafkaTopic);
        } else {
            kafkaTopic = jsonNode.textValue();
        }
        return kafkaTopic;
    }

    default String getDecryptOption(JsonNode config, String defaultValue) {
        String decryptOption = defaultValue == null ? "default" : defaultValue;
        JsonNode jsonNode = config.get("decryptOption");
        if(jsonNode == null) {
            ((ObjectNode)config).put("decryptOption", decryptOption);
        } else {
            decryptOption = jsonNode.textValue();
        }
        return decryptOption;
    }

}
