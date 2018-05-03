package org.xlbean.xltemplating;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.xlbean.xltemplating.core.XlTemplating;

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

        new XlTemplating().execute(
            arguments.getTemplatingEngineFQCN(),
            arguments.getExcelFilePath(),
            arguments.getTemplateDirectoryPath(),
            arguments.getOutputDirectoryPath());
    }

}
