package org.xlbean.xltemplating;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xlbean.XlBean;
import org.xlbean.reader.XlBeanReader;
import org.xlbean.xlscript.XlScriptReader;
import org.xlbean.xlscript.util.JSON;
import org.xlbean.xltemplating.core.TemplateGeneratorProvider;
import org.xlbean.xltemplating.core.TemplatePathResolver;
import org.xlbean.xltemplating.core.TemplatePreprocessor;
import org.xlbean.xltemplating.core.TemplatingContext;
import org.xlbean.xltemplating.core.impl.DefaultTemplateGeneratorProvider;
import org.xlbean.xltemplating.core.impl.DefaultTemplatePathResolver;
import org.xlbean.xltemplating.core.impl.DefaultTemplatePreprocessor;
import org.xlbean.xltemplating.engine.TemplatingEngineFactory;
import org.xlbean.xltemplating.engine.pebble.PebbleEngineFactory;
import org.xlbean.xltemplating.ignore.XlTemplatingIgnoreLoader;
import org.xlbean.xltemplating.ignore.XlTemplatingIgnores;

public class TemplatingContextInitializer {

    public static final TemplatingEngineFactory DEFAULT_TEMPLATING_ENGINE_FACTORY = new PebbleEngineFactory();
    public static final TemplatePathResolver DEFAULT_TEMPLATE_PATH_RESOLVER = new DefaultTemplatePathResolver();
    public static final TemplateGeneratorProvider DEFAULT_TEMPLATE_GENERATOR_PROVIDER = new DefaultTemplateGeneratorProvider();
    public static final TemplatePreprocessor DEFAULT_TEMPLATE_PREPROCESSOR = new DefaultTemplatePreprocessor();

    private static Logger log = LoggerFactory.getLogger(TemplatingContextInitializer.class);

    public TemplatingContext initializeContext(TemplatingArgs args) {

        TemplatingEngineFactory templatingEngineFactory = instantiate(
            args.getTemplatingEngineFQCN(),
            TemplatingEngineFactory.class,
            DEFAULT_TEMPLATING_ENGINE_FACTORY);

        TemplatePathResolver pathResolver = instantiate(
            args.getPathResolverFQCN(),
            TemplatePathResolver.class,
            DEFAULT_TEMPLATE_PATH_RESOLVER);

        TemplateGeneratorProvider generatorProvider = instantiate(
            args.getTemplateGeneratorProvider(),
            TemplateGeneratorProvider.class,
            DEFAULT_TEMPLATE_GENERATOR_PROVIDER);

        TemplatePreprocessor preprocessor = instantiate(
            args.getPreprocessor(),
            TemplatePreprocessor.class,
            DEFAULT_TEMPLATE_PREPROCESSOR);

        Path templateRootDir = Paths.get(args.getTemplateDirectoryPath()).toAbsolutePath();
        Path outputDir = Paths.get(args.getOutputDirectoryPath()).toAbsolutePath();

        TemplatingContext context = new TemplatingContext.Builder()
            .engine(templatingEngineFactory.createEngine(templateRootDir))
            .excel(loadExcel(args.getExcelFilePath()))
            .templateRootDir(templateRootDir)
            .outputRootDir(outputDir)
            .pathResolver(pathResolver)
            .generatorProvider(generatorProvider)
            .preprocessor(preprocessor)
            .ignores(loadXlTemplatingIgores(args))
            .build();

        pathResolver.init(context);
        generatorProvider.init(context);
        preprocessor.init(context);

        return context;
    }

    private XlTemplatingIgnores loadXlTemplatingIgores(TemplatingArgs args) {
        Path ignoreFile = Paths.get(args.getTemplateDirectoryPath()).toAbsolutePath();
        Charset charset = Charset.forName(args.getTemplateCharset());
        return new XlTemplatingIgnoreLoader(ignoreFile, charset).load();
    }

    private XlBean loadExcel(String excelFilePath) {
        XlBeanReader reader = new XlScriptReader();
        log.info("Excel file path: {}", new File(excelFilePath).getAbsolutePath());
        XlBean bean = reader.read(new File(excelFilePath));
        log.debug("{}", JSON.stringify(bean));
        return bean;
    }

    @SuppressWarnings("unchecked")
    private <T> T instantiate(String className, Class<?> interfaceClass, T defaultObject) {
        T templatingEngineFactory;
        if (className == null || className.isEmpty()) {
            templatingEngineFactory = defaultObject;
        } else {
            try {
                Class<?> factoryClass = Class.forName(className);
                if (interfaceClass.isAssignableFrom(factoryClass)) {
                    templatingEngineFactory = (T) factoryClass.newInstance();
                } else {
                    log.error("Specified class name {} does not extend {}", className, interfaceClass.getName());
                    throw new IllegalArgumentException(
                        String.format(
                            "Specified class name %s does not extend %s",
                            className,
                            interfaceClass.getName()));
                }
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                log.error("Specified class name {} is not found.", className);
                throw new IllegalArgumentException(String.format("Specified class name %s is not found.", className));
            }
        }
        log.info(
            "For the interface {}, the implementation {} is going to be used.",
            interfaceClass.getName(),
            templatingEngineFactory.getClass().getName());
        return templatingEngineFactory;
    }

}
