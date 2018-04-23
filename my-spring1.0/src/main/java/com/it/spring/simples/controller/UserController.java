package com.it.spring.simples.controller;

import com.it.spring.simples.service.UserService;
import com.it.spring.spring.annotation.Autowried;
import com.it.spring.spring.annotation.Controller;
import com.it.spring.spring.annotation.RequestMapping;

/**
 * @author lijun
 * @since 2018-04-19 14:57
 */
@Controller
public class UserController {

    @Autowried
    private UserService userService;

    @RequestMapping("getName")
    public String getName(String name){
        return userService.getName(name);
    }
}
