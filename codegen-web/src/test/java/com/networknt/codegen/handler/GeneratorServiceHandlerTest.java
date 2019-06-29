package com.networknt.codegen.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.client.Http2Client;
import com.networknt.exception.ApiException;
import com.networknt.exception.ClientException;
import io.undertow.client.ClientConnection;
import io.undertow.client.ClientRequest;
import io.undertow.client.ClientResponse;
import io.undertow.util.Headers;
import io.undertow.util.Methods;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.IoUtils;
import org.xnio.OptionMap;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by steve on 25/04/17.
 */
public class GeneratorServiceHandlerTest {
    private static String auth = "Bearer eyJraWQiOiIxMDAiLCJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJ1cm46Y29tOm5ldHdvcmtudDpvYXV0aDI6djEiLCJhdWQiOiJ1cm46Y29tLm5ldHdvcmtudCIsImV4cCI6MTgwOTAxMTkyMCwianRpIjoiR2NpMGJQZXoxb0hxT1VzYUZ6WmRkdyIsImlhdCI6MTQ5MzY1MTkyMCwibmJmIjoxNDkzNjUxODAwLCJ2ZXJzaW9uIjoiMS4wIiwidXNlcl9pZCI6InN0ZXZlIiwidXNlcl90eXBlIjoiRU1QTE9ZRUUiLCJjbGllbnRfaWQiOiJmN2Q0MjM0OC1jNjQ3LTRlZmItYTUyZC00YzU3ODc0MjFlNzIiLCJzY29wZSI6WyJjb2RlZ2VuLnIiLCJjb2RlZ2VuLnciLCJzZXJ2ZXIuaW5mby5yIl19.MMIjxGlQknwtlizh80wX1oB75N8wfhqMttP7i3mpKwBa-zUKZcjgtE4rmc39qYXPti9ge3uGHWCQdMOlimf4Psoah-qtsQmZhuCOwejN_OhwvlIbLxCYYP9WaQM_zyuDv6luFO5ETsZQ0-QkxQHkBq1Y3D0VpHNuNlN3ulK5sK678XKw_2VNP6JM85hh1QW8pWYappkAledvyfJC3w5sUWdi3kP_rTGiumXYA0ZoY-hdp9erlin0ClEZ7qPmHSHW_TFjuRr_6rDwE1Xg-U6wNk1hMMFF4161nfdPIhDhCsjG3J4hM0y0ZluI3BnQaGKwtqyDNIJGiiTkK1ckX1qaIw";

    @ClassRule
    public static TestServer server = TestServer.getInstance();
    static final boolean enableHttp2 = server.getServerConfig().isEnableHttp2();
    static final boolean enableHttps = server.getServerConfig().isEnableHttps();
    static final int httpPort = server.getServerConfig().getHttpPort();
    static final int httpsPort = server.getServerConfig().getHttpsPort();
    static final String url = enableHttp2 || enableHttps ? "https://localhost:" + httpsPort : "http://localhost:" + httpPort;

