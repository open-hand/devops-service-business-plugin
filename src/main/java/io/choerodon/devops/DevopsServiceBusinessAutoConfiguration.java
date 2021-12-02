package io.choerodon.devops;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

import io.choerodon.resource.annoation.EnableChoerodonResourceServer;

@EnableFeignClients("io.choerodon")
@EnableEurekaClient
@EnableDiscoveryClient
@SpringBootApplication
@EnableChoerodonResourceServer
@EnableCircuitBreaker
@EnableAsync
public class DevopsServiceBusinessAutoConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsServiceBusinessAutoConfiguration.class);

    public static void main(String[] args) {
        try {
            SpringApplication.run(DevopsServiceBusinessAutoConfiguration.class, args);
        } catch (Exception e) {
            LOGGER.error("start error",e);
        }
    }

}
