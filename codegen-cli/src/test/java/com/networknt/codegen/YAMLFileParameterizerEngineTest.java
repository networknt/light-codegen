package com.networknt.codegen;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.ScalarNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class YAMLFileParameterizerEngineTest {
    @TempDir
    Path directory;

    @Test
    void composesMappingWithSourceMarks() throws IOException {
        File source = source("# header\nenabled: yes\ncount: 0755\n");
        MappingNode mapping = assertInstanceOf(MappingNode.class, YAMLFileParameterizer.loadFile(source));
        assertEquals(2, mapping.getValue().size());
        assertEquals(1, mapping.getValue().getFirst().getKeyNode().getStartMark().orElseThrow().getLine());
        ScalarNode scalar = assertInstanceOf(ScalarNode.class, mapping.getValue().getFirst().getValueNode());
        assertEquals("yes", scalar.getValue());
    }

    @Test
    void preservesLegacyScalarTextInDefaults() throws IOException {
        File source = source("# header\nenabled: on\ncount: 0755\nbinary: 0b11\nseparated: 1_000\n"
                + "time: 1:30\ndate: 2026-10-24\n");
        Path output = directory.resolve("output.yml");
        YAMLFileParameterizer.rewriteFile(source, output.toFile(), options(false, false));
        assertEquals(List.of("# header", "enabled: ${sample.enabled:on}", "count: ${sample.count:0755}",
                "binary: ${sample.binary:0b11}", "separated: ${sample.separated:1_000}",
                "time: ${sample.time:1:30}", "date: ${sample.date:2026-10-24}"), Files.readAllLines(output));
    }

    @Test
    void parameterizesCollectionsAndPreservesInterveningComments() throws IOException {
        File source = source("# header\nfirst: begin\n# scalar comment\nlist:\n  - first\n  - second\nmap:\n  key: value\nlast: end\n");
        Path output = directory.resolve("output.yml");
        YAMLFileParameterizer.rewriteFile(source, output.toFile(), options(false, false));
        assertEquals(List.of("# header", "first: ${sample.first:begin}", "# scalar comment", "list: ${sample.list}",
                "map: ${sample.map}", "last: ${sample.last:end}"), Files.readAllLines(output));
    }

    @Test
    void skippedCollectionsRetainOriginalLines() throws IOException {
        String input = "# header\nlist:\n  - first\n  - second\nmap:\n  key: value\nlast: end\n";
        File source = source(input);
        Path output = directory.resolve("output.yml");
        YAMLFileParameterizer.rewriteFile(source, output.toFile(), options(true, true));
        assertEquals(input.replace("last: end", "last: ${sample.last:end}"), Files.readString(output));
    }

    @Test
    void skipArrayAndSkipMapAreIndependent() throws IOException {
        File source = source("list:\n  - first\nmap:\n  key: value\nlast: end\n");
        Path output = directory.resolve("output.yml");
        YAMLFileParameterizer.rewriteFile(source, output.toFile(), options(true, false));
        assertEquals(List.of("list:", "  - first", "map: ${sample.map}", "last: ${sample.last:end}"), Files.readAllLines(output));
        YAMLFileParameterizer.rewriteFile(source, output.toFile(), options(false, true));
        assertEquals(List.of("list: ${sample.list}", "map:", "  key: value", "last: ${sample.last:end}"), Files.readAllLines(output));
    }

    @Test
    void resourceRewriteUsesEngineMarksAndPreservesUnicode() throws IOException {
        Path output = directory.resolve("resource.yml");
        assertInstanceOf(MappingNode.class, YAMLFileParameterizer.loadResource("parameterizer-engine.yml"));
        YAMLFileParameterizer.rewriteResource("resource", "parameterizer-engine.yml", output.toString(), options(true, true));
        assertEquals(List.of("# Engine resource fixture", "message: ${resource.message:café λ}",
                "enabled: ${resource.enabled:yes}", "list:", "  - value", ""), Files.readAllLines(output));
    }

    @Test
    void duplicateKeysKeepTheirSourceDefaults() throws IOException {
        File source = source("value: first\nvalue: last\n");
        Path output = directory.resolve("output.yml");
        YAMLFileParameterizer.rewriteFile(source, output.toFile(), options(false, false));
        assertEquals(List.of("value: ${sample.value:first}", "value: ${sample.value:last}"), Files.readAllLines(output));
    }

    @Test
    void inPlaceRewriteRemovesBackupAfterSuccess() throws IOException {
        File source = source("enabled: yes\n");
        YAMLFileParameterizer.rewriteAll(directory.toString(), options(false, false));
        assertEquals("enabled: ${sample.enabled:yes}\n", Files.readString(source.toPath()));
        assertFalse(Files.exists(directory.resolve("sample.yml.bak")));
    }

    @Test
    void disabledGenerationCopiesWithoutParsing() throws IOException {
        String input = "# copy verbatim\nvalue: [unfinished\n";
        File source = source(input);
        Path output = directory.resolve("copy.yml");
        YAMLFileParameterizer.rewriteFile(source, output.toFile(), Map.of("generate", false));
        assertEquals(input, Files.readString(output));
    }

    @Test
    void emptyAndMalformedDocumentsReturnNoNode() throws IOException {
        assertNull(YAMLFileParameterizer.loadFile(source("")));
        assertNull(YAMLFileParameterizer.loadFile(source("value: [unfinished\n")));
        assertNull(YAMLFileParameterizer.loadResource("missing-parameterizer-engine.yml"));
    }

    @Test
    void multipleDocumentsRetainSingleDocumentRestriction() throws IOException {
        assertNull(YAMLFileParameterizer.loadFile(source("first: one\n---\nsecond: two\n")));
    }

    @Test
    void sequenceRootRetainsUnsupportedRootContract() throws IOException {
        File source = source("- one\n- two\n");
        var node = YAMLFileParameterizer.loadFile(source);
        assertThrows(YAMLFileParameterizer.UnsupportedNodeTypeException.class,
                () -> YAMLFileParameterizer.parameterize("sample", node,
                        YAMLFileParameterizer.readFile(source), directory.resolve("output.yml").toFile(), options(false, false)));
    }

    private File source(String yaml) throws IOException {
        return Files.writeString(directory.resolve("sample.yml"), yaml).toFile();
    }

    private Map<String, Object> options(boolean skipArray, boolean skipMap) {
        return new HashMap<>(Map.of("generate", true, "skipArray", skipArray, "skipMap", skipMap));
    }
}
