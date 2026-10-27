import com.networknt.codegen.Generator;
import com.networknt.codegen.graphql.GraphqlGenerator;
import com.networknt.codegen.hybrid.HybridServiceGenerator;
import com.networknt.codegen.rest.OpenApiKotlinGenerator;
import com.networknt.codegen.rest.OpenApiLambdaGenerator;
import com.networknt.codegen.rest.OpenApiLambdaNativeGenerator;
import com.networknt.codegen.rest.OpenApiLightGenerator;
import java.nio.file.Files;
import java.nio.file.Path;

class GenerateQualificationProject {
    public static void main(String[] args) throws Exception {
        Generator generator = switch (args[0]) {
            case "rest" -> new OpenApiLightGenerator();
            case "kotlin" -> new OpenApiKotlinGenerator();
            case "lambda" -> new OpenApiLambdaGenerator();
            case "lambdanative" -> new OpenApiLambdaNativeGenerator();
            case "hybrid" -> new HybridServiceGenerator();
            case "graphql" -> new GraphqlGenerator();
            default -> throw new IllegalArgumentException(args[0]);
        };
        var config = Generator.jsonMapper.readTree(Path.of(args[3]).toFile());
        Object model = args[0].equals("graphql") ? Files.readString(Path.of(args[2]))
                : Generator.yamlMapper.readTree(Path.of(args[2]).toFile());
        Files.createDirectories(Path.of(args[1]));
        generator.generate(args[1], model, config);
    }
}
