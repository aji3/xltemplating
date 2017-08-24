package com.xlbean.xltemplating;

import org.xlbean.XlBean;
import org.xlbean.util.XlBeanFactory;

public class XlTemplatingBeanFactory extends XlBeanFactory{

	@Override
	public XlBean createBean() {
		return new XlTemplatingBean();
	}
}
