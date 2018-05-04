package com.it.spring.framework.context.support;

import com.it.spring.framework.beans.BeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 用对配置文件进行查找，读取、解析
 *
 * @author lijun
 * @since 2018-04-23 15:32
 */
public class BeanDefinitionReader {

    public Properties config = new Properties();

    private List<String> registyBeanClasses = new ArrayList<String>();


    /**
     * 在配置文件中，用来获取自动扫描的包名的key
     */
    private final String SCAN_PACKAGE = "scanPackage";

    public BeanDefinitionReader(String... configLocations) {
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(configLocations[0].replace("classpath:", ""));

        try {
            config.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != in) {
                    in.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        doScanner(config.getProperty(SCAN_PACKAGE));
    }


    /**
     * 递归扫描所有的相关联的class，并且保存到一个List中
     *
     * @param packageName packageName
     */
    private void doScanner(String packageName) {

        URL url = this.getClass().getClassLoader().getResource("/" + packageName.replaceAll("\\.", "/"));

        File classDir = new File(url.getFile());

        for (File file : classDir.listFiles()) {
            if (file.isDirectory()) {
                doScanner(packageName + "." + file.getName());
            } else {
                registyBeanClasses.add(packageName + "." + file.getName().replace(".class", ""));
            }
        }


    }


    private String lowerFirstCase(String str) {
        char[] chars = str.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }


    public List<String> loadBeanDefinitions() {
        return  this.registyBeanClasses;
    }


    /**
     * 没注册一个className。就返回一个BeanDefinition
     * @param className  className
     * @return BeanDefinition
     */
    public BeanDefinition registerBean(String className) {
        if (this.registyBeanClasses.contains(className)){
            BeanDefinition beanDefinition = new BeanDefinition();
            beanDefinition.setBeanClassName(className);
            beanDefinition.setFactoryBeanName(lowerFirstCase(className.substring(className.lastIndexOf(".")+1)));
            return beanDefinition;
        }
        return null;
    }

    public Properties getConfig(){
        return this.config;
    }
}
