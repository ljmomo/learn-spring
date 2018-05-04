package com.it.spring.demo.controller;


import com.it.spring.demo.service.IQueryService;
import com.it.spring.framework.annotation.Autowried;
import com.it.spring.framework.annotation.Controller;
import com.it.spring.framework.annotation.RequestMapping;
import com.it.spring.framework.annotation.RequestParam;
import com.it.spring.framework.webmvc.ModelAndView;

import java.util.HashMap;
import java.util.Map;

/**
 * 公布接口url
 *
 * @author junli
 */
@Controller
@RequestMapping("/")
public class PageAction {

    @Autowried
    IQueryService queryService;

    @RequestMapping("/first.html")
    public ModelAndView query(@RequestParam("teacher") String teacher) {
        String result = queryService.query(teacher);
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("teacher", teacher);
        model.put("data", result);
        model.put("token", "123456");
        return new ModelAndView("first.html", model);
    }

}
