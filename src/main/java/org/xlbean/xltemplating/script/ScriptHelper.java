package org.xlbean.xltemplating.script;

import java.util.HashMap;
import java.util.Map;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class ScriptHelper {
    private static final ScriptHelper INSTANCE = new ScriptHelper();

    public static ScriptHelper getInstance() {
        return INSTANCE;
    }

    private Map<String, CompiledScript> scriptCacheMap = new HashMap<>();

    public Object execute(String scriptStr, Map<String, Object> bindings) {
        ScriptEngineManager engineManager = new ScriptEngineManager();
        ScriptEngine scriptEngine = engineManager.getEngineByName("groovy");
        try {
            CompiledScript script = scriptCacheMap.get(scriptStr);
            if (script == null) {
                script = ((Compilable) scriptEngine).compile(scriptStr);
                scriptCacheMap.put(scriptStr, script);
            }
            Bindings scriptBindings = scriptEngine.createBindings();
            scriptBindings.putAll(bindings);
            Object ret = script.eval(scriptBindings);
            scriptBindings.forEach((key, value) -> bindings.put(key, value));
            return ret;
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }

    }

}
