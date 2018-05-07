package com.it.spring.framework.context;

/**
 * @author lijun
 * @since 2018-05-05 22:20
 */
public abstract class AbstractApplicationContext {

    //提供给子类重写
    protected void onRefresh(){
        // For subclasses: do nothing by default.
    }
    protected abstract void refreshBeanFactory();
}
