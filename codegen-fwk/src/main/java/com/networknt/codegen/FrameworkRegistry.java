package com.networknt.codegen;

import com.networknt.service.SingletonServiceFactory;

import java.util.*;

/**
 * This class register all the framework generators.
 * Created by steve on 26/04/17.
 */
public class FrameworkRegistry {

    private final Map<String, Generator> frameworks;

    private static final FrameworkRegistry INSTANCE = new FrameworkRegistry();

    private FrameworkRegistry() {
        Generator[] generators = SingletonServiceFactory.getBeans(Generator.class);
        final Map<String, Generator> map = new HashMap<>();
        if(generators != null) Arrays.stream(generators).forEach(s -> map.put(s.getFramework(), s));
        this.frameworks = Collections.unmodifiableMap(map);
    }

    public Set<String> getFrameworks() {
        return frameworks.keySet();
    }

    public Generator getGenerator(String framework) {
        return frameworks.get(framework);
    }

    public static FrameworkRegistry getInstance() {
        return INSTANCE;
    }

}
