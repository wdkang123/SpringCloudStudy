# 03 - Ribbon

简单的说，Ribbon是Netflix发布的开源项目，主要功能是提供客户端的软件负载均衡算法，将Netflix的中间层服务连接在一起。

Ribbon客户端组件提供一系列完善的配置项如连接超时，重试等。简单的说，就是在配置文件中列出Load Balancer（简称LB）后面所有的机器，Ribbon会自动的帮助你基于某种规则（如简单轮询，随机连接等）去连接这些机器。

我们也很容易使用Ribbon实现自定义的负载均衡算法。



# 1.概述

LB，即负载均衡(Load Balance)，在微服务或分布式集群中经常用的一种应用。

负载均衡简单的说就是将用户的请求平摊的分配到多个服务上，从而达到系统的HA。

常见的负载均衡有软件Nginx，LVS，硬件 F5等。

相应的在中间件，例如：dubbo和SpringCloud中均给我们提供了负载均衡，SpringCloud的负载均衡算法可以自定义。 

 

## 1.1 集中式

即在服务的消费方和提供方之间使用独立的LB设施(可以是硬件，如F5, 也可以是软件，如nginx), 由该设施负责把访问请求通过某种策略转发至服务的提供方。



## 1.2 进程内

将LB逻辑集成到消费方，消费方从服务注册中心获知有哪些地址可用，然后自己再从这些地址中选择出一个合适的服务器。

Ribbon就属于进程内LB，它只是一个类库，集成于消费方进程，消费方通过它来获取到服务提供方的地址。



# 2.Ribbon配置

(后面都用端口来称呼工程了 手打全称太费劲了)

## 2.1 修改microservicecloud-consumer-dept-80工程

**POM中添加**

```xml
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
```



**yml中添加** 

我的80端口占用了 所以我改成了8000

```yaml
server:
  port: 8000

spring:
  application:
    name: microservicecloud-dept-80

eureka:
  client:
    service-url:
      defaultZone: http://localhost:7001/eureka/
  instance:
    instance-id: microservicecloud-dept80
    prefer-ip-address: true
```



## 2.2 修改ConfigBean

80工程下 修改ConfigBean 添加注解@LoadBalanced

```java
package com.wdkang.springcloud.cfgbeans;


import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ConfigBean {
    @Bean
    @LoadBalanced
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }
}

```



## 2.3 修改启动类

修改80工程的启动类

```java
package com.wdkang.springcloud;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class DeptConsumer80_App {
    public static void main(String[] args) {
        SpringApplication.run(DeptConsumer80_App.class, args);
    }
}
```



## 2.4 修改DeptController_Consumer

```java
private static final String REST_URL_PREFIX = "http://MICROSERVICECLOUD-DEPT";
```



## 2.5 测试

启动7001

启动8001

启动80



**http://localhost:8000/consumer/dept/get/1**

```
{"deptno":1,"dname":"开发部","db_source":"clouddb01"}
```



**http://localhost:8000/consumer/dept/list**

```
[{"deptno":1,"dname":"开发部","db_source":"clouddb01"},{"deptno":2,"dname":"人事部","db_source":"clouddb01"},{"deptno":3,"dname":"财务部","db_source":"clouddb01"},{"deptno":4,"dname":"市场部","db_source":"clouddb01"},{"deptno":5,"dname":"运维部","db_source":"clouddb01"}]
```



# 3.Ribbon负载均衡

## 3.1 构建项目

将8001的项目复制两个为8002和8003

注意修改pom中名字

还有父工程中的pom



**8001pom**

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

    <artifactId>microservicecloud-provider-dept-8001</artifactId>

    <dependencies>
        <dependency><!-- 引入自己定义的api通用包，可以使用Dept部门Entity -->
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

        <!-- 修改后立即生效，热部署 -->
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>springloaded</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
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

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
    </dependencies>

</project>
```



**8002 pom**

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
    <artifactId>microservicecloud-provider-dept-8002</artifactId>
    <dependencies>
        <dependency><!-- 引入自己定义的api通用包，可以使用Dept部门Entity -->
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

        <!-- 修改后立即生效，热部署 -->
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>springloaded</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
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

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
    </dependencies>
</project>
```



**8003 pom修改的地方**

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

    <artifactId>microservicecloud-provider-dept-8003</artifactId>

    <dependencies>
        <dependency><!-- 引入自己定义的api通用包，可以使用Dept部门Entity -->
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

        <!-- 修改后立即生效，热部署 -->
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>springloaded</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
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

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
    </dependencies>

