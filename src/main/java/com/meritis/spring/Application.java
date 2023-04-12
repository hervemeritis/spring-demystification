package com.meritis.spring;

import com.meritis.spring.application.Service;
import com.meritis.spring.factory.BeanFactory;

public class Application {

    public static void main(String[] args) {
        BeanFactory factory = new BeanFactory();
        factory.load("com.meritis.spring.application");
        Service service = factory.getSingleton(Service.class);
        service.test();
    }
}
