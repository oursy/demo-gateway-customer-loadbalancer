## spring-cloud-gateway customer loadbalancer
> 简化的`Spring Cloud Gateway`负载均衡,没有使用服务发现组件。

`pom` 引入loadbalancer依赖
```xml
      <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-loadbalancer</artifactId>
        </dependency>
```
自定义服务实例配置
```yml
application:
  load-balancer-clients:
    my-service:
      - host: 127.0.0.1
        port: 9001
        secure: false
      - host: 127.0.0.1
        port: 9011
        secure: false
    app-service:
      - host: 127.0.0.1
        port: 9002
        secure: false
      - host: 127.0.0.1
        port: 9022
        secure: false
```
> 该配置定义了2个服务实例信息。分别为 my-service, app-service 

gateway 配置信息

```yml
spring:
  cloud:
    loadbalancer:
      ribbon:
        enabled: false
    gateway:
      routes:
        - id: my-service
          uri: lb://my-service
          predicates:
            - Path=/service/**
          filters:
            - RewritePath=/service(?<segment>/?.*), $\{segment}
        - id: app-service
          uri: lb://app-service
          predicates:
            - Path=/app/**
          filters:
            - RewritePath=/app(?<segment>/?.*), $\{segment}
```


Reference Document :
> https://spring.io/guides/gs/spring-cloud-loadbalancer/

> https://cloud.spring.io/spring-cloud-commons/reference/html/#spring-cloud-loadbalancer

> https://cloud.spring.io/spring-cloud-static/spring-cloud-gateway/2.2.0.RC2/reference/html/#reactive-loadbalancer-client-filter