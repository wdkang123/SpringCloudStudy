# SpringCloud Config

# 1.概述

## 2.1 配置问题

微服务意味着要将单体应用中的业务拆分成一个个子服务，每个服务的粒度相对较小，因此系统中会出现大量的服务。

由于每个服务都需要必要的配置信息才能运行，所以一套集中式的、动态的配置管理设施是必不可少的。

SpringCloud提供了ConfigServer来解决这个问题，我们每一个微服务自己带着一个application.yml，上百个配置文件的管理。



## 2.2 是什么

SpringCloud Config为微服务架构中的微服务提供集中化的外部配置支持，配置服务器为各个不同微服务应用的所有环境提供了一个中心化的外部配置。

 

## 2.3 怎么用

SpringCloud Config分为服务端和客户端两部分。

服务端也称为分布式配置中心，它是一个独立的微服务应用，用来连接配置服务器并为客户端提供获取配置信息，加密/解密信息等访问接口

客户端则是通过指定的配置中心来管理应用资源，以及与业务相关的配置内容，并在启动的时候从配置中心获取和加载配置信息配置服务器默认采用git来存储配置信息，这样就有助于对环境配置进行版本管理，并且可以通过git客户端工具来方便的管理和访问配置内容。



## 2.4 有啥用

- 集中管理配置文件
- 不同环境不同配置 动态化配置更新
- 运行期间调整配置 服务统一向配置中心拉取自己的配置信息
- 当配置发生改变时 服务不需要重启即可感知到配置的变化并且应用新的配置
- 将配置信息以REST接口形式暴露



## 2.5 与GitHub整合配置

由于SpringCloud Config 默认使用git来存储配置文件 而且使用的是http或者是https



# 2.服务端配置

## 2.1 新建git仓库 

创建一个git仓库（我这里使用的是 本笔记用的仓库）可以独立新建一个

**https://github.com/wdkang123/SpringCloudStudy**

文件我已经上传好了

**https://github.com/wdkang123/SpringCloudStudy/blob/main/config/application.yml**

接着拿好git的地址

**https://github.com/wdkang123/SpringCloudStudy.git**

**git@github.com:wdkang123/SpringCloudStudy.git**



## 2.2 通过git上传配置文件

```yaml
spring:
  profiles:
    active:
      - dev
---
spring:
  profiles: dev     #开发环境
  application:
    name: microservicecloud-config-atguigu-dev
---
spring:
  profiles: test   #测试环境
  application:
    name: microservicecloud-config-atguigu-test
#  请保存为UTF-8格式
```



## 2.3 新建项目

新建maven项目**microservicecloud-config-3344**

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

    <artifactId>microservicecloud-config-3344</artifactId>

    <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
    </properties>

    <dependencies>
        <!-- springCloud Config -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-config-server</artifactId>
        </dependency>
        <!-- 避免Config的Git插件报错：org/eclipse/jgit/api/TransportConfigCallback  -->
        <dependency>
            <groupId>org.eclipse.jgit</groupId>
            <artifactId>org.eclipse.jgit</artifactId>
            <version>4.10.0.201712302008-r</version>
        </dependency>
        <!-- 图形化监控 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <!-- 熔断 -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-hystrix</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-eureka</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-config</artifactId>
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



**application.yml**

配置uri地址为git项目地址

```yaml
server:
  port: 3344

spring:
  application:
    name:  microservicecloud-config
  cloud:
    config:
      label: main
      server:
        git:
          uri: https://github.com/wdkang123/SpringCloudStudy
          search-paths: config

```



## 2.4 新建启动类

```java
package com.wdkang.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class Config_3344_StartSpringCloudApp {
    public static void main(String[] args) {
        SpringApplication.run(Config_3344_StartSpringCloudApp.class, args);
    }
}
```



## 2.5 启动项目

启动该项目 3344



## 2.6 测试

这里是个大坑 弄了好几小时我才搞明白这个逻辑

**http://localhost:3344/main/microservicecloud-config-application-dev.yml**



**基本上所有的访问规则是：**

```
/{application}/{profile}[/{label}]

/{application}-{profile}.yml

/{label}/{application}-{profile}.yml

/{application}-{profile}.properties

/{label}/{application}-{profile}.properties
```



**http://localhost:3344/main/microservicecloud-config-application-dev.yml**

这个路径就拆分为了

**服务**：http://localhost:3344

**github分支**：main

**服务名称**（本项目的application的name）：microservicecloud-config

**配置文件**：application

**配置文件中的分支**：dev （spring: profiles: dev）

**后缀**：yml

**访问结果**：

```yaml
spring:
  profiles:
    active:
    - dev
  application:
    name: microservicecloud-config-wdkang-dev
```



**再来测试一个**

**http://localhost:3344/main/microservicecloud-config-application-test.yml**

```yaml
spring:
  profiles:
    active:
    - dev
  application:
    name: microservicecloud-config-wdkang-test
```



# 3.客户端配置

## 3.1 新建文件

新建一个配置文件 上传到github上

