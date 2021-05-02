# 05 - Hystrix 熔断器



# 1.分布式系统面临的问题

## 1.1 服务雪崩

多个微服务之间调用的时候，假设微服务A调用微服务B和微服务C，微服务B和微服务C又调用其它的微服务，这就是所谓的“扇出”。如果扇出的链路上某个微服务的调用响应时间过长或者不可用，对微服务A的调用就会占用越来越多的系统资源，进而引起系统崩溃，所谓的“雪崩效应”.

对于高流量的应用来说，单一的后端依赖可能会导致所有服务器上的所有资源都在几秒钟内饱和。比失败更糟糕的是，这些应用程序还可能导致服务之间的延迟增加，备份队列，线程和其他系统资源紧张，导致整个系统发生更多的级联故障。这些都表示需要对故障和延迟进行隔离和管理，以便单个依赖关系的失败，不能取消整个应用程序或系统。



## 1.2 解决方案

**熔断模式：**这种模式主要是参考电路熔断，如果一条线路电压过高，保险丝会熔断，防止火灾。放到我们的系统中，如果某个目标服务调用慢或者有大量超时，此时，熔断该服务的调用，对于后续调用请求，不在继续调用目标服务，直接返回，快速释放资源。如果目标服务情况好转则恢复调用。



**隔离模式：**这种模式就像对系统请求按类型划分成一个个小岛的一样，当某个小岛被火少光了，不会影响到其他的小岛。例如可以对不同类型的请求使用线程池来资源隔离，每种类型的请求互不影响，如果一种类型的请求线程资源耗尽，则对后续的该类型请求直接返回，不再调用后续资源。这种模式使用场景非常多，例如将一个服务拆开，对于重要的服务使用单独服务器来部署，再或者公司最近推广的多中心。



**限流模式：**上述的**熔断模式**和**隔离模式**都属于出错后的**容错处理机制**，而**限流模式**则可以称为**预防模式**。限流模式主要是提前对各个类型的请求设置最高的QPS阈值，若高于设置的阈值则对该请求直接返回，不再调用后续资源。这种模式不能解决服务依赖的问题，只能解决系统整体资源分配问题，因为没有被限流的请求依然有可能造成雪崩效应。



# 2.Hystrix的作用

## 2.1 服务降级

Hystrix服务降级，其实就是线程池中单个线程障处理，防止单个线程请求时间太长，导致资源长期被占有而得不到释放，从而导致线程池被快速占用完，导致服务崩溃。

**Hystrix能解决如下问题**

1.请求超时降级，线程资源不足降级，降级之后可以返回自定义数据

2.线程池隔离降级，分布式服务可以针对不同的服务使用不同的线程池，从而互不影响

3.自动触发降级与恢复

4.实现请求缓存和请求合并



## 2.2 服务熔断

熔断模式：这种模式主要是参考电路熔断，如果一条线路电压过高，保险丝会熔断，防止火灾。放到我们的系统中，如果某个目标服务调用慢或者有大量超时，此时，熔断该服务的调用，对于后续调用请求，不在继续调用目标服务，直接返回，快速释放资源。如果目标服务情况好转则恢复调用。



## 2.3 服务限流

限流模式主要是提前对各个类型的请求设置最高的QPS阈值，若高于设置的阈值则对该请求直接返回，不再调用后续资源。这种模式不能解决服务依赖的问题，只能解决系统整体资源分配问题，因为没有被限流的请求依然有可能造成雪崩效应。



## 2.4 进行监控 Hystrix DashBoard

（后边有）



# 3.服务熔断

## 3.1 是什么

熔断机制是应对雪崩效应的一种微服务链路保护机制。

当扇出链路的某个微服务不可用或者响应时间太长时，会进行服务的降级，进而熔断该节点微服务的调用，快速返回"错误"的响应信息。

当检测到该节点微服务调用响应正常后恢复调用链路。

