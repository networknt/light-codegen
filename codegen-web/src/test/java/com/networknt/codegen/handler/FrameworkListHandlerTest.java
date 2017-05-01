package com.networknt.codegen.handler;

import com.networknt.client.Client;
import com.networknt.exception.ApiException;
import com.networknt.exception.ClientException;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;

/**
 * Created by steve on 26/04/17.
 */
public class FrameworkListHandlerTest {
    private static String auth = "Bearer eyJraWQiOiIxMDAiLCJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJ1cm46Y29tOm5ldHdvcmtudDpvYXV0aDI6djEiLCJhdWQiOiJ1cm46Y29tLm5ldHdvcmtudCIsImV4cCI6MTgwOTAxMTkyMCwianRpIjoiR2NpMGJQZXoxb0hxT1VzYUZ6WmRkdyIsImlhdCI6MTQ5MzY1MTkyMCwibmJmIjoxNDkzNjUxODAwLCJ2ZXJzaW9uIjoiMS4wIiwidXNlcl9pZCI6InN0ZXZlIiwidXNlcl90eXBlIjoiRU1QTE9ZRUUiLCJjbGllbnRfaWQiOiJmN2Q0MjM0OC1jNjQ3LTRlZmItYTUyZC00YzU3ODc0MjFlNzIiLCJzY29wZSI6WyJjb2RlZ2VuLnIiLCJjb2RlZ2VuLnciLCJzZXJ2ZXIuaW5mby5yIl19.MMIjxGlQknwtlizh80wX1oB75N8wfhqMttP7i3mpKwBa-zUKZcjgtE4rmc39qYXPti9ge3uGHWCQdMOlimf4Psoah-qtsQmZhuCOwejN_OhwvlIbLxCYYP9WaQM_zyuDv6luFO5ETsZQ0-QkxQHkBq1Y3D0VpHNuNlN3ulK5sK678XKw_2VNP6JM85hh1QW8pWYappkAledvyfJC3w5sUWdi3kP_rTGiumXYA0ZoY-hdp9erlin0ClEZ7qPmHSHW_TFjuRr_6rDwE1Xg-U6wNk1hMMFF4161nfdPIhDhCsjG3J4hM0y0ZluI3BnQaGKwtqyDNIJGiiTkK1ckX1qaIw";

    @ClassRule
    public static TestServer server = TestServer.getInstance();

    static final Logger logger = LoggerFactory.getLogger(GeneratorServiceHandlerTest.class);

    @Test
    public void testGenerator() throws ClientException, ApiException, UnsupportedEncodingException {
        String s = "{\"host\":\"lightapi.net\",\"service\":\"framework\",\"action\":\"list\",\"version\":\"0.0.1\"}";

        CloseableHttpClient client = Client.getInstance().getSyncClient();
        HttpPost httpPost = new HttpPost("http://localhost:8080/api/json");
        httpPost.setHeader("Content-type", "application/json");
        //httpPost.setHeader("Authorization", auth);
        httpPost.setEntity(new StringEntity(s));
        try {
            CloseableHttpResponse response = client.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            String body = IOUtils.toString(response.getEntity().getContent(), "utf8");
            Assert.assertEquals(200, statusCode);
            Assert.assertTrue(body.contains("light-java-rest"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
