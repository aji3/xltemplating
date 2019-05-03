package org.xlbean.xltemplating;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.xlbean.xltemplating.core.TemplatingContext;
import org.xlbean.xltemplating.core.TemplatingLifecycle;
import org.xlbean.xltemplating.validation.StartUpValidator;
import org.xlbean.xltemplating.validation.StartUpValidator.ValidationResult;

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
        
        StartUpValidator validator = new StartUpValidator();
        ValidationResult result = validator.validate(arguments);
        if (result.isError()) {
        	result.getErrors().forEach(System.err::println);
        	return;
        }

        TemplatingContext context = new TemplatingContextInitializer().initializeContext(arguments);

        new TemplatingLifecycle().execute(context);
    }

}
