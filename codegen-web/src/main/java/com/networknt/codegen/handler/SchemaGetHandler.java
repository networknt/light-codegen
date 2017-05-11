package com.networknt.codegen.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.networknt.codegen.FrameworkRegistry;
import com.networknt.codegen.Generator;
import com.networknt.config.Config;
import com.networknt.rpc.Handler;
import com.networknt.rpc.router.ServiceHandler;
import com.networknt.utility.NioUtils;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Set;

/**
 * Created by steve on 10/05/17.
 */
@ServiceHandler(id="lightapi.net/codegen/getSchema/0.0.1")
public class SchemaGetHandler implements Handler {
    static private final XLogger logger = XLoggerFactory.getXLogger(SchemaGetHandler.class);

    @Override
    public ByteBuffer handle(Object input)  {
        logger.entry(input);
        Map<String, Object> map = (Map<String, Object>)input;
        String framework = (String)map.get("framework");
        Generator generator = FrameworkRegistry.getInstance().getGenerator(framework);

        ByteBuffer bf = null;
        try {
            bf = generator.getConfigSchema();
        } catch (IOException e) {
            // return empty back in this case.

        }
        return bf;
    }

}