Hystrix会监控微服务间调用的状况，当失败的调用到一定阈值，**缺省是5秒内20次调用失败就会启动熔断机制**。

熔断机制的注解是**@HystrixCommand**。



## 3.2 新建项目

新建maven项目 父工程microservicecloud

名称为：**microservicecloud-provider-dept-hystrix-8001**



**pom项目**

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

    <artifactId>microservicecloud-provider-dept-hystrix-8001</artifactId>

    <dependencies>
        <!--  hystrix -->
        <!-- https://mvnrepository.com/artifact/org.springframework.cloud/spring-cloud-starter-hystrix -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-hystrix</artifactId>
        </dependency>

        <!-- 引入自己定义的api通用包，可以使用Dept部门Entity -->
        <dependency>
            <groupId>com.wdkang.springcloud</groupId>
            <artifactId>microservicecloud-api</artifactId>
            <version>${project.version}</version>
        </dependency>
        <!-- 将微服务provider侧注册进eureka -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-eureka</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-config</artifactId>
        </dependency>
        <!-- actuator监控信息完善 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
        </dependency>
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
        </dependency>
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>druid</artifactId>
        </dependency>
        <dependency>
            <groupId>ch.qos.logback</groupId>
            <artifactId>logback-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.mybatis.spring.boot</groupId>
            <artifactId>mybatis-spring-boot-starter</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-jetty</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
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



**application.yml**

```yaml
server:
  port: 8001

mybatis:
  config-location: classpath:mybatis/mybatis.cfg.xml  #mybatis所在路径
  type-aliases-package: com.wdkang.springcloud.entities #entity别名类
  mapper-locations:
    - classpath:mybatis/mapper/**/*.xml #mapper映射文件

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
      defaultZone: http://localhost:7001/eureka/
  instance:
    instance-id: microservicecloud-dept8001-hystrix   #自定义服务名称信息
    prefer-ip-address: true     #访问路径可以显示IP地址

info:
  app.name: wdkang-microservicecloud
  company.name: www.wdkang.top
  build.artifactId: $project.artifactId$
  build.version: $project.version$

```



仿造原来的8001 （microservicecloud-provider-dept-8001）

复制过来 service层、dao层、mapper文件之类的都拿来



**接着新建controller层**

注意注解**@HystrixCommand**的位置

```java
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
    private DeptService service = null;

    @RequestMapping(value="/dept/get/{id}",method= RequestMethod.GET)
    @HystrixCommand(fallbackMethod = "processHystrix_Get") // 一旦方法调用出现错误 那么直接调用processHystrix_Get返回
    public Dept get(@PathVariable("id") Long id) {
        Dept dept =  this.service.get(id);
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
```



**然后修改启动类**

```java
package com.wdkang.springcloud;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient //本服务启动后会自动注册进eureka服务中
@EnableDiscoveryClient //服务发现
@EnableCircuitBreaker//对hystrixR熔断机制的支持
public class DeptProvider8001_Hystrix_App {
    public static void main(String[] args) {
        SpringApplication.run(DeptProvider8001_Hystrix_App.class, args);
    }
}
```





## 3.3 启动项目

启动7001

启动microservicecloud-dept-80项目

启动microservicecloud-provider-dept-hystrix-8001



## 3.4 测试

访问

**http://localhost:8000/consumer/dept/get/112**

```
{"deptno":112,"dname":"该ID：112没有没有对应的信息,null--@HystrixCommand","db_source":"no this database in MySQL"}
```

注意这里返回的结果是

```
processHystrix_Get 该函数的运行结果
```



# 4.服务降级

## 4.1 是什么

整体资源快不够了，忍痛将某些服务先关掉，待渡过难关，再开启回来。

且 降级处理是在客户端完成的 和 服务端没有任何关系



## 4.2 搭建项目

修改**microservicecloud-api**工程

根据已经有的DeptClientService接口新建一个**DeptClientServiceFallbackFactory**

