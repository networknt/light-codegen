package com.networknt.codegen;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.codegen.rest.RestGenerator;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

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
        FrameworkRegistry registry = FrameworkRegistry.getInstance();
        Set<String> frameworks = registry.getFrameworks();
        if(frameworks.contains(framework)) {
            Generator generator = registry.getGenerator(framework);
            try {
                Map<String, Object> modelJson;
                if(isUrl(model)) {
                    modelJson = mapper.readValue(new URL(model), new TypeReference<Map<String,Object>>(){});
                } else {
                    Path modelPath = Paths.get(model); // swagger.json
                    modelJson = mapper.readValue(modelPath.toFile(), new TypeReference<Map<String,Object>>(){});
                }

                Map<String, Object> configJson;
                if(isUrl(config)) {
                    configJson = mapper.readValue(new URL(config), new TypeReference<Map<String,Object>>(){});
                } else {
                    Path configPath = Paths.get(config); // config.json
                    configJson = mapper.readValue(configPath.toFile(), new TypeReference<Map<String,Object>>(){});
                }
               generator.generate(output, modelJson, configJson);
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
}
