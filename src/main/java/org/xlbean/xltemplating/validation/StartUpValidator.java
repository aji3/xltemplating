package org.xlbean.xltemplating.validation;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.xlbean.xltemplating.TemplatingArgs;

public class StartUpValidator {

	public ValidationResult validate(TemplatingArgs args) {
		ValidationResult result = new ValidationResult();

		if (!Files.exists(Paths.get(args.getExcelFilePath()))) {
			String message = String.format("Template Excel file doesn't exist [%s]. Use \"-f\" to specify path for Excel file.", args.getExcelFilePath());
			result.addError(message);
		}
		
		if (!Files.exists(Paths.get(args.getTemplateDirectoryPath()))) {
			String message = String.format("Template Folder doesn't exist [%s]. Use \"-t\" to specify path for Templates.", args.getTemplateDirectoryPath());
			result.addError(message);
		}
		
		return result;
	}

	public static class ValidationResult {
		private List<ValidationErrorMessage> errors = new ArrayList<>();

		public void addError(String messageText) {
			ValidationErrorMessage message = new ValidationErrorMessage(messageText);
			errors.add(message);
		}
		
		public boolean isError() {
			return errors.size() > 0;
		}
		
		public List<ValidationErrorMessage> getErrors() {
			return errors;
		}
	}

	public static class ValidationErrorMessage {
		private String message;

		public ValidationErrorMessage(String message) {
			this.message = message;
		}

		public String getMessage() {
			return message;
		}

		@Override
		public String toString() {
			return "ValidationErrorMessage [message=" + message + "]";
		}
		
		

	}
}
