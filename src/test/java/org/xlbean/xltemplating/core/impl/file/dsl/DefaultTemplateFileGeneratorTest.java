package org.xlbean.xltemplating.core.impl.file.dsl;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.xlbean.xltemplating.core.TemplatePathResolver;
import org.xlbean.xltemplating.core.TemplatingContext;
import org.xlbean.xltemplating.core.impl.file.DefaultTemplateFileGenerator;
import org.xlbean.xltemplating.engine.TemplatingEngine;

public class DefaultTemplateFileGeneratorTest {

    @Test
    public void test() {

        TemplatingEngine engine = new TestTemplatingEngine();
        TemplatePathResolver pathResolver = new TestTemplatePathResolver();
        DefaultTemplateFileGenerator generator = new DefaultTemplateFileGenerator(
            engine,
            pathResolver);

        Map<String, Object> excel = new HashMap<>();
        TemplatingContext context = new TemplatingContext.Builder()
            .excel(excel)
            .engine(engine)
            .pathResolver(pathResolver)
            .build();
        generator.execute(Paths.get("test/DefaultTemplateFileDslTest/test.xltmp"), context);
    }

    private class TestTemplatingEngine implements TemplatingEngine {

        @Override
        public void generate(Path templateFile, Path outputFile, Map<String, Object> templateEngineContext) {
            // TODO Auto-generated method stub

        }

        @Override
        public String generateString(String templateStr, Map<String, Object> templateEngineContext) {
            // TODO Auto-generated method stub
            return null;
        }

    }

    private class TestTemplatePathResolver implements TemplatePathResolver {

        @Override
        public void init(TemplatingContext context) {
            // TODO Auto-generated method stub

        }

        @Override
        public Path resolveOutputPath(Path templatePath, TemplatingContext context) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Path resolveOutputPath(Path templatePath, Path outputDir, Map<String, Object> bean,
                TemplatingContext context) {
            // TODO Auto-generated method stub
            return null;
        }

    }
}
