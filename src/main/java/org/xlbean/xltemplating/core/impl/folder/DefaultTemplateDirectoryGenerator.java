package org.xlbean.xltemplating.core.impl.folder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xlbean.xltemplating.core.TemplateGenerator;
import org.xlbean.xltemplating.core.TemplatePathResolver;
import org.xlbean.xltemplating.core.TemplatingContext;

public class DefaultTemplateDirectoryGenerator implements TemplateGenerator {

    private static Logger log = LoggerFactory.getLogger(DefaultTemplateDirectoryGenerator.class);

    private TemplatePathResolver pathResolver;

    public DefaultTemplateDirectoryGenerator(TemplatePathResolver pathResolver) {
        this.pathResolver = pathResolver;
    }

    @Override
    public void execute(Path templateFilePath, TemplatingContext context) {
        Path targetPath = pathResolver.resolveOutputPath(templateFilePath, context);
        log.info("CREATE DIRECTORY: {}", targetPath);
        try {
            Files.createDirectories(targetPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
