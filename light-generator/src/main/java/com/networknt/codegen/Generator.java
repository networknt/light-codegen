package com.networknt.codegen;

import com.fizzed.rocker.runtime.ArrayOfByteArraysOutput;
import com.fizzed.rocker.runtime.DefaultRockerModel;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.util.Map;

import static java.io.File.separator;

/**
 * Created by stevehu on 2017-04-23.
 */
public interface Generator {
    void generate(String targetPath, Object model, Map<String, Object> config) throws IOException;

    default void transfer(String targetPath, String filename, DefaultRockerModel rockerModel) throws IOException {
        try(FileOutputStream fos = new FileOutputStream(targetPath + separator + filename);
            ReadableByteChannel rbc = rockerModel.render(ArrayOfByteArraysOutput.FACTORY).asReadableByteChannel()) {
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        }
    }

}
