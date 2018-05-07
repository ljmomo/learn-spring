package com.it.spring.framework.beans;

import com.it.spring.framework.aop.AopConfig;
import com.it.spring.framework.aop.AopProxy;

/**
 * @author lijun
 * @since 2018-04-23 23:34
 */
public class BeanWrapper {

    private AopProxy aopProxy = new AopProxy();


    /**
     * 还会用到  观察者  模式
     * 支持事件响应，会有一个监听
     */
    private BeanPostProcessor postProcessor;

    /**
     * 包装的实例
     */
    private Object wrapperInstance;

    /**
     * 原始的通过反射new出来，要把包装起来，存下来
     */
    private Object originalInstance;

    public BeanWrapper(Object instance) {
        //从这里开始，我们要把动态的代码添加进来了
        this.wrapperInstance = aopProxy.getProxy(instance);
        this.originalInstance = instance;
    }

    public Object getWrappedInstance() {
        return wrapperInstance;
    }

    public void  setAopConfig(AopConfig config){
        aopProxy.setConfig(config);
    }
    /**
     * 返回代理以后的Class
     * 可能会是这个 $Proxy0
     * return Class
     */
    public Class<?> getWrappedClass() {
        return this.wrapperInstance.getClass();
    }

    public BeanPostProcessor getPostProcessor() {
        return postProcessor;
    }

    public void setPostProcessor(BeanPostProcessor postProcessor) {
        this.postProcessor = postProcessor;
    }

    public Object getOriginalInstance() {
        return originalInstance;
    }
}
