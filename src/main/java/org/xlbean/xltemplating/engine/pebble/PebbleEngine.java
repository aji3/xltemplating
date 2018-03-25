package org.xlbean.xltemplating.engine.pebble;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Map;
import org.xlbean.xltemplating.engine.TemplatingEngine;
import org.xlbean.xltemplating.engine.TemplatingException;
import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.loader.FileLoader;
import com.mitchellbosecke.pebble.template.PebbleTemplate;

public class PebbleEngine implements TemplatingEngine {

    @Override
    public void generate(Path templateFile, Path outputFile,
            Map<String, Object> templateEngineContext) {
        com.mitchellbosecke.pebble.PebbleEngine engine =
                new com.mitchellbosecke.pebble.PebbleEngine.Builder()
                        .loader(new FileLoader())
                        .build();

        try {
            PebbleTemplate compiledTemplate = engine.getTemplate(templateFile.toString());
            Writer writer = new FileWriter(outputFile.toFile());
            compiledTemplate.evaluate(writer, templateEngineContext);
        } catch (PebbleException | IOException e) {
            throw new TemplatingException(e);
        }

    }

}
