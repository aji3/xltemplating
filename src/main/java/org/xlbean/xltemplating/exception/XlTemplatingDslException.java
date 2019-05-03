package org.xlbean.xltemplating.exception;

@SuppressWarnings("serial")
public class XlTemplatingDslException extends RuntimeException {

    public XlTemplatingDslException() {}

    public XlTemplatingDslException(String message) {
        super(message);
    }

    public XlTemplatingDslException(String message, Throwable cause) {
        super(message, cause);
    }

    public XlTemplatingDslException(Throwable cause) {
        super(cause);
    }
}
