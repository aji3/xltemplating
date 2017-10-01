package org.xlbean.xltemplating.xlbean;

import java.util.regex.Pattern;

import org.xlbean.XlBean;
import org.xlbean.xltemplating.script.ScriptHelper;

@SuppressWarnings("serial")
public class XlTemplatingBean extends XlBean {

	Pattern pattern = Pattern.compile("`[^`]+`");
	
	@Override
	public Object get(Object key) {
		if (key instanceof String  && pattern.matcher((String)key).matches()) {
			String keyStr = (String)key;
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
	    if (pattern.matcher((String)key).matches()) {
	        return true;
	    }
	    return super.containsKey(key);
	}
}
