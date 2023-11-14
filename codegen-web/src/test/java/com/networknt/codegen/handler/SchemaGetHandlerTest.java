package com.networknt.codegen.handler;

import com.networknt.client.Http2Client;
import com.networknt.client.simplepool.SimpleConnectionHolder;
import com.networknt.exception.ApiException;
import com.networknt.exception.ClientException;
import com.networknt.server.ServerConfig;
import io.undertow.client.ClientConnection;
import io.undertow.client.ClientRequest;
import io.undertow.client.ClientResponse;
import io.undertow.util.Headers;
import io.undertow.util.Methods;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.IoUtils;
import org.xnio.OptionMap;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by steve on 10/05/17.
 */
@ExtendWith(TestServer.class)
public class SchemaGetHandlerTest {
    private static String auth = "Bearer eyJraWQiOiIxMDAiLCJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJ1cm46Y29tOm5ldHdvcmtudDpvYXV0aDI6djEiLCJhdWQiOiJ1cm46Y29tLm5ldHdvcmtudCIsImV4cCI6MTgwOTAxMTkyMCwianRpIjoiR2NpMGJQZXoxb0hxT1VzYUZ6WmRkdyIsImlhdCI6MTQ5MzY1MTkyMCwibmJmIjoxNDkzNjUxODAwLCJ2ZXJzaW9uIjoiMS4wIiwidXNlcl9pZCI6InN0ZXZlIiwidXNlcl90eXBlIjoiRU1QTE9ZRUUiLCJjbGllbnRfaWQiOiJmN2Q0MjM0OC1jNjQ3LTRlZmItYTUyZC00YzU3ODc0MjFlNzIiLCJzY29wZSI6WyJjb2RlZ2VuLnIiLCJjb2RlZ2VuLnciLCJzZXJ2ZXIuaW5mby5yIl19.MMIjxGlQknwtlizh80wX1oB75N8wfhqMttP7i3mpKwBa-zUKZcjgtE4rmc39qYXPti9ge3uGHWCQdMOlimf4Psoah-qtsQmZhuCOwejN_OhwvlIbLxCYYP9WaQM_zyuDv6luFO5ETsZQ0-QkxQHkBq1Y3D0VpHNuNlN3ulK5sK678XKw_2VNP6JM85hh1QW8pWYappkAledvyfJC3w5sUWdi3kP_rTGiumXYA0ZoY-hdp9erlin0ClEZ7qPmHSHW_TFjuRr_6rDwE1Xg-U6wNk1hMMFF4161nfdPIhDhCsjG3J4hM0y0ZluI3BnQaGKwtqyDNIJGiiTkK1ckX1qaIw";

    public static TestServer server = TestServer.getInstance();
    static final boolean enableHttp2 = ServerConfig.getInstance().isEnableHttp2();
    static final boolean enableHttps = ServerConfig.getInstance().isEnableHttps();
    static final int httpPort = ServerConfig.getInstance().getHttpPort();
    static final int httpsPort = ServerConfig.getInstance().getHttpsPort();
    static final String url = enableHttp2 || enableHttps ? "https://localhost:" + httpsPort : "http://localhost:" + httpPort;

    static final Logger logger = LoggerFactory.getLogger(SchemaGetHandlerTest.class);
    final Http2Client client = Http2Client.getInstance();

    @Test
    public void testGetSchema() throws ClientException, ApiException, UnsupportedEncodingException {
        String s = "{\"host\":\"lightapi.net\",\"service\":\"codegen\",\"action\":\"getSchema\",\"version\":\"0.0.1\",\"data\":{\"framework\":\"openapi\"}}";

        final AtomicReference<ClientResponse> reference = new AtomicReference<>();
        final CountDownLatch latch = new CountDownLatch(1);
        SimpleConnectionHolder.ConnectionToken connectionToken = null;

        try {
            connectionToken = client.borrow(new URI(url), Http2Client.WORKER, client.getDefaultXnioSsl(), Http2Client.BUFFER_POOL, OptionMap.EMPTY);
            ClientConnection connection = (ClientConnection) connectionToken.getRawConnection();
            connection.getIoThread().execute(new Runnable() {
                @Override
                public void run() {
                    final ClientRequest request = new ClientRequest().setMethod(Methods.POST).setPath("/codegen");
                    request.getRequestHeaders().put(Headers.HOST, "localhost");
                    request.getRequestHeaders().put(Headers.AUTHORIZATION, auth);
                    request.getRequestHeaders().put(Headers.CONTENT_TYPE, "application/json");
                    request.getRequestHeaders().put(Headers.TRANSFER_ENCODING, "chunked");
                    connection.sendRequest(request, client.createClientCallback(reference, latch, s));
                }
            });

            latch.await(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("IOException: ", e);
            throw new ClientException(e);
        } finally {
            client.restore(connectionToken);
        }
        int statusCode = reference.get().getResponseCode();
        String body = reference.get().getAttachment(Http2Client.RESPONSE_BODY);
        System.out.println("body = " + body);
        assertEquals(200, statusCode);
        if(statusCode == 200) {
            assertTrue(body.contains("{"));
        }
    }

}
