package com.it.spring.spring.servlet;

import com.it.spring.simples.controller.UserController;
import com.it.spring.spring.annotation.Autowried;
import com.it.spring.spring.annotation.Controller;
import com.it.spring.spring.annotation.Service;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lijun
 * @since 2018-04-19 11:17
 */
@WebServlet(name = "junlimvc", urlPatterns = "/*", loadOnStartup = 1,
        initParams = {@WebInitParam(name = "contextConfigLocation", value = "classpath:application.properties")})
public class DispachServlet extends HttpServlet {

    private Properties contextConfig = new Properties();

    private Map<String, Object> beanMap = new ConcurrentHashMap<String, Object>();

    private List<String> classNames = new ArrayList<String>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        //开始初始化的进程

        //定位
        doLoadConfig(config.getInitParameter("contextConfigLocation"));
        //加载
        doScanner(contextConfig.getProperty("scanPackage"));

        //注册
        doRegistry();

        //自动依赖注入

        //在Spring中是通过调用getBean方法才出发依赖注入的
        doAutowired();


        UserController userController = (UserController) beanMap.get("userController");
        userController.getName("Name");

        //如果是SpringMVC会多设计一个HnandlerMapping

        //将@RequestMapping中配置的url和一个Method关联上
        //以便于从浏览器获得用户输入的url以后，能够找到具体执行的Method通过反射去调用
        initHandlerMapping();
    }

    private void initHandlerMapping() {
    }

    /**
     * 遍历所有已经加载到容器的实体 并进行依赖注入
     */
    private void doAutowired() {
        if (beanMap.isEmpty()) {
            return;
        }
        Set<Map.Entry<String, Object>> entries = beanMap.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field : fields) {
                if (!field.isAnnotationPresent(Autowried.class)) {
                    continue;
                }
                Autowried autowried = field.getAnnotation(Autowried.class);
                String beanName = autowried.value();
                if ("".equals(beanName)) {
                    beanName = field.getType().getName();
                }
                field.setAccessible(true);
                try {
                    field.set(entry.getValue(), beanMap.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * 实例化所有扫描到的类 并且把他放到容器中（即SpringIOC容器中）
     */
    private void doRegistry() {
        if (classNames.isEmpty()) {
            return;
        }
        for (String calssName : classNames) {
            try {
                Class<?> aClass = Class.forName(calssName);
                //被controller注解
                if (aClass.isAnnotationPresent(Controller.class)) {
                    String beanName = lowerFirstCase(aClass.getSimpleName());
                    beanMap.put(beanName, aClass.newInstance());
                } else if (aClass.isAnnotationPresent(Service.class)) {
                    Service service = aClass.getAnnotation(Service.class);
                    String beanName = service.value();
                    if ("".equals(beanName.trim())) {
                        beanName = lowerFirstCase(aClass.getSimpleName());
                    }
                    Object instance = aClass.newInstance();
                    beanMap.put(beanName, instance);

                    System.out.println("doRegistry aClass: "+ aClass.getName());
                    Class<?>[] interfaces = aClass.getInterfaces();
                    for (Class<?> i : interfaces) {
                        System.out.println("doRegistry  i:"+i.getName());
                        beanMap.put(i.getName(), instance);
                    }

                } else {
                    continue;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    /**
     * 根据配置文件提供的需要扫描的包
     * 递归出所有需要实例话类名字
     *
     * @param scanPackage 要扫描的包
     */
    private void doScanner(String scanPackage) {
        URL url = this.getClass().getClassLoader().
                getResource("/" + scanPackage.replaceAll("\\.", "//"));
        File classDir = new File(url.getFile());
        for (File file : classDir.listFiles()) {
            if (file.isDirectory()) {
                doScanner(scanPackage + "." + file.getName());
            } else {
                String className = scanPackage + "." + file.getName().replace(".class", "");
                classNames.add(className);
                System.out.println("className:" + className);
            }
        }

    }

    /**
     * 加载配置文件到contextConfig
     *
     * @param contextConfigLocation 配置文件地址
     */
    private void doLoadConfig(String contextConfigLocation) {
        System.out.println("配置文件路径：" + contextConfigLocation);
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation.replace("classpath:", ""));
        try {
            contextConfig.load(resourceAsStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != resourceAsStream) {
                    resourceAsStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    /**
     * 字符串首字母小写
     *
     * @param str 要转换的字符串
     * @return 新的首字母小写的字符串
     */
    private String lowerFirstCase(String str) {
        char[] chars = str.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }
}
