package com.networknt.codegen;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fizzed.rocker.runtime.RockerRuntime;
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
    @Parameter(names={"--config", "-c"}, required = true,
            description = "Configuration parameters in the form of a file to be passed into your service.")
    String config;
    @Parameter(names={"--output", "-o"},
            description = "The output location of the folder which contains the generated codebase.")
    String output = ".";

    @Parameter(names={"--help", "-h"}, help = true)
    private boolean help;
    
    @Parameter(names={"--reload", "-r"}, description = "Specifies whether rocker hot-reloading should be enabled or not. Default is false."+
    "If this is set to true, the file 'rocker-compiler.conf' must be available in the classpath.")
    private boolean reload;    

    public static void main(String ... argv) throws Exception {
        try {
            Cli cli = new Cli();
            JCommander jCommander = JCommander.newBuilder()
                    .addObject(cli)
                    .build();
            jCommander.parse(argv);
            cli.run(jCommander);
        } catch (ParameterException e)
        {
            System.out.println("Command line parameter error: " + e.getLocalizedMessage());
            e.usage();
        }
    }

    public void run(JCommander jCommander) throws Exception {
        if (help) {
            jCommander.usage();
            return;
        }
        
        RockerRuntime.getInstance().setReloading(reload);

        System.out.printf("%s %s %s %s\n", framework, model, config, output);

        FrameworkRegistry registry = FrameworkRegistry.getInstance();
        Set<String> frameworks = registry.getFrameworks();
        if(frameworks.contains(framework)) {
            Generator generator = registry.getGenerator(framework);
            Object anyModel = null;
            // model can be empty in some cases.
            if(model != null) {
                // check if model is json or not before loading.
                if(model.endsWith("json")) {
                    if(Utils.isUrl(model)) {
                        anyModel = JsonIterator.deserialize(Utils.urlToByteArray(new URL(model)));
                    } else {
                        anyModel = JsonIterator.deserialize(Files.readAllBytes(Paths.get(model)));
                    }
                } else {
                    if(Utils.isUrl(model)) {
                        anyModel = new String(Utils.urlToByteArray(new URL(model)), StandardCharsets.UTF_8);
                    } else {
                        anyModel = new String(Files.readAllBytes(Paths.get(model)), StandardCharsets.UTF_8);
                    }
                }
            }

            Any anyConfig = null;
            if(config != null) {
                if(Utils.isUrl(config)) {
                    anyConfig = JsonIterator.deserialize(Utils.urlToByteArray(new URL(config)));
                } else {
                    anyConfig = JsonIterator.deserialize(Files.readAllBytes(Paths.get(config)));
                }
            }
            generator.generate(output, anyModel, anyConfig);
            System.out.println("A project has been generated successfully in " + output + " folder. Have fun!!!");
        } else {
            System.out.printf("Invalid framework: %s\navaliable frameworks:\n", framework);
            for(String frm : frameworks) {
                System.out.println("\t"+frm);
            }
        }
    }

}
