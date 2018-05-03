package org.xlbean.xltemplating.engine;

import java.nio.file.Path;
import java.util.Map;

/**
 * Interface for template engine wrapper.
 * 
 * @author tanikawa
 *
 */
public interface TemplatingEngine {

    /**
     * Using {@code tmeplateFile}, run the template engine with the
     * {@code templateEngineContext} and generate {@code outputFile}.
     * 
     * @param templateFile
     * @param outputFile
     * @param templateEngineContext
     */
    public void generate(Path templateFile, Path outputFile, Map<String, Object> templateEngineContext);

    /**
     * Using {@code templateStr} as a template, run the template engine with
     * {@code templateEngineContext} and return generated string.
     * 
     * @param templateString
     * @param templateEngineContext
     * @return
     */
    public String generateString(String templateStr, Map<String, Object> templateEngineContext);
}
