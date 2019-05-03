package org.xlbean.xltemplating.core.impl.file.dsl;

import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;
import org.xlbean.xlscript.processor.AbstractXlScriptProcessor;
import org.xlbean.xlscript.script.XlScript;
import org.xlbean.xlscript.script.XlScriptFactory;

public class DefaultTemplateFileDsl {

    /**
     * Run the DSL string given as argument under the Script Base Class
     * {@link DefaultTemplateFileDslConfig}.
     * 
     * @param dsl
     * @param excel
     */
    public DefaultTemplateFileDslConfig evaluate(String dsl, Map<String, Object> excel) {
        DefaultTemplateFileDslConfig baseInstance = new DefaultTemplateFileDslConfig(excel);
        if (dsl == null) {
            return baseInstance;
        }
        Map<String, Object> map = new HashedMap<>();
        map.putAll(excel);
        map.put(AbstractXlScriptProcessor.CONTEXT_KEY_EXCEL, excel);

    	XlScriptFactory factory = new XlScriptFactory();
    	factory.setBaseInstance(baseInstance);
        XlScript script = factory.getXlScript(dsl);
        script.execute(map);
        return baseInstance;
    }

}
