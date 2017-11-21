package com.networknt.codegen.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.networknt.codegen.FrameworkRegistry;
import com.networknt.config.Config;
import com.networknt.rpc.Handler;
import com.networknt.rpc.router.ServiceHandler;
import com.networknt.utility.NioUtils;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.nio.ByteBuffer;
import java.util.Set;

/**
 * Created by steve on 26/04/17.
 */
@ServiceHandler(id="lightapi.net/codegen/listFramework/0.0.1")
public class FrameworkListHandler implements Handler {
    static private final XLogger logger = XLoggerFactory.getXLogger(FrameworkListHandler.class);
    static private Set<String> frameworks = FrameworkRegistry.getInstance().getFrameworks();

    @Override
    public ByteBuffer handle(Object input)  {
        logger.entry(input);
        String result = "";
        try {
            result = Config.getInstance().getMapper().writeValueAsString(frameworks);
        } catch (JsonProcessingException e) {
            // return empty back in this case.
        }
        return NioUtils.toByteBuffer(result);
    }

}
