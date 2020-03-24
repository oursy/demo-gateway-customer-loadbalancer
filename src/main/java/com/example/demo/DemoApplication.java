package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@SpringBootApplication
@EnableConfigurationProperties(value = ApplicationProperties.class)
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }


    @Configuration(proxyBeanMethods = false)
    @LoadBalancerClients(
            value = {
                    @LoadBalancerClient(name = "my-service", configuration = CustomerServiceConfig.CustomerServiceInstanceListSuppler.class),
                    @LoadBalancerClient(name = "app-service", configuration = CustomerServiceConfig.AppServiceInstanceListSupplier.class)
            }
    )
    static class CustomerServiceConfig {

        private final ApplicationProperties applicationProperties;

        CustomerServiceConfig(ApplicationProperties applicationProperties) {
            this.applicationProperties = applicationProperties;
        }

        class CustomerServiceInstanceListSuppler implements ServiceInstanceListSupplier {

            @Override
            public String getServiceId() {
                return "my-service";
            }

            @Override
            public Flux<List<ServiceInstance>> get() {
                List<ServiceInstance> defaultServiceInstances = applicationProperties.getLoadBalancerClients().get(getServiceId())
                        .stream()
                        .map(
                                s -> new DefaultServiceInstance(getServiceId() + UUID.randomUUID().toString(), getServiceId(), s.getHost(), s.getPort(), s.getSecure())
                        ).collect(Collectors.toList());
                return Flux.just(defaultServiceInstances);
            }
        }

        class AppServiceInstanceListSupplier implements ServiceInstanceListSupplier {

            @Override
            public String getServiceId() {
                return "app-service";
            }

            @Override
            public Flux<List<ServiceInstance>> get() {
                List<ServiceInstance> defaultServiceInstances = applicationProperties.getLoadBalancerClients().get(getServiceId())
                        .stream()
                        .map(
                                s -> new DefaultServiceInstance(getServiceId() + UUID.randomUUID().toString(), getServiceId(), s.getHost(), s.getPort(), s.getSecure())
                        ).collect(Collectors.toList());
                return Flux.just(defaultServiceInstances);
            }
        }
    }
}
