package com.xlbean.xltemplating;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.xlbean.XlBean;
import org.xlbean.XlList;
import org.xlbean.reader.XlBeanReader;
import org.xlbean.util.XlBeanFactory;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.template.PebbleTemplate;

public class XlTemplatingMain {
	public static void main(String[] args) throws Exception {
		// Create Pebble engine
		PebbleEngine engine = new PebbleEngine.Builder().build();

		// Override XlBeanFactory
		XlBeanFactory.setInstance(new XlTemplatingBeanFactory());

		// Read Excel data by XlBean
		XlBeanReader reader = new XlBeanReader();
		XlBean bean = reader.read(new File("./AppDefinition.xlsx"));

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
				Files.createDirectories(outputDir);
			}

			// XlBean is set directly to context given to Pebble.
			Map<String, Object> pebbleContext = new HashMap<>();
			pebbleContext.putAll(bean);

//			// Set key-values to Pebble context.
//			for (int keyIndex = 0;; keyIndex++) {
//				String key = file.value("key" + keyIndex);
//				String keyValueScript = file.value("value" + keyIndex);
//				if (key != null && !key.isEmpty() && keyValueScript != null && !keyValueScript.isEmpty()) {
//					// Evaluate value then put the result into the Pebble
//					// context.
//				    @SuppressWarnings("serial")
//                    Object pebbleValue = ScriptHelper.getInstance().execute(keyValueScript, new HashMap<String, Object>(){{putAll(bean);}});
//					pebbleContext.put(key, pebbleValue);
//				} else {
//					break;
//				}
//			}
			XlList customValues = file.list("customValues");
			if (customValues != null) {
			    customValues.stream().filter(elem -> elem != null).forEach(customValue -> {
			        HashMap<String, Object> map = new HashMap<>();
			        map.putAll(bean);
                    Object pebbleValue = ScriptHelper.getInstance().execute(customValue.value("value"), map);
    			    pebbleContext.put(customValue.value("key"), pebbleValue);
    			});
			}

			// Execute Pebble
			PebbleTemplate compiledTemplate = engine.getTemplate(templateFile.toString());
			Writer writer = new FileWriter(outputFile.toFile());
			compiledTemplate.evaluate(writer, pebbleContext);
		}
	}
}
