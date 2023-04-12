package com.meritis.spring.application;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class Service {

    private final Dao dao;
    @Inject
    public Service(Dao dao) {
        this.dao = dao;
    }

    public void test() {
        System.out.println("HelloWorld ! J'ai le Dao : " + dao);
    }
}
