package org.xlbean.xltemplating.core;

import java.nio.file.Path;

public interface TemplateGenerator {

    void execute(Path templateFilePath, TemplatingContext context);
}
