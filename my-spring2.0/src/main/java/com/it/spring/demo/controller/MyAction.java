package com.it.spring.demo.controller;

import com.it.spring.demo.service.IModifyService;
import com.it.spring.demo.service.IQueryService;
import com.it.spring.framework.annotation.Autowried;
import com.it.spring.framework.annotation.Controller;
import com.it.spring.framework.annotation.RequestMapping;
import com.it.spring.framework.annotation.RequestParam;
import com.it.spring.framework.webmvc.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 公布接口url
 *
 * @author junli
 */
@Controller
@RequestMapping("/web")
public class MyAction {

    @Autowried
    IQueryService queryService;
    @Autowried
    IModifyService modifyService;

    @RequestMapping("/query.json")
    public ModelAndView query(HttpServletRequest request, HttpServletResponse response,
                              @RequestParam("name") String name) {
        String result = queryService.query(name);
        System.out.println(result);
        return out(response, result);
    }

    @RequestMapping("/add*.json")
    public ModelAndView add(HttpServletRequest request, HttpServletResponse response,
                            @RequestParam("name") String name, @RequestParam("addr") String addr) {
        String result = modifyService.add(name, addr);
        return out(response, result);
    }

    @RequestMapping("/remove.json")
    public ModelAndView remove(HttpServletRequest request, HttpServletResponse response,
                               @RequestParam("id") Integer id) {
        String result = modifyService.remove(id);
        return out(response, result);
    }

    @RequestMapping("/edit.json")
    public ModelAndView edit(HttpServletRequest request, HttpServletResponse response,
                             @RequestParam("id") Integer id,
                             @RequestParam("name") String name) {
        String result = modifyService.edit(id, name);
        return out(response, result);
    }


    private ModelAndView out(HttpServletResponse resp, String str) {
        try {
            resp.getWriter().write(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
