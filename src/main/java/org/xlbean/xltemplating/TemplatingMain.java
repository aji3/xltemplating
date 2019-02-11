package org.xlbean.xltemplating;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.xlbean.xltemplating.core.TemplatingLifecycle;
import org.xlbean.xltemplating.core.TemplatingContext;

public class TemplatingMain {

    public static void main(String[] args) {
        TemplatingArgs arguments = new TemplatingArgs();

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

        TemplatingContext context = new TemplatingContextInitializer().initializeContext(arguments);

        new TemplatingLifecycle().execute(context);
    }

}
