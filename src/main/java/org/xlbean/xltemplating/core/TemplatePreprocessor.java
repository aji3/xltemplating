package org.xlbean.xltemplating.core;

public interface TemplatePreprocessor {

    public void init(TemplatingContext context);

    /**
     * Execute pre.groovy with all of the properties in xlbean to be set as property
     * of the script. For instance, if xlbean.value("some_key") returns "xxx" then
     * "some_key" is accessible from pre.groovy.
     * 
     * @param context
     */
    public void execute(TemplatingContext context);

}
