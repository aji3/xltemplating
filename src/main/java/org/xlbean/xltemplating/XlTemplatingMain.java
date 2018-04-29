package org.xlbean.xltemplating;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

public class XlTemplatingMain {

    public static void main(String[] args) {
        ParsedArgs arguments = new ParsedArgs();

        CmdLineParser parser = new CmdLineParser(arguments);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            e.printStackTrace();
            return;
        }
        if (arguments.isHelp()) {
            parser.printUsage(System.out);
            return;
        }

        // XlTemplating templating = new XlTemplating();
        // templating.initialize(arguments.getTemplatingEngineFQCN());
        // templating.execute(arguments.getExcelFilePath());

        XlTemplating2 templating = new XlTemplating2();
        // templating.initialize(arguments.getTemplatingEngineFQCN());
        templating.execute(
            arguments.getExcelFilePath(),
            arguments.getTemplateDirectoryPath(),
            arguments.getOutputDirectoryPath());
    }

}
