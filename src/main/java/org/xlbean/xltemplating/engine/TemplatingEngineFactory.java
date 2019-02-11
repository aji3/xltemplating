package org.xlbean.xltemplating.engine;

import java.nio.file.Path;

public abstract class TemplatingEngineFactory {
    public abstract TemplatingEngine createEngine(Path templateRootPath);
}
