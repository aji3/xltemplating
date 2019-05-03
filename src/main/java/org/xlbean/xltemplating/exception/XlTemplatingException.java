package org.xlbean.xltemplating.exception;

@SuppressWarnings("serial")
public class XlTemplatingException extends RuntimeException {

    public XlTemplatingException() {}

    public XlTemplatingException(String message) {
        super(message);
    }

    public XlTemplatingException(String message, Throwable cause) {
        super(message, cause);
    }

    public XlTemplatingException(Throwable cause) {
        super(cause);
    }
}
