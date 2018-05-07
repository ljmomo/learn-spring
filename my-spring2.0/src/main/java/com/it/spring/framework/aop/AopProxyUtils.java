package com.it.spring.framework.aop;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

/**
 * @author lijun
 * @since 2018-05-07 11:15
 */
public class AopProxyUtils {



    /**
     * 先判断一下，这个传进来的这个对象是不是一个代理过的对象
     *如果不是一个代理对象，就直接返回
     * @param proxy
     * @return
     */
    public static  Object getTargetObject(Object proxy) throws Exception {
        if (!isAopProxy(proxy)) {
           return proxy;
        }
        return getProxyTargetObject(proxy);
    }

    private static Object getProxyTargetObject(Object proxy) throws Exception {
       Field h = proxy.getClass().getSuperclass().getDeclaredField("h");
         h.setAccessible(true);
        AopProxy aopProxy = (AopProxy)h.get(proxy);
        Field target = aopProxy.getClass().getDeclaredField("target");
        target.setAccessible(true);
        return target.get(aopProxy);
    }

    private static boolean isAopProxy(Object object){
        return Proxy.isProxyClass(object.getClass());
    }

}
