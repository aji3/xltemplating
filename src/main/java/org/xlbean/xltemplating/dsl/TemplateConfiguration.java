package org.xlbean.xltemplating.dsl;

import java.util.List;
import java.util.Map;

import groovy.lang.Closure;

public class TemplateConfiguration {
    private List<Map<String, Object>> iterator;
    private Closure<String> dir;
    private Closure<String> filename;
    private Closure<Boolean> skip;
    private Closure<Boolean> skipFile;
    private Closure<Boolean> override;

    public List<Map<String, Object>> getIterator() {
        return iterator;
    }

    public void setIterator(List<Map<String, Object>> iterator) {
        this.iterator = iterator;
    }

    public Closure<String> getDir() {
        return dir;
    }

    public void setDir(Closure<String> dir) {
        this.dir = dir;
    }

    public Closure<String> getFilename() {
        return filename;
    }

    public void setFilename(Closure<String> filename) {
        this.filename = filename;
    }

    public Closure<Boolean> getSkip() {
        return skip;
    }

    public void setSkip(Closure<Boolean> skip) {
        this.skip = skip;
    }

    public Closure<Boolean> getSkipFile() {
        return skipFile;
    }

    public void setSkipFile(Closure<Boolean> skipFile) {
        this.skipFile = skipFile;
    }

    public Closure<Boolean> getOverride() {
        return override;
    }

    public void setOverride(Closure<Boolean> override) {
        this.override = override;
    }
}
