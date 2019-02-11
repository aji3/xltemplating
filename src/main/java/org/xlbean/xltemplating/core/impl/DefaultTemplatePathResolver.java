package org.xlbean.xltemplating.core.impl;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.xlbean.xltemplating.core.TemplatePathResolver;
import org.xlbean.xltemplating.core.TemplatingContext;
import org.xlbean.xltemplating.engine.TemplatingEngine;

public class DefaultTemplatePathResolver implements TemplatePathResolver {

    private TemplatingEngine engine;

    @Override
    public void init(TemplatingContext context) {
        this.engine = context.getEngine();
    }

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
    @Override
    public Path resolveOutputPath(Path templatePath, TemplatingContext context) {
        return resolveOutputPath(templatePath, null, null, context);
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
    @Override
    public Path resolveOutputPath(
            Path templatePath,
            Path outputDir,
            Map<String, Object> bean,
            TemplatingContext context) {
        Map<String, Object> map = new HashMap<>();
        map.putAll(context.getExcel());
        if (bean != null) {
            map.putAll(bean);
        }

        String templateResolvedPathStr = resolveTemplateString(templatePath.toString(), map, context);
        Path templateResolvedPath = Paths.get(templateResolvedPathStr);
        Path relativeTemplatePath = context.getTemplateRootDir().relativize(templateResolvedPath);
        if (outputDir != null) {
            relativeTemplatePath = relativeTemplatePath.resolve(outputDir);
        }
        return context.getOutputRootDir().resolve(relativeTemplatePath);
    }

    /**
     * Evaluate {@code path} by templating engine.
     * 
     * @param path
     * @param bean
     * @return
     */
    private String resolveTemplateString(String path, Map<String, Object> bean, TemplatingContext context) {
        return engine.generateString(path, (Map<String, Object>) bean);
    }
}
