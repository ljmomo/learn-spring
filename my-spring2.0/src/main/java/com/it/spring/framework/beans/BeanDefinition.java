package com.it.spring.framework.beans;

/**
 * 用来存储配置文件中的信息
 * 相遇保证在内存中的配置
 * @author lijun
 * @since 2018-04-23 21:26
 */
public class BeanDefinition {

    /**
     * 类全名包括包名
     */
    private String beanClassName;

    /**
     * 是否懒加载
     */
    private boolean lazyInit = false;

    /**
     * 类名称首字母小写
     */
    private String factoryBeanName;


    public String getBeanClassName() {
        return beanClassName;
    }

    public void setBeanClassName(String beanClassName) {
        this.beanClassName = beanClassName;
    }

    public boolean isLazyInit() {
        return lazyInit;
    }

    public void setLazyInit(boolean lazyInit) {
        this.lazyInit = lazyInit;
    }

    public String getFactoryBeanName() {
        return factoryBeanName;
    }

    public void setFactoryBeanName(String factoryBeanName) {
        this.factoryBeanName = factoryBeanName;
    }
}
