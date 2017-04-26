package com.networknt.codegen.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.networknt.codegen.rest.RestGenerator;
import com.networknt.rpc.Handler;
import com.networknt.rpc.router.ServiceHandler;
import com.networknt.utility.HashUtil;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Created by steve on 25/04/17.
 */
@ServiceHandler(id="lightapi.net/codegen/generate/0.0.1")
public class GeneratorServiceHandler implements Handler {
    static private final XLogger logger = XLoggerFactory.getXLogger(GeneratorServiceHandler.class);

    @Override
    public ByteBuffer handle(Object input)  {
        logger.entry(input);
        Map<String, Object> map = (Map<String, Object>)input;
        String framework = (String)map.get("framework");
        Map<String, Object> model = (Map<String, Object>)map.get("model");  // should be a json of spec
        Map<String, Object> config = (Map<String, Object>)map.get("config"); // should be a json of config

        // generate a destination folder name.
        String output = HashUtil.generateUUID();
        //System.out.printf("%s %s %s %s", framework, model, config);
        try {
            if(framework != null && framework.equals("light-java-rest")) {
                RestGenerator generator = new RestGenerator();
                generator.generate("/tmp/" + output, model, config);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // return the location of the zip file
        String zipFile = output + ".zip";
        ByteBuffer buffer = ByteBuffer.allocateDirect(zipFile.length());
        try {
            buffer.put(zipFile.getBytes("US-ASCII"));
        } catch (UnsupportedEncodingException e) {
            logger.error("Exception:" + e.getMessage(), e);
            throw new RuntimeException(e);
        }
        buffer.flip();
        return buffer;
    }
}
