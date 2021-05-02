package com.wdkang.springcloud.service;

import com.wdkang.springcloud.entities.Dept;

import java.util.List;

public interface DeptService {
    boolean add(Dept dept);
    Dept get(Long id);
    List<Dept> list();
}