**https://github.com/wdkang123/SpringCloudStudy/tree/main/config**

```yaml
spring:
  profiles:
    active:
      - dev
---
server:
  port: 8201
spring:
  profiles: dev
  application:
    name: microservicecloud-config-client
eureka:
  client:
    service-url:
      defaultZone: http://localhost:7001/eureka/
---
server:
  port: 8202
spring:
  profiles: test
  application:
    name: microservicecloud-config-client
eureka:
  client:
    service-url:
      defaultZone: http://localhost:7001/eureka/
```



## 3.2 新建项目

新建项目**microservicecloud-config-client-3355**

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

    <artifactId>microservicecloud-config-client-3355</artifactId>

    <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
    </properties>
    <dependencies>
        <!-- SpringCloud Config客户端 -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-config</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-hystrix</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-eureka</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-config</artifactId>
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



## 3.3 配置文件

**applicaiton.yml是用户级的资源配置项**

**bootstrap.yml是系统级的，优先级更加高**



Spring Cloud会创建一个`Bootstrap Context`，作为Spring应用的`Application Context`的父上下文。

初始化的时候，`Bootstrap Context`负责从外部源加载配置属性并解析配置。

这两个上下文共享一个从外部获取的`Environment`。`Bootstrap`属性有高优先级，默认情况下，它们不会被本地配置覆盖。

 `Bootstrap context`和`Application Context`有着不同的约定，

所以新增了一个`bootstrap.yml`文件，保证`Bootstrap Context`和`Application Context`配置的分离。



**bootstrap.yml**

```yml
spring:
  cloud:
    config:
      name: microservicecloud-config-client #需要从github上读取的资源名称，注意没有yml后缀名
      profile: dev   #本次访问的配置项
      label: main
      uri: http://localhost:3344  #本微服务启动后先去找3344号服务，通过SpringCloudConfig获取GitHub的服务地址
```



**application.yml**

```yaml
spring:
  application:
    name: microservicecloud-config-client
```



## 3.4 编写rest检验配置文件生效效果

```java
package com.wdkang.springcloud.rest;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConfigClientRest {
    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${eureka.client.service-url.defaultZone}")
    private String eurekaServers;

    @Value("${server.port}")
    private String port;

    @RequestMapping("/config")
    public String getConfig() {
        String str = "applicationName: "+applicationName+"\t eurekaServers:"+eurekaServers+"\t port: "+port;
        System.out.println("******str: "+ str);
        return "applicationName: "+applicationName+"\t eurekaServers:"+eurekaServers+"\t port: "+port;
    }
}

```



## 3.5 启动类

```java
package com.wdkang.springcloud;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ConfigClient_3355_StartSpringCloudApp {
    public static void main(String[] args) {
        SpringApplication.run(ConfigClient_3355_StartSpringCloudApp.class,args);
    }
}
```



## 3.6 启动项目

启动3344

启动3355



**先看看这个是否通过**

**http://localhost:3344/main/microservicecloud-config-client-dev.yml**

```yaml
spring:
  profiles:
    active:
    - dev
  application:
    name: microservicecloud-config-client
eureka:
  client:
    service-url:
      defaultZone: http://localhost:7001/eureka/
server:
  port: 8201
```



## 3.7 测试

没有报错



**访问**

**http://localhost:8201/config**

```
applicationName: microservicecloud-config-client eurekaServers:http://localhost:7001/eureka/ port: 8201
```

浏览器返回了对应的结果

所以说明config读取正确





# 4.SpringCloud Config 实战

## 4.1 新建配置文件

**microservicecloud-config-eureka-client.yml**

```yaml
spring:
  profiles:
    active:
      - dev
---
server:
  port: 7001 #注册中心占用7001端口,冒号后面必须要有空格

spring:
  profiles: dev
  application:
    name: microservicecloud-config-eureka-client

eureka:
  instance:
    hostname: localhost #冒号后面必须要有空格
  client:
    register-with-eureka: false #当前的eureka-server自己不注册进服务列表中
    fetch-registry: false #不通过eureka获取注册信息
    service-url:
      defaultZone: http://localhost:7001/eureka/
---
server:
  port: 7001 #注册中心占用7001端口,冒号后面必须要有空格

spring:
  profiles: test
  application:
    name: microservicecloud-config-eureka-client

eureka:
  instance:
    hostname: localhost #冒号后面必须要有空格
  client:
    register-with-eureka: false #当前的eureka-server自己不注册进服务列表中
    fetch-registry: false #不通过eureka获取注册信息
    service-url:
      defaultZone: http://localhost:7001/eureka/
```



**microservicecloud-config-dept-client.yml**

