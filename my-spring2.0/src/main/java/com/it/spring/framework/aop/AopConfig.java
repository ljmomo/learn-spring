package com.it.spring.framework.aop;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lijun
 * @since 2018-05-07 10:17
 * 只是对application中的expresssion的封装
 * 目标代理对象的 一个方法要哦增强
 * 由自己实现的业务逻辑去增强
 * 配置文件的目的：告诉spring，哪些类的哪些方法需要增强，增强的类容是什么
 * 对配置文件中所体现的类容进行封装
 */
public class AopConfig {

    /**
     * 以目标对象需要增强的Method作为key，
     * 需要增强的代码内容作为value
     */
    private Map<Method, Aspect> points = new HashMap<>();


    public  void  put(Method target,Object aspect,Method[] points){
        this.points.put(target, new Aspect(aspect, points));
    }

    public Aspect get(Method method){
        return this.points.get(method);
    }

    public boolean contains(Method method){
        return this.points.containsKey(method);
    }

    /**
     * 对增强的代码的封装
     */
    public class Aspect{
        /**
         * 待会将LogAspet这个对象赋值给它
         */
        private  Object aspect;
        /**
         * 会将LogAspet的before方法和after方法赋值进来
         */
        private Method[] points;

        public Aspect(Object aspect, Method[] points) {
            this.aspect = aspect;
            this.points = points;
        }

        public Object getAspect() {
            return aspect;
        }

        public Method[] getPoints() {
            return points;
        }
    }
}
