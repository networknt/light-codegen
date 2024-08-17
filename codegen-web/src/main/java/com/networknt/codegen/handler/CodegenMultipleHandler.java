package com.networknt.codegen.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.codegen.CodegenWebConfig;
import com.networknt.codegen.FrameworkRegistry;
import com.networknt.codegen.Generator;
import com.networknt.codegen.Utils;
import com.networknt.codegen.graphql.GraphqlGenerator;
import com.networknt.config.Config;
import com.networknt.config.JsonMapper;
import com.networknt.rpc.HybridHandler;
import com.networknt.rpc.router.JsonHandler;
import com.networknt.rpc.router.ServiceHandler;
import com.networknt.utility.HashUtil;
import com.networknt.utility.NioUtils;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static java.io.File.separator;

/**
 * This is the handler that does the code generation for consumer request with multiple projects. It is used to generate a folder with
 * more than one project. For example, generate query side and command side projects for CQRS.
 *
 * @author Steve Hu
 */
@ServiceHandler(id="lightapi.net/codegen/multiple/0.0.1")
public class CodegenMultipleHandler implements HybridHandler {
    static private final String CONFIG_NAME = "codegen-web";
    static private final String STATUS_INVALID_FRAMEWORK = "ERR11100";
    static private final String STATUS_MISSING_GENERATOR_ITEM = "ERR11101";
    static private final String STATUS_INVALID_MODEL_URL = "ERR11103";
    static private final String STATUS_INVALID_CONFIG_JSON = "ERR11104";
    static private final String STATUS_INVALID_CONFIG_URL_EXTENSION = "ERR11105";
    static private final String STATUS_GENERATOR_EXCEPTION = "ERR11106";
    static private final String STATUS_COMPRESSION_EXCEPTION = "ERR11107";

    static private final Logger logger = LoggerFactory.getLogger(CodegenMultipleHandler.class);

    static private CodegenWebConfig codegenWebConfig = (CodegenWebConfig) Config.getInstance().getJsonObjectConfig(CONFIG_NAME, CodegenWebConfig.class);


    @Override
    public ByteBuffer handle(HttpServerExchange exchange, Object input)  {
        // generate a destination folder name.
        String output = HashUtil.generateUUID();
        String zipFile = output + ".zip";
        String projectFolder = codegenWebConfig.getTmpFolder() + separator + output;

        List<Map<String, Object>> generators = (List<Map<String, Object>>)((Map<String, Object>)input).get("generators");

        if(generators == null || generators.size() == 0) {
            return NioUtils.toByteBuffer(getStatus(exchange, STATUS_MISSING_GENERATOR_ITEM));
        }

        try {
            for(Map<String, Object> generatorMap: generators) {
                String framework = (String)generatorMap.get("framework");
                if(!FrameworkRegistry.getInstance().getFrameworks().contains(framework)) {
                    return NioUtils.toByteBuffer(getStatus(exchange, STATUS_INVALID_FRAMEWORK, framework));
                }
                String modelType = (String)generatorMap.get("modelType");
                Object model = null; // the model can be JsonNode or String depending on the framework
                if("C".equals(modelType)) {
                    String modelText = (String)generatorMap.get("modelText");
                    if(GraphqlGenerator.FRAMEWORK.equals(framework)) {
                        model = modelText;
                    } else {
                        // json or yaml?
                        if(modelText.startsWith("{") || modelText.startsWith("[")) {
                            // This is a json string.
                            model = Generator.jsonMapper.readTree(modelText);
                        } else {
                            // This is a yaml string
                            model = Generator.yamlMapper.readTree(modelText);
                        }
                    }
                } else if("U".equals(modelType)) {
                    String modelUrl = (String)generatorMap.get("modelUrl");
                    // make sure it is a valid URL.
                    if(Utils.isUrl(modelUrl)) {
                        // if it is a json file.
                        if(modelUrl.endsWith(".json")) {
                            model = Generator.jsonMapper.readTree((Utils.urlToByteArray(new URL(modelUrl))));
                        } else if (modelUrl.endsWith(".yml") || modelUrl.endsWith(".yaml")) {
                            model = Generator.yamlMapper.readTree(Utils.urlToByteArray(new URL(modelUrl)));
                        } else {
                            model = new String(Utils.urlToByteArray(new URL(modelUrl)), StandardCharsets.UTF_8);
                        }
                    } else {
                        // return an error here for invalid model URL.
                        return NioUtils.toByteBuffer(getStatus(exchange, STATUS_INVALID_MODEL_URL, modelUrl));
                    }
                }

                String configType = (String)generatorMap.get("configType");
                JsonNode config = null;
                if("C".equals(configType)) {
                    String configText = (String)generatorMap.get("configText");
                    configText = configText.trim();
                    // the config must be json.
                    if(configText.startsWith("{") || configText.startsWith("[")) {
                        config = Generator.jsonMapper.readTree(configText);
                    } else {
                        config = Generator.yamlMapper.readTree(configText);
                    }
                } else if("U".equals(configType)) {
                    String configUrl = (String)generatorMap.get("configUrl");
                    configUrl = configUrl.trim();
                    if(configUrl.endsWith(".json")) {
                        config = Generator.jsonMapper.readTree(Utils.urlToByteArray(new URL(configUrl)));
                    } else if(configUrl.endsWith(".yml") || configUrl.endsWith(".yaml")) {
                        config = Generator.yamlMapper.readTree(Utils.urlToByteArray(new URL(configUrl)));
                    } else {
                        return NioUtils.toByteBuffer(getStatus(exchange, STATUS_INVALID_CONFIG_URL_EXTENSION, configUrl));
                    }
                }
                Generator generator = FrameworkRegistry.getInstance().getGenerator(framework);
                generator.generate(projectFolder, model, config);
            }
        } catch (Exception e) {
            logger.error("Exception:", e);
            return NioUtils.toByteBuffer(getStatus(exchange, STATUS_GENERATOR_EXCEPTION, e.getMessage()));
        }

        try {
            // TODO generated code is in tmp folder, zip and move to the target folder
            NioUtils.create(codegenWebConfig.getZipFolder() + separator + zipFile, projectFolder);
            // delete the project folder.
            Files.walk(Paths.get(projectFolder), FileVisitOption.FOLLOW_LINKS)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .peek(System.out::println)
                    .forEach(File::delete);
            // check if any zip file that needs to be deleted from zipFolder
            NioUtils.deleteOldFiles(codegenWebConfig.getZipFolder(), codegenWebConfig.getZipKeptMinute());
        } catch (Exception e) {
            logger.error("Exception:", e);
            return NioUtils.toByteBuffer(getStatus(exchange, STATUS_COMPRESSION_EXCEPTION, e.getMessage()));
        }

        exchange.getResponseHeaders()
                .add(new HttpString("Content-Type"), "application/zip")
                .add(new HttpString("Content-Disposition"), "attachment");

        // return the zip file
        File file = new File(codegenWebConfig.getZipFolder() + separator + zipFile);
        return NioUtils.toByteBuffer(file);
    }
}
