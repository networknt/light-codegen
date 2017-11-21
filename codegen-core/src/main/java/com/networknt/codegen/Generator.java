package com.networknt.codegen;

import com.fizzed.rocker.runtime.ArrayOfByteArraysOutput;
import com.fizzed.rocker.runtime.DefaultRockerModel;
import com.jsoniter.any.Any;
import org.apache.commons.io.IOUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static java.io.File.separator;

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

}
