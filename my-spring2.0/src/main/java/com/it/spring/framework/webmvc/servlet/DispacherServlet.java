package com.it.spring.framework.webmvc.servlet;

import com.it.spring.framework.annotation.Controller;
import com.it.spring.framework.annotation.RequestMapping;
import com.it.spring.framework.annotation.RequestParam;
import com.it.spring.framework.context.ApplicationContext;
import com.it.spring.framework.webmvc.HandlerAdapter;
import com.it.spring.framework.webmvc.HandlerMapping;
import com.it.spring.framework.webmvc.ModelAndView;
import com.it.spring.framework.webmvc.ViewResolver;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author lijun
 * @since 2018-05-02 14:44
 */
@WebServlet(name = "junlimvc", urlPatterns = "/*", loadOnStartup = 1,
        initParams = {@WebInitParam(name = "contextConfigLocation", value = "classpath:application.properties")})
public class DispacherServlet extends HttpServlet {
    private static final long serialVersionUID = 4601782659533259502L;
    private final String LOCATION = "contextConfigLocation";

    // private Map<String,HandlerMapping> handlerMapping = new HashMap<String,HandlerMapping>();

    //课后再去思考一下这样设计的经典之处
    //GPHandlerMapping最核心的设计，也是最经典的
    //它牛B到直接干掉了Struts、Webwork等MVC框架
    private List<HandlerMapping> handlerMappings = new ArrayList<HandlerMapping>();

    private Map<HandlerMapping, HandlerAdapter> handlerAdapters = new HashMap<HandlerMapping, HandlerAdapter>();

