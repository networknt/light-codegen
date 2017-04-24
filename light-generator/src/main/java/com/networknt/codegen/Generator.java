package com.networknt.codegen;

import com.fizzed.rocker.runtime.ArrayOfByteArraysOutput;
import com.fizzed.rocker.runtime.DefaultRockerModel;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;

import static java.io.File.separator;

/**
 * Created by stevehu on 2017-04-23.
 */
public interface Generator {
    void generate(String targetPath, Object model, Map<String, Object> config) throws IOException;

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

    default void copy(String folder, String path, String filename, Path src) throws IOException {
        /*
        if(Files.notExists(Paths.get(absPath))) {
            Files.createDirectories(Paths.get(absPath));
        }
        */
        Files.copy(src, Paths.get(folder, path, filename), StandardCopyOption.REPLACE_EXISTING);
    }

}