    // please note that the modelText and configText are strings not json objects
    static String single = "{\"host\":\"lightapi.net\",\"service\":\"codegen\",\"action\":\"single\",\"version\":\"0.0.1\",\"data\":{\"framework\":\"openapi\",\"modelType\":\"C\",\"modelText\":\"{\\\"openapi\\\":\\\"3.0.0\\\",\\\"info\\\":{\\\"version\\\":\\\"1.0.0\\\",\\\"title\\\":\\\"Swagger Petstore\\\",\\\"license\\\":{\\\"name\\\":\\\"MIT\\\"}},\\\"servers\\\":[{\\\"url\\\":\\\"http://petstore.swagger.io/v1\\\"}],\\\"paths\\\":{\\\"/pets\\\":{\\\"get\\\":{\\\"summary\\\":\\\"List all pets\\\",\\\"operationId\\\":\\\"listPets\\\",\\\"tags\\\":[\\\"pets\\\"],\\\"parameters\\\":[{\\\"name\\\":\\\"limit\\\",\\\"in\\\":\\\"query\\\",\\\"description\\\":\\\"How many items to return at one time (max 100)\\\",\\\"required\\\":false,\\\"schema\\\":{\\\"type\\\":\\\"integer\\\",\\\"format\\\":\\\"int32\\\"}}],\\\"security\\\":[{\\\"petstore_auth\\\":[\\\"read:pets\\\"]}],\\\"responses\\\":{\\\"200\\\":{\\\"description\\\":\\\"An paged array of pets\\\",\\\"headers\\\":{\\\"x-next\\\":{\\\"description\\\":\\\"A link to the next page of responses\\\",\\\"schema\\\":{\\\"type\\\":\\\"string\\\"}}},\\\"content\\\":{\\\"application/json\\\":{\\\"schema\\\":{\\\"type\\\":\\\"array\\\",\\\"items\\\":{\\\"$ref\\\":\\\"#/components/schemas/Pet\\\"}},\\\"example\\\":[{\\\"id\\\":1,\\\"name\\\":\\\"catten\\\",\\\"tag\\\":\\\"cat\\\"},{\\\"id\\\":2,\\\"name\\\":\\\"doggy\\\",\\\"tag\\\":\\\"dog\\\"}]}}},\\\"default\\\":{\\\"description\\\":\\\"unexpected error\\\",\\\"content\\\":{\\\"application/json\\\":{\\\"schema\\\":{\\\"$ref\\\":\\\"#/components/schemas/Error\\\"}}}}}},\\\"post\\\":{\\\"summary\\\":\\\"Create a pet\\\",\\\"operationId\\\":\\\"createPets\\\",\\\"requestBody\\\":{\\\"description\\\":\\\"Pet to add to the store\\\",\\\"required\\\":true,\\\"content\\\":{\\\"application/json\\\":{\\\"schema\\\":{\\\"$ref\\\":\\\"#/components/schemas/Pet\\\"}}}},\\\"tags\\\":[\\\"pets\\\"],\\\"security\\\":[{\\\"petstore_auth\\\":[\\\"read:pets\\\",\\\"write:pets\\\"]}],\\\"responses\\\":{\\\"201\\\":{\\\"description\\\":\\\"Null response\\\"},\\\"default\\\":{\\\"description\\\":\\\"unexpected error\\\",\\\"content\\\":{\\\"application/json\\\":{\\\"schema\\\":{\\\"$ref\\\":\\\"#/components/schemas/Error\\\"}}}}}}},\\\"/pets/{petId}\\\":{\\\"get\\\":{\\\"summary\\\":\\\"Info for a specific pet\\\",\\\"operationId\\\":\\\"showPetById\\\",\\\"tags\\\":[\\\"pets\\\"],\\\"parameters\\\":[{\\\"name\\\":\\\"petId\\\",\\\"in\\\":\\\"path\\\",\\\"required\\\":true,\\\"description\\\":\\\"The id of the pet to retrieve\\\",\\\"schema\\\":{\\\"type\\\":\\\"string\\\"}}],\\\"security\\\":[{\\\"petstore_auth\\\":[\\\"read:pets\\\"]}],\\\"responses\\\":{\\\"200\\\":{\\\"description\\\":\\\"Expected response to a valid request\\\",\\\"content\\\":{\\\"application/json\\\":{\\\"schema\\\":{\\\"$ref\\\":\\\"#/components/schemas/Pet\\\"},\\\"example\\\":{\\\"id\\\":1,\\\"name\\\":\\\"Jessica Right\\\",\\\"tag\\\":\\\"pet\\\"}}}},\\\"default\\\":{\\\"description\\\":\\\"unexpected error\\\",\\\"content\\\":{\\\"application/json\\\":{\\\"schema\\\":{\\\"$ref\\\":\\\"#/components/schemas/Error\\\"}}}}}},\\\"delete\\\":{\\\"summary\\\":\\\"Delete a specific pet\\\",\\\"operationId\\\":\\\"deletePetById\\\",\\\"tags\\\":[\\\"pets\\\"],\\\"parameters\\\":[{\\\"name\\\":\\\"petId\\\",\\\"in\\\":\\\"path\\\",\\\"required\\\":true,\\\"description\\\":\\\"The id of the pet to delete\\\",\\\"schema\\\":{\\\"type\\\":\\\"string\\\"}},{\\\"name\\\":\\\"key\\\",\\\"in\\\":\\\"header\\\",\\\"required\\\":true,\\\"description\\\":\\\"The key header\\\",\\\"schema\\\":{\\\"type\\\":\\\"string\\\"}}],\\\"security\\\":[{\\\"petstore_auth\\\":[\\\"write:pets\\\"]}],\\\"responses\\\":{\\\"200\\\":{\\\"description\\\":\\\"Expected response to a valid request\\\",\\\"content\\\":{\\\"application/json\\\":{\\\"schema\\\":{\\\"$ref\\\":\\\"#/components/schemas/Pet\\\"},\\\"examples\\\":{\\\"response\\\":{\\\"value\\\":{\\\"id\\\":1,\\\"name\\\":\\\"Jessica Right\\\",\\\"tag\\\":\\\"pet\\\"}}}}}},\\\"default\\\":{\\\"description\\\":\\\"unexpected error\\\",\\\"content\\\":{\\\"application/json\\\":{\\\"schema\\\":{\\\"$ref\\\":\\\"#/components/schemas/Error\\\"}}}}}}}},\\\"components\\\":{\\\"securitySchemes\\\":{\\\"petstore_auth\\\":{\\\"type\\\":\\\"oauth2\\\",\\\"description\\\":\\\"This API uses OAuth 2 with the client credential grant flow.\\\",\\\"flows\\\":{\\\"clientCredentials\\\":{\\\"tokenUrl\\\":\\\"https://localhost:6882/token\\\",\\\"scopes\\\":{\\\"write:pets\\\":\\\"modify pets in your account\\\",\\\"read:pets\\\":\\\"read your pets\\\"}}}}},\\\"schemas\\\":{\\\"Pet\\\":{\\\"type\\\":\\\"object\\\",\\\"required\\\":[\\\"id\\\",\\\"name\\\"],\\\"properties\\\":{\\\"id\\\":{\\\"type\\\":\\\"integer\\\",\\\"format\\\":\\\"int64\\\"},\\\"name\\\":{\\\"type\\\":\\\"string\\\"},\\\"tag\\\":{\\\"type\\\":\\\"string\\\"}}},\\\"Error\\\":{\\\"type\\\":\\\"object\\\",\\\"required\\\":[\\\"code\\\",\\\"message\\\"],\\\"properties\\\":{\\\"code\\\":{\\\"type\\\":\\\"integer\\\",\\\"format\\\":\\\"int32\\\"},\\\"message\\\":{\\\"type\\\":\\\"string\\\"}}}}}}\",\"configType\":\"C\",\"configText\":\"{\\\"name\\\":\\\"petstore\\\",\\\"version\\\":\\\"3.0.1\\\",\\\"groupId\\\":\\\"com.networknt\\\",\\\"artifactId\\\":\\\"petstore\\\",\\\"rootPackage\\\":\\\"com.networknt.petstore\\\",\\\"handlerPackage\\\":\\\"com.networknt.petstore.handler\\\",\\\"modelPackage\\\":\\\"com.networknt.petstore.model\\\",\\\"overwriteHandler\\\":true,\\\"overwriteHandlerTest\\\":true,\\\"overwriteModel\\\":true,\\\"httpPort\\\":8080,\\\"enableHttp\\\":false,\\\"httpsPort\\\":8443,\\\"enableHttps\\\":true,\\\"enableHttp2\\\":true,\\\"enableRegistry\\\":false,\\\"supportDb\\\":false,\\\"supportH2ForTest\\\":false,\\\"supportClient\\\":false,\\\"dockerOrganization\\\":\\\"networknt\\\"}\"}}";
    static String multiple = "{\"host\":\"lightapi.net\",\"service\":\"codegen\",\"action\":\"multiple\",\"version\":\"0.0.1\",\"data\":{\"generators\":[{\"framework\":\"openapi\",\"modelType\":\"C\",\"modelText\":\"{\\\"openapi\\\":\\\"3.0.0\\\",\\\"info\\\":{\\\"version\\\":\\\"1.0.0\\\",\\\"title\\\":\\\"Swagger Petstore\\\",\\\"license\\\":{\\\"name\\\":\\\"MIT\\\"}},\\\"servers\\\":[{\\\"url\\\":\\\"http://petstore.swagger.io/v1\\\"}],\\\"paths\\\":{\\\"/pets\\\":{\\\"get\\\":{\\\"summary\\\":\\\"List all pets\\\",\\\"operationId\\\":\\\"listPets\\\",\\\"tags\\\":[\\\"pets\\\"],\\\"parameters\\\":[{\\\"name\\\":\\\"limit\\\",\\\"in\\\":\\\"query\\\",\\\"description\\\":\\\"How many items to return at one time (max 100)\\\",\\\"required\\\":false,\\\"schema\\\":{\\\"type\\\":\\\"integer\\\",\\\"format\\\":\\\"int32\\\"}}],\\\"security\\\":[{\\\"petstore_auth\\\":[\\\"read:pets\\\"]}],\\\"responses\\\":{\\\"200\\\":{\\\"description\\\":\\\"An paged array of pets\\\",\\\"headers\\\":{\\\"x-next\\\":{\\\"description\\\":\\\"A link to the next page of responses\\\",\\\"schema\\\":{\\\"type\\\":\\\"string\\\"}}},\\\"content\\\":{\\\"application/json\\\":{\\\"schema\\\":{\\\"type\\\":\\\"array\\\",\\\"items\\\":{\\\"$ref\\\":\\\"#/components/schemas/Pet\\\"}},\\\"example\\\":[{\\\"id\\\":1,\\\"name\\\":\\\"catten\\\",\\\"tag\\\":\\\"cat\\\"},{\\\"id\\\":2,\\\"name\\\":\\\"doggy\\\",\\\"tag\\\":\\\"dog\\\"}]}}},\\\"default\\\":{\\\"description\\\":\\\"unexpected error\\\",\\\"content\\\":{\\\"application/json\\\":{\\\"schema\\\":{\\\"$ref\\\":\\\"#/components/schemas/Error\\\"}}}}}},\\\"post\\\":{\\\"summary\\\":\\\"Create a pet\\\",\\\"operationId\\\":\\\"createPets\\\",\\\"requestBody\\\":{\\\"description\\\":\\\"Pet to add to the store\\\",\\\"required\\\":true,\\\"content\\\":{\\\"application/json\\\":{\\\"schema\\\":{\\\"$ref\\\":\\\"#/components/schemas/Pet\\\"}}}},\\\"tags\\\":[\\\"pets\\\"],\\\"security\\\":[{\\\"petstore_auth\\\":[\\\"read:pets\\\",\\\"write:pets\\\"]}],\\\"responses\\\":{\\\"201\\\":{\\\"description\\\":\\\"Null response\\\"},\\\"default\\\":{\\\"description\\\":\\\"unexpected error\\\",\\\"content\\\":{\\\"application/json\\\":{\\\"schema\\\":{\\\"$ref\\\":\\\"#/components/schemas/Error\\\"}}}}}}},\\\"/pets/{petId}\\\":{\\\"get\\\":{\\\"summary\\\":\\\"Info for a specific pet\\\",\\\"operationId\\\":\\\"showPetById\\\",\\\"tags\\\":[\\\"pets\\\"],\\\"parameters\\\":[{\\\"name\\\":\\\"petId\\\",\\\"in\\\":\\\"path\\\",\\\"required\\\":true,\\\"description\\\":\\\"The id of the pet to retrieve\\\",\\\"schema\\\":{\\\"type\\\":\\\"string\\\"}}],\\\"security\\\":[{\\\"petstore_auth\\\":[\\\"read:pets\\\"]}],\\\"responses\\\":{\\\"200\\\":{\\\"description\\\":\\\"Expected response to a valid request\\\",\\\"content\\\":{\\\"application/json\\\":{\\\"schema\\\":{\\\"$ref\\\":\\\"#/components/schemas/Pet\\\"},\\\"example\\\":{\\\"id\\\":1,\\\"name\\\":\\\"Jessica Right\\\",\\\"tag\\\":\\\"pet\\\"}}}},\\\"default\\\":{\\\"description\\\":\\\"unexpected error\\\",\\\"content\\\":{\\\"application/json\\\":{\\\"schema\\\":{\\\"$ref\\\":\\\"#/components/schemas/Error\\\"}}}}}},\\\"delete\\\":{\\\"summary\\\":\\\"Delete a specific pet\\\",\\\"operationId\\\":\\\"deletePetById\\\",\\\"tags\\\":[\\\"pets\\\"],\\\"parameters\\\":[{\\\"name\\\":\\\"petId\\\",\\\"in\\\":\\\"path\\\",\\\"required\\\":true,\\\"description\\\":\\\"The id of the pet to delete\\\",\\\"schema\\\":{\\\"type\\\":\\\"string\\\"}},{\\\"name\\\":\\\"key\\\",\\\"in\\\":\\\"header\\\",\\\"required\\\":true,\\\"description\\\":\\\"The key header\\\",\\\"schema\\\":{\\\"type\\\":\\\"string\\\"}}],\\\"security\\\":[{\\\"petstore_auth\\\":[\\\"write:pets\\\"]}],\\\"responses\\\":{\\\"200\\\":{\\\"description\\\":\\\"Expected response to a valid request\\\",\\\"content\\\":{\\\"application/json\\\":{\\\"schema\\\":{\\\"$ref\\\":\\\"#/components/schemas/Pet\\\"},\\\"examples\\\":{\\\"response\\\":{\\\"value\\\":{\\\"id\\\":1,\\\"name\\\":\\\"Jessica Right\\\",\\\"tag\\\":\\\"pet\\\"}}}}}},\\\"default\\\":{\\\"description\\\":\\\"unexpected error\\\",\\\"content\\\":{\\\"application/json\\\":{\\\"schema\\\":{\\\"$ref\\\":\\\"#/components/schemas/Error\\\"}}}}}}}},\\\"components\\\":{\\\"securitySchemes\\\":{\\\"petstore_auth\\\":{\\\"type\\\":\\\"oauth2\\\",\\\"description\\\":\\\"This API uses OAuth 2 with the client credential grant flow.\\\",\\\"flows\\\":{\\\"clientCredentials\\\":{\\\"tokenUrl\\\":\\\"https://localhost:6882/token\\\",\\\"scopes\\\":{\\\"write:pets\\\":\\\"modify pets in your account\\\",\\\"read:pets\\\":\\\"read your pets\\\"}}}}},\\\"schemas\\\":{\\\"Pet\\\":{\\\"type\\\":\\\"object\\\",\\\"required\\\":[\\\"id\\\",\\\"name\\\"],\\\"properties\\\":{\\\"id\\\":{\\\"type\\\":\\\"integer\\\",\\\"format\\\":\\\"int64\\\"},\\\"name\\\":{\\\"type\\\":\\\"string\\\"},\\\"tag\\\":{\\\"type\\\":\\\"string\\\"}}},\\\"Error\\\":{\\\"type\\\":\\\"object\\\",\\\"required\\\":[\\\"code\\\",\\\"message\\\"],\\\"properties\\\":{\\\"code\\\":{\\\"type\\\":\\\"integer\\\",\\\"format\\\":\\\"int32\\\"},\\\"message\\\":{\\\"type\\\":\\\"string\\\"}}}}}}\",\"configType\":\"C\",\"configText\":\"{\\\"name\\\":\\\"petstore\\\",\\\"version\\\":\\\"3.0.1\\\",\\\"groupId\\\":\\\"com.networknt\\\",\\\"artifactId\\\":\\\"petstore\\\",\\\"rootPackage\\\":\\\"com.networknt.petstore\\\",\\\"handlerPackage\\\":\\\"com.networknt.petstore.handler\\\",\\\"modelPackage\\\":\\\"com.networknt.petstore.model\\\",\\\"overwriteHandler\\\":true,\\\"overwriteHandlerTest\\\":true,\\\"overwriteModel\\\":true,\\\"httpPort\\\":8080,\\\"enableHttp\\\":false,\\\"httpsPort\\\":8443,\\\"enableHttps\\\":true,\\\"enableHttp2\\\":true,\\\"enableRegistry\\\":false,\\\"supportDb\\\":false,\\\"supportH2ForTest\\\":false,\\\"supportClient\\\":false,\\\"dockerOrganization\\\":\\\"networknt\\\"}\"}]}}";
    // keep the modelText and configText here for constructing the single and multiple strings above.
    static String modelText = "{\"openapi\":\"3.0.0\",\"info\":{\"version\":\"1.0.0\",\"title\":\"Swagger Petstore\",\"license\":{\"name\":\"MIT\"}},\"servers\":[{\"url\":\"http://petstore.swagger.io/v1\"}],\"paths\":{\"/pets\":{\"get\":{\"summary\":\"List all pets\",\"operationId\":\"listPets\",\"tags\":[\"pets\"],\"parameters\":[{\"name\":\"limit\",\"in\":\"query\",\"description\":\"How many items to return at one time (max 100)\",\"required\":false,\"schema\":{\"type\":\"integer\",\"format\":\"int32\"}}],\"security\":[{\"petstore_auth\":[\"read:pets\"]}],\"responses\":{\"200\":{\"description\":\"An paged array of pets\",\"headers\":{\"x-next\":{\"description\":\"A link to the next page of responses\",\"schema\":{\"type\":\"string\"}}},\"content\":{\"application/json\":{\"schema\":{\"type\":\"array\",\"items\":{\"$ref\":\"#/components/schemas/Pet\"}},\"example\":[{\"id\":1,\"name\":\"catten\",\"tag\":\"cat\"},{\"id\":2,\"name\":\"doggy\",\"tag\":\"dog\"}]}}},\"default\":{\"description\":\"unexpected error\",\"content\":{\"application/json\":{\"schema\":{\"$ref\":\"#/components/schemas/Error\"}}}}}},\"post\":{\"summary\":\"Create a pet\",\"operationId\":\"createPets\",\"requestBody\":{\"description\":\"Pet to add to the store\",\"required\":true,\"content\":{\"application/json\":{\"schema\":{\"$ref\":\"#/components/schemas/Pet\"}}}},\"tags\":[\"pets\"],\"security\":[{\"petstore_auth\":[\"read:pets\",\"write:pets\"]}],\"responses\":{\"201\":{\"description\":\"Null response\"},\"default\":{\"description\":\"unexpected error\",\"content\":{\"application/json\":{\"schema\":{\"$ref\":\"#/components/schemas/Error\"}}}}}}},\"/pets/{petId}\":{\"get\":{\"summary\":\"Info for a specific pet\",\"operationId\":\"showPetById\",\"tags\":[\"pets\"],\"parameters\":[{\"name\":\"petId\",\"in\":\"path\",\"required\":true,\"description\":\"The id of the pet to retrieve\",\"schema\":{\"type\":\"string\"}}],\"security\":[{\"petstore_auth\":[\"read:pets\"]}],\"responses\":{\"200\":{\"description\":\"Expected response to a valid request\",\"content\":{\"application/json\":{\"schema\":{\"$ref\":\"#/components/schemas/Pet\"},\"example\":{\"id\":1,\"name\":\"Jessica Right\",\"tag\":\"pet\"}}}},\"default\":{\"description\":\"unexpected error\",\"content\":{\"application/json\":{\"schema\":{\"$ref\":\"#/components/schemas/Error\"}}}}}},\"delete\":{\"summary\":\"Delete a specific pet\",\"operationId\":\"deletePetById\",\"tags\":[\"pets\"],\"parameters\":[{\"name\":\"petId\",\"in\":\"path\",\"required\":true,\"description\":\"The id of the pet to delete\",\"schema\":{\"type\":\"string\"}},{\"name\":\"key\",\"in\":\"header\",\"required\":true,\"description\":\"The key header\",\"schema\":{\"type\":\"string\"}}],\"security\":[{\"petstore_auth\":[\"write:pets\"]}],\"responses\":{\"200\":{\"description\":\"Expected response to a valid request\",\"content\":{\"application/json\":{\"schema\":{\"$ref\":\"#/components/schemas/Pet\"},\"examples\":{\"response\":{\"value\":{\"id\":1,\"name\":\"Jessica Right\",\"tag\":\"pet\"}}}}}},\"default\":{\"description\":\"unexpected error\",\"content\":{\"application/json\":{\"schema\":{\"$ref\":\"#/components/schemas/Error\"}}}}}}}},\"components\":{\"securitySchemes\":{\"petstore_auth\":{\"type\":\"oauth2\",\"description\":\"This API uses OAuth 2 with the client credential grant flow.\",\"flows\":{\"clientCredentials\":{\"tokenUrl\":\"https://localhost:6882/token\",\"scopes\":{\"write:pets\":\"modify pets in your account\",\"read:pets\":\"read your pets\"}}}}},\"schemas\":{\"Pet\":{\"type\":\"object\",\"required\":[\"id\",\"name\"],\"properties\":{\"id\":{\"type\":\"integer\",\"format\":\"int64\"},\"name\":{\"type\":\"string\"},\"tag\":{\"type\":\"string\"}}},\"Error\":{\"type\":\"object\",\"required\":[\"code\",\"message\"],\"properties\":{\"code\":{\"type\":\"integer\",\"format\":\"int32\"},\"message\":{\"type\":\"string\"}}}}}}";
    static String configText = "{\"name\":\"petstore\",\"version\":\"3.0.1\",\"groupId\":\"com.networknt\",\"artifactId\":\"petstore\",\"rootPackage\":\"com.networknt.petstore\",\"handlerPackage\":\"com.networknt.petstore.handler\",\"modelPackage\":\"com.networknt.petstore.model\",\"overwriteHandler\":true,\"overwriteHandlerTest\":true,\"overwriteModel\":true,\"httpPort\":8080,\"enableHttp\":false,\"httpsPort\":8443,\"enableHttps\":true,\"enableHttp2\":true,\"enableRegistry\":false,\"supportDb\":false,\"supportH2ForTest\":false,\"supportClient\":false,\"dockerOrganization\":\"networknt\"}";

