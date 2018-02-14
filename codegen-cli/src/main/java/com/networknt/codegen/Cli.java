package com.networknt.codegen;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

/**
 * Created by steve on 24/04/17.
 */
public class Cli {

    @Parameter(names={"--framework", "-f"}, required = true,
            description = "The framework template to be used as scaffolding to the generated code.")
    String framework;
    @Parameter(names={"--model", "-m"},
            description = "The model (aka spec, schema, etc..) provided will drive code generation to match given " +
                    "interface functions, parameter types, scopes, etc... " +
                    "This can be in differing expected formats, given the intended framework type to generate. " +
                    "Please see the documentation for the most up to date usage.")
    String model;
    @Parameter(names={"--config", "-c"},
            description = "Configuration parameters in the form of a file to be passed into your service.")
    String config;
    @Parameter(names={"--output", "-o"},
            description = "The output location of the folder which contains the generated codebase.")
    String output;

    @Parameter(names={"--help", "-h"}, help = true)
    private boolean help;

    public static void main(String ... argv) {
        Cli cli = new Cli();
        JCommander jCommander = JCommander.newBuilder()
                .addObject(cli)
                .build();
        jCommander.parse(argv);
        cli.run(jCommander);
    }

    public void run(JCommander jCommander) {
        if (help) {
            jCommander.usage();
            return;
        }

        System.out.printf("%s %s %s %s\n", framework, model, config, output);

        FrameworkRegistry registry = FrameworkRegistry.getInstance();
        Set<String> frameworks = registry.getFrameworks();
        if(frameworks.contains(framework)) {
            Generator generator = registry.getGenerator(framework);
            try {
                Object anyModel = null;
                // model can be empty in some cases.
                if(model != null) {
                    // check if model is json or not before loading.
                    if(model.endsWith("json")) {
                        if(isUrl(model)) {
                            anyModel = JsonIterator.deserialize(urlToByteArray(new URL(model)));
                        } else {
                            anyModel = JsonIterator.deserialize(Files.readAllBytes(Paths.get(model)));
                        }
                    } else {
                        if(isUrl(model)) {
                            anyModel = new String(urlToByteArray(new URL(model)), StandardCharsets.UTF_8);
                        } else {
                            anyModel = new String(Files.readAllBytes(Paths.get(model)), StandardCharsets.UTF_8);
                        }
                    }
                }

                Any anyConfig = null;
                if(config != null) {
                    if(isUrl(config)) {
                        anyConfig = JsonIterator.deserialize(urlToByteArray(new URL(config)));
                    } else {
                        anyConfig = JsonIterator.deserialize(Files.readAllBytes(Paths.get(config)));
                    }
                }
               generator.generate(output, anyModel, anyConfig);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.printf("Invalid framework %s", framework);
        }
    }

    private boolean isUrl(String location) {
        return location.startsWith("http://") || location.startsWith("https://");
    }

    private byte[] urlToByteArray(URL url) throws IOException{
        try (BufferedInputStream in = new BufferedInputStream(url.openStream()); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte data[] = new byte[1024];
            int count;
            while ((count = in.read(data, 0, 1024)) != -1) {
                out.write(data, 0, count);
            }
            return out.toByteArray();
        }
    }
}
