package org.xlbean.xltemplating;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.control.CompilationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xlbean.XlBean;
import org.xlbean.reader.XlBeanReader;
import org.xlbean.util.XlBeanFactory;
import org.xlbean.xltemplating.engine.pebble.PebbleStringLoader;
import org.xlbean.xltemplating.xlbean.XlTemplatingBeanFactory;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.loader.DelegatingLoader;
import com.mitchellbosecke.pebble.loader.Loader;
import com.mitchellbosecke.pebble.template.PebbleTemplate;

import groovy.lang.GroovyShell;
import groovy.lang.Script;

public class XlTemplating2 {

    private static Logger log = LoggerFactory.getLogger(XlTemplating2.class);

    public void execute(String excelFilePath, String templateDirPathStr, String outputDirPathStr) {

        // Update XlBeanFactory to be able to execute Groovy script defined as `key`
        XlBeanFactory.setInstance(new XlTemplatingBeanFactory());

        final XlBean bean = loadXlBean(excelFilePath);
        System.out.println(bean);

        executePreexec(bean);

        System.out.println(bean);

        Path templateDirPath = Paths.get(templateDirPathStr).toAbsolutePath();
        Path outputDirPath = Paths.get(outputDirPathStr).toAbsolutePath();

        try {
            Files
                .walk(templateDirPath)
                .forEach(path -> this.controller(path, templateDirPath, outputDirPath, bean));
            // .map(path -> resolveTemplateString(path, bean))
            // .map(path -> outputDirPath.resolve(templateDirPath.relativize(path)))
            // .forEach(this::controller);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void executePreexec(XlBean xlbean) {
        log.info("EXECUTE PREEXEC script");
        try {
            GroovyShell shell = new GroovyShell();
            Script script = shell.parse(new File("C:\\Users\\tanikawa\\git\\xltemplating\\template\\preexec.txt"));
            xlbean.entrySet().forEach(entry -> script.setProperty(entry.getKey(), entry.getValue()));
            script.run();
        } catch (CompilationFailedException | IOException e) {
            throw new RuntimeException(e);
        }
        log.info("PREEXEC script Completed");
    }

    private void controller(Path templateFilePath, Path templateDirPath, Path outputDirPath, XlBean bean) {
        if (Files.isDirectory(templateFilePath)) {
            createDirectory(templateFilePath, templateDirPath, outputDirPath, bean);
        } else {
            createFile(templateFilePath, templateDirPath, outputDirPath, bean);
        }
    }

    private void createFile(Path templateFilePath, Path templateDirPath, Path outputDirPath, XlBean bean) {

        if (templateFilePath.toString().endsWith(bean.value("templateExtention"))) {
            System.out.println("THIS IS TEMPLATE: " + templateFilePath);

            new Template(templateFilePath, templateDirPath, outputDirPath, bean).execute();

        } else {
            Path templateResolvedPath = Paths.get(
                resolveTemplateString(templateFilePath.toAbsolutePath().toString(), bean));
            Path targetPath = outputDirPath.resolve(templateDirPath.relativize(templateResolvedPath));
            System.out.println("CREATE FILE: " + targetPath);
            try {
                Files.copy(templateFilePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private void createDirectory(Path templateFilePath, Path templateDirPath, Path outputDirPath, XlBean bean) {
        try {
            Path templateResolvedPath = Paths.get(
                resolveTemplateString(templateFilePath.toAbsolutePath().toString(), bean));
            Path targetPath = outputDirPath.resolve(templateDirPath.relativize(templateResolvedPath));
            System.out.println("CREATE DIRECTORY: " + targetPath);
            Files.createDirectories(targetPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static String resolveTemplateString(String path, Map<String, ?> bean) {
        String templatePath = "temp";

        PebbleStringLoader stringLoader = new PebbleStringLoader();
        stringLoader.putTemplate(templatePath, path);

        List<Loader<?>> defaultLoadingStrategies = new ArrayList<>();
        defaultLoadingStrategies.add(stringLoader);
        Loader<?> loader = new DelegatingLoader(defaultLoadingStrategies);

        PebbleEngine engine = new PebbleEngine.Builder().loader(loader).build();

        StringWriter writer = new StringWriter();
        try {
            PebbleTemplate pebbleTemplate = engine.getTemplate(templatePath);
            pebbleTemplate.evaluate(writer, (Map<String, Object>) bean);
        } catch (PebbleException | IOException e) {
            throw new RuntimeException(e);
        }

        return writer.toString();
    }

    private XlBean loadXlBean(String excelFilePath) {
        // Read Excel data by XlBean
        XlBeanReader reader = new XlBeanReader();
        log.info("Excel file path: {}", new File(excelFilePath).getAbsolutePath());
        return reader.read(new File(excelFilePath));
    }

}
