/**
 * @author Valar Morghulis
 * @Date 2023/9/1
 */
package com.ychengycheng.service.impl;


import com.ychengycheng.service.GreetingService;
import com.ychengycheng.annotation.YchengApi;

@YchengApi
public class GreetingServiceImpl implements GreetingService {
    @Override
    public String sayHello() {
        return "hello!!!";
    }
}
