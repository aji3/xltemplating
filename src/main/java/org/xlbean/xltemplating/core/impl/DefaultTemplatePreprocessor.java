package org.xlbean.xltemplating.core.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

import org.codehaus.groovy.control.CompilationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xlbean.xlscript.processor.AbstractXlScriptProcessor.XlScriptBindingsBuilder;
import org.xlbean.xlscript.script.XlScript;
import org.xlbean.xlscript.script.XlScriptFactory;
import org.xlbean.xltemplating.core.TemplatePreprocessor;
import org.xlbean.xltemplating.core.TemplatingContext;

public class DefaultTemplatePreprocessor implements TemplatePreprocessor {

    private static Logger log = LoggerFactory.getLogger(DefaultTemplatePreprocessor.class);

    @Override
    public void init(TemplatingContext context) {
        // no operation
    }

    /**
     * Execute pre.groovy with all of the properties in xlbean to be set as property
     * of the script. For instance, if xlbean.value("some_key") returns "xxx" then
     * "some_key" is accessible from pre.groovy.
     * 
     * @param context
     */
    public void execute(TemplatingContext context) {
        if (!Files.exists(context.getPreScriptPath())) {
            log.info("PRE script doesn't exist");
            return;
        }
        log.info("EXECUTE PRE script");
        try {
            Map<String, Object> map = new XlScriptBindingsBuilder().excel(context.getExcel()).build();
            String scriptText = new String(Files.readAllBytes(context.getPreScriptPath()));
        	XlScriptFactory factory = new XlScriptFactory();
            XlScript script = factory.getXlScript(scriptText);
            script.execute(map);
        } catch (CompilationFailedException | IOException e) {
            throw new RuntimeException(e);
        }
        log.info("PRE script Completed");
        log.trace("{}", context.getExcel());
    }
}
