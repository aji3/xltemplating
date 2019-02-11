package org.xlbean.xltemplating.core.impl;

import java.nio.file.Files;
import java.nio.file.Path;

import org.xlbean.xltemplating.core.TemplateGenerator;
import org.xlbean.xltemplating.core.TemplateGeneratorProvider;
import org.xlbean.xltemplating.core.TemplatingContext;
import org.xlbean.xltemplating.core.impl.file.DefaultTemplateFileGenerator;
import org.xlbean.xltemplating.core.impl.folder.DefaultTemplateDirectoryGenerator;

public class DefaultTemplateGeneratorProvider implements TemplateGeneratorProvider {

    private TemplateGenerator directoryGenerator;
    private TemplateGenerator fileGenerator;

    @Override
    public void init(TemplatingContext context) {
        directoryGenerator = new DefaultTemplateDirectoryGenerator(context.getPathResolver());
        fileGenerator = new DefaultTemplateFileGenerator(context.getEngine(), context.getPathResolver());
    }

    @Override
    public TemplateGenerator getTemplateGenerator(Path templateFilePath) {
        if (Files.isDirectory(templateFilePath)) {
            return directoryGenerator;
        } else {
            return fileGenerator;
        }
    }

}
