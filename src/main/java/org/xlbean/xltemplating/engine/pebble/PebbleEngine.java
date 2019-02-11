package org.xlbean.xltemplating.engine.pebble;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.xlbean.xltemplating.engine.TemplatingEngine;
import org.xlbean.xltemplating.engine.TemplatingException;

import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.loader.DelegatingLoader;
import com.mitchellbosecke.pebble.loader.Loader;
import com.mitchellbosecke.pebble.template.PebbleTemplate;

public class PebbleEngine implements TemplatingEngine {

    private com.mitchellbosecke.pebble.PebbleEngine engine;

    public PebbleEngine(com.mitchellbosecke.pebble.PebbleEngine engine) {
        this.engine = engine;
    }

    @Override
    public void generate(Path templateFile,
            Path outputFile,
            Map<String, Object> templateEngineContext) {
        try {
            PebbleTemplate compiledTemplate = engine.getTemplate(templateFile.toString());
            Writer writer = new FileWriter(outputFile.toFile());
            compiledTemplate.evaluate(writer, templateEngineContext);
        } catch (PebbleException | IOException e) {
            throw new TemplatingException(e);
        }

    }

    @Override
    public String generateString(String templateStr, Map<String, Object> templateEngineContext) {
        final String TEMP_TEMPLATE_PATH = "temp";

        PebbleStringLoader stringLoader = new PebbleStringLoader();
        stringLoader.putTemplate(TEMP_TEMPLATE_PATH, templateStr);

        List<Loader<?>> defaultLoadingStrategies = new ArrayList<>();
        defaultLoadingStrategies.add(stringLoader);
        Loader<?> loader = new DelegatingLoader(defaultLoadingStrategies);

        com.mitchellbosecke.pebble.PebbleEngine engine = new com.mitchellbosecke.pebble.PebbleEngine.Builder()
            .loader(loader)
            .build();

        StringWriter writer = new StringWriter();
        try {
            PebbleTemplate pebbleTemplate = engine.getTemplate(TEMP_TEMPLATE_PATH);
            pebbleTemplate.evaluate(writer, templateEngineContext);
        } catch (PebbleException | IOException e) {
            throw new RuntimeException(e);
        }

        return writer.toString();
    }

}
