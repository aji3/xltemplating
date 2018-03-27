package org.xlbean.xltemplating;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.xlbean.XlBean;
import org.xlbean.XlList;
import org.xlbean.reader.XlBeanReader;
import org.xlbean.util.XlBeanFactory;
import org.xlbean.xltemplating.engine.TemplatingEngine;
import org.xlbean.xltemplating.engine.TemplatingEngineFactory;
import org.xlbean.xltemplating.engine.pebble.PebbleEngineFactory;
import org.xlbean.xltemplating.script.ScriptHelper;
import org.xlbean.xltemplating.xlbean.XlTemplatingBeanFactory;

public class XlTemplatingMain {

    public static final TemplatingEngineFactory DEFAULT_TEMPLATING_ENGINE_FACTORY_FQCN =
            new PebbleEngineFactory();

    private static TemplatingEngineFactory templatingEngineFactory;

    private static void initializeFactory(String className) {
        try {
            Class<?> factoryClass = Class.forName(className);
            if (TemplatingEngineFactory.class.isAssignableFrom(factoryClass)) {
                templatingEngineFactory = (TemplatingEngineFactory) factoryClass.newInstance();
            } else {
                System.out.println(
                        "Specified template engine factory " + templatingEngineFactory
                                + " does not extend org.xlbean.xltemplating.engine.TemplatingEngineFactory class. "
                                + "org.xlbean.xltemplating.engine.pebble.PebbleEngineFactory is used.");
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            System.out.println(
                    "Specified template engine factory " + templatingEngineFactory
                            + " is not found. "
                            + "org.xlbean.xltemplating.engine.pebble.PebbleEngineFactory is used.");
            templatingEngineFactory = DEFAULT_TEMPLATING_ENGINE_FACTORY_FQCN;
        }
    }

    public static void main(String[] args) {
        ParsedArgs arguments = new ParsedArgs();

        CmdLineParser parser = new CmdLineParser(arguments);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            e.printStackTrace();
            return;
        }
        if (arguments.isHelp()) {
            parser.printUsage(System.out);
            return;
        }

        initializeFactory(arguments.getTemplatingEngineFQCN());

        // Override XlBeanFactory
        XlBeanFactory.setInstance(new XlTemplatingBeanFactory());

        // Read Excel data by XlBean
        XlBeanReader reader = new XlBeanReader();
        System.out.println(
                "Excel file path: " + new File(arguments.getExcelFilePath()).getAbsolutePath());
        XlBean bean = reader.read(new File(arguments.getExcelFilePath()));

        // Execute pre-execute script
        XlList scriptList = bean.list("preExecute");
        if (scriptList != null) {
            scriptList.forEach(script -> {
                ScriptHelper.getInstance().execute(script.value("logic"), bean);
            });
        }

        // Path root = Paths.get(bean.value("rootPath"));
        Path root = resolveRootPath(bean.value("rootPath"), arguments.getExcelFilePath());
        for (XlBean file : bean.list("files")) {
            if (file == null || !Boolean.valueOf(file.value("generate"))) {
                continue;
            }

            // Read path of templates and output files.
            Path templateDir = root.resolve(file.value("templateDirPath"));
            Path templateFile = templateDir.resolve(file.value("templateFileName"));
            Path outputDir = root.resolve(file.value("outputDirPath"));
            Path outputFile = outputDir.resolve(file.value("outputFileName"));
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

            Map<String, Object> templateEngineContext = new HashMap<>();
            templateEngineContext.putAll(bean);

            XlList customValues = file.list("customValues");
            if (customValues != null) {
                customValues.stream().filter(elem -> elem != null).forEach(customValue -> {
                    HashMap<String, Object> map = new HashMap<>();
                    map.putAll(bean);
                    Object pebbleValue =
                            ScriptHelper.getInstance().execute(customValue.value("value"), map);
                    templateEngineContext.put(customValue.value("key"), pebbleValue);
                });
            }

            // Execute Templating Engine
            TemplatingEngine engine =
                    templatingEngineFactory.createEngine(root.toAbsolutePath().toString());
            engine.generate(templateFile, outputFile, templateEngineContext);
        }
    }

    /**
     * Get {@link Path} object from rootPath. When the value is relative path, this method uses
     * directory of the Excel file as root.
     * 
     * @param pathStr
     * @param excelFilePath
     * @return
     */
    public static Path resolveRootPath(String pathStr, String excelFilePath) {
        Path retPath = null;
        if (new File(pathStr).isAbsolute()) {
            retPath = Paths.get(pathStr);
        } else {
            // excelFilePath should always pointing to Excel file hence it is OK to refer to parent
            // for its folder.
            Path excelFileDir = Paths.get(excelFilePath).getParent();
            retPath = excelFileDir.resolve(pathStr);
            if (pathStr.endsWith(".")) {
                retPath = retPath.getParent();
            }
        }
        return retPath;
    }
}
