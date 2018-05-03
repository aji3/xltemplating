package org.xlbean.xltemplating.dsl;

import java.util.List;
import java.util.Map;

import groovy.lang.Closure;
import groovy.lang.Script;

/**
 * DSL base class for template. This class defines basic structure of DSL.
 * 
 * <p>
 * Basically result of DSL is stored in {@code config}.
 * </p>
 * 
 * @author tanikawa
 *
 */
public abstract class TemplateDsl extends Script {

    private TemplateConfiguration config = new TemplateConfiguration();

    public void iterator(Closure<List<Map<String, Object>>> iterator) {
        config.setIterator(iterator.call());
    }

    public TemplateConfiguration getConfig() {
        return config;
    }

    public void output(Closure<OutputDelegate> cl) {
        cl.setDelegate(new OutputDelegate());
        cl.call();
    }

    public class OutputDelegate {
        public void dir(Closure<String> cl) {
            config.setDir(cl);
        }

        public void filename(Closure<String> cl) {
            config.setFilename(cl);
        }

        public void skip(Closure<Boolean> cl) {
            config.setSkip(cl);
        }

        public void skipFile(Closure<Boolean> cl) {
            config.setSkipFile(cl);
        }

        public void override(Closure<Boolean> cl) {
            config.setOverride(cl);
        }
    }
}
