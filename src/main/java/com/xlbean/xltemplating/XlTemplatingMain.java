package com.xlbean.xltemplating;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.xlbean.XlBean;
import org.xlbean.XlList;
import org.xlbean.reader.XlBeanReader;
import org.xlbean.util.XlBeanFactory;

import com.xlbean.xltemplating.engine.TemplatingEngine;
import com.xlbean.xltemplating.engine.TemplatingEngineFactory;
import com.xlbean.xltemplating.script.ScriptHelper;
import com.xlbean.xltemplating.xlbean.XlTemplatingBeanFactory;

public class XlTemplatingMain {
	private TemplatingEngineFactory templatingEngineFactory;

	public static void main(String[] args) {
		new XlTemplatingMain().start(args);
	}

	private void initializeFactory(String className) {
		try {
			Class<?> factoryClass = Class.forName(className);
			if (factoryClass.isAssignableFrom(TemplatingEngineFactory.class)) {
				templatingEngineFactory = (TemplatingEngineFactory) factoryClass.newInstance();
			}
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			// meaning the class does not set to class path, which is OK
		}
	}

	public void start(String[] args) {
		Args arguments = new Args();

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
        
		initializeFactory(arguments.getTemplatingEngineFQCN());

		// Override XlBeanFactory
		XlBeanFactory.setInstance(new XlTemplatingBeanFactory());

		// Read Excel data by XlBean
		XlBeanReader reader = new XlBeanReader();
		XlBean bean = reader.read(new File(arguments.getExcelFilePath()));

		// Execute pre-execute script
		XlList scriptList = bean.list("preExecute");
		if (scriptList != null) {
			scriptList.forEach(script -> {
				ScriptHelper.getInstance().execute(script.value("logic"), bean);
			});
		}

		Path root = Paths.get(bean.value("rootPath"));
		for (XlBean file : bean.list("files")) {
			if (file == null || !Boolean.valueOf(file.value("generate"))) {
				continue;
			}

			// Read path of templates and output files.
			Path templateDir = root.resolve(file.value("templateDirPath"));
			Path templateFile = templateDir.resolve(file.value("templateFileName"));
			Path outputDir = root.resolve(file.value("outputDirPath"));
			Path outputFile = outputDir.resolve(file.value("outputFileName"));
			if (!Files.exists(outputDir)) {
				// Create output directory if it doesn't exist.
				try {
					Files.createDirectories(outputDir);
				} catch (IOException e) {
					throw new RuntimeException("File system error occured for creating output directories/files", e);
				}
			}

			// XlBean is set directly to context given to Pebble.
			Map<String, Object> templateEngineContext = new HashMap<>();
			templateEngineContext.putAll(bean);

			XlList customValues = file.list("customValues");
			if (customValues != null) {
				customValues.stream().filter(elem -> elem != null).forEach(customValue -> {
					HashMap<String, Object> map = new HashMap<>();
					map.putAll(bean);
					Object pebbleValue = ScriptHelper.getInstance().execute(customValue.value("value"), map);
					templateEngineContext.put(customValue.value("key"), pebbleValue);
				});
			}

			// Execute Templating Engine
			TemplatingEngine engine = templatingEngineFactory.createEngine();
			engine.generate(templateFile, outputFile, templateEngineContext);
		}
	}
}