    static String singleWithModelUrl = "{\"host\":\"lightapi.net\",\"service\":\"codegen\",\"action\":\"single\",\"version\":\"0.0.1\",\"data\":{\"framework\":\"openapi\",\"modelType\":\"U\",\"modelUrl\":\"https://raw.githubusercontent.com/networknt/model-config/master/rest/openapi/petstore/1.0.0/openapi.json\",\"configType\":\"C\",\"configText\":\"{\\\"name\\\":\\\"petstore\\\",\\\"version\\\":\\\"3.0.1\\\",\\\"groupId\\\":\\\"com.networknt\\\",\\\"artifactId\\\":\\\"petstore\\\",\\\"rootPackage\\\":\\\"com.networknt.petstore\\\",\\\"handlerPackage\\\":\\\"com.networknt.petstore.handler\\\",\\\"modelPackage\\\":\\\"com.networknt.petstore.model\\\",\\\"overwriteHandler\\\":true,\\\"overwriteHandlerTest\\\":true,\\\"overwriteModel\\\":true,\\\"httpPort\\\":8080,\\\"enableHttp\\\":false,\\\"httpsPort\\\":8443,\\\"enableHttps\\\":true,\\\"enableHttp2\\\":true,\\\"enableRegistry\\\":false,\\\"supportDb\\\":false,\\\"supportH2ForTest\\\":false,\\\"supportClient\\\":false,\\\"dockerOrganization\\\":\\\"networknt\\\"}\"}}";
    static String multipleWithAllUrl = "{\"host\":\"lightapi.net\",\"service\":\"codegen\",\"action\":\"multiple\",\"version\":\"0.0.1\",\"data\":{\"generators\":[{\"framework\":\"openapi\",\"modelType\":\"U\",\"modelUrl\":\"https://raw.githubusercontent.com/networknt/model-config/master/rest/openapi/petstore/1.0.0/openapi.json\",\"configType\":\"U\",\"configUrl\":\"https://raw.githubusercontent.com/networknt/model-config/master/rest/openapi/petstore/1.0.0/config.json\"}]}}";

