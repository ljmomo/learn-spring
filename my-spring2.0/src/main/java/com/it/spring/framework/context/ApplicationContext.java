package com.it.spring.framework.context;

import com.it.spring.framework.annotation.Autowried;
import com.it.spring.framework.annotation.Controller;
import com.it.spring.framework.annotation.Service;
import com.it.spring.framework.beans.BeanDefinition;
import com.it.spring.framework.beans.BeanPostProcessor;
import com.it.spring.framework.beans.BeanWrapper;
import com.it.spring.framework.context.support.BeanDefinitionReader;
import com.it.spring.framework.core.BeanFactory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lijun
 * @since 2018-04-23 15:04
 */
public class ApplicationContext implements BeanFactory {

    /**
     * 配置文件
     */
    private String[] configLocations;


    private BeanDefinitionReader reader;


    /**
     * beanDefinitionMap用来保存配置信息
     */
    private Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<String, BeanDefinition>();


    /**
     * 用来保证注册式单例的容器
     */
    private Map<String, Object> beanCacheMap = new HashMap<String, Object>();


    /**
     * 用来存储所有的被代理过的对象
     */
    private Map<String, BeanWrapper> beanWrapperMap = new ConcurrentHashMap<String, BeanWrapper>();

    public ApplicationContext(String... configLocations) {
        this.configLocations = configLocations;
        this.configLocations = configLocations;
        refresh();
    }


    public void refresh() {
        //定位
        this.reader = new BeanDefinitionReader(configLocations);
        //加载
        List<String> beanDefinitions = reader.loadBeanDefinitions();

        //注册
        doRegisty(beanDefinitions);

        //依赖注入（lazy-init = false） 要执行依赖注入 即调用getBean
        doAutowrited();


    }

    /**
     * 执行自动化的依赖注入
     */
    private void doAutowrited() {
        for (Map.Entry<String, BeanDefinition> beanDefinitionEntry : this.beanDefinitionMap.entrySet()) {
            String beanName = beanDefinitionEntry.getKey();
            if (!beanDefinitionEntry.getValue().isLazyInit()) {
                getBean(beanName);
            }
        }

        for(Map.Entry<String,BeanWrapper> beanWrapperEntry : this.beanWrapperMap.entrySet()){
            populateBean(beanWrapperEntry.getKey(),beanWrapperEntry.getValue().getWrappedInstance());

        }

    }


    public void populateBean(String beanName, Object instance) {
        Class<?> clazz = instance.getClass();
        if (!(clazz.isAnnotationPresent(Controller.class) || clazz.isAnnotationPresent(Service
                .class))) {
            return;

        }
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
           if (!field.isAnnotationPresent(Autowried.class)){
                 continue;
           }
            Autowried autowired = field.getAnnotation(Autowried.class);
            String autowriteBeanName = autowired.value().trim();
            if ("".equals(autowriteBeanName)){
                autowriteBeanName = field.getType().getName();
            }
            field.setAccessible(true);

            try {
                /**
                 *会产生循环依赖问题   即A依赖B  B没有实例化
                 */
                //System.out.println("=======================" +instance +"," + autowiredBeanName + "," + this.beanWrapperMap.get(autowiredBeanName));
                field.set(instance,this.beanWrapperMap.get(autowriteBeanName).getWrappedInstance());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }

    }


    /**
     * 真正的将BeanDefinitions注册到beanDefinitionMap中
     *
     * @param beanDefinitions beanDefinitions
     */
    private void doRegisty(List<String> beanDefinitions) {

        /**
         * beanName 有三种情况
         * 1.默认是类名字首字母小写
         */

        try {
            for (String className : beanDefinitions) {
                Class<?> beanClass = Class.forName(className);
                //如果是一个接口是不能被实例化的 用他的实现类来实例化
                if (beanClass.isInterface()) {
                    continue;
                }
                BeanDefinition beanDefinition = reader.registerBean(className);
                if (beanDefinition != null) {
                    this.beanDefinitionMap.put(beanDefinition.getFactoryBeanName(), beanDefinition);
                }
                Class<?>[] interfaces = beanClass.getInterfaces();
                for (Class<?> i : interfaces) {
                    this.beanDefinitionMap.put(i.getName(), beanDefinition);

                }

                //到这里为止，容器初始化完毕
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据beanName从IOC容器之中获得一个实例Bean
     * 依赖注入,从这里开始，通过读取BeanDefinition中的信息
     * 然后通过反射机制创建一个实例并返回
     * spring的做法是不会把原生的对象放进去的，会用一个BeanWrapper来进行一次包装
     * 装饰器模式
     * 1.保留原来的OOP关系
     * 2.我们需要对它进行扩展，增强（为了以后得AOP打基础）
     *
     * @param beanName
     * @return
     */
    @Override
    public Object getBean(String beanName) {
        BeanDefinition beanDefinition = this.beanDefinitionMap.get(beanName);
        //String className = beanDefinition.getBeanClassName();


        BeanPostProcessor beanPostProcessor = new BeanPostProcessor();

        Object instance = instantionBean(beanDefinition);
        if (instance == null) {
            return null;
        }

        //在实例初始化以前调用一次
        beanPostProcessor.postProcessBeforeInitialization(instance, beanName);

        BeanWrapper beanWrapper = new BeanWrapper(instance);
        beanWrapper.setPostProcessor(beanPostProcessor);
        this.beanWrapperMap.put(beanName, beanWrapper);

        //在实例初始化以后调用一次
        beanPostProcessor.postProcessAfterInitialization(instance, beanName);

        //populateBean(beanName,instance);

        // 通过这样一调用，相当于给我们自己留有了可操作的空间
        return this.beanWrapperMap.get(beanName).getWrappedInstance();
    }

    /**
     * 传入一个BeanDefinition，就返回一个实例Bean
     *
     * @param beanDefinition BeanDefinition
     * @return Object
     */
    private Object instantionBean(BeanDefinition beanDefinition) {
        Object instance = null;
        String className = beanDefinition.getBeanClassName();
        try {
            //根据Class才能确定一个类是否有实例
            if (this.beanCacheMap.containsKey(className)) {
                instance = this.beanCacheMap.get(className);
            } else {
                Class<?> clazz = Class.forName(className);
                instance = clazz.newInstance();
                this.beanCacheMap.put(className, instance);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return instance;
    }

    public String[] getBeanDefinitionNames() {
        return this.beanDefinitionMap.keySet().toArray(new String[this.beanDefinitionMap.size()]);
    }

    public Properties getConfig(){
        return this.reader.getConfig();
    }
}