</project>
```



**父工程pom修改的地方**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.wdkang.springcloud</groupId>
    <artifactId>microservicecloud</artifactId>
    <version>1.0-SNAPSHOT</version>
    <modules>
        <module>microservicecloud-api</module>
        <module>microservicecloud-provider-dept-8001</module>
        <module>microservicecloud-provider-dept-8002</module>
        <module>microservicecloud-provider-dept-8003</module>
        <module>microservicecloud-dept-80</module>
        <module>microservicecloud-eureka-7001</module>
    </modules>

    <packaging>pom</packaging>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.source>1.8</maven.compiler.source>
        <maven.compiler.target>1.8</maven.compiler.target>
        <junit.version>4.12</junit.version>
        <log4j.version>1.2.17</log4j.version>
        <lombok.version>1.16.18</lombok.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>Dalston.SR1</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>1.5.9.RELEASE</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <dependency>
                <groupId>mysql</groupId>
                <artifactId>mysql-connector-java</artifactId>
                <version>5.1.47</version>
            </dependency>
            <dependency>
                <groupId>com.alibaba</groupId>
                <artifactId>druid</artifactId>
                <version>1.0.31</version>
            </dependency>
            <dependency>
                <groupId>org.mybatis.spring.boot</groupId>
                <artifactId>mybatis-spring-boot-starter</artifactId>
                <version>1.3.0</version>
            </dependency>
            <dependency>
                <groupId>ch.qos.logback</groupId>
                <artifactId>logback-core</artifactId>
                <version>1.2.3</version>
            </dependency>
            <dependency>
                <groupId>junit</groupId>
                <artifactId>junit</artifactId>
                <version>${junit.version}</version>
                <scope>test</scope>
            </dependency>
            <dependency>
                <groupId>log4j</groupId>
                <artifactId>log4j</artifactId>
                <version>${log4j.version}</version>
            </dependency>
            <dependency>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <finalName>microservicecloud</finalName>
        <resources>
            <resource>
                <directory>src/main/resources</directory>
                <filtering>true</filtering>
            </resource>
        </resources>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-resources-plugin</artifactId>
                <configuration>
                    <delimiters>
                        <delimit>$</delimit>
                    </delimiters>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

修改后如果不识别 就挂掉idea重新启动一下



## 3.2 修改配置文件

新建数据库 02 和 03

```sql
DROP DATABASE IF EXISTS cloudDB02;
CREATE DATABASE cloudDB02 CHARACTER SET UTF8;
USE cloudDB02;
CREATE TABLE dept
(
  deptno BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  dname VARCHAR(60),
  db_source   VARCHAR(60)
);
INSERT INTO dept(dname,db_source) VALUES('开发部',DATABASE());
INSERT INTO dept(dname,db_source) VALUES('人事部',DATABASE());
INSERT INTO dept(dname,db_source) VALUES('财务部',DATABASE());
INSERT INTO dept(dname,db_source) VALUES('市场部',DATABASE());
INSERT INTO dept(dname,db_source) VALUES('运维部',DATABASE());
SELECT * FROM dept;
```

 

```sql
DROP DATABASE IF EXISTS cloudDB03;
CREATE DATABASE cloudDB02 CHARACTER SET UTF8;
USE cloudDB03;
CREATE TABLE dept
(
  deptno BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  dname VARCHAR(60),
  db_source   VARCHAR(60)
);
INSERT INTO dept(dname,db_source) VALUES('开发部',DATABASE());
INSERT INTO dept(dname,db_source) VALUES('人事部',DATABASE());
INSERT INTO dept(dname,db_source) VALUES('财务部',DATABASE());
INSERT INTO dept(dname,db_source) VALUES('市场部',DATABASE());
INSERT INTO dept(dname,db_source) VALUES('运维部',DATABASE());
SELECT * FROM dept;
```



**8001 yml**

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

info:
  app.name: wdkang-microservicecloud
  company.name: www.wdkang.top
  build.artifactId: $project.artifactId$
  build.version: $project.version$

```



**8002 yml**

```yaml
server:
  port: 8002

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
        url: jdbc:mysql://localhost:3306/cloudDB02
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
    instance-id: microservicecloud-dept8002
    prefer-ip-address: true

info:
  app.name: wdkang-microservicecloud
  company.name: www.wdkang.top
  build.artifactId: $project.artifactId$
  build.version: $project.version$
```



**8003 yml**

```yaml
server:
  port: 8003

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
        url: jdbc:mysql://localhost:3306/cloudDB03
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
    instance-id: microservicecloud-dept8003
    prefer-ip-address: true

info:
  app.name: wdkang-microservicecloud
  company.name: www.wdkang.top
  build.artifactId: $project.artifactId$
  build.version: $project.version$
```



## 3.3 测试

**http://localhost:8001/dept/list**

**http://localhost:8002/dept/list**

**http://localhost:8003/dept/list**

返回结果

```
[{"deptno":1,"dname":"开发部","db_source":"clouddb03"},{"deptno":2,"dname":"人事部","db_source":"clouddb03"},{"deptno":3,"dname":"财务部","db_source":"clouddb03"},{"deptno":4,"dname":"市场部","db_source":"clouddb03"},{"deptno":5,"dname":"运维部","db_source":"clouddb03"}]
```

确定3个服务都没有问题



## 3.4 启动80

访问

**http://localhost:8000/consumer/dept/list**

一直刷新 会看到 轮询的结果

```
[{"deptno":1,"dname":"开发部","db_source":"clouddb01"},{"deptno":2,"dname":"人事部","db_source":"clouddb01"},{"deptno":3,"dname":"财务部","db_source":"clouddb01"},{"deptno":4,"dname":"市场部","db_source":"clouddb01"},{"deptno":5,"dname":"运维部","db_source":"clouddb01"}]
```

```
[{"deptno":1,"dname":"开发部","db_source":"clouddb02"},{"deptno":2,"dname":"人事部","db_source":"clouddb02"},{"deptno":3,"dname":"财务部","db_source":"clouddb02"},{"deptno":4,"dname":"市场部","db_source":"clouddb02"},{"deptno":5,"dname":"运维部","db_source":"clouddb02"}]
```

```
[{"deptno":1,"dname":"开发部","db_source":"clouddb03"},{"deptno":2,"dname":"人事部","db_source":"clouddb03"},{"deptno":3,"dname":"财务部","db_source":"clouddb03"},{"deptno":4,"dname":"市场部","db_source":"clouddb03"},{"deptno":5,"dname":"运维部","db_source":"clouddb03"}]
```



# 4.自定义Ribbon(略过 自己找资料吧)



