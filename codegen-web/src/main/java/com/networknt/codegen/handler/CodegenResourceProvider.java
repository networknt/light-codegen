package com.networknt.codegen.handler;

import com.networknt.resources.PathResourceProvider;
import io.undertow.server.handlers.resource.PathResourceManager;
import io.undertow.server.handlers.resource.ResourceManager;

import java.io.File;
import java.net.URL;

/**
 * @author Nicholas Azar
 * Created on April 22, 2018
 */
public class CodegenResourceProvider implements PathResourceProvider {
    @Override
    public String getPath() {
        return "/view";
    }

    @Override
    public Boolean isPrefixPath() {
        return true;
    }

    @Override
    public ResourceManager getResourceManager() {
        URL resourceDir = CodegenResourceProvider.class.getResource("/view");
        if (resourceDir != null) {
            return new PathResourceManager(new File(resourceDir.getFile()).toPath());
        }
        return null;
    }
}
