package com.xlbean.xltemplating;

import org.kohsuke.args4j.Option;

public class ParsedArgs {

	@Option(name="-f", aliases="--excelFile", usage="Path to Excel definition file")
	private String excelFilePath = "definitions.xlsx";

    @Option(name="-t", aliases="--templatingEngineFactory", usage="Fully Qualified Class Name of template engine factory class")
    private String templatingEngineFQCN = "com.xlbean.xltemplating.engine.pebble.PebbleEngineFactory";

    @Option(name="-h", aliases="--help", usage="Print usage message and exit")
    private boolean help = false;

	public String getExcelFilePath() {
		return excelFilePath;
	}

	public void setExcelFilePath(String excelFilePath) {
		this.excelFilePath = excelFilePath;
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
