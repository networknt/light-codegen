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
    @ClassRule
    public static TestServer server = TestServer.getInstance();

    static final Logger logger = LoggerFactory.getLogger(GeneratorServiceHandlerTest.class);

    @Test
    public void testGenerator() throws ClientException, ApiException, UnsupportedEncodingException {
        String s = "{\"host\":\"lightapi.net\",\"service\":\"framework\",\"action\":\"list\",\"version\":\"0.0.1\"}";

        CloseableHttpClient client = Client.getInstance().getSyncClient();
        HttpPost httpPost = new HttpPost("http://localhost:8080/api/json");
        httpPost.setHeader("Content-type", "application/json");
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
