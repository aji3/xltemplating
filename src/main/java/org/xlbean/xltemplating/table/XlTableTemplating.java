package org.xlbean.xltemplating.table;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xlbean.XlBean;
import org.xlbean.XlList;
import org.xlbean.reader.XlBeanReader;
import org.xlbean.util.XlBeanFactory;
import org.xlbean.xltemplating.engine.TemplatingEngine;
import org.xlbean.xltemplating.engine.TemplatingEngineFactory;
import org.xlbean.xltemplating.engine.pebble.PebbleEngineFactory;
import org.xlbean.xltemplating.script.ScriptHelper;
import org.xlbean.xltemplating.xlbean.XlTemplatingBeanFactory;

public class XlTableTemplating {

    public static final TemplatingEngineFactory DEFAULT_TEMPLATING_ENGINE_FACTORY = new PebbleEngineFactory();

    private static Logger log = LoggerFactory.getLogger(XlTableTemplating.class);

    private TemplatingEngineFactory templatingEngineFactory;

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

    public void initialize(String templatingEngineFQCN) {

        initializeTemplatingEngineFactory(templatingEngineFQCN);

        // Update XlBeanFactory to be able to execute Groovy script defined as `key`
        XlBeanFactory.setInstance(new XlTemplatingBeanFactory());
    }

    public void execute(String excelFilePath) {

        XlBean bean = loadXlBean(excelFilePath);

        executePreExecuteScripts(bean);

        // Path root = Paths.get(bean.value("rootPath"));
        Path root = resolveRootPath(bean.value("rootPath"), excelFilePath);
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
            if (Files.exists(outputFile) && !Boolean.valueOf(file.value("override"))) {
                // if output file already exists and this file is not "override"
                System.out.println("Skip: " + outputFile);
                continue;
            }

            Map<String, Object> templateEngineContext = new HashMap<>();
            templateEngineContext.putAll(bean);

            XlList customValues = file.list("customValues");
            if (customValues != null) {
                customValues.stream().filter(elem -> elem != null).forEach(customValue -> {
                    Map<String, Object> map = new HashMap<>();
                    map.putAll(bean);
                    Object pebbleValue = ScriptHelper.getInstance().execute(customValue.value("value"), map);
                    templateEngineContext.put(customValue.value("key"), pebbleValue);
                });
            }

            log.info("Generate: {}", outputFile);
            log.info("Data: {}", templateEngineContext.toString().replaceAll("[\r\n]", ""));

            // Execute Templating Engine
            TemplatingEngine engine = templatingEngineFactory.createEngine(root.toAbsolutePath().toString());
            engine.generate(templateFile, outputFile, templateEngineContext);
        }
    }

    private XlBean loadXlBean(String excelFilePath) {
        // Read Excel data by XlBean
        XlBeanReader reader = new XlBeanReader();
        log.info("Excel file path: {}", new File(excelFilePath).getAbsolutePath());
        return reader.read(new File(excelFilePath));
    }

    private void executePreExecuteScripts(XlBean bean) {
        // Execute pre-execute script
        XlList scriptList = bean.list("preExecute");
        if (scriptList != null) {
            scriptList.forEach(script -> {
                ScriptHelper.getInstance().execute(script.value("logic"), bean);
            });
        }
    }

    /**
     * Get {@link Path} object from rootPath. When the value is relative path, this
     * method uses directory of the Excel file as root.
     * 
     * @param pathStr
     * @param excelFilePath
     * @return
     */
    private static Path resolveRootPath(String pathStr, String excelFilePath) {
        Path retPath = null;
        if (new File(pathStr).isAbsolute()) {
            retPath = Paths.get(pathStr);
        } else {
            // excelFilePath should always pointing to Excel file hence it is OK to refer to
            // parent
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
