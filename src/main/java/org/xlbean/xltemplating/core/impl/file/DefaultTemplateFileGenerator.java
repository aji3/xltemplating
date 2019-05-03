package org.xlbean.xltemplating.core.impl.file;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xlbean.XlBean;
import org.xlbean.util.XlBeanFactory;
import org.xlbean.xlscript.processor.AbstractXlScriptProcessor.XlScriptBindingsBuilder;
import org.xlbean.xlscript.util.JSON;
import org.xlbean.xltemplating.core.TemplateGenerator;
import org.xlbean.xltemplating.core.TemplatePathResolver;
import org.xlbean.xltemplating.core.TemplatingContext;
import org.xlbean.xltemplating.core.impl.file.dsl.DefaultTemplateFileDsl;
import org.xlbean.xltemplating.core.impl.file.dsl.DefaultTemplateFileDslConfig;
import org.xlbean.xltemplating.core.impl.file.dsl.DefaultTemplateFileDslConfig.OutputConfig;
import org.xlbean.xltemplating.engine.TemplatingEngine;
import org.xlbean.xltemplating.exception.XlTemplatingDslException;
import org.xlbean.xltemplating.exception.XlTemplatingException;

/**
 * Generate output from template file.
 * 
 * <p>
 * 
 * </p>
 * 
 * @author tanikawa
 *
 */
public class DefaultTemplateFileGenerator implements TemplateGenerator {

	private static Logger log = LoggerFactory.getLogger(DefaultTemplateFileGenerator.class);

	private TemplatingEngine engine;

	private TemplatePathResolver pathResolver;

	public DefaultTemplateFileGenerator(TemplatingEngine engine, TemplatePathResolver pathResolver) {
		this.engine = engine;
		this.pathResolver = pathResolver;
	}

	/**
	 * Iterate over the beans specified by DSL in template file.
	 * 
	 * @param templateFilePath
	 * @param context
	 */
	@Override
	public void execute(Path templateFilePath, TemplatingContext context) {
		if (context.isTemplate(templateFilePath)) {
			generateFromDsl(templateFilePath, context);
		} else {
			copyFile(templateFilePath, context);
		}
	}

	private void generateFromDsl(Path templateFilePath, TemplatingContext context) {
		Map<String, Object> excel = context.getExcel();

		log.info("- START template file. {}", templateFilePath);

		String dslString = readDSLStringFromTemplateFile(templateFilePath);
		log.debug("-- Template has DSL -> {}", dslString != null);
		DefaultTemplateFileDslConfig dslConfig = new DefaultTemplateFileDsl().evaluate(dslString, excel);

		int iteration = -1;
		for (Map<String, Object> data : dslConfig.getIterator()) {
			iteration++;

			log.info("-- ITERATION {} : START", iteration);

			if (log.isDebugEnabled()) {
				log.debug("--- DATA: {}" ,JSON.stringify(data));
			}

			OutputConfig outputConfig = dslConfig.getOutputConfig(data);

			if (log.isDebugEnabled()) {
				XlBean bean = XlBeanFactory.getInstance().createBean();
				bean.set(outputConfig);
				log.debug("--- OUTPUT CONFIG: {}", JSON.stringify(bean));
			}

			Path outputDir = pathResolver.resolveOutputPath(templateFilePath.getParent(),
					Paths.get(outputConfig.getDir() == null ? "" : outputConfig.getDir()), data, context);
			Path outputFile = outputDir.resolve(outputConfig.getFilename() == null
					? templateFilePath.getFileName().toString().replace(context.getTemplateExtension(), "")
					: outputConfig.getFilename());

			if (outputConfig.isSkip()) {
				log.info("--- RESULT: SKIP - skip=true.");
				log.info("-- ITERATION {} : END", iteration);
				continue;
			}

			createOutputDirs(outputDir);

			if (outputConfig.isSkipFile()) {
				log.info("--- RESULT: SKIP - skipFile=true");
				log.info("-- ITERATION {} : END", iteration);
				continue;
			}
			if (Files.exists(outputFile) && !outputConfig.isOverride()) {
				// if output file already exists and this file is not "override"
				log.info("--- RESULT: SKIP - target file already exists and override=false.");
				log.info("-- ITERATION {} : END", iteration);
				continue;
			}

			generateFile(templateFilePath, outputFile, data, context);
			log.info("-- ITERATION {} : END", iteration);
		}

		log.info("- END template file.");
	}

	/**
	 * Copy template file to output directory. Target path is resolved by using
	 * templating engine.
	 * 
	 * @param templateFilePath
	 * @param context
	 */
	private void copyFile(Path templateFilePath, TemplatingContext context) {
		Path targetPath = pathResolver.resolveOutputPath(templateFilePath, context);
		log.info("COPY FILE: {}", targetPath);
		try {
			Files.copy(templateFilePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new XlTemplatingException(e);
		}
	}

	private void createOutputDirs(Path outputDir) {
		if (!Files.exists(outputDir)) {
			// Create output directory if it doesn't exist.
			try {
				Files.createDirectories(outputDir);
			} catch (IOException e) {
				throw new XlTemplatingException("File system error occured for creating output directories/files", e);
			}
		}
	}

	private String readDSLStringFromTemplateFile(Path templatePath) {
		try (BufferedReader br = Files.newBufferedReader(templatePath)) {
			String line = br.readLine();
			if (!"{####".equals(line.trim())) {
				// DSL must start from the first line of the template file.
				return null;
			}
			StringBuilder dsl = new StringBuilder();
			while ((line = br.readLine()) != null) {
				if ("####}".equals(line.trim())) {
					return dsl.toString();
				}
				dsl.append(line);
				dsl.append("\r\n");
			}
			throw new XlTemplatingDslException("Illegal DSL. DSL not closed." + templatePath);
		} catch (MalformedInputException e) {
			// ignore, since this is caused by excel file.
			log.warn("Skip file with MalformedInputException. {}", templatePath);
			return null;
		} catch (IOException e) {
			throw new XlTemplatingException(e);
		}
	}

	private void generateFile(Path templateFilePath, Path outputFile, Map<String, Object> data,
			TemplatingContext context) {
		Map<String, Object> templateEngineContext = new XlScriptBindingsBuilder().excel(context.getExcel()).putAll(data)
				.build();

		log.info("--- RESULT: CREATE FILE: {}", outputFile);

		// Execute Templating Engine
		engine.generate(templateFilePath, outputFile, templateEngineContext);

	}

}
