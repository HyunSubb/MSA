package com.example.ordersystem.common.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    // eureka에 등록된 서비스명을 사용해서 내부서비스 호출(내부통신)
    // ex) "http://product-service" 이런 url를 우리 자체 유레카에서 먼저 찾는다.
    // 안 붙일 경우에는 인터넷상에서 product-service를 찾는다...
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
