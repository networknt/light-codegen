package com.networknt.codegen.hybrid;

import com.networknt.codegen.Generator;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class KafkaSchemaRegistrationGenerationTest {
    @TempDir Path output;

    @Test
    void serverAndServiceEmitCanonicalKafkaSettingsAndValues() throws Exception {
        ObjectNode server = (ObjectNode) Generator.jsonMapper.readTree(
                getClass().getResourceAsStream("/serverConfig.json"));
        ObjectNode service = (ObjectNode) Generator.yamlMapper.readTree(
                getClass().getResourceAsStream("/service.yaml"));
        for (ObjectNode config : List.of(server, service)) {
            config.put("kafkaProducer", true);
            config.put("kafkaConsumer", true);
            config.put("kafkaStreams", true);
        }
        new HybridServerGenerator().generate(output.resolve("server").toString(), null, server);
        new HybridServiceGenerator().generate(output.resolve("service").toString(),
                Generator.yamlMapper.readTree(getClass().getResourceAsStream("/spec.yaml")), service);
        for (String folder : List.of("server/src/main/resources/config", "server/src/test/resources/config",
                "service/src/test/resources/config")) {
            Path directory = output.resolve(folder);
            String values = Files.readString(directory.resolve("values.yml"));
            for (String client : List.of("kafka-producer", "kafka-consumer", "kafka-streams")) {
                // The server only emits producer/Streams files; the service emits all three.
                if (!folder.equals("server/src/test/resources/config") && !(folder.startsWith("server/") && client.equals("kafka-consumer"))) {
                    String yaml = Files.readString(directory.resolve(client + ".yml"));
                    assertTrue(yaml.contains("auto.register.schemas: ${" + client + ".auto.register.schemas:true}"));
                    assertFalse(yaml.contains("schema.registry.auto.register.schemas"));
                }
                assertTrue(values.contains(client + ".auto.register.schemas: true"));
            }
            assertFalse(values.contains("schema.registry.auto.register.schemas"));
        }
        String consumer = templates.hybrid.kafkaConsumerYml.template("events").render().toString();
        assertTrue(consumer.contains("auto.register.schemas: ${kafka-consumer.auto.register.schemas:true}"));
        assertFalse(consumer.contains("schema.registry.auto.register.schemas"));
    }
}
