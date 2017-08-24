package com.xlbean.xltemplating;

import java.util.regex.Pattern;

import org.xlbean.XlBean;

@SuppressWarnings("serial")
public class XlTemplatingBean extends XlBean {

	Pattern pattern = Pattern.compile("`[^`]+`");
	
	@Override
	public Object get(Object key) {
		if (key instanceof String  && pattern.matcher((String)key).matches()) {
//	    System.out.println(key);
//	    if (key instanceof String && ((String) key).indexOf('`') >= 0) {
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
