package com.example.demo;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {

    private final Map<String, List<Instance>> loadBalancerClients = new HashMap<>();

    public Map<String, List<Instance>> getLoadBalancerClients() {
        return loadBalancerClients;
    }

    public static class Instance {
        private String host;
        private int port;
        private Boolean secure = false;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public Boolean getSecure() {
            return secure;
        }

        public void setSecure(Boolean secure) {
            this.secure = secure;
        }
    }


}
