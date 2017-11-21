package com.networknt.codegen.handler;

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

import java.io.UnsupportedEncodingException;
import java.net.URI;
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

    static final Logger logger = LoggerFactory.getLogger(GeneratorServiceHandlerTest.class);

    @Test
    public void testMissingGeneratorItem() throws ClientException, ApiException, UnsupportedEncodingException {
        String s = "{\"host\":\"lightapi.net\",\"service\":\"codegen\",\"action\":\"generate\",\"version\":\"0.0.1\",\"data\":{\"model\":{\"key\":\"value\"},\"config\":{\"key\":\"value\"},\"framework\":\"framework\"}}";

        final AtomicReference<ClientResponse> reference = new AtomicReference<>();
        final Http2Client client = Http2Client.getInstance();
        final CountDownLatch latch = new CountDownLatch(1);
        final ClientConnection connection;
        try {
            connection = client.connect(new URI(url), Http2Client.WORKER, Http2Client.SSL, Http2Client.POOL, OptionMap.EMPTY).get();
        } catch (Exception e) {
            throw new ClientException(e);
        }
        try {
            connection.getIoThread().execute(new Runnable() {
                @Override
                public void run() {
                    final ClientRequest request = new ClientRequest().setMethod(Methods.POST).setPath("/api/json");
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
            IoUtils.safeClose(connection);
        }
        int statusCode = reference.get().getResponseCode();
        String body = reference.get().getAttachment(Http2Client.RESPONSE_BODY);
        Assert.assertEquals(200, statusCode);
        if(statusCode == 200) {
            Assert.assertTrue(body.contains("ERR11101"));
        }
    }

    @Test
    public void testInvalidFramework() throws ClientException, ApiException, UnsupportedEncodingException {
        String s = "{\"host\":\"lightapi.net\",\"service\":\"codegen\",\"action\":\"generate\",\"version\":\"0.0.1\",\"data\":{\"generators\":[{\"model\":{\"key\":\"value\"},\"config\":{\"key\":\"value\"},\"framework\":\"framework\"}]}}";

        final AtomicReference<ClientResponse> reference = new AtomicReference<>();
        final Http2Client client = Http2Client.getInstance();
        final CountDownLatch latch = new CountDownLatch(1);
        final ClientConnection connection;
        try {
            connection = client.connect(new URI(url), Http2Client.WORKER, Http2Client.SSL, Http2Client.POOL, OptionMap.EMPTY).get();
        } catch (Exception e) {
            throw new ClientException(e);
        }
        try {
            connection.getIoThread().execute(new Runnable() {
                @Override
                public void run() {
                    final ClientRequest request = new ClientRequest().setMethod(Methods.POST).setPath("/api/json");
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
            IoUtils.safeClose(connection);
        }
        int statusCode = reference.get().getResponseCode();
        String body = reference.get().getAttachment(Http2Client.RESPONSE_BODY);
        Assert.assertEquals(200, statusCode);
        if(statusCode == 200) {
            Assert.assertTrue(body.contains("ERR11100"));
        }
    }


    @Test
    public void testGenerator() throws ClientException, ApiException, UnsupportedEncodingException {
        String s = "{\n" +
                "  \"host\": \"lightapi.net\",\n" +
                "  \"service\": \"codegen\",\n" +
                "  \"action\": \"generate\",\n" +
                "  \"version\": \"0.0.1\",\n" +
                "  \"data\": {\n" +
                "  \"generators\": [\n" +
                "    {\n" +
                "      \"framework\": \"swagger\",\n" +
                "      \"model\": {\n" +
                "        \"swagger\": \"2.0\",\n" +
                "        \"info\": {\n" +
                "          \"description\": \"This is a sample server Petstore server.  You can find out more about Swagger at [http://swagger.io](http://swagger.io) or on [irc.freenode.net, #swagger](http://swagger.io/irc/).  For this sample, you can use the api key `special-key` to test the authorization filters.\",\n" +
                "          \"version\": \"1.0.0\",\n" +
                "          \"title\": \"Swagger Petstore\",\n" +
                "          \"termsOfService\": \"http://swagger.io/terms/\",\n" +
                "          \"contact\": {\n" +
                "            \"email\": \"apiteam@swagger.io\"\n" +
                "          },\n" +
                "          \"license\": {\n" +
                "            \"name\": \"Apache 2.0\",\n" +
                "            \"url\": \"http://www.apache.org/licenses/LICENSE-2.0.html\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"host\": \"petstore.swagger.io\",\n" +
                "        \"basePath\": \"/v2\",\n" +
                "        \"tags\": [\n" +
                "          {\n" +
                "            \"name\": \"pet\",\n" +
                "            \"description\": \"Everything about your Pets\",\n" +
                "            \"externalDocs\": {\n" +
                "              \"description\": \"Find out more\",\n" +
                "              \"url\": \"http://swagger.io\"\n" +
                "            }\n" +
                "          },\n" +
                "          {\n" +
                "            \"name\": \"store\",\n" +
                "            \"description\": \"Access to Petstore orders\"\n" +
                "          },\n" +
                "          {\n" +
                "            \"name\": \"user\",\n" +
                "            \"description\": \"Operations about user\",\n" +
                "            \"externalDocs\": {\n" +
                "              \"description\": \"Find out more about our store\",\n" +
                "              \"url\": \"http://swagger.io\"\n" +
                "            }\n" +
                "          }\n" +
                "        ],\n" +
                "        \"schemes\": [\n" +
                "          \"http\"\n" +
                "        ],\n" +
                "        \"paths\": {\n" +
                "          \"/pet\": {\n" +
                "            \"post\": {\n" +
                "              \"tags\": [\n" +
                "                \"pet\"\n" +
                "              ],\n" +
                "              \"summary\": \"Add a new pet to the store\",\n" +
                "              \"description\": \"\",\n" +
                "              \"operationId\": \"addPet\",\n" +
                "              \"consumes\": [\n" +
                "                \"application/json\",\n" +
                "                \"application/xml\"\n" +
                "              ],\n" +
                "              \"produces\": [\n" +
                "                \"application/xml\",\n" +
                "                \"application/json\"\n" +
                "              ],\n" +
                "              \"parameters\": [\n" +
                "                {\n" +
                "                  \"in\": \"body\",\n" +
                "                  \"name\": \"body\",\n" +
                "                  \"description\": \"Pet object that needs to be added to the store\",\n" +
                "                  \"required\": true,\n" +
                "                  \"schema\": {\n" +
                "                    \"$ref\": \"#/definitions/Pet\"\n" +
                "                  }\n" +
                "                }\n" +
                "              ],\n" +
                "              \"responses\": {\n" +
                "                \"405\": {\n" +
                "                  \"description\": \"Invalid input\"\n" +
                "                }\n" +
                "              },\n" +
                "              \"security\": [\n" +
                "                {\n" +
                "                  \"petstore_auth\": [\n" +
                "                    \"write:pets\",\n" +
                "                    \"read:pets\"\n" +
                "                  ]\n" +
                "                }\n" +
                "              ]\n" +
                "            },\n" +
                "            \"put\": {\n" +
                "              \"tags\": [\n" +
                "                \"pet\"\n" +
                "              ],\n" +
                "              \"summary\": \"Update an existing pet\",\n" +
                "              \"description\": \"\",\n" +
                "              \"operationId\": \"updatePet\",\n" +
                "              \"consumes\": [\n" +
                "                \"application/json\",\n" +
                "                \"application/xml\"\n" +
                "              ],\n" +
                "              \"produces\": [\n" +
                "                \"application/xml\",\n" +
                "                \"application/json\"\n" +
                "              ],\n" +
                "              \"parameters\": [\n" +
                "                {\n" +
                "                  \"in\": \"body\",\n" +
                "                  \"name\": \"body\",\n" +
                "                  \"description\": \"Pet object that needs to be added to the store\",\n" +
                "                  \"required\": true,\n" +
                "                  \"schema\": {\n" +
                "                    \"$ref\": \"#/definitions/Pet\"\n" +
                "                  }\n" +
                "                }\n" +
                "              ],\n" +
                "              \"responses\": {\n" +
                "                \"400\": {\n" +
                "                  \"description\": \"Invalid ID supplied\"\n" +
                "                },\n" +
                "                \"404\": {\n" +
                "                  \"description\": \"Pet not found\"\n" +
                "                },\n" +
                "                \"405\": {\n" +
                "                  \"description\": \"Validation exception\"\n" +
                "                }\n" +
                "              },\n" +
                "              \"security\": [\n" +
                "                {\n" +
                "                  \"petstore_auth\": [\n" +
                "                    \"write:pets\",\n" +
                "                    \"read:pets\"\n" +
                "                  ]\n" +
                "                }\n" +
                "              ]\n" +
                "            }\n" +
                "          },\n" +
                "          \"/pet/findByStatus\": {\n" +
                "            \"get\": {\n" +
                "              \"tags\": [\n" +
                "                \"pet\"\n" +
                "              ],\n" +
                "              \"summary\": \"Finds Pets by status\",\n" +
                "              \"description\": \"Multiple status values can be provided with comma separated strings\",\n" +
                "              \"operationId\": \"findPetsByStatus\",\n" +
                "              \"produces\": [\n" +
                "                \"application/xml\",\n" +
                "                \"application/json\"\n" +
                "              ],\n" +
                "              \"parameters\": [\n" +
                "                {\n" +
                "                  \"name\": \"status\",\n" +
                "                  \"in\": \"query\",\n" +
                "                  \"description\": \"Status values that need to be considered for filter\",\n" +
                "                  \"required\": true,\n" +
                "                  \"type\": \"array\",\n" +
                "                  \"items\": {\n" +
                "                    \"type\": \"string\",\n" +
                "                    \"enum\": [\n" +
                "                      \"available\",\n" +
                "                      \"pending\",\n" +
                "                      \"sold\"\n" +
                "                    ],\n" +
                "                    \"default\": \"available\"\n" +
                "                  },\n" +
                "                  \"collectionFormat\": \"multi\"\n" +
                "                }\n" +
                "              ],\n" +
                "              \"responses\": {\n" +
                "                \"200\": {\n" +
                "                  \"description\": \"successful operation\",\n" +
                "                  \"schema\": {\n" +
                "                    \"type\": \"array\",\n" +
                "                    \"items\": {\n" +
                "                      \"$ref\": \"#/definitions/Pet\"\n" +
                "                    }\n" +
                "                  }\n" +
                "                },\n" +
                "                \"400\": {\n" +
                "                  \"description\": \"Invalid status value\"\n" +
                "                }\n" +
                "              },\n" +
                "              \"security\": [\n" +
                "                {\n" +
                "                  \"petstore_auth\": [\n" +
                "                    \"write:pets\",\n" +
                "                    \"read:pets\"\n" +
                "                  ]\n" +
                "                }\n" +
                "              ]\n" +
                "            }\n" +
                "          },\n" +
                "          \"/pet/findByTags\": {\n" +
                "            \"get\": {\n" +
                "              \"tags\": [\n" +
                "                \"pet\"\n" +
                "              ],\n" +
                "              \"summary\": \"Finds Pets by tags\",\n" +
                "              \"description\": \"Muliple tags can be provided with comma separated strings. Use tag1, tag2, tag3 for testing.\",\n" +
                "              \"operationId\": \"findPetsByTags\",\n" +
                "              \"produces\": [\n" +
                "                \"application/xml\",\n" +
                "                \"application/json\"\n" +
                "              ],\n" +
                "              \"parameters\": [\n" +
                "                {\n" +
                "                  \"name\": \"tags\",\n" +
                "                  \"in\": \"query\",\n" +
                "                  \"description\": \"Tags to filter by\",\n" +
                "                  \"required\": true,\n" +
                "                  \"type\": \"array\",\n" +
                "                  \"items\": {\n" +
                "                    \"type\": \"string\"\n" +
                "                  },\n" +
                "                  \"collectionFormat\": \"multi\"\n" +
                "                }\n" +
                "              ],\n" +
                "              \"responses\": {\n" +
                "                \"200\": {\n" +
                "                  \"description\": \"successful operation\",\n" +
                "                  \"schema\": {\n" +
                "                    \"type\": \"array\",\n" +
                "                    \"items\": {\n" +
                "                      \"$ref\": \"#/definitions/Pet\"\n" +
                "                    }\n" +
                "                  }\n" +
                "                },\n" +
                "                \"400\": {\n" +
                "                  \"description\": \"Invalid tag value\"\n" +
                "                }\n" +
                "              },\n" +
                "              \"security\": [\n" +
                "                {\n" +
                "                  \"petstore_auth\": [\n" +
                "                    \"write:pets\",\n" +
                "                    \"read:pets\"\n" +
                "                  ]\n" +
                "                }\n" +
                "              ],\n" +
                "              \"deprecated\": true\n" +
                "            }\n" +
                "          },\n" +
                "          \"/pet/{petId}\": {\n" +
                "            \"get\": {\n" +
                "              \"tags\": [\n" +
                "                \"pet\"\n" +
                "              ],\n" +
                "              \"summary\": \"Find pet by ID\",\n" +
                "              \"description\": \"Returns a single pet\",\n" +
                "              \"operationId\": \"getPetById\",\n" +
                "              \"produces\": [\n" +
                "                \"application/xml\",\n" +
                "                \"application/json\"\n" +
                "              ],\n" +
                "              \"parameters\": [\n" +
                "                {\n" +
                "                  \"name\": \"petId\",\n" +
                "                  \"in\": \"path\",\n" +
                "                  \"description\": \"ID of pet to return\",\n" +
                "                  \"required\": true,\n" +
                "                  \"type\": \"integer\",\n" +
                "                  \"format\": \"int64\"\n" +
                "                }\n" +
                "              ],\n" +
                "              \"responses\": {\n" +
                "                \"200\": {\n" +
                "                  \"description\": \"successful operation\",\n" +
                "                  \"schema\": {\n" +
                "                    \"$ref\": \"#/definitions/Pet\"\n" +
                "                  },\n" +
                "                  \"examples\": {\n" +
                "                    \"application/json\": {\n" +
                "                      \"photoUrls\": [\n" +
                "                        \"aeiou\"\n" +
                "                      ],\n" +
                "                      \"name\": \"doggie\",\n" +
                "                      \"id\": 123456789,\n" +
                "                      \"category\": {\n" +
                "                        \"name\": \"aeiou\",\n" +
                "                        \"id\": 123456789\n" +
                "                      },\n" +
                "                      \"tags\": [\n" +
                "                        {\n" +
                "                          \"name\": \"aeiou\",\n" +
                "                          \"id\": 123456789\n" +
                "                        }\n" +
                "                      ],\n" +
                "                      \"status\": \"aeiou\"\n" +
                "                    }\n" +
                "                  }\n" +
                "                },\n" +
                "                \"400\": {\n" +
                "                  \"description\": \"Invalid ID supplied\"\n" +
                "                },\n" +
                "                \"404\": {\n" +
                "                  \"description\": \"Pet not found\"\n" +
                "                }\n" +
                "              },\n" +
                "              \"security\": [\n" +
                "                {\n" +
                "                  \"api_key\": []\n" +
                "                }\n" +
                "              ]\n" +
                "            },\n" +
                "            \"post\": {\n" +
                "              \"tags\": [\n" +
                "                \"pet\"\n" +
                "              ],\n" +
                "              \"summary\": \"Updates a pet in the store with form data\",\n" +
                "              \"description\": \"\",\n" +
                "              \"operationId\": \"updatePetWithForm\",\n" +
                "              \"consumes\": [\n" +
                "                \"application/x-www-form-urlencoded\"\n" +
                "              ],\n" +
                "              \"produces\": [\n" +
                "                \"application/xml\",\n" +
                "                \"application/json\"\n" +
                "              ],\n" +
                "              \"parameters\": [\n" +
                "                {\n" +
                "                  \"name\": \"petId\",\n" +
                "                  \"in\": \"path\",\n" +
                "                  \"description\": \"ID of pet that needs to be updated\",\n" +
                "                  \"required\": true,\n" +
                "                  \"type\": \"integer\",\n" +
                "                  \"format\": \"int64\"\n" +
                "                },\n" +
                "                {\n" +
                "                  \"name\": \"name\",\n" +
                "                  \"in\": \"formData\",\n" +
                "                  \"description\": \"Updated name of the pet\",\n" +
                "                  \"required\": false,\n" +
                "                  \"type\": \"string\"\n" +
                "                },\n" +
                "                {\n" +
                "                  \"name\": \"status\",\n" +
                "                  \"in\": \"formData\",\n" +
                "                  \"description\": \"Updated status of the pet\",\n" +
                "                  \"required\": false,\n" +
                "                  \"type\": \"string\"\n" +
                "                }\n" +
                "              ],\n" +
                "              \"responses\": {\n" +
                "                \"405\": {\n" +
                "                  \"description\": \"Invalid input\"\n" +
                "                }\n" +
                "              },\n" +
                "              \"security\": [\n" +
                "                {\n" +
                "                  \"petstore_auth\": [\n" +
                "                    \"write:pets\",\n" +
                "                    \"read:pets\"\n" +
                "                  ]\n" +
                "                }\n" +
                "              ]\n" +
                "            },\n" +
                "            \"delete\": {\n" +
                "              \"tags\": [\n" +
                "                \"pet\"\n" +
                "              ],\n" +
                "              \"summary\": \"Deletes a pet\",\n" +
                "              \"description\": \"\",\n" +
                "              \"operationId\": \"deletePet\",\n" +
                "              \"produces\": [\n" +
                "                \"application/xml\",\n" +
                "                \"application/json\"\n" +
                "              ],\n" +
                "              \"parameters\": [\n" +
                "                {\n" +
                "                  \"name\": \"api_key\",\n" +
                "                  \"in\": \"header\",\n" +
                "                  \"required\": false,\n" +
                "                  \"type\": \"string\"\n" +
                "                },\n" +
                "                {\n" +
                "                  \"name\": \"petId\",\n" +
                "                  \"in\": \"path\",\n" +
                "                  \"description\": \"Pet id to delete\",\n" +
                "                  \"required\": true,\n" +
                "                  \"type\": \"integer\",\n" +
                "                  \"format\": \"int64\"\n" +
                "                }\n" +
                "              ],\n" +
                "              \"responses\": {\n" +
                "                \"400\": {\n" +
                "                  \"description\": \"Invalid ID supplied\"\n" +
                "                },\n" +
                "                \"404\": {\n" +
                "                  \"description\": \"Pet not found\"\n" +
                "                }\n" +
                "              },\n" +
                "              \"security\": [\n" +
                "                {\n" +
                "                  \"petstore_auth\": [\n" +
                "                    \"write:pets\",\n" +
                "                    \"read:pets\"\n" +
                "                  ]\n" +
                "                }\n" +
                "              ]\n" +
                "            }\n" +
                "          },\n" +
                "          \"/pet/{petId}/uploadImage\": {\n" +
                "            \"post\": {\n" +
                "              \"tags\": [\n" +
                "                \"pet\"\n" +
                "              ],\n" +
                "              \"summary\": \"uploads an image\",\n" +
                "              \"description\": \"\",\n" +
                "              \"operationId\": \"uploadFile\",\n" +
                "              \"consumes\": [\n" +
                "                \"multipart/form-data\"\n" +
                "              ],\n" +
                "              \"produces\": [\n" +
                "                \"application/json\"\n" +
                "              ],\n" +
                "              \"parameters\": [\n" +
                "                {\n" +
                "                  \"name\": \"petId\",\n" +
                "                  \"in\": \"path\",\n" +
                "                  \"description\": \"ID of pet to update\",\n" +
                "                  \"required\": true,\n" +
                "                  \"type\": \"integer\",\n" +
                "                  \"format\": \"int64\"\n" +
                "                },\n" +
                "                {\n" +
                "                  \"name\": \"additionalMetadata\",\n" +
                "                  \"in\": \"formData\",\n" +
                "                  \"description\": \"Additional data to pass to server\",\n" +
                "                  \"required\": false,\n" +
                "                  \"type\": \"string\"\n" +
                "                },\n" +
                "                {\n" +
                "                  \"name\": \"file\",\n" +
                "                  \"in\": \"formData\",\n" +
                "                  \"description\": \"file to upload\",\n" +
                "                  \"required\": false,\n" +
                "                  \"type\": \"file\"\n" +
                "                }\n" +
                "              ],\n" +
                "              \"responses\": {\n" +
                "                \"200\": {\n" +
                "                  \"description\": \"successful operation\",\n" +
                "                  \"schema\": {\n" +
                "                    \"$ref\": \"#/definitions/ApiResponse\"\n" +
                "                  }\n" +
                "                }\n" +
                "              },\n" +
                "              \"security\": [\n" +
                "                {\n" +
                "                  \"petstore_auth\": [\n" +
                "                    \"write:pets\",\n" +
                "                    \"read:pets\"\n" +
                "                  ]\n" +
                "                }\n" +
                "              ]\n" +
                "            }\n" +
                "          },\n" +
                "          \"/store/inventory\": {\n" +
                "            \"get\": {\n" +
                "              \"tags\": [\n" +
                "                \"store\"\n" +
                "              ],\n" +
                "              \"summary\": \"Returns pet inventories by status\",\n" +
                "              \"description\": \"Returns a map of status codes to quantities\",\n" +
                "              \"operationId\": \"getInventory\",\n" +
                "              \"produces\": [\n" +
                "                \"application/json\"\n" +
                "              ],\n" +
                "              \"parameters\": [],\n" +
                "              \"responses\": {\n" +
                "                \"200\": {\n" +
                "                  \"description\": \"successful operation\",\n" +
                "                  \"schema\": {\n" +
                "                    \"type\": \"object\",\n" +
                "                    \"additionalProperties\": {\n" +
                "                      \"type\": \"integer\",\n" +
                "                      \"format\": \"int32\"\n" +
                "                    }\n" +
                "                  }\n" +
                "                }\n" +
                "              },\n" +
                "              \"security\": [\n" +
                "                {\n" +
                "                  \"api_key\": []\n" +
                "                }\n" +
                "              ]\n" +
                "            }\n" +
                "          },\n" +
                "          \"/store/order\": {\n" +
                "            \"post\": {\n" +
                "              \"tags\": [\n" +
                "                \"store\"\n" +
                "              ],\n" +
                "              \"summary\": \"Place an order for a pet\",\n" +
                "              \"description\": \"\",\n" +
                "              \"operationId\": \"placeOrder\",\n" +
                "              \"produces\": [\n" +
                "                \"application/xml\",\n" +
                "                \"application/json\"\n" +
                "              ],\n" +
                "              \"parameters\": [\n" +
                "                {\n" +
                "                  \"in\": \"body\",\n" +
                "                  \"name\": \"body\",\n" +
                "                  \"description\": \"order placed for purchasing the pet\",\n" +
                "                  \"required\": true,\n" +
                "                  \"schema\": {\n" +
                "                    \"$ref\": \"#/definitions/Order\"\n" +
                "                  }\n" +
                "                }\n" +
                "              ],\n" +
                "              \"responses\": {\n" +
                "                \"200\": {\n" +
                "                  \"description\": \"successful operation\",\n" +
                "                  \"schema\": {\n" +
                "                    \"$ref\": \"#/definitions/Order\"\n" +
                "                  }\n" +
                "                },\n" +
                "                \"400\": {\n" +
                "                  \"description\": \"Invalid Order\"\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "          },\n" +
                "          \"/store/order/{orderId}\": {\n" +
                "            \"get\": {\n" +
                "              \"tags\": [\n" +
                "                \"store\"\n" +
                "              ],\n" +
                "              \"summary\": \"Find purchase order by ID\",\n" +
                "              \"description\": \"For valid response try integer IDs with value >= 1 and <= 10. Other values will generated exceptions\",\n" +
                "              \"operationId\": \"getOrderById\",\n" +
                "              \"produces\": [\n" +
                "                \"application/xml\",\n" +
                "                \"application/json\"\n" +
                "              ],\n" +
                "              \"parameters\": [\n" +
                "                {\n" +
                "                  \"name\": \"orderId\",\n" +
                "                  \"in\": \"path\",\n" +
                "                  \"description\": \"ID of pet that needs to be fetched\",\n" +
                "                  \"required\": true,\n" +
                "                  \"type\": \"integer\",\n" +
                "                  \"maximum\": 10,\n" +
                "                  \"minimum\": 1,\n" +
                "                  \"format\": \"int64\"\n" +
                "                }\n" +
                "              ],\n" +
                "              \"responses\": {\n" +
                "                \"200\": {\n" +
                "                  \"description\": \"successful operation\",\n" +
                "                  \"schema\": {\n" +
                "                    \"$ref\": \"#/definitions/Order\"\n" +
                "                  }\n" +
                "                },\n" +
                "                \"400\": {\n" +
                "                  \"description\": \"Invalid ID supplied\"\n" +
                "                },\n" +
                "                \"404\": {\n" +
                "                  \"description\": \"Order not found\"\n" +
                "                }\n" +
                "              }\n" +
                "            },\n" +
                "            \"delete\": {\n" +
                "              \"tags\": [\n" +
                "                \"store\"\n" +
                "              ],\n" +
                "              \"summary\": \"Delete purchase order by ID\",\n" +
                "              \"description\": \"For valid response try integer IDs with positive integer value. Negative or non-integer values will generate API errors\",\n" +
                "              \"operationId\": \"deleteOrder\",\n" +
                "              \"produces\": [\n" +
                "                \"application/xml\",\n" +
                "                \"application/json\"\n" +
                "              ],\n" +
                "              \"parameters\": [\n" +
                "                {\n" +
                "                  \"name\": \"orderId\",\n" +
                "                  \"in\": \"path\",\n" +
                "                  \"description\": \"ID of the order that needs to be deleted\",\n" +
                "                  \"required\": true,\n" +
                "                  \"type\": \"integer\",\n" +
                "                  \"minimum\": 1,\n" +
                "                  \"format\": \"int64\"\n" +
                "                }\n" +
                "              ],\n" +
                "              \"responses\": {\n" +
                "                \"400\": {\n" +
                "                  \"description\": \"Invalid ID supplied\"\n" +
                "                },\n" +
                "                \"404\": {\n" +
                "                  \"description\": \"Order not found\"\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "          },\n" +
                "          \"/user\": {\n" +
                "            \"post\": {\n" +
                "              \"tags\": [\n" +
                "                \"user\"\n" +
                "              ],\n" +
                "              \"summary\": \"Create user\",\n" +
                "              \"description\": \"This can only be done by the logged in user.\",\n" +
                "              \"operationId\": \"createUser\",\n" +
                "              \"produces\": [\n" +
                "                \"application/xml\",\n" +
                "                \"application/json\"\n" +
                "              ],\n" +
                "              \"parameters\": [\n" +
                "                {\n" +
                "                  \"in\": \"body\",\n" +
                "                  \"name\": \"body\",\n" +
                "                  \"description\": \"Created user object\",\n" +
                "                  \"required\": true,\n" +
                "                  \"schema\": {\n" +
                "                    \"$ref\": \"#/definitions/User\"\n" +
                "                  }\n" +
                "                }\n" +
                "              ],\n" +
                "              \"responses\": {\n" +
                "                \"default\": {\n" +
                "                  \"description\": \"successful operation\"\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "          },\n" +
                "          \"/user/createWithArray\": {\n" +
                "            \"post\": {\n" +
                "              \"tags\": [\n" +
                "                \"user\"\n" +
                "              ],\n" +
                "              \"summary\": \"Creates list of users with given input array\",\n" +
                "              \"description\": \"\",\n" +
                "              \"operationId\": \"createUsersWithArrayInput\",\n" +
                "              \"produces\": [\n" +
                "                \"application/xml\",\n" +
                "                \"application/json\"\n" +
                "              ],\n" +
                "              \"parameters\": [\n" +
                "                {\n" +
                "                  \"in\": \"body\",\n" +
                "                  \"name\": \"body\",\n" +
                "                  \"description\": \"List of user object\",\n" +
                "                  \"required\": true,\n" +
                "                  \"schema\": {\n" +
                "                    \"type\": \"array\",\n" +
                "                    \"items\": {\n" +
                "                      \"$ref\": \"#/definitions/User\"\n" +
                "                    }\n" +
                "                  }\n" +
                "                }\n" +
                "              ],\n" +
                "              \"responses\": {\n" +
                "                \"default\": {\n" +
                "                  \"description\": \"successful operation\"\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "          },\n" +
                "          \"/user/createWithList\": {\n" +
                "            \"post\": {\n" +
                "              \"tags\": [\n" +
                "                \"user\"\n" +
                "              ],\n" +
                "              \"summary\": \"Creates list of users with given input array\",\n" +
                "              \"description\": \"\",\n" +
                "              \"operationId\": \"createUsersWithListInput\",\n" +
                "              \"produces\": [\n" +
                "                \"application/xml\",\n" +
                "                \"application/json\"\n" +
                "              ],\n" +
                "              \"parameters\": [\n" +
                "                {\n" +
                "                  \"in\": \"body\",\n" +
                "                  \"name\": \"body\",\n" +
                "                  \"description\": \"List of user object\",\n" +
                "                  \"required\": true,\n" +
                "                  \"schema\": {\n" +
                "                    \"type\": \"array\",\n" +
                "                    \"items\": {\n" +
                "                      \"$ref\": \"#/definitions/User\"\n" +
                "                    }\n" +
                "                  }\n" +
                "                }\n" +
                "              ],\n" +
                "              \"responses\": {\n" +
                "                \"default\": {\n" +
                "                  \"description\": \"successful operation\"\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "          },\n" +
                "          \"/user/login\": {\n" +
                "            \"get\": {\n" +
                "              \"tags\": [\n" +
                "                \"user\"\n" +
                "              ],\n" +
                "              \"summary\": \"Logs user into the system\",\n" +
                "              \"description\": \"\",\n" +
                "              \"operationId\": \"loginUser\",\n" +
                "              \"produces\": [\n" +
                "                \"application/xml\",\n" +
                "                \"application/json\"\n" +
                "              ],\n" +
                "              \"parameters\": [\n" +
                "                {\n" +
                "                  \"name\": \"username\",\n" +
                "                  \"in\": \"query\",\n" +
                "                  \"description\": \"The user name for login\",\n" +
                "                  \"required\": true,\n" +
                "                  \"type\": \"string\"\n" +
                "                },\n" +
                "                {\n" +
                "                  \"name\": \"password\",\n" +
                "                  \"in\": \"query\",\n" +
                "                  \"description\": \"The password for login in clear text\",\n" +
                "                  \"required\": true,\n" +
                "                  \"type\": \"string\"\n" +
                "                }\n" +
                "              ],\n" +
                "              \"responses\": {\n" +
                "                \"200\": {\n" +
                "                  \"description\": \"successful operation\",\n" +
                "                  \"schema\": {\n" +
                "                    \"type\": \"string\"\n" +
                "                  },\n" +
                "                  \"headers\": {\n" +
                "                    \"X-Rate-Limit\": {\n" +
                "                      \"type\": \"integer\",\n" +
                "                      \"format\": \"int32\",\n" +
                "                      \"description\": \"calls per hour allowed by the user\"\n" +
                "                    },\n" +
                "                    \"X-Expires-After\": {\n" +
                "                      \"type\": \"string\",\n" +
                "                      \"format\": \"date-time\",\n" +
                "                      \"description\": \"date in UTC when token expires\"\n" +
                "                    }\n" +
                "                  }\n" +
                "                },\n" +
                "                \"400\": {\n" +
                "                  \"description\": \"Invalid username/password supplied\"\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "          },\n" +
                "          \"/user/logout\": {\n" +
                "            \"get\": {\n" +
                "              \"tags\": [\n" +
                "                \"user\"\n" +
                "              ],\n" +
                "              \"summary\": \"Logs out current logged in user session\",\n" +
                "              \"description\": \"\",\n" +
                "              \"operationId\": \"logoutUser\",\n" +
                "              \"produces\": [\n" +
                "                \"application/xml\",\n" +
                "                \"application/json\"\n" +
                "              ],\n" +
                "              \"parameters\": [],\n" +
                "              \"responses\": {\n" +
                "                \"default\": {\n" +
                "                  \"description\": \"successful operation\"\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "          },\n" +
                "          \"/user/{username}\": {\n" +
                "            \"get\": {\n" +
                "              \"tags\": [\n" +
                "                \"user\"\n" +
                "              ],\n" +
                "              \"summary\": \"Get user by user name\",\n" +
                "              \"description\": \"\",\n" +
                "              \"operationId\": \"getUserByName\",\n" +
                "              \"produces\": [\n" +
                "                \"application/xml\",\n" +
                "                \"application/json\"\n" +
                "              ],\n" +
                "              \"parameters\": [\n" +
                "                {\n" +
                "                  \"name\": \"username\",\n" +
                "                  \"in\": \"path\",\n" +
                "                  \"description\": \"The name that needs to be fetched. Use user1 for testing. \",\n" +
                "                  \"required\": true,\n" +
                "                  \"type\": \"string\"\n" +
                "                }\n" +
                "              ],\n" +
                "              \"responses\": {\n" +
                "                \"200\": {\n" +
                "                  \"description\": \"successful operation\",\n" +
                "                  \"schema\": {\n" +
                "                    \"$ref\": \"#/definitions/User\"\n" +
                "                  }\n" +
                "                },\n" +
                "                \"400\": {\n" +
                "                  \"description\": \"Invalid username supplied\"\n" +
                "                },\n" +
                "                \"404\": {\n" +
                "                  \"description\": \"User not found\"\n" +
                "                }\n" +
                "              }\n" +
                "            },\n" +
                "            \"put\": {\n" +
                "              \"tags\": [\n" +
                "                \"user\"\n" +
                "              ],\n" +
                "              \"summary\": \"Updated user\",\n" +
                "              \"description\": \"This can only be done by the logged in user.\",\n" +
                "              \"operationId\": \"updateUser\",\n" +
                "              \"produces\": [\n" +
                "                \"application/xml\",\n" +
                "                \"application/json\"\n" +
                "              ],\n" +
                "              \"parameters\": [\n" +
                "                {\n" +
                "                  \"name\": \"username\",\n" +
                "                  \"in\": \"path\",\n" +
                "                  \"description\": \"name that need to be updated\",\n" +
                "                  \"required\": true,\n" +
                "                  \"type\": \"string\"\n" +
                "                },\n" +
                "                {\n" +
                "                  \"in\": \"body\",\n" +
                "                  \"name\": \"body\",\n" +
                "                  \"description\": \"Updated user object\",\n" +
                "                  \"required\": true,\n" +
                "                  \"schema\": {\n" +
                "                    \"$ref\": \"#/definitions/User\"\n" +
                "                  }\n" +
                "                }\n" +
                "              ],\n" +
                "              \"responses\": {\n" +
                "                \"400\": {\n" +
                "                  \"description\": \"Invalid user supplied\"\n" +
                "                },\n" +
                "                \"404\": {\n" +
                "                  \"description\": \"User not found\"\n" +
                "                }\n" +
                "              }\n" +
                "            },\n" +
                "            \"delete\": {\n" +
                "              \"tags\": [\n" +
                "                \"user\"\n" +
                "              ],\n" +
                "              \"summary\": \"Delete user\",\n" +
                "              \"description\": \"This can only be done by the logged in user.\",\n" +
                "              \"operationId\": \"deleteUser\",\n" +
                "              \"produces\": [\n" +
                "                \"application/xml\",\n" +
                "                \"application/json\"\n" +
                "              ],\n" +
                "              \"parameters\": [\n" +
                "                {\n" +
                "                  \"name\": \"username\",\n" +
                "                  \"in\": \"path\",\n" +
                "                  \"description\": \"The name that needs to be deleted\",\n" +
                "                  \"required\": true,\n" +
                "                  \"type\": \"string\"\n" +
                "                }\n" +
                "              ],\n" +
                "              \"responses\": {\n" +
                "                \"400\": {\n" +
                "                  \"description\": \"Invalid username supplied\"\n" +
                "                },\n" +
                "                \"404\": {\n" +
                "                  \"description\": \"User not found\"\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "          }\n" +
                "        },\n" +
                "        \"securityDefinitions\": {\n" +
                "          \"petstore_auth\": {\n" +
                "            \"type\": \"oauth2\",\n" +
                "            \"authorizationUrl\": \"http://petstore.swagger.io/oauth/dialog\",\n" +
                "            \"flow\": \"implicit\",\n" +
                "            \"scopes\": {\n" +
                "              \"write:pets\": \"modify pets in your account\",\n" +
                "              \"read:pets\": \"read your pets\"\n" +
                "            }\n" +
                "          },\n" +
                "          \"api_key\": {\n" +
                "            \"type\": \"apiKey\",\n" +
                "            \"name\": \"api_key\",\n" +
                "            \"in\": \"header\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"definitions\": {\n" +
                "          \"Order\": {\n" +
                "            \"type\": \"object\",\n" +
                "            \"properties\": {\n" +
                "              \"id\": {\n" +
                "                \"type\": \"integer\",\n" +
                "                \"format\": \"int64\"\n" +
                "              },\n" +
                "              \"petId\": {\n" +
                "                \"type\": \"integer\",\n" +
                "                \"format\": \"int64\"\n" +
                "              },\n" +
                "              \"quantity\": {\n" +
                "                \"type\": \"integer\",\n" +
                "                \"format\": \"int32\"\n" +
                "              },\n" +
                "              \"shipDate\": {\n" +
                "                \"type\": \"string\",\n" +
                "                \"format\": \"date-time\"\n" +
                "              },\n" +
                "              \"status\": {\n" +
                "                \"type\": \"string\",\n" +
                "                \"description\": \"Order Status\",\n" +
                "                \"enum\": [\n" +
                "                  \"placed\",\n" +
                "                  \"approved\",\n" +
                "                  \"delivered\"\n" +
                "                ]\n" +
                "              },\n" +
                "              \"complete\": {\n" +
                "                \"type\": \"boolean\",\n" +
                "                \"default\": false\n" +
                "              }\n" +
                "            },\n" +
                "            \"xml\": {\n" +
                "              \"name\": \"Order\"\n" +
                "            }\n" +
                "          },\n" +
                "          \"Category\": {\n" +
                "            \"type\": \"object\",\n" +
                "            \"properties\": {\n" +
                "              \"id\": {\n" +
                "                \"type\": \"integer\",\n" +
                "                \"format\": \"int64\"\n" +
                "              },\n" +
                "              \"name\": {\n" +
                "                \"type\": \"string\"\n" +
                "              }\n" +
                "            },\n" +
                "            \"xml\": {\n" +
                "              \"name\": \"Category\"\n" +
                "            }\n" +
                "          },\n" +
                "          \"User\": {\n" +
                "            \"type\": \"object\",\n" +
                "            \"properties\": {\n" +
                "              \"id\": {\n" +
                "                \"type\": \"integer\",\n" +
                "                \"format\": \"int64\"\n" +
                "              },\n" +
                "              \"username\": {\n" +
                "                \"type\": \"string\"\n" +
                "              },\n" +
                "              \"firstName\": {\n" +
                "                \"type\": \"string\"\n" +
                "              },\n" +
                "              \"lastName\": {\n" +
                "                \"type\": \"string\"\n" +
                "              },\n" +
                "              \"email\": {\n" +
                "                \"type\": \"string\"\n" +
                "              },\n" +
                "              \"password\": {\n" +
                "                \"type\": \"string\"\n" +
                "              },\n" +
                "              \"phone\": {\n" +
                "                \"type\": \"string\"\n" +
                "              },\n" +
                "              \"userStatus\": {\n" +
                "                \"type\": \"integer\",\n" +
                "                \"format\": \"int32\",\n" +
                "                \"description\": \"User Status\"\n" +
                "              }\n" +
                "            },\n" +
                "            \"xml\": {\n" +
                "              \"name\": \"User\"\n" +
                "            }\n" +
                "          },\n" +
                "          \"Tag\": {\n" +
                "            \"type\": \"object\",\n" +
                "            \"properties\": {\n" +
                "              \"id\": {\n" +
                "                \"type\": \"integer\",\n" +
                "                \"format\": \"int64\"\n" +
                "              },\n" +
                "              \"name\": {\n" +
                "                \"type\": \"string\"\n" +
                "              }\n" +
                "            },\n" +
                "            \"xml\": {\n" +
                "              \"name\": \"Tag\"\n" +
                "            }\n" +
                "          },\n" +
                "          \"Pet\": {\n" +
                "            \"type\": \"object\",\n" +
                "            \"required\": [\n" +
                "              \"name\",\n" +
                "              \"photoUrls\"\n" +
                "            ],\n" +
                "            \"properties\": {\n" +
                "              \"id\": {\n" +
                "                \"type\": \"integer\",\n" +
                "                \"format\": \"int64\"\n" +
                "              },\n" +
                "              \"category\": {\n" +
                "                \"$ref\": \"#/definitions/Category\"\n" +
                "              },\n" +
                "              \"name\": {\n" +
                "                \"type\": \"string\",\n" +
                "                \"example\": \"doggie\"\n" +
                "              },\n" +
                "              \"photoUrls\": {\n" +
                "                \"type\": \"array\",\n" +
                "                \"xml\": {\n" +
                "                  \"name\": \"photoUrl\",\n" +
                "                  \"wrapped\": true\n" +
                "                },\n" +
                "                \"items\": {\n" +
                "                  \"type\": \"string\"\n" +
                "                }\n" +
                "              },\n" +
                "              \"tags\": {\n" +
                "                \"type\": \"array\",\n" +
                "                \"xml\": {\n" +
                "                  \"name\": \"tag\",\n" +
                "                  \"wrapped\": true\n" +
                "                },\n" +
                "                \"items\": {\n" +
                "                  \"$ref\": \"#/definitions/Tag\"\n" +
                "                }\n" +
                "              },\n" +
                "              \"status\": {\n" +
                "                \"type\": \"string\",\n" +
                "                \"description\": \"pet status in the store\",\n" +
                "                \"enum\": [\n" +
                "                  \"available\",\n" +
                "                  \"pending\",\n" +
                "                  \"sold\"\n" +
                "                ]\n" +
                "              }\n" +
                "            },\n" +
                "            \"xml\": {\n" +
                "              \"name\": \"Pet\"\n" +
                "            }\n" +
                "          },\n" +
                "          \"ApiResponse\": {\n" +
                "            \"type\": \"object\",\n" +
                "            \"properties\": {\n" +
                "              \"code\": {\n" +
                "                \"type\": \"integer\",\n" +
                "                \"format\": \"int32\"\n" +
                "              },\n" +
                "              \"type\": {\n" +
                "                \"type\": \"string\"\n" +
                "              },\n" +
                "              \"message\": {\n" +
                "                \"type\": \"string\"\n" +
                "              }\n" +
                "            }\n" +
                "          }\n" +
                "        },\n" +
                "        \"externalDocs\": {\n" +
                "          \"description\": \"Find out more about Swagger\",\n" +
                "          \"url\": \"http://swagger.io\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"config\": {\n" +
                "        \"rootPackage\": \"com.networknt.petstore\",\n" +
                "        \"handlerPackage\": \"com.networknt.petstore.handler\",\n" +
                "        \"modelPackage\": \"com.networknt.petstore.model\",\n" +
                "        \"artifactId\": \"petstore\",\n" +
                "        \"groupId\": \"com.networknt\",\n" +
                "        \"name\": \"petstore\",\n" +
                "        \"version\": \"1.0.1\",\n" +
                "        \"overwriteHandler\": false,\n" +
                "        \"overwriteHandlerTest\": false\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}}";


        final AtomicReference<ClientResponse> reference = new AtomicReference<>();
        final Http2Client client = Http2Client.getInstance();
        final CountDownLatch latch = new CountDownLatch(1);
        final ClientConnection connection;
        try {
            connection = client.connect(new URI(url), Http2Client.WORKER, Http2Client.SSL, Http2Client.POOL, OptionMap.EMPTY).get();
        } catch (Exception e) {
            throw new ClientException(e);
        }
        try {
            connection.getIoThread().execute(new Runnable() {
                @Override
                public void run() {
                    final ClientRequest request = new ClientRequest().setMethod(Methods.POST).setPath("/api/json");
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
            IoUtils.safeClose(connection);
        }
        int statusCode = reference.get().getResponseCode();
        String body = reference.get().getAttachment(Http2Client.RESPONSE_BODY);
        Assert.assertEquals(200, statusCode);
        if(statusCode == 200) {
            Assert.assertTrue(body.contains(".zip"));
        }
    }
}
