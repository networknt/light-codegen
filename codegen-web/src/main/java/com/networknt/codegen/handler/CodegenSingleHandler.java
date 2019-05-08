package com.networknt.codegen.handler;

import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;
import com.networknt.codegen.CodegenWebConfig;
import com.networknt.codegen.FrameworkRegistry;
import com.networknt.codegen.Generator;
import com.networknt.codegen.Utils;
import com.networknt.config.Config;
import com.networknt.config.JsonMapper;
import com.networknt.rpc.Handler;
import com.networknt.rpc.router.JsonHandler;
import com.networknt.rpc.router.ServiceHandler;
import com.networknt.status.Status;
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
 * This is the handler that does the code generation for consumer request from the single page application in the view folder. There
 * handler is only responsible for single project generation. It can accept text or url for the model and config from the UI.
 *
 * @author Steve Hu
 */
@ServiceHandler(id="lightapi.net/codegen/single/0.0.1")
public class CodegenSingleHandler implements Handler {
    static private final String CONFIG_NAME = "codegen-web";
    static private final String STATUS_INVALID_FRAMEWORK = "ERR11100";
    static private final String STATUS_MISSING_GENERATOR_ITEM = "ERR11101";
    static private final String STATUS_INVALID_MODEL_URL = "ERR11103";
    static private final String STATUS_INVALID_CONFIG_JSON = "ERR11104";
    static private final String STATUS_INVALID_CONFIG_URL_EXTENSION = "ERR11105";
    static private final String STATUS_GENERATOR_EXCEPTION = "ERR11106";
    static private final String STATUS_COMPRESSION_EXCEPTION = "ERR11107";

    static private final Logger logger = LoggerFactory.getLogger(CodegenSingleHandler.class);

    static private CodegenWebConfig codegenWebConfig = (CodegenWebConfig) Config.getInstance().getJsonObjectConfig(CONFIG_NAME, CodegenWebConfig.class);


    @Override
    public ByteBuffer handle(HttpServerExchange exchange, Object input)  {
        // generate a destination folder name.
        String output = HashUtil.generateUUID();
        String zipFile = output + ".zip";
        String projectFolder = codegenWebConfig.getTmpFolder() + separator + output;

        Map<String, Object> generatorMap = (Map<String, Object>)input;
        if(logger.isDebugEnabled()) logger.debug("dataMap = " + JsonMapper.toJson(generatorMap));

        if(generatorMap == null) {
            return NioUtils.toByteBuffer(getStatus(exchange, STATUS_MISSING_GENERATOR_ITEM));
        }
        String framework = (String)generatorMap.get("framework");
        if(!FrameworkRegistry.getInstance().getFrameworks().contains(framework)) {
            return NioUtils.toByteBuffer(getStatus(exchange, STATUS_INVALID_FRAMEWORK, framework));
        }
        try {
            String modelType = (String)generatorMap.get("modelType");
            Object model = null; // the model can be Any or String depending on the framework
            if("C".equals(modelType)) {
                String modelText = (String)generatorMap.get("modelText");
                modelText = modelText.trim();
                // json or yaml?
                if(modelText.startsWith("{") || modelText.startsWith("[")) {
                    // This is a json string.
                    model = JsonIterator.deserialize(modelText);
                } else {
                    model = modelText;
                }
            } else if("U".equals(modelType)) {
                String modelUrl = (String)generatorMap.get("modelUrl");
                // make sure it is a valid URL.
                if(Utils.isUrl(modelUrl)) {
                    // if it is a json file.
                    if(modelUrl.endsWith(".json")) {
                        model = JsonIterator.deserialize(Utils.urlToByteArray(new URL(modelUrl)));
                    } else {
                        model = new String(Utils.urlToByteArray(new URL(modelUrl)), StandardCharsets.UTF_8);
                    }
                } else {
                    return NioUtils.toByteBuffer(getStatus(exchange, STATUS_INVALID_MODEL_URL, modelUrl));
                }
            }

            String configType = (String)generatorMap.get("configType");
            Any config = null;
            if("C".equals(configType)) {
                String configText = (String)generatorMap.get("configText");
                configText = configText.trim();
                // the config must be json.
                if(configText.startsWith("{") || configText.startsWith("[")) {
                    config = JsonIterator.deserialize(configText);
                } else {
                    return NioUtils.toByteBuffer(getStatus(exchange, STATUS_INVALID_CONFIG_JSON));
                }
            } else if("U".equals(configType)) {
                String configUrl = (String)generatorMap.get("configUrl");
                configUrl = configUrl.trim();
                // make sure that the file extension is .json
                if(configUrl.endsWith(".json")) {
                    config = JsonIterator.deserialize(Utils.urlToByteArray(new URL(configUrl)));
                } else {
                    return NioUtils.toByteBuffer(getStatus(exchange, STATUS_INVALID_CONFIG_URL_EXTENSION, configUrl));
                }
            }

            Generator generator = FrameworkRegistry.getInstance().getGenerator(framework);
            generator.generate(projectFolder, model, Any.wrap(config));

        } catch (Exception e) {
            logger.error("Exception:", e);
            // return an error status to the user.
            return NioUtils.toByteBuffer(getStatus(exchange, STATUS_GENERATOR_EXCEPTION, e.getMessage()));
        }

        try {
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

    @Override
    public ByteBuffer validate(String serviceId, Object object) {
        // get schema from serviceId, remember that the schema is for the data object only.
        // the input object is the data attribute of the request body.
        Map<String, Object> serviceMap = (Map<String, Object>) JsonHandler.schema.get(serviceId);
        if(logger.isDebugEnabled()) {
            try {
                logger.debug("serviceId = " + serviceId  + " serviceMap = " + Config.getInstance().getMapper().writeValueAsString(serviceMap));
            } catch (Exception e) {
                logger.error("Exception:", e);
            }
        }
        logger.debug("Skipping validation on generator request for now.");
        return null;
    }
}
