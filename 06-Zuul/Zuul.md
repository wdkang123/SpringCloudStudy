# 06 - Zuul

# 1.是什么

Zuul包含了对请求的**路由**和**过滤**两个最主要的功能：

其中路由功能负责将外部请求转发到具体的微服务实例上

是实现外部访问统一入口的基础而过滤器功能则负责对请求的处理过程进行干预

是实现请求校验、服务聚合等功能的基础.Zuul和Eureka进行整合，将Zuul自身注册为Eureka服务治理下的应用

同时从Eureka中获得其他微服务的消息，也即以后的访问微服务都是通过Zuul跳转后获得。

**注意：Zuul服务最终还是会注册进Eureka**

**提供 = 代理 + 路由 + 过滤 三大功能**

 

# 2.路由基本配置

## 2.1 搭建项目

新建项目**microservicecloud-zuul-gateway-9572**

父项目为**microservicecloud**



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

    <artifactId>microservicecloud-zuul-gateway-9527</artifactId>

    <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
    </properties>
    <dependencies>
        <!-- zuul路由网关 -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-zuul</artifactId>
            <version>1.4.7.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-eureka</artifactId>
        </dependency>
        <!-- actuator监控 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <!--  hystrix容错-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-hystrix</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-config</artifactId>
        </dependency>
        <!-- 日常标配 -->
        <dependency>
            <groupId>com.wdkang.springcloud</groupId>
            <artifactId>microservicecloud-api</artifactId>
            <version>${project.version}</version>
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
        <!-- 热部署插件 -->
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



**application.yml**为

```yaml
server:
  port: 9527

spring:
  application:
    name: microservicecloud-zuul-gateway

eureka:
  client:
    service-url:
      defaultZone: http://localhost:7001/eureka
  instance:
    instance-id: gateway-9527
    prefer-ip-address: true


info:
  app.name: wdkang-microcloud
  company.name: www.wdkang.top
  build.artifactId: $project.artifactId$
  build.version: $project.version$
```



## 2.2 启动项目

启动7001

启动9527（当前项目）

启动**microservicecloud-provider-dept-8001**



## 2.3 测试

不通过Zuul访问

**http://localhost:8001/dept/get/1**

```
{"deptno":1,"dname":"开发部","db_source":"clouddb01"}
```

通过Zuul进行访问

**http://localhost:9527/microservicecloud-dept/dept/get/1**

```
{"deptno":1,"dname":"开发部","db_source":"clouddb01"}
```





# 3.路由访问映射

## 3.1 配置映射

修改 **application.yml**

```yaml
server:
  port: 9527

spring:
  application:
    name: microservicecloud-zuul-gateway

eureka:
  client:
    service-url:
      defaultZone: http://localhost:7001/eureka
  instance:
    instance-id: gateway-9527
    prefer-ip-address: true

zuul:
  routes:
    mydept.serviceId: microservicecloud-dept
    mydept.path: /mydept/**

info:
  app.name: wdkang-microcloud
  company.name: www.wdkang.top
  build.artifactId: $project.artifactId$
  build.version: $project.version$
```



## 3.2 忽略真实服务名

ignored-services: microservicecloud-dept

单个用名字

多个用星号`*`

```yaml
server:
  port: 9527

spring:
  application:
    name: microservicecloud-zuul-gateway

eureka:
  client:
    service-url:
      defaultZone: http://localhost:7001/eureka
  instance:
    instance-id: gateway-9527
    prefer-ip-address: true

zuul:
  ignored-services: microservicecloud-dept
  routes:
    mydept.serviceId: microservicecloud-dept
    mydept.path: /mydept/**

info:
  app.name: wdkang-microcloud
  company.name: www.wdkang.top
  build.artifactId: $project.artifactId$
  build.version: $project.version$
```



## 3.3 设置统一公共前缀

```yaml
server:
  port: 9527

spring:
  application:
    name: microservicecloud-zuul-gateway

eureka:
  client:
    service-url:
      defaultZone: http://localhost:7001/eureka
  instance:
    instance-id: gateway-9527
    prefer-ip-address: true

zuul:
  prefix: /wdkang
  ignored-services: "*"
  routes:
    mydept.serviceId: microservicecloud-dept
    mydept.path: /mydept/**

info:
  app.name: wdkang-microcloud
  company.name: www.wdkang.top
  build.artifactId: $project.artifactId$
  build.version: $project.version$
```



