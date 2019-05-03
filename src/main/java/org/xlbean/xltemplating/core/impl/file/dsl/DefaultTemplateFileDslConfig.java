package org.xlbean.xltemplating.core.impl.file.dsl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import groovy.lang.Closure;

/**
 * DSL base class for template. This class defines basic structure of DSL.
 * 
 * <p>
 * Result of DSL is stored in {@code config}.
 * </p>
 * 
 * @author tanikawa
 */
public class DefaultTemplateFileDslConfig {

    public static final String RESERVEDKEY_LOOP_CURRENT_INDEX = "$index";

    private Map<String, Object> excel;
    private List<Map<String, Object>> iterator;
    private Closure<?> outputClosure;

    public DefaultTemplateFileDslConfig(Map<String, Object> excel) {
        this.excel = excel;
    }

    /**
     * Evaluate {@code closure} right away and set respond value to iterator.
     * 
     * @param closure
     */
    public void iterator(Closure<List<Map<String, Object>>> closure) {
        this.iterator = closure.call();
    }

    public void output(Closure<?> cl) {
        outputClosure = cl;
    }

    public class OutputDelegate {

        private OutputConfig config = new OutputConfig();

        public void dir(Closure<?> cl) {
            Object obj = cl.call();
            config.setDir(obj == null ? null : obj.toString());
        }

        public void filename(Closure<?> cl) {
            Object obj = cl.call();
            config.setFilename(obj == null ? null : obj.toString());
        }

        public void skip(Closure<Boolean> cl) {
            config.setSkip(cl.call());
        }

        public void skipFile(Closure<Boolean> cl) {
            config.setSkipFile(cl.call());
        }

        public void override(Closure<Boolean> cl) {
            config.setOverride(cl.call());
        }

        public OutputConfig getConfig() {
            return config;
        }
    }

    public static class OutputConfig {
        private String dir;
        private String filename;
        private Boolean skip = false;
        private Boolean skipFile = false;
        private Boolean override = false;

        public String getDir() {
            return dir;
        }

        public void setDir(String dir) {
            this.dir = dir;
        }

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        /**
         * If true, skip to create directory or file.
         * 
         * @return
         */
        public Boolean isSkip() {
            return skip;
        }

        public void setSkip(Boolean skip) {
            this.skip = skip;
        }

        /**
         * When true, folder for this file will be created but file itself will not be
         * created.
         * 
         * @return
         */
        public Boolean isSkipFile() {
            return skipFile;
        }

        public void setSkipFile(Boolean skipFile) {
            this.skipFile = skipFile;
        }

        /**
         * If true, overrides output file if it already exists.
         * 
         * @return
         */
        public Boolean isOverride() {
            return override;
        }

        public void setOverride(Boolean override) {
            this.override = override;
        }
    }

    /**
     * Evaluate "output" closure with given {@code data}.
     * 
     * <p>
     * If "output" closure is not set, returns blank OutputConfig instance.
     * </p>
     * 
     * @param data
     * @return
     */
    public OutputConfig getOutputConfig(Map<String, Object> data) {
        if (outputClosure == null) {
            return new OutputConfig();
        }
        if (data != null) {
            data.forEach((key, value) -> outputClosure.setProperty(key, value));
        }
        OutputDelegate delegate = new OutputDelegate();
        outputClosure.setDelegate(delegate);
        outputClosure.call();
        return delegate.getConfig();
    }

    public List<Map<String, Object>> getIterator() {
        if (iterator == null) {
            return Arrays.asList(excel);
        }
        return iterator;
    }

}
