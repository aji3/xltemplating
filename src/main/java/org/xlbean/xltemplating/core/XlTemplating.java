package org.xlbean.xltemplating.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.codehaus.groovy.control.CompilationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xlbean.XlBean;
import org.xlbean.reader.XlBeanReader;
import org.xlbean.util.XlBeanFactory;
import org.xlbean.xltemplating.engine.TemplatingEngineFactory;
import org.xlbean.xltemplating.engine.pebble.PebbleEngineFactory;
import org.xlbean.xltemplating.xlbean.XlTemplatingBeanFactory;

import groovy.lang.GroovyShell;
import groovy.lang.Script;

/**
 * 
 * 
 * @author tanikawa
 *
 */
public class XlTemplating {

    public static final TemplatingEngineFactory DEFAULT_TEMPLATING_ENGINE_FACTORY = new PebbleEngineFactory();

    private static Logger log = LoggerFactory.getLogger(XlTemplating.class);

    /**
     * Walk through files and sub directories in the {@code templateDirPathStr}, and
     * does the following processing:
     * <ul>
     * <li>If it is a file, it checks file extension if it matches
     * "templateExtention" value defined in the excel sheet. If it matches, then run
     * templating logic with the file, otherwise, it simply copy the file to
     * {@code outputDirPathStr}</li>
     * <li>If it is a directory, the full path of the directory will be handled by
     * templating engine</li>
     * </ul>
     * 
     * @param excelFilePath
     * @param templateDirPathStr
     * @param outputDirPathStr
     */
    public void execute(
            String templatingEngineFQCN,
            String excelFilePath,
            String templateDirPathStr,
            String outputDirPathStr) {

        TemplatingContext context = initializeContext(
            excelFilePath,
            templateDirPathStr,
            outputDirPathStr,
            templatingEngineFQCN);

        executePreScript(context);

        walkThroughTemplates(context);
    }

    private TemplatingContext initializeContext(
            String excelFilePath,
            String templateDirPathStr,
            String outputDirPathStr,
            String templatingEngineFQCN) {

        // Update XlBeanFactory to be able to execute Groovy script defined as `key`
        XlBeanFactory.setInstance(new XlTemplatingBeanFactory());

        TemplatingEngineFactory templatingEngineFactory = initializeTemplatingEngineFactory(templatingEngineFQCN);

        return new TemplatingContext.Builder()
            .engine(templatingEngineFactory.createEngine(templateDirPathStr))
            .xlbean(loadXlBean(excelFilePath))
            .templateRootDir(Paths.get(templateDirPathStr).toAbsolutePath())
            .outputRootDir(Paths.get(outputDirPathStr).toAbsolutePath())
            .build();
    }

    /**
     * Walk through all template directories and files and generate output.
     * 
     * @param context
     */
    private void walkThroughTemplates(TemplatingContext context) {
        try {
            Files
                .walk(context.getTemplateRootDir())
                .forEach(path -> controller(path, context));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Execute pre.groovy with all of the properties in xlbean to be set as property
     * of the script. For instance, if xlbean.value("some_key") returns "xxx" then
     * "some_key" is accessible from pre.groovy.
     * 
     * @param context
     */
    private void executePreScript(TemplatingContext context) {
        log.info("EXECUTE PRE script");
        try {
            GroovyShell shell = new GroovyShell();
            Script script = shell.parse(context.getPreScriptPath().toFile());
            script.setProperty("xlbean", context.getXlbean());
            context.getXlbean().entrySet().forEach(entry -> script.setProperty(entry.getKey(), entry.getValue()));
            script.run();
        } catch (CompilationFailedException | IOException e) {
            throw new RuntimeException(e);
        }
        log.info("PRE script Completed");
        log.trace("{}", context.getXlbean());
    }

    /**
     * Delegates processing to appropriate method based on whether it is directory
     * or file.
     * 
     * @param templateFilePath
     * @param context
     */
    private void controller(Path templateFilePath, TemplatingContext context) {
        log.info("START: {}", templateFilePath);
        try {
            if (Files.isDirectory(templateFilePath)) {
                createDirectory(templateFilePath, context);
            } else {
                createFile(templateFilePath, context);
            }
        } finally {
            log.info("END: {}", templateFilePath);
        }
    }

    /**
     * Generate output file from file from template directory. If the file is
     * template file, then it runs file generation logic, otherwise it simply copy
     * the file to output directory.
     * 
     * @param templateFilePath
     * @param context
     */
    private void createFile(Path templateFilePath, TemplatingContext context) {
        if (context.isTemplate(templateFilePath)) {
            new FileTemplateGenerator().execute(templateFilePath, context);
        } else {
            copyFile(templateFilePath, context);
        }
    }

    /**
     * Copy template file to output directory. Target path is resolved by using
     * templating engine.
     * 
     * @param templateFilePath
     * @param context
     */
    private void copyFile(Path templateFilePath, TemplatingContext context) {
        Path targetPath = context.resolveOutputPath(templateFilePath, context.getXlbean());
        log.info("COPY FILE: {}", targetPath);
        try {
            Files.copy(templateFilePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createDirectory(Path templateFilePath, TemplatingContext context) {
        Path targetPath = context.resolveOutputPath(templateFilePath, context.getXlbean());
        log.info("CREATE DIRECTORY: {}", targetPath);
        try {
            Files.createDirectories(targetPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private XlBean loadXlBean(String excelFilePath) {
        XlBeanReader reader = new XlBeanReader();
        log.info("Excel file path: {}", new File(excelFilePath).getAbsolutePath());
        XlBean bean = reader.read(new File(excelFilePath));
        log.trace("{}", bean);
        return bean;
    }

    /**
     * Initialize templating engine factory from given className. The className must
     * inherit {@link TemplatingEngineFactory}. The default class is
     * {@link PebbleEngineFactory}
     * 
     * @param className
     */
    private TemplatingEngineFactory initializeTemplatingEngineFactory(String className) {
        TemplatingEngineFactory templatingEngineFactory;
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
                    templatingEngineFactory = DEFAULT_TEMPLATING_ENGINE_FACTORY;
                }
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                log.error(
                    "Specified template engine factory {} is not found.",
                    className);
                templatingEngineFactory = DEFAULT_TEMPLATING_ENGINE_FACTORY;
            }
        }
        log.info("Using template engine factory: {}", templatingEngineFactory.getClass().getName());
        return templatingEngineFactory;
    }
}