千万不要忘记加上**@Component**

```java
package com.wdkang.springcloud.service;

import com.wdkang.springcloud.entities.Dept;
import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;


@Component // 切记不要忘记Component
public class DeptClientServiceFallbackFactory implements FallbackFactory<DeptClientService> {
    @Override
    public DeptClientService create(Throwable throwable) {
        return new DeptClientService() {
            @Override
            public Dept get(long id) {
                Dept dept = new Dept();
                dept.setDeptno(id);
                dept.setDname("该ID："+id+"没有没有对应的信息,Consumer客户端提供的降级信息,此刻服务Provider已经关闭");
                dept.setDb_source("no this database in MySQL");
                return dept;
            }

            @Override
            public List<Dept> list() {
                return null;
            }

            @Override
            public boolean add(Dept dept) {
                return false;
            }
        };
    }
}
```



修改**microservicecloud-api**工程

**DeptClientService**类的注解

```java
package com.wdkang.springcloud.service;


import com.wdkang.springcloud.entities.Dept;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@FeignClient(value = "MICROSERVICECLOUD-DEPT",fallbackFactory=DeptClientServiceFallbackFactory.class)
public interface DeptClientService {
    @RequestMapping(value = "/dept/get/{id}",method = RequestMethod.GET)
    Dept get(@PathVariable("id") long id);

    @RequestMapping(value = "/dept/list",method = RequestMethod.GET)
    List<Dept> list();

    @RequestMapping(value = "/dept/add",method = RequestMethod.POST)
    boolean add(Dept dept);
}

```



修改**microservicecloud-consumer-dept-feign**工程的yml

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

feign:
  hystrix:
    enabled: true
```



## 4.3 启动项目

**api那个项目 最好是 mvn clean 和 install 一下**

启动microservicecloud-provider-dept-8001

启动microservicecloud-consumer-dept-feign

启动7001



## 4.4 测试

**http://localhost:8000/consumer/dept/get/1**

```
{"deptno":1,"dname":"开发部","db_source":"clouddb01"}
```



此时我们关掉8001的服务 继续测试该接口

**http://localhost:8000/consumer/dept/get/1**

```
{"deptno":1,"dname":"该ID：1没有没有对应的信息,Consumer客户端提供的降级信息,此刻服务Provider已经关闭","db_source":"no this database in MySQL"}
```

我们发现已经降级了





# 5.流量监控 Hystrix DashBoard

## 5.1 新建项目

新建maven工程 叫 **microservicecloud-consumer-hystrix-dashboard**

父级为**microservicecloud**

**pom为**

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

    <artifactId>microservicecloud-consumer-hystrix-dashboard</artifactId>

    <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
    </properties>
    <dependencies>
        <!-- 自己定义的api -->
        <dependency>
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
        <!-- feign相关 -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-feign</artifactId>
        </dependency>
        <!-- hystrix和 hystrix-dashboard相关-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-hystrix</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-hystrix-dashboard</artifactId>
        </dependency>
    </dependencies>

</project>
```



**application.yml为**

```yaml
server:
  port: 9001
```



注意需要被监控的羡慕 要有（类似8001、8002、8003等）

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```



## 5.2 创建启动类

注意添加注解**@EnableHystrixDashboard**

```java
package com.wdkang.springcloud;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;

@SpringBootApplication
@EnableHystrixDashboard
public class DeptConsumer_DashBoard_App {
    public static void main(String[] args) {
        SpringApplication.run(DeptConsumer_DashBoard_App.class, args);
    }
}
```



## 5.3 启动项目

启动7001

启动9001（当前项目）

启动**microservicecloud-provider-dept-hystrix-8001**别启动错了



## 5.4 测试

访问

**http://localhost:9001/hystrix**



将这个地址填入到框中进行监控

**http://localhost:8001/hystrix.stream**



最后访问几次链接 

**http://localhost:8001/dept/get/1**



就可以看到如下的效果了

【1】