```yaml
spring:
  profiles:
    active:
      - dev
---
server:
  port: 8001
spring:
  profiles: dev
  application:
    name: microservicecloud-config-dept-client
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
mybatis:
  config-location: classpath:mybatis/mybatis.cfg.xml
  type-aliases-package: com.wdkang.springcloud.entities
  mapper-locations:
    - classpath:mybatis/mapper/**/*.xml

eureka:
  client: #客户端注册进eureka服务列表内
    service-url:
      defaultZone: http://localhost:7001/eureka
  instance:
    instance-id: dept-8001
    prefer-ip-address: true

info:
  app.name: wdkang-microservicecloud-springcloudconfig01
  company.name: www.wdkang.top
  build.artifactId: $project.artifactId$
  build.version: $project.version$
---
server:
  port: 8001
spring:
  profiles: test
  application:
    name: microservicecloud-config-dept-client
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    driver-class-name: org.gjt.mm.mysql.Driver
    url: jdbc:mysql://localhost:3306/cloudDB02
    username: root
    password: 123123
    dbcp2:
      min-idle: 5
      initial-size: 5
      max-total: 5
      max-wait-millis: 200


mybatis:
  config-location: classpath:mybatis/mybatis.cfg.xml
  type-aliases-package: com.wdkang.springcloud.entities
  mapper-locations:
    - classpath:mybatis/mapper/**/*.xml

eureka:
  client: #客户端注册进eureka服务列表内
    service-url:
      defaultZone: http://localhost:7001/eureka
  instance:
    instance-id: dept-8001
    prefer-ip-address: true

info:
  app.name: wdkang-microservicecloud-springcloudconfig02
  company.name: www.wdkang.top
  build.artifactId: $project.artifactId$
  build.version: $project.version$
```



## 4.2 新建工程

新建maven项目**microservicecloud-config-eureka-client-7001**

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

    <artifactId>microservicecloud-config-eureka-client-7001</artifactId>

    <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
    </properties>
    <dependencies>
        <!-- SpringCloudConfig配置 -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-config</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-eureka-server</artifactId>
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



**bootstrap.yml**

```yaml
spring:
  cloud:
    config:
      name: microservicecloud-config-eureka-client     #需要从github上读取的资源名称，注意没有yml后缀名
      profile: dev
      label: master
      uri: http://localhost:3344      #SpringCloudConfig获取的服务地址
```



**application.yml**

```yaml
spring:
  application:
    name: microservicecloud-config-eureka-client
```



## 4.3 启动项目

启动 3344 （config的服务器）

确保没有问题之后

启动该项目

测试如果eureka访问正常的 则OK



## 4.4 测试

**http://localhost:7001/**

成功访问到了eureka



## 4.5 继续搭建项目

创建maven项目**microservicecloud-config-dept-client-8001**

父工程为**microservicecloud**

其内容结构仿造之前的**microservicecloud-provier-dept-8001**



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

    <artifactId>microservicecloud-config-dept-client-8001</artifactId>

    <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
    </properties>
    <dependencies>
        <!-- SpringCloudConfig配置 -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-config</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-eureka</artifactId>
        </dependency>
        <dependency>
            <groupId>com.wdkang.springcloud</groupId>
            <artifactId>microservicecloud-api</artifactId>
            <version>${project.version}</version>
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



**bootstrap.yml**

```yaml
spring:
  cloud:
    config:
      name: microservicecloud-config-dept-client #需要从github上读取的资源名称，注意没有yml后缀名
      #profile配置是什么就取什么配置dev or test
      #profile: dev
      profile: test
      label: main
      uri: http://localhost:3344  #SpringCloudConfig获取的服务地址
```



**application.yml**

```yaml
spring:
  application:
    name: microservicecloud-config-dept-client
```



其余的部分和之前的8001一致即可



## 4.6 创建启动类

```java
package com.wdkang.springcloud;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient //本服务启动后会自动注册进eureka服务中
@EnableDiscoveryClient //服务发现
public class DeptProvider8001_App {
    public static void main(String[] args) {
        SpringApplication.run(DeptProvider8001_App.class, args);
    }
}
```



## 4.7 启动项目

启动3344

启动**microservicecloud-config-eureka-client-7001**

启动**microservicecloud-config-dept-client-8001**



## 4.8 测试

启动不了就多尝试几次 这个3344是真的自闭 好几次起不来

启动之后访问

**http://localhost:7001/**

访问正常



接着访问

**http://localhost:8001/dept/list**

```
[{"deptno":1,"dname":"开发部","db_source":"clouddb02"},{"deptno":2,"dname":"人事部","db_source":"clouddb02"},{"deptno":3,"dname":"财务部","db_source":"clouddb02"},{"deptno":4,"dname":"市场部","db_source":"clouddb02"},{"deptno":5,"dname":"运维部","db_source":"clouddb02"}]
```



将本地的**profile: test**换成**profile: dev**

重新启动后再次访问

**http://localhost:8001/dept/list**

```
[{"deptno":1,"dname":"开发部","db_source":"clouddb01"},{"deptno":2,"dname":"人事部","db_source":"clouddb01"},{"deptno":3,"dname":"财务部","db_source":"clouddb01"},{"deptno":4,"dname":"市场部","db_source":"clouddb01"},{"deptno":5,"dname":"运维部","db_source":"clouddb01"}]
```









































