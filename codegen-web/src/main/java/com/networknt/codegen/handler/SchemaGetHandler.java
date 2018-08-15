package com.networknt.codegen.handler;

import com.networknt.codegen.FrameworkRegistry;
import com.networknt.codegen.Generator;
import com.networknt.rpc.Handler;
import com.networknt.rpc.router.ServiceHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Created by steve on 10/05/17.
 */
@ServiceHandler(id="lightapi.net/codegen/getSchema/0.0.1")
public class SchemaGetHandler implements Handler {
    static private final Logger logger = LoggerFactory.getLogger(SchemaGetHandler.class);

    @Override
    public ByteBuffer handle(HttpServerExchange exchange, Object input)  {
        exchange.getResponseHeaders().add(new HttpString("Content-Type"), "application/json");
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