    private List<ViewResolver> viewResolvers = new ArrayList<ViewResolver>();


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatch(req, resp);
        }catch (Exception e){
            resp.getWriter().write("<font size='25' color='blue'>500 Exception</font><br/>Details:<br/>" + Arrays.toString(e.getStackTrace()).replaceAll("\\[|\\]","")
                    .replaceAll("\\s","\r\n") +  "<font color='green'><i>Copyright@GupaoEDU</i></font>");
            e.printStackTrace();
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        HandlerMapping handler = getHandler(req);
        if (handler == null){
            resp.getWriter().write("<font size='25' color='red'>404 Not Found</font><br/><font color='green'><i>Copyright@GupaoEDU</i></font>");
            return;
        }
        HandlerAdapter ha = getHandlerAdapter(handler);

        //这一步只是调用方法，得到返回值
       ModelAndView mv =  ha.handle(req,resp,handler);

       //这一步才是真的输出
        processDisptchResult(resp,mv);

    }

    private void processDisptchResult(HttpServletResponse resp, ModelAndView mv) throws Exception {
        //调用viewResolver的resolveView方法
        if (null == mv) {
            return;
        }
        if (this.viewResolvers.isEmpty()) {
            return;
        }
        for (ViewResolver viewResolver : this.viewResolvers) {
            if (!mv.getViewName().equals(viewResolver.getViewName())) {
                continue;
            }
           String out =  viewResolver.viewResolver(mv);
            if (out!=null){
                resp.setCharacterEncoding("UTF-8");
                resp.getWriter().write(out);
                break;
            }
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        //相当于把IOC容器初始化了
        ApplicationContext context = new ApplicationContext(config.getInitParameter(LOCATION));

        initStrategies(context);
    }

    protected void initStrategies(ApplicationContext context) {
        /**
         * 有就中策略
         * 针对于每个用户请求，都会经过一些处理的策略之后，最终才能有结果输出
         * 没种策略可以自定义干预，但最终的结果都是一致
         *
         */

        //===================这里说的就是传说中的九大组件===============
        /**
         *文件上传解析，如果请求类型是multipart将通过MultipartResolver进行文件上传解析
         */
        initMultipartResolver(context);
        //本地化解析
        initLocaleResolver(context);
        //主题解析
        initThemeResolver(context);
        /**
         * 我们自己实现
         * HandlerMapping 用来保存Controller中配置的RequestMapping和Method的一个对应关系
         * 通过HandlerMapping 将请求映射到处理
         */
        initHandlerMappings(context);

        /**
         *我们自己实现
         * HandlerAdapters 用来动态匹配Method参数，包括类装换，动态赋值
         * 通过HandlerAdapter进行多类型的参数动态匹配
         *
         */
        initHandlerAdapters(context);

        /**
         * 如果执行过程中遇到异常 将交给HandlerExceptionResolver来解析
         */
        initHandlerExceptionResolvers(context);

        /**
         *直接解析请求到视图名
         */
        initRequestToViewNameTranslator(context);

        /**
         * 我们会自己实现
         * 通过ViewResolver实现动态模板的解析
         * 自己解析一套模板语言
         * 通过viewResolver 解析逻辑视图到具体视图
         */
        initViewResolvers(context);

    }


    private void initRequestToViewNameTranslator(ApplicationContext context) {
    }

    private void initHandlerExceptionResolvers(ApplicationContext context) {
    }


    private void initThemeResolver(ApplicationContext context) {
    }


    private void initMultipartResolver(ApplicationContext context) {

    }

    private void initLocaleResolver(ApplicationContext context) {
    }


    /**
     * 自己实现的HandlerMapping
     * 将Controller中配置的RequestMapping和Method进行一一对应
     *
     * @param context context
     */
    private void initHandlerMappings(ApplicationContext context) {
        /**
         * 按照我们的理解通常应该是一个Map 来保存 RequestMapping 和 Method
         * 例如：Map<String,Method> map;
         * map.put(url,Method)
         * 但是这里不是用Map 而是用List
         */

        // 首先从容器中取出所有的实例
        String[] beanNames = context.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            Object controller = context.getBean(beanName);
            Class<?> clazz = controller.getClass();

            //判断实例是不是被@Controller注解 如果不是则跳出本次循环
            if (!clazz.isAnnotationPresent(Controller.class)) {
                continue;
            }
            String baseUrl = "";

            //如果是@Controller注解 则判断是不是被@RequestMapping注解
            if (clazz.isAnnotationPresent(RequestMapping.class)) {
                RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
                baseUrl = requestMapping.value();
            }

            //扫描所有的public方法
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {

                if (!method.isAnnotationPresent(RequestMapping.class)) {
                    continue;
                }
                RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
                String regex = (baseUrl + requestMapping.value().replaceAll("\\*", ".*")).replaceAll("/+", "/");
                Pattern pattern = Pattern.compile(regex);
                this.handlerMappings.add(new HandlerMapping(controller, method, pattern));
                System.out.println("Mapping: " + regex + " , " + method);
            }

        }

    }

    /**
     * 自己实现HandlerAdapters
     *
     * @param context context
     */
    private void initHandlerAdapters(ApplicationContext context) {
        /**
         * 在初始化阶段，我们能做的就是，将这些参数的名字或者类型按照一定的顺序保存下来
         * 因为后面用反射调用的时候，传的形参是一个数组
         * 可以通过记录这些参数的位置的index，挨个从数组添值，这样的话，就和参数的顺序无关了
         */
        for (HandlerMapping handlerMapping : this.handlerMappings) {
            //每一个方法有一个参数列表，那么这里保存的是形惨列表
            Map<String, Integer> paramMapping = new HashMap<>();
            //这里只是出来了命名参数
            Annotation[][] pa = handlerMapping.getMethod().getParameterAnnotations();
            for (int i = 0; i < pa.length; i++) {
                for (Annotation a : pa[i]) {
                    if (a instanceof RequestParam) {
                        String paramName = ((RequestParam) a).value();
                        if (!"".equals(paramName.trim())) {
                            paramMapping.put(paramName, i);
                        }
                    }

                }
            }

            Class<?>[] parameterTypes = handlerMapping.getMethod().getParameterTypes();
            for (int i = 0; i < parameterTypes.length; i++) {
                Class<?> type = parameterTypes[i];
                if (type == HttpServletRequest.class || type == HttpServletResponse.class) {
                    paramMapping.put(type.getName(), i);

                }

            }
            this.handlerAdapters.put(handlerMapping, new HandlerAdapter(paramMapping));
        }
    }

    /**
     * 自己实现ViewResolvers
     *
     * @param context context
     */
    private void initViewResolvers(ApplicationContext context) {
        /**
         *   在页面敲一个http://localhost/first.html
         *   解决页面名字和模板文件关联的问题
         */
        String templateRoot = context.getConfig().getProperty("templateRoot");
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();

        File templateRootDir = new File(templateRootPath);
        for (File template : templateRootDir.listFiles()) {
            this.viewResolvers.add(new ViewResolver(template.getName(), template));
        }


    }


    /**
     *根据 HandlerMapping 获得 HandlerAdapter
     * @param handler HandlerMapping
     * @return HandlerAdapter
     */
    private HandlerAdapter getHandlerAdapter(HandlerMapping handler) {
        if(this.handlerAdapters.isEmpty()){return  null;}
        return this.handlerAdapters.get(handler);
    }

    /**
     *根据request 获得 HandlerMapping
     * @param req request
     * @return HandlerMapping
     */
    private HandlerMapping getHandler(HttpServletRequest req) {

        if(this.handlerMappings.isEmpty()){ return  null;}


        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replace(contextPath,"").replaceAll("/+","/");

        for (HandlerMapping handler : this.handlerMappings) {
            Matcher matcher = handler.getPattern().matcher(url);
            if(!matcher.matches()){ continue;}
            return handler;
        }
        return null;
    }

}
