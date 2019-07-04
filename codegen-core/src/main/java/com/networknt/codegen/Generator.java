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

import com.fizzed.rocker.runtime.ArrayOfByteArraysOutput;
import com.fizzed.rocker.runtime.DefaultRockerModel;
import com.jsoniter.any.Any;

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

    // config schema cache
    Map<String, byte []> schemaMap = new HashMap<>();

    /**
     * Generate the project based on the input parameters.
     *
     * @param targetPath The output directory of the generated project
     * @param model The optional model data that trigger the generation, i.e. swagger specification, graphql IDL etc.
     * @param config A json object that controls how the generator behaves.
     * @throws IOException throws IOException
     */
    void generate(String targetPath, Object model, Any config) throws IOException;

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

}
