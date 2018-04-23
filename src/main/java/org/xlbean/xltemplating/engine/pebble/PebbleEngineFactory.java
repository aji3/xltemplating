package org.xlbean.xltemplating.engine.pebble;

import java.io.File;
import java.io.Reader;
import org.xlbean.xltemplating.engine.TemplatingEngine;
import org.xlbean.xltemplating.engine.TemplatingEngineFactory;
import com.mitchellbosecke.pebble.error.LoaderException;
import com.mitchellbosecke.pebble.loader.FileLoader;
import com.mitchellbosecke.pebble.loader.Loader;

public class PebbleEngineFactory extends TemplatingEngineFactory {

    @Override
    public TemplatingEngine createEngine(String rootPath) {
        Loader<String> loader = new ExtendedFileLoader(rootPath);
        com.mitchellbosecke.pebble.PebbleEngine engine = new com.mitchellbosecke.pebble.PebbleEngine.Builder()
            .loader(loader)
            .build();

        return new PebbleEngine(engine);
    }

    private static class ExtendedFileLoader extends FileLoader {

        private String prefixForRelativePath;

        public ExtendedFileLoader(String prefixForRelativePath) {
            if (!prefixForRelativePath.endsWith("/")) {
                prefixForRelativePath += "/";
            }
            this.prefixForRelativePath = prefixForRelativePath;
        }

        @Override
        public Reader getReader(String templateName) throws LoaderException {
            if (!new File(templateName).isAbsolute()) {
                templateName = prefixForRelativePath + templateName;
            }
            return super.getReader(templateName);
        }
    }

}
