package org.xlbean.xltemplating.engine.pebble;

import java.io.Reader;
import java.nio.file.Path;

import org.xlbean.xltemplating.engine.TemplatingEngine;
import org.xlbean.xltemplating.engine.TemplatingEngineFactory;

import com.mitchellbosecke.pebble.error.LoaderException;
import com.mitchellbosecke.pebble.loader.FileLoader;
import com.mitchellbosecke.pebble.loader.Loader;

public class PebbleEngineFactory extends TemplatingEngineFactory {

    @Override
    public TemplatingEngine createEngine(Path rootPath) {
        Loader<String> loader = new ExtendedFileLoader(rootPath);
        com.mitchellbosecke.pebble.PebbleEngine engine = new com.mitchellbosecke.pebble.PebbleEngine.Builder()
            .loader(loader)
            .build();

        return new PebbleEngine(engine);
    }

    private static class ExtendedFileLoader extends FileLoader {

        private Path prefixForRelativePath;

        public ExtendedFileLoader(Path prefixForRelativePath) {
            this.prefixForRelativePath = prefixForRelativePath;
        }

        @Override
        public Reader getReader(String templateName) throws LoaderException {
            return super.getReader(prefixForRelativePath.resolve(templateName).toString());
        }
    }

}
