package org.xlbean.xltemplating;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xlbean.xltemplating.dsl.Configuration;
import org.xlbean.xltemplating.dsl.TemplateDsl;
import org.xlbean.xltemplating.engine.TemplatingEngine;
import org.xlbean.xltemplating.engine.TemplatingEngineFactory;
import org.xlbean.xltemplating.engine.pebble.PebbleEngineFactory;

import groovy.lang.GroovyShell;
import groovy.lang.Script;

public class Template {

    public static final TemplatingEngineFactory DEFAULT_TEMPLATING_ENGINE_FACTORY = new PebbleEngineFactory();

    private static Logger log = LoggerFactory.getLogger(Template.class);

    private Map<String, ?> xlbean;
    private Path templateFilePath;
    private Path templateRootDirPath;
    private Path outputRootDirPath;

    private TemplatingEngineFactory templatingEngineFactory;

    public Template(Path templateFilePath, Path templateRootDirPath, Path outputRootDirPath, Map<String, ?> xlbean) {
        this.xlbean = xlbean;
        this.templateFilePath = templateFilePath;
        this.templateRootDirPath = templateRootDirPath;
        this.outputRootDirPath = outputRootDirPath;
        initializeTemplatingEngineFactory(null);
    }

    private void initializeTemplatingEngineFactory(String className) {
        if (className == null || className.isEmpty()) {
            templatingEngineFactory = DEFAULT_TEMPLATING_ENGINE_FACTORY;
        } else {
            try {
                Class<?> factoryClass = Class.forName(className);
                if (TemplatingEngineFactory.class.isAssignableFrom(factoryClass)) {
                    templatingEngineFactory = (TemplatingEngineFactory) factoryClass.newInstance();
                } else {
                    log.error(
                        "Specified template engine factory {} does not extend org.xlbean.xltemplating.engine.TemplatingEngineFactory class.",
                        className);
                }
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                log.error(
                    "Specified template engine factory {} is not found.",
                    className);
                templatingEngineFactory = DEFAULT_TEMPLATING_ENGINE_FACTORY;
            }
        }
        log.info("Using template engine factory: {}", templatingEngineFactory.getClass().getName());
    }

    private List<Map<String, ?>> iterator() {
        if (scriptConfig != null) {
            return scriptConfig.getIterator();
        } else {
            return Arrays.asList(xlbean);
        }
    }

    private Path baseDir() {
        String ret;
        if (scriptConfig == null || scriptConfig.getBaseDir() == null) {
            ret = templateFilePath.getParent().toString();
        } else {
            ret = scriptConfig.getBaseDir().call();
        }
        return Paths.get(XlTemplating2.resolveTemplateString(ret, xlbean));
    }

    protected Path outputDir() {
        String ret;
        if (scriptConfig == null || scriptConfig.getDir() == null) {
            ret = "";
        } else {
            ret = scriptConfig.getDir().call();
        }
        return Paths.get(XlTemplating2.resolveTemplateString(ret, xlbean));
    }

    protected String outputFileName() {
        String ret;
        if (scriptConfig == null || scriptConfig.getFilename() == null) {
            ret = templateFilePath.getFileName().toString().replace(".xtmpl", "");
        } else {
            ret = scriptConfig.getFilename().call();
        }
        return XlTemplating2.resolveTemplateString(ret, xlbean);
    }

    protected boolean skip() {
        if (scriptConfig == null || scriptConfig.getSkip() == null) {
            return false;
        } else {
            return scriptConfig.getSkip().call();
        }
    }

    protected boolean skipFile() {
        if (scriptConfig == null || scriptConfig.getSkipFile() == null) {
            return false;
        } else {
            return scriptConfig.getSkipFile().call();
        }
    }

    protected boolean override() {
        if (scriptConfig == null || scriptConfig.getOverride() == null) {
            return true;
        } else {
            return scriptConfig.getOverride().call();
        }
    }

    private String extractDSL(Path templatePath) {
        try (BufferedReader br = Files.newBufferedReader(templatePath)) {
            String line = br.readLine();
            if (!"{####".equals(line.trim())) {
                // DSL must start from the first line of the template file.
                return null;
            }
            StringBuilder dsl = new StringBuilder();
            while ((line = br.readLine()) != null) {
                if ("####}".equals(line)) {
                    return dsl.toString();
                }
                dsl.append(line);
                dsl.append("\r\n");
            }
            throw new RuntimeException("Illegal DSL. DSL not closed." + templatePath);
        } catch (MalformedInputException e) {
            // ignore, since this is caused by excel file.
            log.warn("Skip file with MalformedInputException. {}", templatePath);
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Script script;
    private Configuration scriptConfig;

    private void initializeDsl() {

        String dsl = extractDSL(templateFilePath);
        if (dsl != null) {
            CompilerConfiguration compilerConfig = new CompilerConfiguration();
            compilerConfig.setScriptBaseClass(TemplateDsl.class.getName());

            GroovyShell shell = new GroovyShell(compilerConfig);
            script = shell.parse(dsl);
            script.setProperty("xlbean", xlbean);
            script.run();
            scriptConfig = (Configuration) script.getProperty("config");
        }
    }

    public void execute() {
        initializeDsl();

        for (Map<String, ?> data : iterator()) {
            registerDataToScriptContext(data);

            Path baseDir = baseDir();
            Path outputDir = baseDir.resolve(outputDir());
            outputDir = outputRootDirPath.resolve(templateRootDirPath.relativize(outputDir));
            Path outputFile = outputDir.resolve(outputFileName());
            if (skip()) {
                log.info("Skip: " + outputFile);
                continue;
            }
            if (!Files.exists(outputDir)) {
                // Create output directory if it doesn't exist.
                try {
                    Files.createDirectories(outputDir);
                } catch (IOException e) {
                    throw new RuntimeException(
                        "File system error occured for creating output directories/files",
                        e);
                }
            }
            if (Files.exists(outputFile) && !override() || skipFile()) {
                // if output file already exists and this file is not "override"
                log.info("Skip: " + outputFile);
                continue;
            }
            Map<String, Object> templateEngineContext = new HashMap<>();
            templateEngineContext.put("xlbean", xlbean);
            templateEngineContext.putAll(data);
            templateEngineContext.put("_it", data);

            log.info("Generate: {}", outputFile);
            log.info("Data: {}", templateEngineContext.toString().replaceAll("[\r\n]", ""));

            // Execute Templating Engine
            TemplatingEngine engine = templatingEngineFactory.createEngine(
                templateRootDirPath.toAbsolutePath().toString());
            engine.generate(templateFilePath, outputFile, templateEngineContext);

            unregisterDataFromScriptContext(data);
        }

    }

    private void registerDataToScriptContext(Map<String, ?> data) {
        if (script != null) {
            data
                .entrySet()
                .stream()
                .forEach(
                    entry -> script.getBinding().setProperty(entry.getKey(), entry.getValue()));
            script.getBinding().setProperty("_it", data);
        }
    }

    private void unregisterDataFromScriptContext(Map<String, ?> data) {
        if (script != null) {
            data
                .keySet()
                .stream()
                .forEach(key -> script.getBinding().setProperty(key, null));
            script.getBinding().setProperty("_it", null);
        }
    }

}
