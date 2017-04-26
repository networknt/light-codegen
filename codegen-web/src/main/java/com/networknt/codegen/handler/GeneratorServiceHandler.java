package com.networknt.codegen.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.networknt.codegen.Utils;
import com.networknt.codegen.rest.RestGenerator;
import com.networknt.rpc.Handler;
import com.networknt.rpc.router.ServiceHandler;
import com.networknt.status.Status;
import com.networknt.utility.HashUtil;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Created by steve on 25/04/17.
 */
@ServiceHandler(id="lightapi.net/codegen/generate/0.0.1")
public class GeneratorServiceHandler implements Handler {
    static private final String STATUS_INVALID_FRAMEWORK = "ERR11100";

    static private final XLogger logger = XLoggerFactory.getXLogger(GeneratorServiceHandler.class);

    @Override
    public ByteBuffer handle(Object input)  {
        logger.entry(input);
        Map<String, Object> map = (Map<String, Object>)input;
        String framework = (String)map.get("framework");
        Map<String, Object> model = (Map<String, Object>)map.get("model");  // should be a json of spec
        Map<String, Object> config = (Map<String, Object>)map.get("config"); // should be a json of config
        if(!FrameworkListHandler.frameworks.contains(framework)) {
            Status status = new Status("ERR11100", framework);
            return Utils.toByteBuffer(status.toString());
        }
        // TODO validate the model and config with json schema

        // generate a destination folder name.
        String output = HashUtil.generateUUID();
        CompletableFuture.runAsync(() -> {
            try {
                if(framework != null && framework.equals("light-java-rest")) {
                    RestGenerator generator = new RestGenerator();
                    generator.generate("/tmp/" + output, model, config);
                }

                // generated code is in tmp folder, zip and move to the target folder

            } catch (Exception e) {
                logger.error("Exception:", e);
            }
        });

        // return the location of the zip file
        String zipFile = output + ".zip";
        return Utils.toByteBuffer(zipFile);
    }
}
