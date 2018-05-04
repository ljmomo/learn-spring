package com.it.spring.framework.webmvc;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author lijun
 * @since 2018-05-04 15:48
 * 设计这个 类的主要目的是：
 * 1.将一个静态文件变为一个动态文件
 * 2.根据用户传送参数不同，产生不同的结果
 * 最终输出字符串，交给Response
 */
public class ViewResolver {

    private String viewName;
    private File templateFile;

    public ViewResolver(String viewName, File templateFile) {
        this.viewName = viewName;
        this.templateFile = templateFile;
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public File getTemplateFile() {
        return templateFile;
    }

    public void setTemplateFile(File templateFile) {
        this.templateFile = templateFile;
    }

    public String viewResolver(ModelAndView mv) throws Exception {
        StringBuffer sb = new StringBuffer();
        RandomAccessFile ra = new RandomAccessFile(this.templateFile, "r");
        try {
            String line = null;
            while (null!=(line =  ra.readLine())){
              line = new String(line.getBytes("ISO-8859-1"),"UTF-8");
              Matcher m = matcher(line);
              while (m.find()){
                  for (int i = 1 ;i<= m.groupCount();i++){
                      String paramName = m.group(i);
                      Object paramValue = mv.getModel().get(paramName);
                      if (null == paramValue) {
                          continue;
                      }
                      line = line.replaceAll("￥\\{" + paramName + "\\}", paramValue.toString());
                      //line = new String(line.getBytes("UTF-8"), "ISO-8859-1");
                  }
              }
                sb.append(line);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            ra.close();
        }
        return sb.toString();
    }


    private Matcher matcher(String str){
        Pattern pattern = Pattern.compile("￥\\{(.+?)\\}",Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(str);
        return  matcher;
    }

}
