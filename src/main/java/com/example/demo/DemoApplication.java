package com.example.demo;

import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.reactive.LoadBalancerProperties;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.cloud.loadbalancer.core.HealthCheckServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.netty.http.client.HttpClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SpringBootApplication
@EnableConfigurationProperties(value = ApplicationProperties.class)
@EnableCaching
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }


    @Configuration(proxyBeanMethods = false)
    @LoadBalancerClients(
            value = {
                    @LoadBalancerClient(name = "my-service", configuration = MyServiceInstanceListSuppler.class),
                    @LoadBalancerClient(name = "app-service", configuration = AppServiceInstanceListSupplier.class)
            }
    )
    static class CustomerServiceConfig {
    }


    static class CustomerServiceNameServiceInstance implements ServiceInstanceListSupplier {

        private final String serviceId;

        private final List<ApplicationProperties.Instance> instances;

        CustomerServiceNameServiceInstance(String serviceId, Map<String, List<ApplicationProperties.Instance>> instances) {
            Assert.hasText(serviceId, "ServiceId is Empty");
            this.serviceId = serviceId;
            this.instances = instances.get(serviceId);
        }

        @Override
        public String getServiceId() {
            return serviceId;
        }

        @Override
        public Flux<List<ServiceInstance>> get() {
            List<ServiceInstance> defaultServiceInstances = new ArrayList<>();
            for (int i = 0; i < this.instances.size(); i++) {
                ApplicationProperties.Instance instance = instances.get(i);
                DefaultServiceInstance defaultServiceInstance =
                        new DefaultServiceInstance(getServiceId() + i + 1, getServiceId(), instance.getHost(), instance.getPort(), instance.getSecure());
                defaultServiceInstances.add(defaultServiceInstance);
            }
            return Flux.just(defaultServiceInstances);
        }
    }

    @Bean
    WebClient healthWebClient(WebClient.Builder webClientBuilder) {
        HttpClient httpClient = HttpClient.create()
                .tcpConfiguration(client ->
                        client.doOnConnected(conn -> conn
                                .addHandlerLast(new ReadTimeoutHandler(5))
                                .addHandlerLast(new WriteTimeoutHandler(5))));
        return webClientBuilder.clientConnector(new ReactorClientHttpConnector(httpClient)).build();
    }


    @Configuration(proxyBeanMethods = false)
    static
    class AppServiceInstanceListSupplier {
        private final ApplicationProperties applicationProperties;

        AppServiceInstanceListSupplier(ApplicationProperties applicationProperties) {
            this.applicationProperties = applicationProperties;
        }

        @Bean
        public ServiceInstanceListSupplier appServiceInstanceListSupplier(
                LoadBalancerProperties loadBalancerProperties,
                ApplicationContext context, WebClient healthWebClient
        ) {
            CustomerServiceNameServiceInstance firstDelegate = new CustomerServiceNameServiceInstance("app-service", applicationProperties.getLoadBalancerClients());
            return new HealthCheckServiceInstanceListSupplier(firstDelegate,
                    loadBalancerProperties.getHealthCheck(), healthWebClient);
        }
    }

    @Configuration(proxyBeanMethods = false)
    static
    class MyServiceInstanceListSuppler {

        private final ApplicationProperties applicationProperties;

        public MyServiceInstanceListSuppler(ApplicationProperties applicationProperties) {
            this.applicationProperties = applicationProperties;
        }

        @Bean
        public ServiceInstanceListSupplier myServiceInstanceListSupplier(
                LoadBalancerProperties loadBalancerProperties,
                ApplicationContext context,
                WebClient healthWebClient) {
            CustomerServiceNameServiceInstance firstDelegate = new CustomerServiceNameServiceInstance("my-service", applicationProperties.getLoadBalancerClients());
            return new HealthCheckServiceInstanceListSupplier(firstDelegate,
                    loadBalancerProperties.getHealthCheck(), healthWebClient);
        }
    }
}
