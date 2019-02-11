package org.xlbean.xltemplating.core;

import java.nio.file.Path;
import java.util.Map;

public interface TemplatePathResolver {

    public void init(TemplatingContext context);

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
    public Path resolveOutputPath(Path templatePath, TemplatingContext context);

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
    public Path resolveOutputPath(
            Path templatePath,
            Path outputDir,
            Map<String, Object> bean,
            TemplatingContext context);

}
