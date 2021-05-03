package com.wdkang.springcloud.controller;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.wdkang.springcloud.entities.Dept;
import com.wdkang.springcloud.service.DeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DeptController {

    @Autowired
    private DeptService deptService = null;

    @RequestMapping(value="/dept/get/{id}",method= RequestMethod.GET)
    @HystrixCommand(fallbackMethod = "processHystrix_Get") // 一旦方法调用出现错误 那么直接调用processHystrix_Get返回
    public Dept get(@PathVariable("id") Long id) {
        Dept dept =  this.deptService.get(id);
        if(null == dept) {
            throw new RuntimeException("该ID："+id+"没有没有对应的信息");
        }
        return dept;
    }

    public Dept processHystrix_Get(@PathVariable("id") Long id) {
        Dept dept = new Dept();
        dept.setDeptno(id);
        dept.setDname("该ID："+id+"没有没有对应的信息,null--@HystrixCommand");
        dept.setDb_source("no this database in MySQL");
        return dept;
    }
}
