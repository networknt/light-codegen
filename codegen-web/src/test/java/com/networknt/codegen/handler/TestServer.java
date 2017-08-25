package com.networknt.codegen.handler;

import com.networknt.server.Server;
import com.networknt.server.ServerConfig;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by steve on 25/04/17.
 */
public class TestServer extends ExternalResource {
    static final Logger logger = LoggerFactory.getLogger(TestServer.class);

    private static final AtomicInteger refCount = new AtomicInteger(0);
    private static Server server;

    private static final TestServer instance  = new TestServer();

    public static TestServer getInstance () {
        return instance;
    }

    public ServerConfig getServerConfig() {
        return Server.config;
    }

    private TestServer() {

    }

    @Override
    protected void before() {
        try {
            if (refCount.get() == 0) {
                Server.start();
            }
        }
        finally {
            refCount.getAndIncrement();
        }
    }

    @Override
    protected void after() {
        refCount.getAndDecrement();
        if (refCount.get() == 0) {
            Server.stop();
        }
    }
}
