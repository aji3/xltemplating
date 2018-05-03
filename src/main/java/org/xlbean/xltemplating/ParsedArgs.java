package org.xlbean.xltemplating;

import org.kohsuke.args4j.Option;
import org.xlbean.xltemplating.table.XlTableTemplating;

public class ParsedArgs {

    @Option(name = "-f", aliases = "--excelFile", usage = "Path to Excel definition file")
    private String excelFilePath = "definitions.xlsx";

    @Option(name = "-t", aliases = "--templateDir", usage = "Path to template root directory")
    private String templateDirectoryPath = "./template";

    @Option(name = "-o", aliases = "--outputDir", usage = "Path to output directory")
    private String outputDirectoryPath = "./output";

    @Option(name = "-e", aliases = "--templatingEngineFactory",
            usage = "Fully Qualified Class Name of template engine factory class")
    private String templatingEngineFQCN = XlTableTemplating.DEFAULT_TEMPLATING_ENGINE_FACTORY.getClass().getName();

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

}
