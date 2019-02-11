package org.xlbean.xltemplating.ignore;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.xlbean.xltemplating.ignore.XlTemplatingIgnores.XlTemplatingIgnoresBuilder;

public class XlTemplatingIgnoreLoader {

    private Path ignoreFileDir;
    private Charset charset;

    public XlTemplatingIgnoreLoader(Path ignoreFileDir, Charset charset) {
        this.ignoreFileDir = ignoreFileDir;
        this.charset = charset;
    }

    public XlTemplatingIgnores load() {
        XlTemplatingIgnores ignores = null;
        if (Files.exists(ignoreFileDir)) {
            try {
                List<String> ignoress = Files
                    .readAllLines(ignoreFileDir.resolve(".xltmpignore"), charset)
                    .stream()
                    .map(this::rightTrim)
                    .filter(this::isValidLine)
                    .map(this::unescape)
                    .collect(Collectors.toList());
                ignores = new XlTemplatingIgnoresBuilder()
                    .ignoreFileDir(ignoreFileDir)
                    .ignores(ignoress)
                    .build();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            ignores = new XlTemplatingIgnoresBuilder()
                .ignoreFileDir(ignoreFileDir)
                .build();
        }
        return ignores;
    }

    private boolean isValidLine(String line) {
        if (line == null) {
            return false;
        }
        String trimmed = line.trim();
        if (trimmed.startsWith("#") || trimmed.isEmpty()) {
            return false;
        }
        return true;
    }

    private String rightTrim(String line) {
        int i = line.length() - 1;
        while (i >= 0 && Character.isWhitespace(line.charAt(i))) {
            i--;
        }
        return line.substring(0, i + 1);
    }

    private String unescape(String line) {
        if (line.startsWith("\\#")) {
            line = line.replace("\\#", "#");
        }
        if (line.endsWith("\\")) {
            line = line.replaceAll("\\\\$", "");
        }
        return line;
    }
}
