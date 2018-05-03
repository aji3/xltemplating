package org.xlbean.xltemplating.core;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.xlbean.XlBean;
import org.xlbean.xltemplating.engine.TemplatingEngine;

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
    private XlBean xlbean;
    private String preScriptName = DEFAULT_PRESCRIPT_NAME;
    /**
     * Root directory of template files and directories
     */
    private Path templateRootDir;
    /**
     * Root directory of output files and directories
     */
    private Path outputRootDir;

    private TemplatingContext() {}

    /**
     * Resolve output path from given parameters. It will do the following process:
     * <ol>
     * <li>Evaluate path of the template file as string with xlbean in this context
     * and given bean.</li>
     * <li>Replace tepmlateRootDir with outputRootDir.</li>
     * </ol>
     * 
     * @param templatePath
     * @param bean
     * @return
     */
    public Path resolveOutputPath(Path templatePath, Map<String, Object> bean) {
        return resolveOutputPath(templatePath, null, bean);
    }

    /**
     * Resolve output path from given parameters. It will do the following process:
     * <ol>
     * <li>Evaluate path of the template file as string with xlbean in this context
     * and given bean.</li>
     * <li>Append given outputDir to the path.</li>
     * <li>Replace tepmlateRootDir with outputRootDir.</li>
     * </ol>
     * 
     * @param templatePath
     * @param outputDir
     * @param bean
     * @return
     */
    public Path resolveOutputPath(Path templatePath, Path outputDir, Map<String, Object> bean) {
        Map<String, Object> map = new HashMap<>();
        map.putAll(xlbean);
        if (!xlbean.equals(bean)) {
            map.putAll(bean);
        }

        String templateResolvedPathStr = resolveTemplateString(templatePath.toString(), map);
        Path templateResolvedPath = Paths.get(templateResolvedPathStr);
        Path relativeTemplatePath = templateRootDir.relativize(templateResolvedPath);
        if (outputDir != null) {
            relativeTemplatePath = relativeTemplatePath.resolve(outputDir);
        }
        return outputRootDir.resolve(relativeTemplatePath);
    }

    /**
     * Evaluate {@code path} by templating engine.
     * 
     * @param path
     * @param bean
     * @return
     */
    public String resolveTemplateString(String path, Map<String, Object> bean) {
        return engine.generateString(path, (Map<String, Object>) bean);
    }

    public Path getPreScriptPath() {
        return templateRootDir.resolve(getPreScriptName());
    }

    public boolean isTemplate(Path path) {
        return path.toString().endsWith(getTemplateExtension());
    }

    public String getTemplateExtension() {
        String ret = xlbean.value(XLBEAN_KEY_TEMPLATE_EXTENTION);
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

    public XlBean getXlbean() {
        return xlbean;
    }

    public String getPreScriptName() {
        return preScriptName;
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

        public Builder xlbean(XlBean xlbean) {
            context.xlbean = xlbean;
            return this;
        }

        public TemplatingContext build() {
            return context;
        }
    }
}
