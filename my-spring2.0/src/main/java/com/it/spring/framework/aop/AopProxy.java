package com.it.spring.framework.aop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author lijun
 * @since 2018-05-07 10:45
 */
public class AopProxy implements InvocationHandler{

    private AopConfig config;
    private Object target;

    /**
     * 把原生的对象传进来
     */

    public  Object getProxy(Object instance){
        this.target = instance;
        Class<?> clazz = instance.getClass();
        return Proxy.newProxyInstance(clazz.getClassLoader(),clazz.getInterfaces(),this);
    }


    /**
     *
     * @param proxy
     * @param method
     * @param args
     * @return
     * @throws Throwable
     */

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
       Method m =  this.target.getClass().getMethod(method.getName(),method.getParameterTypes());
        /**
         * 在原始的方法调用以前要执行增强的代码
         * 这里需要通过原生方法去找，通过代理方法去Map中式找不到的
         */
        if (config.contains(m)){
            AopConfig.Aspect aspect = config.get(m);
            aspect.getPoints()[0].invoke(aspect.getAspect());
        }
        Object obj = method.invoke(this.target, args);
        System.out.println("--"+method.getName());
        if (config.contains(m)){
            AopConfig.Aspect aspect = config.get(m);
            aspect.getPoints()[1].invoke(aspect.getAspect());
        }
       return obj;
    }

    public void setConfig(AopConfig config) {
        this.config = config;
    }
}
