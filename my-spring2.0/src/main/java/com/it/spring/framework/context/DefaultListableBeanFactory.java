package com.it.spring.framework.context;

import com.it.spring.framework.beans.BeanDefinition;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lijun
 * @since 2018-05-05 22:17
 */
public class DefaultListableBeanFactory extends AbstractApplicationContext{

    //beanDefinitionMap用来保存配置信息
    protected Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<String, BeanDefinition>();

    @Override
    protected void onRefresh() {

    }

    @Override
    protected void refreshBeanFactory() {

    }
}
