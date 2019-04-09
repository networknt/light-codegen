package com.networknt.codegen.handler;

import com.networknt.rpc.Handler;
import com.networknt.rpc.router.ServiceHandler;
import com.networknt.utility.NioUtils;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormData;
import io.undertow.util.HttpString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * @author Nicholas Azar
 * Created on July 8, 2017.
 */
@Deprecated
@ServiceHandler(id="lightapi.net/codegen/validateUploadFile/0.0.1")
public class ValidateUploadFileHandler implements Handler {
    static private final Logger logger = LoggerFactory.getLogger(ValidateUploadFileHandler.class);

    /**
     * Retrieve the file from the request and validate the json contents against a schema.
     * Will return the contents of the file as well.
     *
     * @param o The multipart as parsed from com.networknt.rpc.router.MultipartHandler
     *
     * @return The contents of the file if valid, null otherwise. TODO to provide error messages.
     */
    @Override
    public ByteBuffer handle(HttpServerExchange exchange, Object o) {
        exchange.getResponseHeaders().add(new HttpString("Content-Type"), "application/json");
        if (o instanceof FormData) {
            File file = this.getFileFromForm((FormData)o);
            try {
                // TODO validate against schema... where do I find the schema?
                String fileContents = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
                return NioUtils.toByteBuffer(fileContents);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    /**
     * Return the first file received in the request.
     *
     * @param formData The multipart request.
     * @return A file if the request has one, null otherwise.
     */
    private File getFileFromForm(FormData formData) {
        for (String data: formData) {
            for (FormData.FormValue formValue : formData.get(data)) {
                if (formValue.isFile()) {
                    return formValue.getPath().toFile();
                }
            }
        }
        return null;
    }
}
