package com.networknt.codegen;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.codegen.rest.RestGenerator;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Created by steve on 24/04/17.
 */
public class Cli {
    public static ObjectMapper mapper = new ObjectMapper();

    @Parameter(names={"--framework", "-f"})
    String framework;
    @Parameter(names={"--model", "-m"})
    String model;
    @Parameter(names={"--config", "-c"})
    String config;
    @Parameter(names={"--output", "-o"})
    String output;


    public static void main(String ... argv) {
        Cli cli = new Cli();
        JCommander.newBuilder()
                .addObject(cli)
                .build()
                .parse(argv);
        cli.run();
    }

    public void run() {
        System.out.printf("%s %s %s %s", framework, model, config, output);
        try {
            // TODO All generators should be implemented as SPI.
            if(framework != null && framework.equals("light-java-rest")) {
                Path modelPath = Paths.get(model); // swagger.json
                Map<String, Object> modelJson = mapper.readValue(modelPath.toFile(), new TypeReference<Map<String,Object>>(){});
                Path configPath = Paths.get(config); // config.json
                Map<String, Object> configJson = mapper.readValue(configPath.toFile(), new TypeReference<Map<String,Object>>(){});
                RestGenerator generator = new RestGenerator();
                generator.generate(output, modelJson, configJson);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
