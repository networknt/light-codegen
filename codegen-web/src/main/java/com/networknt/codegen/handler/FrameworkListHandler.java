package com.networknt.codegen.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.networknt.codegen.FrameworkRegistry;
import com.networknt.config.Config;
import com.networknt.rpc.Handler;
import com.networknt.rpc.router.ServiceHandler;
import com.networknt.utility.NioUtils;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Set;

/**
 * Created by steve on 26/04/17.
 */
@ServiceHandler(id="lightapi.net/codegen/listFramework/0.0.1")
public class FrameworkListHandler implements Handler {
    static private final Logger logger = LoggerFactory.getLogger(FrameworkListHandler.class);
    static private Set<String> frameworks = FrameworkRegistry.getInstance().getFrameworks();

    /**
     * Returns a JSON list of all available frameworks defined in the service.yml of codegen-cli.
     * If any issues occur with converting the result to JSON, an empty string is returned (not an empty json list).
     */
    @Override
    public ByteBuffer handle(HttpServerExchange exchange, Object input)  {
        String result = "";
        exchange.getResponseHeaders().add(new HttpString("Content-Type"), "application/json");
        try {
            result = Config.getInstance().getMapper().writeValueAsString(frameworks);
        } catch (JsonProcessingException e) {
            // return empty back in this case.
        }
        return NioUtils.toByteBuffer(result);
    }

}
