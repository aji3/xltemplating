package org.xlbean.xltemplating.engine.pebble;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import com.mitchellbosecke.pebble.error.LoaderException;
import com.mitchellbosecke.pebble.loader.FileLoader;

public class PebbleStringLoader extends FileLoader {

    private Map<String, String> templateMap = new HashMap<>();

    @Override
    public Reader getReader(String templatePath) throws LoaderException {
        if (!templateMap.containsKey(templatePath)) {
            return null;
        }
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(templateMap.get(templatePath).getBytes(getCharset()));
            return new InputStreamReader(bais);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public void putTemplate(String templatePath, String contents) {
        templateMap.put(templatePath, contents);
    }

}
