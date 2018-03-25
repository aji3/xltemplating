package org.xlbean.xltemplating.engine;

@SuppressWarnings("serial")
public class TemplatingException extends RuntimeException {

    public TemplatingException(Exception e) {
        super(e);
    }
}
