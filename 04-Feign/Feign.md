# 04 - Feign 负载均衡

## 1.新建microservicecloud-dept-feign

**POM文件**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <artifactId>microservicecloud</artifactId>
        <groupId>com.wdkang.springcloud</groupId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>microservicecloud-consumer-dept-feign</artifactId>

    <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
    </properties>

    <dependencies>
        <dependency><!-- 自己定义的api -->
            <groupId>com.wdkang.springcloud</groupId>
            <artifactId>microservicecloud-api</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
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

        <!-- Ribbon相关 -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-eureka</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-ribbon</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-config</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-feign</artifactId>
        </dependency>
    </dependencies>
</project>
```



## 2.修改microservicecloud-api

**pom为**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <groupId>com.wdkang.springcloud</groupId>
        <artifactId>microservicecloud</artifactId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>microservicecloud-api</artifactId>

    <dependencies><!-- 当前Module需要用到的jar包，按自己需求添加，如果父类已经包含了，可以不用写版本号 -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-feign</artifactId>
        </dependency>
    </dependencies>
</project>
```

**application.yml**

```yaml
server:
  port: 8000

spring:
  application:
    name: microservicecloud-dept-feign

eureka:
  client:
    service-url:
      defaultZone: http://localhost:7001/eureka/
  instance:
    instance-id: microservicecloud-dept-feign
    prefer-ip-address: true
```



## **3.新建DeptClientService接口**

```java
package com.wdkang.springcloud.service;


import com.wdkang.springcloud.entities.Dept;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@FeignClient(value = "MICROSERVICECLOUD-DEPT")
public interface DeptClientService {
    @RequestMapping(value = "/dept/get/{id}",method = RequestMethod.GET)
    Dept get(@PathVariable("id") long id);

    @RequestMapping(value = "/dept/list",method = RequestMethod.GET)
    List<Dept> list();

    @RequestMapping(value = "/dept/add",method = RequestMethod.POST)
    boolean add(Dept dept);
}

```



## 4.修改DeptController_Feign

```java
package com.wdkang.springcloud.controller;


import com.wdkang.springcloud.entities.Dept;
import com.wdkang.springcloud.service.DeptClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DeptController_Feign {

    @Autowired
    private DeptClientService deptClientService = null;

    @RequestMapping(value = "/consumer/dept/get/{id}")
    public Dept get(@PathVariable("id") Long id) {
        return this.deptClientService.get(id);
    }

    @RequestMapping(value = "/consumer/dept/list")
    public List<Dept> list() {
        return this.deptClientService.list();
    }

    @RequestMapping(value = "/consumer/dept/add")
    public Object add(Dept dept) {
        return this.deptClientService.add(dept);
    }

}

```



## 5.修改启动类

```java
package com.wdkang.springcloud;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;


@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients(basePackages= {"com.wdkang.springcloud"})
public class DeptConsumer80_Feign_App {
    public static void main(String[] args) {
        SpringApplication.run(DeptConsumer80_Feign_App.class, args);
    }
}

```



## 6.启动项目

启动7001

启动8001

启动8002

启动8003

启动8000（此处的8000为Feign的那个项目：microservicecloud-consumer-dept-feign）



## 7.测试

**http://localhost:8000/consumer/dept/list**

```
[{"deptno":1,"dname":"开发部","db_source":"clouddb03"},{"deptno":2,"dname":"人事部","db_source":"clouddb03"},{"deptno":3,"dname":"财务部","db_source":"clouddb03"},{"deptno":4,"dname":"市场部","db_source":"clouddb03"},{"deptno":5,"dname":"运维部","db_source":"clouddb03"}]
```

```
[{"deptno":1,"dname":"开发部","db_source":"clouddb02"},{"deptno":2,"dname":"人事部","db_source":"clouddb02"},{"deptno":3,"dname":"财务部","db_source":"clouddb02"},{"deptno":4,"dname":"市场部","db_source":"clouddb02"},{"deptno":5,"dname":"运维部","db_source":"clouddb02"}]
```

```
[{"deptno":1,"dname":"开发部","db_source":"clouddb01"},{"deptno":2,"dname":"人事部","db_source":"clouddb01"},{"deptno":3,"dname":"财务部","db_source":"clouddb01"},{"deptno":4,"dname":"市场部","db_source":"clouddb01"},{"deptno":5,"dname":"运维部","db_source":"clouddb01"}]
```



**Feign自带负载均衡配置项**



## 8.总结

 Feign通过接口的方法调用Rest服务（之前是Ribbon+RestTemplate），

该请求发送给Eureka服务器（http://MICROSERVICECLOUD-DEPT/dept/list）,

通过Feign直接找到服务接口，由于在进行服务调用的时候融合了Ribbon技术，所以也支持负载均衡作用。

 