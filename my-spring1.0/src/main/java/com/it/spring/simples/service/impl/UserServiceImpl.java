package com.it.spring.simples.service.impl;

import com.it.spring.simples.service.UserService;
import com.it.spring.spring.annotation.Service;

/**
 * @author lijun
 * @since 2018-04-19 14:58
 */
@Service
public class UserServiceImpl implements UserService {
    @Override
    public String getName(String name) {
        System.out.println("my name is "+name);
        return name;
    }
}
