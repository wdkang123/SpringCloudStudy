# 02 - Eureka注册于发现

**设计Eureka时遵循AP的原则**

CAP原则又称CAP定理，指的是在一个分布式系统中，Consistency（一致性）、 Availability（可用性）、Partition tolerance（分区容错性），三者不可兼得。



# 1.配置Eureka

## 1.1 新建子模块

新建module 名称为**microservicecloud-eureka-7001**

别忘了选择父工程



## 1.2 POM文件

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.wdkang.springcloud</groupId>
        <artifactId>microservicecloud</artifactId>
        <version>1.0-SNAPSHOT</version>
    </parent>

    <artifactId>microservicecloud-eureka-7001</artifactId>

    <dependencies>
        <!-- eureka-server服务端 -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-eureka-server</artifactId>
        </dependency>
        <!-- 修改后立即生效，热部署 -->
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>springloaded</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
        </dependency>
    </dependencies>
</project>
```



## 1.3 配置信息

application.yml

```yaml
server: 
  port: 7001

eureka:
  instance:
    hostname: localhost # eureka服务端的实例名称
  client:
    register-with-eureka: false # false表示不向注册中心注册自己。
    fetch-registry: false # false表示自己端就是注册中心，我的职责就是维护服务实例，并不需要去检索服务
    service-url:
      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/ # 设置与Eureka Server交互的地址查询服务和注册服务都需要依赖这个地址。
```



## 1.4 测试项目

启动访问

**http://localhost:7001/**



# 2.新建子模块 provider-dept-8001

## 2.1 新建module

新建module **microservicecloud-provider-dept-8001**

选择父工程

## 2.2 POM文件

```xml
<!-- 将微服务provider侧注册进eureka -->
<dependency>
<groupId>org.springframework.cloud</groupId>
<artifactId>spring-cloud-starter-eureka</artifactId>
</dependency>

<dependency>
<groupId>org.springframework.cloud</groupId>
<artifactId>spring-cloud-starter-config</artifactId>
</dependency>
```



## 2.3 配置文件

application.yml

```yaml
eureka:
  client: # 客户端注册进eureka服务列表内
    service-url: 
      defaultZone: http://localhost:7001/eureka
```



## 2.4 修改启动类

**添加注解**

```java
@EnableEurekaClient // 本服务启动后会自动注册进eureka服务中
```



## 2.5  测试项目

**http://localhost:7001/**



## 2.6 修改注册eureka时的名字

修改yml中的   **instance-id** 为
 **microservicecloud-dept8001**

正常情况下 应该和spring application name一致

```yaml
server:
  port: 8001

mybatis:
    config-location: classpath:mybatis/mybatis.cfg.xml
    type-aliases-package: com.wdkang.springcloud.entities
    mapper-locations:
        - classpath:mybatis/mapper/**/*.xml

spring:
    application:
        name: microservicecloud-dept
    datasource:
        type: com.alibaba.druid.pool.DruidDataSource
        driver-class-name: org.gjt.mm.mysql.Driver
        url: jdbc:mysql://localhost:3306/cloudDB01
        username: root
        password: 123123
    dbcp2:
        min-idle: 5
        initial-size: 5
        max-total: 5
        max-wait-millis: 200

eureka:
  client: #客户端注册进eureka服务列表内
    service-url:
      defaultZone: http://localhost:7001/eureka
  instance:
    instance-id: microservicecloud-dept8001
    prefer-ip-address: true

```

修改之后重新启动项目



# 3.Disconvery服务

## 3.1 Disconvery

修改8001下的DeptController

```java
package com.wdkang.springcloud.controller;


import com.wdkang.springcloud.entities.Dept;
import com.wdkang.springcloud.service.DeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class DeptController {
    @Autowired
    private DeptService deptService;

    @Autowired
    private DiscoveryClient discoveryClient;

    private static final String REST_URL_PREFIX = "http://localhost:8001";

    @RequestMapping(value="/dept/add",method = RequestMethod.POST)

    public boolean add(@RequestBody Dept dept) {
        return deptService.add(dept);
    }

    @RequestMapping(value="/dept/get/{id}",method = RequestMethod.GET)
    public Dept get(@PathVariable("id") Long id) {
        return deptService.get(id);
    }

    @RequestMapping(value="/dept/list",method=RequestMethod.GET)
    public List<Dept> list() {
        return deptService.list();
    }

    // 测试@EnableDiscoveryClient, 消费端可以调用服务发现
    @RequestMapping(value = "/dept/discovery", method = RequestMethod.GET)
    public Object discovery() {
        List<String> list = discoveryClient.getServices();
        System.out.println("**********" + list);
        List<ServiceInstance> srvList = discoveryClient.getInstances("MICROSERVICECLOUD-DEPT");
        for (ServiceInstance element : srvList) {
            System.out.println(element.getServiceId() + "\t" + element.getHost() + "\t" + element.getPort() + "\t"
                    + element.getUri());
        }
        return discoveryClient;
    }
}
```

在8001中的主自动类上加

```
@EnableDiscoveryClient
```



## 3.2 调用Disconvery服务

修改80的DeptController_Consumer

```java
package com.wdkang.springcloud.controller;


import com.wdkang.springcloud.entities.Dept;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
public class DeptController_Consumer {
    private static final String REST_URL_PREFIX = "http://localhost:8001";

    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping(value="/consumer/dept/add")
    public boolean add(Dept dept) {
        return restTemplate.postForObject(REST_URL_PREFIX+"/dept/add", dept, Boolean.class);
    }

    @RequestMapping(value="/consumer/dept/get/{id}")
    public Dept get(@PathVariable("id") Long id) {
        return restTemplate.getForObject(REST_URL_PREFIX+"/dept/get/"+id, Dept.class);
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value="/consumer/dept/list")
    public List<Dept> list() {
        return restTemplate.getForObject(REST_URL_PREFIX+"/dept/list", List.class);
    }

    // 测试@EnableDiscoveryClient, 消费端可以调用服务发现
    @RequestMapping(value="/consumer/dept/discovery")
    public Object discovery() {
        return restTemplate.getForObject(REST_URL_PREFIX+"/dept/discovery", Object.class);
    }
}
```


