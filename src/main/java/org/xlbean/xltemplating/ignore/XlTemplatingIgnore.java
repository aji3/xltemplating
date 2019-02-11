package org.xlbean.xltemplating.ignore;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;

public class XlTemplatingIgnore {

    protected Path ignoreFileDir;

    protected String path;

    protected boolean withSlash = false;

    protected boolean directoryOnly = false;

    protected boolean negate = false;

    public XlTemplatingIgnore(Path templateRootPath, String path) {
        directoryOnly = path.endsWith("/");
        if (directoryOnly) {
            path = path.substring(0, path.length() - 1);
        }
        negate = path.startsWith("!");
        if (negate) {
            path = path.substring(1, path.length());
        }
        withSlash = path.contains("/");
        this.ignoreFileDir = templateRootPath;
        if (withSlash && !path.startsWith("**") && !path.startsWith("/")) {
            path = "/" + path;
        }
        this.path = path;
    }

    public boolean matches(Path path) {
        if (directoryOnly && !Files.isDirectory(path)) {
            return false;
        }
        FileSystem fs = FileSystems.getDefault();
        PathMatcher matcher = fs.getPathMatcher("glob:" + this.path);
        Path targetPath;
        if (withSlash) {
            targetPath = Paths.get("/").resolve(ignoreFileDir.relativize(path));
        } else {
            targetPath = path.getFileName();
        }
        return matcher.matches(targetPath);
    }

    public String getPath() {
        return path;
    }

    public boolean isNegate() {
        return negate;
    }
}
