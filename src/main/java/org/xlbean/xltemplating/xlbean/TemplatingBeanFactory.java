package org.xlbean.xltemplating.xlbean;

import org.xlbean.XlBean;
import org.xlbean.util.XlBeanFactory;

public class TemplatingBeanFactory extends XlBeanFactory {

    @Override
    public XlBean createBean() {
        return new TemplatingBean();
    }
}
