package org.xlbean.xltemplating;

import java.nio.charset.Charset;

import org.kohsuke.args4j.Option;

public class TemplatingArgs {

    @Option(name = "-f", aliases = "--excelFile", usage = "Path to Excel definition file")
    private String excelFilePath = "definitions.xlsx";

    @Option(name = "-t", aliases = "--templateDir", usage = "Path to template root directory")
    private String templateDirectoryPath = "./template";

    @Option(name = "-o", aliases = "--outputDir", usage = "Path to output directory")
    private String outputDirectoryPath = "./output";

    @Option(name = "-e", aliases = "--templatingEngineFactory",
            usage = "Fully Qualified Class Name of template engine factory class")
    private String templatingEngineFQCN = TemplatingContextInitializer.DEFAULT_TEMPLATING_ENGINE_FACTORY
        .getClass()
        .getName();

    @Option(name = "-c", aliases = "--templateCharset", usage = "Charset of template files")
    private String templateCharset = Charset.defaultCharset().toString();

    @Option(name = "-p", aliases = "--outputCharset", usage = "Charset of output files")
    private String outputCharset = Charset.defaultCharset().toString();

    private String pathResolverFQCN = TemplatingContextInitializer.DEFAULT_TEMPLATE_PATH_RESOLVER.getClass().getName();

    private String templateGeneratorProvider = TemplatingContextInitializer.DEFAULT_TEMPLATE_GENERATOR_PROVIDER
        .getClass()
        .getName();

    private String preprocessor = TemplatingContextInitializer.DEFAULT_TEMPLATE_PREPROCESSOR.getClass().getName();

    @Option(name = "-h", aliases = "--help", usage = "Print usage message and exit")
    private boolean help = false;

    public String getExcelFilePath() {
        return excelFilePath;
    }

    public void setExcelFilePath(String excelFilePath) {
        this.excelFilePath = excelFilePath;
    }

    public String getTemplateDirectoryPath() {
        return templateDirectoryPath;
    }

    public void setTemplateDirectoryPath(String templateDirectoryPath) {
        this.templateDirectoryPath = templateDirectoryPath;
    }

    public String getOutputDirectoryPath() {
        return outputDirectoryPath;
    }

    public void setOutputDirectoryPath(String outputDirectoryPath) {
        this.outputDirectoryPath = outputDirectoryPath;
    }

    public String getTemplatingEngineFQCN() {
        return templatingEngineFQCN;
    }

    public void setTemplatingEngineFQCN(String templatingEngineFQCN) {
        this.templatingEngineFQCN = templatingEngineFQCN;
    }

    public boolean isHelp() {
        return help;
    }

    public void setHelp(boolean help) {
        this.help = help;
    }

    public String getPathResolverFQCN() {
        return pathResolverFQCN;
    }

    public void setPathResolverFQCN(String pathResolverFQCN) {
        this.pathResolverFQCN = pathResolverFQCN;
    }

    public String getTemplateGeneratorProvider() {
        return templateGeneratorProvider;
    }

    public void setTemplateGeneratorProvider(String templateGeneratorProvider) {
        this.templateGeneratorProvider = templateGeneratorProvider;
    }

    public String getPreprocessor() {
        return preprocessor;
    }

    public void setPreprocessor(String preprocessor) {
        this.preprocessor = preprocessor;
    }

    public String getTemplateCharset() {
        return templateCharset;
    }

    public void setTemplateCharset(String templateCharset) {
        this.templateCharset = templateCharset;
    }

    public String getOutputCharset() {
        return outputCharset;
    }

    public void setOutputCharset(String outputCharset) {
        this.outputCharset = outputCharset;
    }

}
