package org.xlbean.xltemplating.core;

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
import org.xlbean.xltemplating.dsl.TemplateConfiguration;
import org.xlbean.xltemplating.dsl.TemplateDsl;

import groovy.lang.GroovyShell;
import groovy.lang.Script;

/**
 * Generate output from template file.
 * 
 * <p>
 * 
 * </p>
 * 
 * @author tanikawa
 *
 */
public class FileTemplateGenerator {

    private static Logger log = LoggerFactory.getLogger(FileTemplateGenerator.class);

    /**
     * Iterate over the beans specified by DSL in template file.
     * 
     * @param templateFilePath
     * @param context
     */
    public void execute(Path templateFilePath, TemplatingContext context) {

        Map<String, Object> xlbean = context.getXlbean();

        String dsl = extractDSL(templateFilePath);
        DslHandler dslHandler = new DslHandler();
        dslHandler.initializeDsl(dsl, xlbean);

        for (Map<String, Object> data : dslHandler.iterator(templateFilePath, xlbean)) {
            dslHandler.registerDataToScriptContext(data);

            Path outputDir = context.resolveOutputPath(
                templateFilePath.getParent(),
                Paths.get(dslHandler.outputDir(templateFilePath)),
                data);
            Path outputFile = outputDir.resolve(dslHandler.outputFileName(templateFilePath));

            if (dslHandler.skip()) {
                log.info("Skip: {}", outputFile);
                continue;
            }

            createOutputDirs(outputDir);

            if (Files.exists(outputFile) && !dslHandler.override()
                    || dslHandler.skipFile()) {
                // if output file already exists and this file is not "override"
                log.info("Skip: {}", outputFile);
                continue;
            }

            generateFile(templateFilePath, outputFile, data, context);

            dslHandler.unregisterDataFromScriptContext(data);
        }
    }

    private void createOutputDirs(Path outputDir) {
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

    private void generateFile(
            Path templateFilePath,
            Path outputFile,
            Map<String, ?> data,
            TemplatingContext context) {
        Map<String, Object> templateEngineContext = new HashMap<>();
        templateEngineContext.put("xlbean", context.getXlbean());
        templateEngineContext.putAll(data);
        // "_it" is a reserved word for accessing data object from template
        templateEngineContext.put("_it", data);

        log.info("Generate: {}", outputFile);
        log.trace("Data: {}", templateEngineContext.toString().replaceAll("[\r\n]", ""));

        // Execute Templating Engine
        context.getEngine().generate(templateFilePath, outputFile, templateEngineContext);

    }

    /**
     * 
     * 
     * @author tanikawa
     *
     */
    private static class DslHandler {

        private Script script;
        private TemplateConfiguration scriptConfig;

        private enum ScriptMode {
            DataUnregistered, DataRegistered,
        };

        private ScriptMode mode = ScriptMode.DataUnregistered;

        /**
         * Run the DSL string given as argument under the Script Base Class
         * {@link TemplateDsl}.
         * 
         * @param dsl
         * @param xlbean
         */
        public void initializeDsl(String dsl, Map<String, Object> xlbean) {
            if (dsl != null) {
                CompilerConfiguration compilerConfig = new CompilerConfiguration();
                compilerConfig.setScriptBaseClass(TemplateDsl.class.getName());

                GroovyShell shell = new GroovyShell(compilerConfig);
                script = shell.parse(dsl);
                script.setProperty("xlbean", xlbean);
                script.run();
                scriptConfig = (TemplateConfiguration) script.getProperty("config");
            }
        }

        public void registerDataToScriptContext(Map<String, ?> data) {
            if (script != null) {
                data
                    .entrySet()
                    .stream()
                    .forEach(
                        entry -> script.getBinding().setProperty(entry.getKey(), entry.getValue()));
                script.getBinding().setProperty("_it", data);
            }
            mode = ScriptMode.DataRegistered;
        }

        public void unregisterDataFromScriptContext(Map<String, ?> data) {
            if (script != null) {
                data
                    .keySet()
                    .stream()
                    .forEach(key -> script.getBinding().setProperty(key, null));
                script.getBinding().setProperty("_it", null);
            }
            mode = ScriptMode.DataUnregistered;
        }

        /**
         * 
         * 
         * @param templateFilePath
         * @param xlbean
         * @return
         */
        public List<Map<String, Object>> iterator(Path templateFilePath, Map<String, Object> xlbean) {
            List<Map<String, Object>> ret = null;
            if (scriptConfig != null) {
                ret = scriptConfig.getIterator();
            }
            if (ret == null) {
                ret = Arrays.asList(xlbean);
            }
            return ret;
        }

        public String outputDir(Path templateFilePath) {
            if (mode != ScriptMode.DataRegistered) {
                throw new RuntimeException("Illegal status error");
            }
            if (scriptConfig == null || scriptConfig.getDir() == null) {
                return "";
            } else {
                return scriptConfig.getDir().call();
            }
        }

        public String outputFileName(Path templateFilePath) {
            if (mode != ScriptMode.DataRegistered) {
                throw new RuntimeException("Illegal status error");
            }
            if (scriptConfig == null || scriptConfig.getFilename() == null) {
                return templateFilePath.getFileName().toString().replace(".xtmpl", "");
            } else {
                return scriptConfig.getFilename().call();
            }
        }

        public boolean skip() {
            if (mode != ScriptMode.DataRegistered) {
                throw new RuntimeException("Illegal status error");
            }
            if (scriptConfig == null || scriptConfig.getSkip() == null) {
                return false;
            } else {
                return scriptConfig.getSkip().call();
            }
        }

        public boolean skipFile() {
            if (mode != ScriptMode.DataRegistered) {
                throw new RuntimeException("Illegal status error");
            }
            if (scriptConfig == null || scriptConfig.getSkipFile() == null) {
                return false;
            } else {
                return scriptConfig.getSkipFile().call();
            }
        }

        public boolean override() {
            if (mode != ScriptMode.DataRegistered) {
                throw new RuntimeException("Illegal status error");
            }
            if (scriptConfig == null || scriptConfig.getOverride() == null) {
                return true;
            } else {
                return scriptConfig.getOverride().call();
            }
        }

    }

}
