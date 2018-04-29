package org.xlbean.xltemplating.dsl;

import java.util.List;
import java.util.Map;

import org.xlbean.XlBean;

import groovy.lang.Closure;
import groovy.lang.Script;

public abstract class TemplateDsl extends Script {

    private XlBean xlbean;

    private Configuration config = new Configuration();

    public void iterator(Closure<List<Map<String, ?>>> iterator) {
        config.setIterator(iterator.call());
    }

    public Configuration getConfig() {
        return config;
    }

    public XlBean getXlbean() {
        return xlbean;
    }

    public void setXlbean(XlBean xlbean) {
        this.xlbean = xlbean;
    }

    public void output(Closure<OutputDelegate> cl) {
        cl.setDelegate(new OutputDelegate());
        cl.call();
    }

    public class OutputDelegate {
        public void baseDir(Closure<String> cl) {
            config.setBaseDir(cl);
        }

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
