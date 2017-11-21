package com.networknt.codegen.handler;

import com.jsoniter.any.Any;
import com.networknt.codegen.CodegenWebConfig;
import com.networknt.codegen.FrameworkRegistry;
import com.networknt.codegen.Generator;
import com.networknt.config.Config;
import com.networknt.rpc.Handler;
import com.networknt.rpc.router.ServiceHandler;
import com.networknt.status.Status;
import com.networknt.utility.HashUtil;
import com.networknt.utility.NioUtils;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static java.io.File.separator;

/**
 * This is the handler that does the code generation for consumer request. There
 * might multiple generator objects in the request as an array. It loops each
 * generator object to generate projects into the same folder.
 *
 * @author Steve Hu
 */
@ServiceHandler(id="lightapi.net/codegen/generate/0.0.1")
public class GeneratorServiceHandler implements Handler {
    static private final String CONFIG_NAME = "codegen-web";
    static private final String STATUS_INVALID_FRAMEWORK = "ERR11100";
    static private final String STATUS_MISSING_GENERATOR_ITEM = "ERR11101";

    static private final XLogger logger = XLoggerFactory.getXLogger(GeneratorServiceHandler.class);

    static private CodegenWebConfig codegenWebConfig = (CodegenWebConfig) Config.getInstance().getJsonObjectConfig(CONFIG_NAME, CodegenWebConfig.class);


    @Override
    public ByteBuffer handle(Object input)  {
        logger.entry(input);

        // generate a destination folder name.
        String output = HashUtil.generateUUID();
        String zipFile = output + ".zip";
        String projectFolder = codegenWebConfig.getTmpFolder() + separator + output;

        Map<String, Object> map = (Map<String, Object>)input;
        List<Map<String, Object>> generators = (List<Map<String, Object>>)map.get("generators");
        if(generators == null || generators.size() == 0) {
            Status status = new Status(STATUS_MISSING_GENERATOR_ITEM);
            return NioUtils.toByteBuffer(status.toString());
        }
        for(Map<String, Object> generatorMap: generators) {
            String framework = (String)generatorMap.get("framework");
            Object model = Any.wrap(generatorMap.get("model"));  // should be a JSON of spec or IDL
            Map<String, Object> config = (Map<String, Object>)generatorMap.get("config"); // should be a json of config
            if(!FrameworkRegistry.getInstance().getFrameworks().contains(framework)) {
                Status status = new Status(STATUS_INVALID_FRAMEWORK, framework);
                return NioUtils.toByteBuffer(status.toString());
            }
            // TODO validate the model and config with json schema
            try {
                Generator generator = FrameworkRegistry.getInstance().getGenerator(framework);
                generator.generate(projectFolder, model, Any.wrap(config));
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Exception:", e);
            }
        }

        try {
            // TODO generated code is in tmp folder, zip and move to the target folder
            NioUtils.create(codegenWebConfig.getZipFolder() + separator + zipFile, projectFolder);
            // delete the project folder.
            Files.walk(Paths.get(projectFolder), FileVisitOption.FOLLOW_LINKS)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .peek(System.out::println)
                    .forEach(File::delete);
            // check if any zip file that needs to be deleted from zipFolder
            NioUtils.deleteOldFiles(codegenWebConfig.getZipFolder(), codegenWebConfig.getZipKeptMinute());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception:", e);
        }

        // return the location of the zip file
        return NioUtils.toByteBuffer(zipFile);
    }
}
