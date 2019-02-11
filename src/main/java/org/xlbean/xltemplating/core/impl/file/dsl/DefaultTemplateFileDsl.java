package org.xlbean.xltemplating.core.impl.file.dsl;

import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;
import org.xlbean.xlscript.processor.AbstractXlScriptProcessor;
import org.xlbean.xlscript.util.XlScript;

public class DefaultTemplateFileDsl {

    /**
     * Run the DSL string given as argument under the Script Base Class
     * {@link DefaultTemplateFileDslScript}.
     * 
     * @param dsl
     * @param excel
     */
    public DefaultTemplateFileDslScript evaluate(String dsl, Map<String, Object> excel) {
        DefaultTemplateFileDslScript baseScript = new DefaultTemplateFileDslScript(excel);
        if (dsl == null) {
            return baseScript;
        }
        Map<String, Object> map = new HashedMap<>();
        map.putAll(excel);
        map.put(AbstractXlScriptProcessor.CONTEXT_KEY_EXCEL, excel);

        XlScript script = new XlScript(baseScript);
        script.evaluate(dsl, map);
        return baseScript;
    }

}