    static final Logger logger = LoggerFactory.getLogger(GeneratorServiceHandlerTest.class);

    /**
     * The original generate action is removed and single and multiple are added. This is to ensure that the old
     * action will return an error message.
     * @throws ClientException
     */
    @Test
    public void testMissingGeneratorItem() throws ClientException {
        Map<String, String> params = new HashMap<>();
        params.put("host", "lightapi.net");
        params.put("service", "codegen");
        params.put("action", "generate");
        params.put("version", "0.0.1");


        final AtomicReference<ClientResponse> reference = new AtomicReference<>();
        final Http2Client client = Http2Client.getInstance();
        final CountDownLatch latch = new CountDownLatch(1);
        final ClientConnection connection;
        try {
            connection = client.connect(new URI(url), Http2Client.WORKER, Http2Client.SSL, Http2Client.BUFFER_POOL, OptionMap.EMPTY).get();
        } catch (Exception e) {
            throw new ClientException(e);
        }
        try {
            connection.getIoThread().execute(() -> {
                final ClientRequest request = new ClientRequest().setMethod(Methods.POST).setPath("/codegen");

                request.getRequestHeaders().put(Headers.HOST, "localhost");
                request.getRequestHeaders().put(Headers.AUTHORIZATION, auth);
                request.getRequestHeaders().put(Headers.CONTENT_TYPE, "application/json");
                request.getRequestHeaders().put(Headers.TRANSFER_ENCODING, "chunked");
                try {
                    connection.sendRequest(request, client.createClientCallback(reference, latch, new ObjectMapper().writeValueAsString(params)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            latch.await(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("IOException: ", e);
            throw new ClientException(e);
        } finally {
            IoUtils.safeClose(connection);
        }
        int statusCode = reference.get().getResponseCode();
        String body = reference.get().getAttachment(Http2Client.RESPONSE_BODY);
        Assert.assertEquals(400, statusCode);
        if(statusCode == 400) {
            Assert.assertTrue(body.contains("ERR11200"));
        }
    }

    /**
     * This is the multiple generators endpoint with an invalid framework.
     * @throws ClientException
     * @throws IOException
     */
    @Test
    public void testInvalidFrameworkMultiple() throws ClientException, IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("host", "lightapi.net");
        params.put("service", "codegen");
        params.put("action", "multiple");
        params.put("version", "0.0.1");
        params.put("data", new ObjectMapper().readValue("{\"generators\":[{\"framework\":\"invalid\",\"modelType\":\"C\",\"modelText\":{\"key\":\"value\"},\"configType\":\"C\",\"configText\":{\"key\":\"value\"}}]}", Map.class));

        final AtomicReference<ClientResponse> reference = new AtomicReference<>();
        final Http2Client client = Http2Client.getInstance();
        final CountDownLatch latch = new CountDownLatch(1);
        final ClientConnection connection;
        try {
            connection = client.connect(new URI(url), Http2Client.WORKER, Http2Client.SSL, Http2Client.BUFFER_POOL, OptionMap.EMPTY).get();
        } catch (Exception e) {
            throw new ClientException(e);
        }
        try {
            connection.getIoThread().execute(() -> {
                final ClientRequest request = new ClientRequest().setMethod(Methods.POST).setPath("/codegen");
                request.getRequestHeaders().put(Headers.HOST, "localhost");
                request.getRequestHeaders().put(Headers.AUTHORIZATION, auth);
                request.getRequestHeaders().put(Headers.CONTENT_TYPE, "application/json");
                request.getRequestHeaders().put(Headers.TRANSFER_ENCODING, "chunked");
                try {
                    connection.sendRequest(request, client.createClientCallback(reference, latch, new ObjectMapper().writeValueAsString(params)));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            });

            latch.await(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("IOException: ", e);
            throw new ClientException(e);
        } finally {
            IoUtils.safeClose(connection);
        }
        int statusCode = reference.get().getResponseCode();
        String body = reference.get().getAttachment(Http2Client.RESPONSE_BODY);
        Assert.assertEquals(400, statusCode);
        if(statusCode == 400) {
            Assert.assertTrue(body.contains("ERR11100"));
        }
    }

    /**
     * This is the multiple generators endpoint with an invalid framework.
     * @throws ClientException
     * @throws IOException
     */
    @Test
    public void testInvalidFrameworkSingle() throws ClientException, IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("host", "lightapi.net");
        params.put("service", "codegen");
        params.put("action", "single");
        params.put("version", "0.0.1");
        params.put("data", new ObjectMapper().readValue("{\"framework\":\"invalid\",\"modelType\":\"C\",\"modelText\":{\"key\":\"value\"},\"configType\":\"C\",\"configText\":{\"key\":\"value\"}}", Map.class));

        final AtomicReference<ClientResponse> reference = new AtomicReference<>();
        final Http2Client client = Http2Client.getInstance();
        final CountDownLatch latch = new CountDownLatch(1);
        final ClientConnection connection;
        try {
            connection = client.connect(new URI(url), Http2Client.WORKER, Http2Client.SSL, Http2Client.BUFFER_POOL, OptionMap.EMPTY).get();
        } catch (Exception e) {
            throw new ClientException(e);
        }
        try {
            connection.getIoThread().execute(() -> {
                final ClientRequest request = new ClientRequest().setMethod(Methods.POST).setPath("/codegen");
                request.getRequestHeaders().put(Headers.HOST, "localhost");
                request.getRequestHeaders().put(Headers.AUTHORIZATION, auth);
                request.getRequestHeaders().put(Headers.CONTENT_TYPE, "application/json");
                request.getRequestHeaders().put(Headers.TRANSFER_ENCODING, "chunked");
                try {
                    connection.sendRequest(request, client.createClientCallback(reference, latch, new ObjectMapper().writeValueAsString(params)));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            });

            latch.await(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("IOException: ", e);
            throw new ClientException(e);
        } finally {
            IoUtils.safeClose(connection);
        }
        int statusCode = reference.get().getResponseCode();
        String body = reference.get().getAttachment(Http2Client.RESPONSE_BODY);
        Assert.assertEquals(400, statusCode);
        if(statusCode == 400) {
            Assert.assertTrue(body.contains("ERR11100"));
        }
    }

    /**
     * Test multiple generators with both model and config as strings.
     * @throws ClientException
     * @throws ApiException
     * @throws IOException
     */
    @Test
    public void testGeneratorMultipleText() throws ClientException, ApiException, IOException {

        final AtomicReference<ClientResponse> reference = new AtomicReference<>();
        final Http2Client client = Http2Client.getInstance();
        final CountDownLatch latch = new CountDownLatch(1);
        final ClientConnection connection;
        try {
            connection = client.connect(new URI(url), Http2Client.WORKER, Http2Client.SSL, Http2Client.BUFFER_POOL, OptionMap.EMPTY).get();
        } catch (Exception e) {
            throw new ClientException(e);
        }
        try {
            connection.getIoThread().execute(new Runnable() {
                @Override
                public void run() {
                    final ClientRequest request = new ClientRequest().setMethod(Methods.POST).setPath("/codegen");
                    request.getRequestHeaders().put(Headers.HOST, "localhost");
                    request.getRequestHeaders().put(Headers.AUTHORIZATION, auth);
                    request.getRequestHeaders().put(Headers.CONTENT_TYPE, "application/json");
                    request.getRequestHeaders().put(Headers.TRANSFER_ENCODING, "chunked");
                    connection.sendRequest(request, client.createClientCallback(reference, latch, multiple));
                }
            });

            latch.await(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("IOException: ", e);
            throw new ClientException(e);
        } finally {
            IoUtils.safeClose(connection);
        }
        int statusCode = reference.get().getResponseCode();
        String body = reference.get().getAttachment(Http2Client.RESPONSE_BODY);
        Assert.assertEquals(200, statusCode);
        Assert.assertNotNull(body);
    }

    /**
     * Test with a single generator with both model and config as strings.
     * @throws ClientException
     * @throws ApiException
     * @throws IOException
     */
    @Test
    public void testGeneratorSingleText() throws ClientException, ApiException, IOException {

        final AtomicReference<ClientResponse> reference = new AtomicReference<>();
        final Http2Client client = Http2Client.getInstance();
        final CountDownLatch latch = new CountDownLatch(1);
        final ClientConnection connection;
        try {
            connection = client.connect(new URI(url), Http2Client.WORKER, Http2Client.SSL, Http2Client.BUFFER_POOL, OptionMap.EMPTY).get();
        } catch (Exception e) {
            throw new ClientException(e);
        }
        try {
            connection.getIoThread().execute(new Runnable() {
                @Override
                public void run() {
                    final ClientRequest request = new ClientRequest().setMethod(Methods.POST).setPath("/codegen");
                    request.getRequestHeaders().put(Headers.HOST, "localhost");
                    request.getRequestHeaders().put(Headers.AUTHORIZATION, auth);
                    request.getRequestHeaders().put(Headers.CONTENT_TYPE, "application/json");
                    request.getRequestHeaders().put(Headers.TRANSFER_ENCODING, "chunked");
                    connection.sendRequest(request, client.createClientCallback(reference, latch, single));
                }
            });

            latch.await(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("IOException: ", e);
            throw new ClientException(e);
        } finally {
            IoUtils.safeClose(connection);
        }
        int statusCode = reference.get().getResponseCode();
        String body = reference.get().getAttachment(Http2Client.RESPONSE_BODY);
        Assert.assertEquals(200, statusCode);
        Assert.assertNotNull(body);
    }

    /**
     * Test multiple generators with model from github.com and config with the content as a string.
     * @throws ClientException
     * @throws ApiException
     * @throws IOException
     */
    @Test
    public void testGeneratorMultipleAllUrl() throws ClientException, ApiException, IOException {

        final AtomicReference<ClientResponse> reference = new AtomicReference<>();
        final Http2Client client = Http2Client.getInstance();
        final CountDownLatch latch = new CountDownLatch(1);
        final ClientConnection connection;
        try {
            connection = client.connect(new URI(url), Http2Client.WORKER, Http2Client.SSL, Http2Client.BUFFER_POOL, OptionMap.EMPTY).get();
        } catch (Exception e) {
            throw new ClientException(e);
        }
        try {
            connection.getIoThread().execute(new Runnable() {
                @Override
                public void run() {
                    final ClientRequest request = new ClientRequest().setMethod(Methods.POST).setPath("/codegen");
                    request.getRequestHeaders().put(Headers.HOST, "localhost");
                    request.getRequestHeaders().put(Headers.AUTHORIZATION, auth);
                    request.getRequestHeaders().put(Headers.CONTENT_TYPE, "application/json");
                    request.getRequestHeaders().put(Headers.TRANSFER_ENCODING, "chunked");
                    connection.sendRequest(request, client.createClientCallback(reference, latch, multipleWithAllUrl));
                }
            });

            latch.await(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("IOException: ", e);
            throw new ClientException(e);
        } finally {
            IoUtils.safeClose(connection);
        }
        int statusCode = reference.get().getResponseCode();
        String body = reference.get().getAttachment(Http2Client.RESPONSE_BODY);
        Assert.assertEquals(200, statusCode);
        Assert.assertNotNull(body);
    }

    /**
     * Test with single generator with both model and config from github.com
     * @throws ClientException
     * @throws ApiException
     * @throws IOException
     */
    @Test
    public void testGeneratorSingleModelUrl() throws ClientException, ApiException, IOException {

        final AtomicReference<ClientResponse> reference = new AtomicReference<>();
        final Http2Client client = Http2Client.getInstance();
        final CountDownLatch latch = new CountDownLatch(1);
        final ClientConnection connection;
        try {
            connection = client.connect(new URI(url), Http2Client.WORKER, Http2Client.SSL, Http2Client.BUFFER_POOL, OptionMap.EMPTY).get();
        } catch (Exception e) {
            throw new ClientException(e);
        }
        try {
            connection.getIoThread().execute(new Runnable() {
                @Override
                public void run() {
                    final ClientRequest request = new ClientRequest().setMethod(Methods.POST).setPath("/codegen");
                    request.getRequestHeaders().put(Headers.HOST, "localhost");
                    request.getRequestHeaders().put(Headers.AUTHORIZATION, auth);
                    request.getRequestHeaders().put(Headers.CONTENT_TYPE, "application/json");
                    request.getRequestHeaders().put(Headers.TRANSFER_ENCODING, "chunked");
                    connection.sendRequest(request, client.createClientCallback(reference, latch, singleWithModelUrl));
                }
            });

            latch.await(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("IOException: ", e);
            throw new ClientException(e);
        } finally {
            IoUtils.safeClose(connection);
        }
        int statusCode = reference.get().getResponseCode();
        String body = reference.get().getAttachment(Http2Client.RESPONSE_BODY);
        Assert.assertEquals(200, statusCode);
        Assert.assertNotNull(body);
    }

}
