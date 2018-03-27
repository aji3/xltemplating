package org.xlbean.xltemplating.xlbean;

import java.util.regex.Pattern;
import org.xlbean.XlBean;
import org.xlbean.xltemplating.script.ScriptHelper;

/**
 * Inherits {@link XlBean} to extend {@link #get(Object)} method if format of the key is `SOME_KEY`
 * then SOME_KEY will be executed as Groovy program. For instance, assuming that this
 * XlTemplatingBean object has a parameter 'key' as a type "String", however in fact the value is
 * number format and you want to use this as Integer value from Pebble template. In that case, you
 * can define "`Integer.decode(key)`" on your Pebble template so that the code will be executed as
 * Groovy program and the result will be passed to Pebble engine as Integer.
 * 
 * @author tanikawa
 *
 */
@SuppressWarnings("serial")
public class XlTemplatingBean extends XlBean {

    Pattern pattern = Pattern.compile("`[^`]+`");

    @Override
    public Object get(Object key) {
        if (key instanceof String && pattern.matcher((String) key).matches()) {
            String keyStr = (String) key;
            return ScriptHelper.getInstance().execute(keyStr.replaceAll("`", ""), this);
        }
        return super.get(key);
    }

    @Override
    protected boolean canPut(Object value) {
        return true;
    }

    @Override
    public boolean containsKey(Object key) {
        if (pattern.matcher((String) key).matches()) {
            return true;
        }
        return super.containsKey(key);
    }
}
