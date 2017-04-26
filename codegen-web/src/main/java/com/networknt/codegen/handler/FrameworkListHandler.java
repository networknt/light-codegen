package com.networknt.codegen.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.networknt.codegen.Utils;
import com.networknt.codegen.rest.RestGenerator;
import com.networknt.config.Config;
import com.networknt.rpc.Handler;
import com.networknt.rpc.router.ServiceHandler;
import com.networknt.utility.HashUtil;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by steve on 26/04/17.
 */
@ServiceHandler(id="lightapi.net/framework/list/0.0.1")
public class FrameworkListHandler implements Handler {
    static private final XLogger logger = XLoggerFactory.getXLogger(FrameworkListHandler.class);
    public static final List<String> frameworks = new ArrayList<>();
    static {
        frameworks.add("light-java-rest");
        frameworks.add("light-java-graphql");
    }
    @Override
    public ByteBuffer handle(Object input)  {
        logger.entry(input);
        String result = "";
        try {
            result = Config.getInstance().getMapper().writeValueAsString(frameworks);
        } catch (JsonProcessingException e) {
            // return empty back in this case.
        }
        return Utils.toByteBuffer(result);
    }

}
