package com.networknt.codegen;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fizzed.rocker.runtime.RockerRuntime;
import com.networknt.utility.NioUtils;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import static java.io.File.separator;

/**
 * Created by steve on 24/04/17.
 */
public class Cli {
    private static final String JSON="json";
    private static final String YAML="yaml";
    private static final String YML="yml";
    private static final String GRAPHQLS = "graphqls";

    private ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

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

    @Parameter(names={"--parameterize", "-p"}, description = "The location of configuration files that need to be parameterized.")
    String parameterizationDir;

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
            Object modelNode = null;
            // model can be empty in some cases.
            if(model != null) {
                String ext = NioUtils.getFileExtension(model);
                if(StringUtils.equalsIgnoreCase(ext, JSON)) {
                    if (Utils.isUrl(model)) {
                        modelNode = Generator.jsonMapper.readTree(Utils.urlToByteArray(new URL(model)));
                    } else {
                        modelNode = Generator.jsonMapper.readTree(Files.readAllBytes(Paths.get(model)));
                    }
                } else if(StringUtils.equalsIgnoreCase(ext, YML) || StringUtils.equalsIgnoreCase(ext, YAML)) {
                    if (Utils.isUrl(model)) {
                        modelNode = Generator.yamlMapper.readTree(Utils.urlToByteArray(new URL(model)));
                    } else {
                        modelNode = Generator.yamlMapper.readTree(Files.readAllBytes(Paths.get(model)));
                    }
                } else if(StringUtils.equalsIgnoreCase(ext, GRAPHQLS)) {
                    if(Utils.isUrl(model)) {
                        modelNode = new String(Utils.urlToByteArray(new URL(model)), StandardCharsets.UTF_8);
                    } else {
                        modelNode = new String(Files.readAllBytes(Paths.get(model)), StandardCharsets.UTF_8);
                    }
                } else {
                    throw new UnsupportedOperationException("Unknown model file format " + ext);
                }
            }

            JsonNode configNode = null;
            if(config != null) {
                String ext = NioUtils.getFileExtension(config);
                if (StringUtils.equalsIgnoreCase(ext, JSON)) {
                    if (Utils.isUrl(config)) {
                        configNode = Generator.jsonMapper.readTree(Utils.urlToByteArray(new URL(config)));
                    } else {
                        configNode = Generator.jsonMapper.readTree(Files.readAllBytes(Paths.get(config)));
                    }
                } else if (StringUtils.equalsIgnoreCase(ext, YML)||StringUtils.equalsIgnoreCase(ext, YAML)) {
                    if (Utils.isUrl(config)) {
                        configNode = Generator.yamlMapper.readTree(Utils.urlToByteArray(new URL(config)));
                    } else {
                        configNode = Generator.yamlMapper.readTree(Files.readAllBytes(Paths.get(config)));
                    }
                } else {
                    throw new UnsupportedOperationException("Unknown file format " + ext);
                }

            }
            if(parameterizationDir != null) {
                Map<String, Object> map = Generator.jsonMapper.convertValue(configNode.get(YAMLFileParameterizer.GENERATE_ENV_VARS), new TypeReference<Map<String, Object>>(){});
                YAMLFileParameterizer.rewriteAll(parameterizationDir, output + separator + YAMLFileParameterizer.DEFAULT_DEST_DIR, map);
            }
            generator.generate(output, modelNode, configNode);
            System.out.println("A project has been generated successfully in " + output + " folder. Have fun!!!");
        } else {
            System.out.printf("Invalid framework: %s\navaliable frameworks:\n", framework);
            for(String frm : frameworks) {
                System.out.println("\t"+frm);
            }
        }
    }

}
