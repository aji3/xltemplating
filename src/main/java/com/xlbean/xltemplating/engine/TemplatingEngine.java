package com.xlbean.xltemplating.engine;

import java.nio.file.Path;
import java.util.Map;

public interface TemplatingEngine {

	public void generate(Path templateFile, Path outputFile, Map<String, Object> templateEngineContext);
}
