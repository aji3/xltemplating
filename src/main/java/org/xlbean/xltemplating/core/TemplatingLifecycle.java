package org.xlbean.xltemplating.core;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xlbean.xltemplating.ignore.XlTemplatingIgnoreLoader;
import org.xlbean.xltemplating.ignore.XlTemplatingIgnores;
import org.xlbean.xltemplating.ignore.XlTemplatingIgnores.XlTemplatingIgnoresBuilder;

/**
 * 
 * 
 * @author tanikawa
 */
public class TemplatingLifecycle {

    private static Logger log = LoggerFactory.getLogger(TemplatingLifecycle.class);

    /**
     * Walk through files and sub directories in the {@code templateDirPathStr}, and
     * does the following processing:
     * <ul>
     * <li>If it is a file, it checks file extension if it matches
     * "templateExtention" value defined in the excel sheet. If it matches, then run
     * templating logic with the file, otherwise, it simply copy the file to
     * {@code outputDirPathStr}</li>
     * <li>If it is a directory, the full path of the directory will be handled by
     * templating engine</li>
     * </ul>
     * 
     * @param excelFilePath
     * @param templateDirPathStr
     * @param outputDirPathStr
     */
    public void execute(TemplatingContext context) {

        context.getPreprocessor().execute(context);

        walkThroughTemplates(context);
    }

    public List<Path> getTargetTemplates(TemplatingContext context) {
        XlTemplatingIgnores ignores = new XlTemplatingIgnoresBuilder()
            .ignoreFileDir(context.getTemplateRootDir())
            .build();
        return walk(context.getTemplateRootDir(), ignores);
    }

    /**
     * Walk through all template directories and files and generate output.
     * 
     * @param context
     */
    private void walkThroughTemplates(TemplatingContext context) {
        List<Path> paths = getTargetTemplates(context);
        paths.forEach(p -> generate(p, context));
    }

    private List<Path> walk(Path path, XlTemplatingIgnores ignores) {
        List<Path> paths = new ArrayList<>();
        walk(path, ignores, paths);
        return paths;
    }

    private List<Path> walk(Path path, XlTemplatingIgnores ignores, List<Path> paths) {
        if (!Files.exists(path)) {
            return paths;
        }
        if (ignores.isNotIgnore(path)) {
            paths.add(path);
        } else {
            return paths;
        }
        if (Files.isDirectory(path)) {
            Path ignoreFile = path.resolve(".xltmpignore");
            if (Files.exists(ignoreFile)) {
                // if another ignore file exists, then load the file and add it to the ignores
                // and walk through the child folders.
                XlTemplatingIgnoreLoader loader = new XlTemplatingIgnoreLoader(path, Charset.forName("utf-8"));
                XlTemplatingIgnores newIgnores = loader.load();
                ignores.merge(newIgnores);
                try {
                    Files.list(path).forEach(p -> walk(p, ignores, paths));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                // after walking through child folders, remove this ignores.
                ignores.remove(newIgnores);
            } else {
                try {
                    Files.list(path).forEach(p -> walk(p, ignores, paths));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return paths;
    }

    /**
     * Delegates processing to appropriate method based on whether it is directory
     * or file.
     * 
     * @param templateFilePath
     * @param context
     */
    private void generate(Path templateFilePath, TemplatingContext context) {
        log.info("START: {}", templateFilePath);
        try {
            context.getGeneratorProvider().getTemplateGenerator(templateFilePath).execute(templateFilePath, context);
        } finally {
            log.info("END: {}", templateFilePath);
        }
    }

}
