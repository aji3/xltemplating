package org.xlbean.xltemplating.core;

import java.nio.file.Path;

import org.xlbean.XlBean;
import org.xlbean.xltemplating.engine.TemplatingEngine;
import org.xlbean.xltemplating.ignore.XlTemplatingIgnores;

/**
 * Context which is shared across processing.
 * 
 * @author tanikawa
 *
 */
public class TemplatingContext {

    private static final String DEFAULT_PRESCRIPT_NAME = "pre.groovy";
    private static final String XLBEAN_KEY_TEMPLATE_EXTENTION = "templateExtention";

    private TemplatingEngine engine;

    /**
     * XlBean instance of excel file which contains template definition
     */
    private XlBean excel;

    /**
     * Name of script file name which should be executed at the beginning.
     */
    private String preScriptName = DEFAULT_PRESCRIPT_NAME;

    /**
     * Root directory of template files and directories
     */
    private Path templateRootDir;

    /**
     * Root directory of output files and directories
     */
    private Path outputRootDir;

    private TemplatePathResolver pathResolver;

    private TemplatePreprocessor preprocessor;

    private TemplateGeneratorProvider generatorProvider;

    private XlTemplatingIgnores ignores;

    protected TemplatingContext() {}

    public Path getPreScriptPath() {
        return templateRootDir.resolve(getPreScriptName());
    }

    public boolean isTemplate(Path path) {
        return path.toString().endsWith(getTemplateExtension());
    }

    public String getTemplateExtension() {
        String ret = excel.string(XLBEAN_KEY_TEMPLATE_EXTENTION);
        if (ret != null && !ret.isEmpty()) {
            return ret;
        } else {
            return ".xtmpl";
        }
    }

    public TemplatingEngine getEngine() {
        return engine;
    }

    public Path getTemplateRootDir() {
        return templateRootDir;
    }

    public Path getOutputRootDir() {
        return outputRootDir;
    }

    public XlBean getExcel() {
        return excel;
    }

    public String getPreScriptName() {
        return preScriptName;
    }

    public TemplatePathResolver getPathResolver() {
        return pathResolver;
    }

    public static class Builder {
        private TemplatingContext context;

        public Builder() {
            context = new TemplatingContext();
        }

        public Builder engine(TemplatingEngine engine) {
            context.engine = engine;
            return this;
        }

        public Builder templateRootDir(Path templateRootDir) {
            context.templateRootDir = templateRootDir;
            return this;
        }

        public Builder outputRootDir(Path outputRootDir) {
            context.outputRootDir = outputRootDir;
            return this;
        }

        public Builder preScriptName(String preScriptName) {
            context.preScriptName = preScriptName;
            return this;
        }

        public Builder excel(XlBean excel) {
            context.excel = excel;
            return this;
        }

        public Builder pathResolver(TemplatePathResolver pathResolver) {
            context.pathResolver = pathResolver;
            return this;
        }

        public Builder generatorProvider(TemplateGeneratorProvider generatorProvider) {
            context.generatorProvider = generatorProvider;
            return this;
        }

        public Builder preprocessor(TemplatePreprocessor preprocessor) {
            context.preprocessor = preprocessor;
            return this;
        }

        public Builder ignores(XlTemplatingIgnores ignores) {
            context.ignores = ignores;
            return this;
        }

        public TemplatingContext build() {
            return context;
        }
    }

    public TemplatePreprocessor getPreprocessor() {
        return preprocessor;
    }

    public TemplateGeneratorProvider getGeneratorProvider() {
        return generatorProvider;
    }

    public XlTemplatingIgnores getIgnores() {
        return ignores;
    }
}
