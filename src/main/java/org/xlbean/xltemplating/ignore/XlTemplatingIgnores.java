package org.xlbean.xltemplating.ignore;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class XlTemplatingIgnores {

    private List<XlTemplatingIgnore> ignoreLogics;

    public boolean isNotIgnore(Path path) {
        boolean isIgnore = false;
        for (XlTemplatingIgnore ignore : ignoreLogics) {
            if (isIgnore && ignore.isNegate() && ignore.matches(path)) {
                isIgnore = false;
            } else if (!isIgnore && !ignore.isNegate() && ignore.matches(path)) {
                isIgnore = true;
            }
        }
        return !isIgnore;
    }

    public List<XlTemplatingIgnore> getIgnores() {
        return ignoreLogics;
    }

    public void merge(XlTemplatingIgnores newIgnores) {
        ignoreLogics.addAll(newIgnores.getIgnores());
    }

    public void remove(XlTemplatingIgnores ignoresToRemove) {
        ignoreLogics.removeAll(ignoresToRemove.getIgnores());
    }

    public static class XlTemplatingIgnoresBuilder {
        public static final String DEFAULT_XLTMPIGNORE_PATH = "/.xltmpignore";

        private Path ignoreFileDir;
        private List<String> ignores = new ArrayList<>();

        public XlTemplatingIgnoresBuilder ignoreFileDir(Path ignoreFileDir) {
            this.ignoreFileDir = ignoreFileDir;
            return this;
        }

        public XlTemplatingIgnoresBuilder ignores(List<String> ignores) {
            if (ignores != null) {
                this.ignores.addAll(ignores);
                this.ignores.add(DEFAULT_XLTMPIGNORE_PATH);
            }
            return this;
        }

        public XlTemplatingIgnores build() {
            XlTemplatingIgnores ignores = new XlTemplatingIgnores();
            ignores.ignoreLogics = this.ignores
                .stream()
                .map(this::createIgnore)
                .collect(Collectors.toList());
            return ignores;
        }

        private XlTemplatingIgnore createIgnore(String pathStr) {
            return new XlTemplatingIgnore(ignoreFileDir, pathStr);
        }
    }

}
