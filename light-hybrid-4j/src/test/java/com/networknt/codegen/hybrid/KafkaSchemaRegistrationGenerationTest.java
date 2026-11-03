package com.networknt.codegen.hybrid;

import com.networknt.codegen.Generator;
import com.networknt.config.ConfigInjection;
import tools.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class KafkaSchemaRegistrationGenerationTest {
    @TempDir Path output;

    @Test
    void serverAndServiceEmitCanonicalKafkaSettingsAndValues() throws Exception {
        for (int flags = 0; flags < 8; flags++) {
            boolean producer = (flags & 1) != 0;
            boolean consumer = (flags & 2) != 0;
            boolean streams = (flags & 4) != 0;
            ObjectNode server = (ObjectNode) Generator.jsonMapper.readTree(
                    getClass().getResourceAsStream("/serverConfig.json"));
            ObjectNode service = (ObjectNode) Generator.yamlMapper.readTree(
                    getClass().getResourceAsStream("/service.yaml"));
            for (ObjectNode config : List.of(server, service)) {
                config.put("kafkaProducer", producer);
                config.put("kafkaConsumer", consumer);
                config.put("kafkaStreams", streams);
            }
            Path serverOutput = output.resolve("server-" + flags);
            Path serviceOutput = output.resolve("service-" + flags);
            new HybridServerGenerator().generate(serverOutput.toString(), null, server);
            new HybridServiceGenerator().generate(serviceOutput.toString(),
                    Generator.yamlMapper.readTree(getClass().getResourceAsStream("/spec.yaml")), service);

            // Preserve the existing server emission contract: its consumer flag emits Streams.
            Set<String> serverClients = new HashSet<>();
            if (producer) serverClients.add("kafka-producer");
            if (consumer) serverClients.add("kafka-streams");
            Set<String> serviceClients = new HashSet<>();
            if (producer) serviceClients.add("kafka-producer");
            if (consumer) serviceClients.add("kafka-consumer");
            if (streams) serviceClients.add("kafka-streams");
            assertConfigsAndValues(serverOutput.resolve("src/main/resources/config"), serverClients);
            assertConfigsAndValues(serviceOutput.resolve("src/test/resources/config"), serviceClients);
            assertValues(serverOutput.resolve("src/test/resources/config/values.yml"), serverClients);
        }
    }

    private void assertConfigsAndValues(Path directory, Set<String> expectedClients) throws Exception {
        assertValues(directory.resolve("values.yml"), expectedClients);
        Map<String, Object> original = ConfigInjection.getDecryptedValueMap();
        try {
            for (String client : List.of("kafka-producer", "kafka-consumer", "kafka-streams")) {
                Path path = directory.resolve(client + ".yml");
                assertEquals(expectedClients.contains(client), Files.exists(path), path.toString());
                if (!expectedClients.contains(client)) continue;
                String yaml = Files.readString(path);
                String placeholder = "${" + client + ".auto.register.schemas:true}";
                assertTrue(yaml.contains("auto.register.schemas: " + placeholder), path.toString());
                assertFalse(yaml.contains("schema.registry.auto.register.schemas"), path.toString());
                ConfigInjection.setDecryptedValueMap(new HashMap<>(Map.of(client + ".schema.registry.auto.register.schemas", false)));
                assertEquals(true, ConfigInjection.getInjectValue(placeholder, true), "Old names require explicit migration");
                ConfigInjection.setDecryptedValueMap(new HashMap<>(Map.of(client + ".auto.register.schemas", false)));
                assertEquals(false, ConfigInjection.getInjectValue(placeholder, true), path.toString());
            }
        } finally {
            ConfigInjection.setDecryptedValueMap(original);
        }
    }

    private void assertValues(Path path, Set<String> expectedClients) throws Exception {
        String values = Files.readString(path);
        for (String client : List.of("kafka-producer", "kafka-consumer", "kafka-streams")) {
            assertEquals(expectedClients.contains(client), values.contains(client + ".auto.register.schemas: true"),
                    path + " " + client);
        }
    }
}
