package org.xlbean.xltemplating.core;

import java.nio.file.Path;

public interface TemplateGeneratorProvider {

    public void init(TemplatingContext context);

    public TemplateGenerator getTemplateGenerator(Path templateFilePath);
}
